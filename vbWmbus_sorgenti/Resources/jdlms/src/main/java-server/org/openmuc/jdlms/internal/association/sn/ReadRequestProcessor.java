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
package org.openmuc.jdlms.internal.association.sn;

import static org.openmuc.jdlms.internal.DlmsEnumFunctions.enumToAxdrEnum;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import org.openmuc.jdlms.IllegalAttributeAccessException;
import org.openmuc.jdlms.IllegalMethodAccessException;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.internal.APdu;
import org.openmuc.jdlms.internal.DataConverter;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrBoolean;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrOctetString;
import org.openmuc.jdlms.internal.asn1.cosem.COSEMpdu;
import org.openmuc.jdlms.internal.asn1.cosem.DataBlockResult;
import org.openmuc.jdlms.internal.asn1.cosem.ParameterizedAccess;
import org.openmuc.jdlms.internal.asn1.cosem.ReadRequest;
import org.openmuc.jdlms.internal.asn1.cosem.ReadResponse;
import org.openmuc.jdlms.internal.asn1.cosem.ReadResponse.SubChoice;
import org.openmuc.jdlms.internal.asn1.cosem.Unsigned16;
import org.openmuc.jdlms.internal.asn1.cosem.VariableAccessSpecification;
import org.openmuc.jdlms.internal.association.AssociationMessenger;
import org.openmuc.jdlms.internal.association.RequestProcessorData;

public class ReadRequestProcessor extends SnRequestProcessorBase {

    public ReadRequestProcessor(AssociationMessenger associationMessenger, RequestProcessorData requestProcessorData) {
        super(associationMessenger, requestProcessorData);
    }

    @Override
    public void processRequest(COSEMpdu request) throws IOException {

        ReadRequest readReq = request.readRequest;
        List<VariableAccessSpecification> list = readReq.list();

        ReadResponse readRes = new ReadResponse();
        for (VariableAccessSpecification varAccessSpec : list) {
            switch (varAccessSpec.getChoiceIndex()) {
            case VARIABLE_NAME:
                readRes.add(varNameAccess(varAccessSpec));
                break;
            case PARAMETERIZED_ACCESS:
                readRes.add(varParamAccess(varAccessSpec));
                break;

            case READ_DATA_BLOCK_ACCESS:
                // list of method access
                break;

            case BLOCK_NUMBER_ACCESS:
            case WRITE_DATA_BLOCK_ACCESS:
            case _ERR_NONE_SELECTED:
            default:
                // TODO handle this case properly
                // illegal at this point
                break;

            }

        }

        APdu aPdu = newAPdu();

        aPdu.getCosemPdu().setReadResponse(readRes);

        byte[] encodedApdu = this.associationMessenger.encode(aPdu);

        if (!this.associationMessenger.apduTooLarge(encodedApdu.length)) {
            this.associationMessenger.send(encodedApdu);
        }
        else {
            sendResposneAsBlocks(readRes, encodedApdu);

        }

    }

    private void sendResposneAsBlocks(ReadResponse readRes, byte[] encodedApdu) throws IOException {
        readRes.encodeAndSave(encodedApdu.length);
        byte[] rawDataToSend = readRes.getCode();

        final int expectedApduOverhead = encodedApdu.length - rawDataToSend.length;

        ByteBuffer rawDataBuffer = ByteBuffer.wrap(rawDataToSend);

        long blockNumber = 1;

        final int blockSize = this.associationMessenger.getMaxMessageLength() - expectedApduOverhead;

        byte[] blockVal = new byte[blockSize];

        boolean lastBlock;
        do {
            rawDataBuffer.get(blockVal);
            lastBlock = !rawDataBuffer.hasRemaining();

            ReadResponse blockResponse = new ReadResponse();
            SubChoice block = new SubChoice();
            DataBlockResult blockData = new DataBlockResult(new AxdrBoolean(lastBlock), new Unsigned16(blockNumber++),
                    new AxdrOctetString(blockVal));
            block.setDataBlockResult(blockData);
            blockResponse.add(block);

            APdu blockApdu = newAPdu();
            blockApdu.getCosemPdu().setReadResponse(blockResponse);

            this.associationMessenger.encodeAndSend(blockApdu);

            if (rawDataBuffer.remaining() < blockVal.length) {
                if (lastBlock) {
                    break;
                }

                blockVal = new byte[rawDataBuffer.remaining()];
            }

            veifyBlockNumberAccessResponse(blockNumber);
        } while (!lastBlock);
    }

    private void veifyBlockNumberAccessResponse(long blockNumber) throws IOException {
        COSEMpdu cosemPdu = this.associationMessenger.readNextApdu().getCosemPdu();
        if (cosemPdu == null || cosemPdu.getChoiceIndex() != COSEMpdu.Choices.READREQUEST) {
            // error
            throw new IOException("Wrong COSEM PDU: " + cosemPdu);
        }

        ReadRequest readRequest = cosemPdu.readRequest;

        if (readRequest.size() != 1) {
            // error
        }

        VariableAccessSpecification blockNumSpec = readRequest.get(0);
        if (blockNumSpec.getChoiceIndex() != VariableAccessSpecification.Choices.BLOCK_NUMBER_ACCESS) {
            // error
        }

        long blockNumRes = blockNumSpec.blockNumberAccess.blockNumber.getValue();

        if (blockNumRes != blockNumber) {
            // error
        }
    }

    private SubChoice varParamAccess(VariableAccessSpecification varAccessSpec) {
        ParameterizedAccess parameterizedAccess = varAccessSpec.parameterizedAccess;
        final int variableName = (int) parameterizedAccess.variableName.getValue() & 0xFFFF;

        return access(variableName, parameterizedAccess);

    }

    private SubChoice access(final int variableName, ParameterizedAccess parameterizedAccess) {
        SubChoice res = new SubChoice();

        try {
            DataObject resDo = this.requestProcessorData.getDirectory().snGetOrInvoke(logicalDeviceId(), variableName,
                    parameterizedAccess, connectionId());
            res.setData(DataConverter.convertDataObjectToData(resDo));
        } catch (IllegalAttributeAccessException e) {
            res.setDataAccessError(enumToAxdrEnum(e.getAccessResultCode()));
        } catch (IllegalMethodAccessException e) {
            res.setDataAccessError(enumToAxdrEnum(e.getMethodResultCode()));
        }

        return res;
    }

    private ReadResponse.SubChoice varNameAccess(VariableAccessSpecification varAccessSpec) {
        final int variableName = (int) varAccessSpec.variableName.getValue() & 0xFFFF;
        ParameterizedAccess parameterizedAccess = null;

        return access(variableName, parameterizedAccess);
    }

}
