/**
 * Copyright 2012-18 Fraunhofer ISE
 *
 * This file is part of jDLMS.
 * For more information visit http://www.openmuc.org
 *
 * jDLMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jDLMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jDLMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.openmuc.jdlms;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.reflect.Whitebox.invokeMethod;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openmuc.jdlms.RawMessageData.RawMessageDataBuilder;
import org.openmuc.jdlms.internal.APdu;
import org.openmuc.jdlms.internal.PduHelper;
import org.openmuc.jdlms.internal.ServerConnectionData;
import org.openmuc.jdlms.internal.asn1.cosem.ACTIONRequest;
import org.openmuc.jdlms.internal.asn1.cosem.ACTIONResponse;
import org.openmuc.jdlms.internal.asn1.cosem.ActionRequestNextPblock;
import org.openmuc.jdlms.internal.asn1.cosem.ActionResponseWithPblock;
import org.openmuc.jdlms.internal.asn1.cosem.COSEMpdu;
import org.openmuc.jdlms.internal.asn1.cosem.DataBlockSA;
import org.openmuc.jdlms.internal.asn1.cosem.InvokeIdAndPriority;
import org.openmuc.jdlms.internal.asn1.cosem.Unsigned32;
import org.openmuc.jdlms.internal.association.AssociationMessenger;
import org.openmuc.jdlms.internal.association.RequestProcessorData;
import org.openmuc.jdlms.internal.association.ln.ActionRequestProcessor;
import org.openmuc.jdlms.sessionlayer.server.ServerSessionLayer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ActionRequestProcessor.class)
public class AssociationActionFragmentTest {

    private static final String SEND_ACTION_RESPONSE_AS_FRAGMENTS_METHOD_NAME = "sendActionResponseAsFragments";
    static ByteArrayOutputStream byteAOS;

    @BeforeClass()
    public static void setUp() {
        byteAOS = new ByteArrayOutputStream();
    }

    @BeforeClass()
    public static void shutdown() throws IOException {
        byteAOS.close();
    }

    @Test()
    public void test1() throws Exception {

        final InvokeIdAndPriority invokeIdAndPriorityFinal = new InvokeIdAndPriority(new byte[] { (byte) 0xF & 2 });

        ServerSessionLayer sessionLayer = PowerMockito.mock(ServerSessionLayer.class);
        final LinkedList<byte[]> dataFifo = new LinkedList<>();
        when(sessionLayer.readNextMessage()).thenAnswer(new Answer<byte[]>() {
            @Override
            public byte[] answer(InvocationOnMock invocation) throws Throwable {
                assertFalse("Data is empty.", dataFifo.isEmpty());
                return dataFifo.removeFirst();
            }
        });

        doAnswer(new Answer<Void>() {
            long blockCounter = 1;

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {

                byte[] rdata = invocation.getArgumentAt(0, byte[].class);

                APdu apPdu = APdu.decode(rdata, RawMessageData.builder());

                COSEMpdu cosemPdu = apPdu.getCosemPdu();

                assertEquals("Didn't reply with action response.", COSEMpdu.Choices.ACTION_RESPONSE,
                        cosemPdu.getChoiceIndex());

                ACTIONResponse actionResponse = cosemPdu.actionResponse;

                assertEquals("Didn't reply with action response pBlock.",
                        ACTIONResponse.Choices.ACTION_RESPONSE_WITH_PBLOCK, actionResponse.getChoiceIndex());

                ActionResponseWithPblock withPblock = actionResponse.actionResponseWithPblock;
                InvokeIdAndPriority invokeIdAndPriority = withPblock.invokeIdAndPriority;

                assertEquals(PduHelper.invokeIdFrom(invokeIdAndPriorityFinal),
                        PduHelper.invokeIdFrom(invokeIdAndPriority));
                DataBlockSA pblock = withPblock.pblock;

                assertEquals("Block Number's are not equal.", blockCounter++, pblock.blockNumber.getValue());

                if (pblock.lastBlock.getValue()) {
                    return null;
                }

                byte[] rawData = pblock.rawData.getValue();
                byteAOS.write(rawData);

                COSEMpdu retCosemPdu = new COSEMpdu();
                ACTIONRequest actionRequest = new ACTIONRequest();
                ActionRequestNextPblock nextPblock = new ActionRequestNextPblock(invokeIdAndPriorityFinal,
                        new Unsigned32(pblock.blockNumber.getValue()));
                actionRequest.setActionRequestNextPblock(nextPblock);
                retCosemPdu.setActionRequest(actionRequest);
                APdu retAPdu = new APdu(null, retCosemPdu);

                byte[] buffer = new byte[0xFFFF];
                int retLength = retAPdu.encode(buffer, PowerMockito.mock(RawMessageDataBuilder.class));
                dataFifo.addLast(Arrays.copyOfRange(buffer, buffer.length - retLength, buffer.length));
                return null;
            }
        }).when(sessionLayer).send(Matchers.any(byte[].class));

        ServerConnectionData connectionData = new ServerConnectionData(sessionLayer, 0L);
        connectionData.setClientMaxReceivePduSize(15);
        connectionData.setSecuritySuite(SecuritySuite.builder().build());

        AssociationMessenger associationMessenger = new AssociationMessenger(connectionData, null);
        RequestProcessorData requestProcessorData = mock(RequestProcessorData.class);

        Whitebox.setInternalState(requestProcessorData, connectionData);

        ActionRequestProcessor actionRequestProcessor = spy(
                new ActionRequestProcessor(associationMessenger, requestProcessorData));

        // raw random Data

        final byte[] data = new byte[400];
        Random random = new Random();
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (random.nextInt() & 0xFF);
        }

        invokeMethod(actionRequestProcessor, SEND_ACTION_RESPONSE_AS_FRAGMENTS_METHOD_NAME, invokeIdAndPriorityFinal,
                data);
        assertArrayEquals("Server did not build die data correctly", data, byteAOS.toByteArray());

    }

}
