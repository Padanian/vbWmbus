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
package org.openmuc.jdlms.internal.association.ln;

import static org.openmuc.jdlms.internal.DataConverter.convertDataObjectToData;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

import org.openmuc.jasn1.ber.ReverseByteArrayOutputStream;
import org.openmuc.jdlms.AttributeAddress;
import org.openmuc.jdlms.IllegalAttributeAccessException;
import org.openmuc.jdlms.ObisCode;
import org.openmuc.jdlms.SelectiveAccessDescription;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.internal.APdu;
import org.openmuc.jdlms.internal.DataConverter;
import org.openmuc.jdlms.internal.asn1.axdr.AxdrType;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrBoolean;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrEnum;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrOctetString;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrOptional;
import org.openmuc.jdlms.internal.asn1.cosem.COSEMpdu;
import org.openmuc.jdlms.internal.asn1.cosem.CosemAttributeDescriptor;
import org.openmuc.jdlms.internal.asn1.cosem.CosemAttributeDescriptorWithSelection;
import org.openmuc.jdlms.internal.asn1.cosem.Data;
import org.openmuc.jdlms.internal.asn1.cosem.DataBlockG;
import org.openmuc.jdlms.internal.asn1.cosem.DataBlockG.SubChoiceResult;
import org.openmuc.jdlms.internal.asn1.cosem.GETRequest;
import org.openmuc.jdlms.internal.asn1.cosem.GETResponse;
import org.openmuc.jdlms.internal.asn1.cosem.GetDataResult;
import org.openmuc.jdlms.internal.asn1.cosem.GetRequestNext;
import org.openmuc.jdlms.internal.asn1.cosem.GetRequestNormal;
import org.openmuc.jdlms.internal.asn1.cosem.GetRequestWithList;
import org.openmuc.jdlms.internal.asn1.cosem.GetResponseNormal;
import org.openmuc.jdlms.internal.asn1.cosem.GetResponseWithDatablock;
import org.openmuc.jdlms.internal.asn1.cosem.GetResponseWithList;
import org.openmuc.jdlms.internal.asn1.cosem.GetResponseWithList.SubSeqOfResult;
import org.openmuc.jdlms.internal.asn1.cosem.InvokeIdAndPriority;
import org.openmuc.jdlms.internal.asn1.cosem.SelectiveAccessDescriptor;
import org.openmuc.jdlms.internal.asn1.cosem.Unsigned32;
import org.openmuc.jdlms.internal.asn1.iso.acse.ACSEApdu;
import org.openmuc.jdlms.internal.association.AssociationMessenger;
import org.openmuc.jdlms.internal.association.RequestProcessorBase;
import org.openmuc.jdlms.internal.association.RequestProcessorData;

public class GetRequestProcessor extends RequestProcessorBase {

    /**
     * Overhead of the APdu is ~ 35 bytes.
     */
    private static final int OVERHEAD = 35;

    public GetRequestProcessor(AssociationMessenger associationMessenger, RequestProcessorData requestProcessorData) {
        super(associationMessenger, requestProcessorData);
    }

    @Override
    public void processRequest(COSEMpdu request) throws IOException {
        final int clientMaxReceivePduSize = this.associationMessenger.getMaxMessageLength();

        GETRequest getRequest = request.getRequest;
        GETResponse getResponse;

        InvokeIdAndPriority invokeIdPrio = null;

        byte[] bytesToSend = null;
        switch (getRequest.getChoiceIndex()) {
        case GET_REQUEST_NORMAL:
            invokeIdPrio = getRequest.getRequestNormal.invokeIdAndPriority;
            getResponse = processGetRequestNormal(getRequest.getRequestNormal, invokeIdPrio);

            bytesToSend = encodeData(getResponse);

            encodeAndSend(clientMaxReceivePduSize, getResponse, invokeIdPrio, bytesToSend,
                    getResponse.getResponseNormal.result.data);
            break;

        case GET_REQUEST_WITH_LIST:
            invokeIdPrio = getRequest.getRequestWithList.invokeIdAndPriority;
            getResponse = processGetRequestWithList(getRequest.getRequestWithList, invokeIdPrio);

            bytesToSend = encodeData(getResponse);

            encodeAndSend(clientMaxReceivePduSize, getResponse, invokeIdPrio, bytesToSend,
                    getResponse.getResponseWithList.result);
            break;

        case GET_REQUEST_NEXT:
        case _ERR_NONE_SELECTED:
        default:
            // should not occur
            // TODO answer with illegal request response
            throw new IOException();
        }

    }

    private void encodeAndSend(final long clientMaxReceivePduSize, GETResponse getResponse,
            InvokeIdAndPriority invokeIdPrio, byte[] bytesToSend, AxdrType axdrData) throws IOException {
        if (clientMaxReceivePduSize != 0 && bytesToSend.length > clientMaxReceivePduSize) {
            sendAsDataBlocks(clientMaxReceivePduSize, getResponse, invokeIdPrio, axdrData, bytesToSend.length);
        }
        else {
            this.associationMessenger.send(bytesToSend);
        }
    }

    private void sendAsDataBlocks(final long clientMaxReceivePduSize, GETResponse getResponse,
            InvokeIdAndPriority invokeIdPrio, AxdrType data, int size) throws IOException {
        ReverseByteArrayOutputStream axdrOStream = new ReverseByteArrayOutputStream(size);
        data.encode(axdrOStream);
        byte[] completeBlock = axdrOStream.getArray();

        final int blockSize = (int) clientMaxReceivePduSize - OVERHEAD;
        byte[] block = new byte[blockSize];
        long blockNumber = 1L;

        try (DataInputStream ds = new DataInputStream(new ByteArrayInputStream(completeBlock))) {
            while (true) {
                if (blockSize > ds.available()) {
                    block = new byte[ds.available()];
                }
                ds.readFully(block);

                boolean lastBlock = ds.available() == 0;

                SubChoiceResult result = new SubChoiceResult();
                result.setRawData(new AxdrOctetString(block));
                DataBlockG dataBlockG = new DataBlockG(new AxdrBoolean(lastBlock), new Unsigned32(blockNumber++),
                        result);
                GetResponseWithDatablock gdata = new GetResponseWithDatablock(invokeIdPrio, dataBlockG);
                getResponse.setGetResponseWithDatablock(gdata);

                ACSEApdu acseAPdu = null;
                COSEMpdu coseMpdu = new COSEMpdu();
                coseMpdu.setGetResponse(getResponse);
                APdu blockApdu = new APdu(acseAPdu, coseMpdu);

                this.associationMessenger.encodeAndSend(blockApdu);

                if (lastBlock) {
                    break;
                }

                APdu nextApdu = this.associationMessenger.readNextApdu();

                if (!isGetNext(nextApdu)) {
                    // TODO error
                    throw new IllegalStateException();
                }

                GetRequestNext nextGetRequest = nextApdu.getCosemPdu().getRequest.getRequestNext;
                boolean resposneNumEquals = nextGetRequest.blockNumber.getValue() == blockNumber - 1;
                if (!resposneNumEquals) {
                    // TODO handle this case
                    throw new IllegalStateException();
                }

                invokeIdPrio = nextGetRequest.invokeIdAndPriority;
            }
        }

    }

    private byte[] encodeData(GETResponse getResponse) throws IOException {
        COSEMpdu coseMpdu = new COSEMpdu();
        coseMpdu.setGetResponse(getResponse);

        APdu aPdu = new APdu(null, coseMpdu);

        return this.associationMessenger.encode(aPdu);
    }

    private static boolean isGetNext(APdu nextApdu) {
        return nextApdu.getCosemPdu().getChoiceIndex() == COSEMpdu.Choices.GET_REQUEST
                && nextApdu.getCosemPdu().getRequest.getChoiceIndex() == GETRequest.Choices.GET_REQUEST_NEXT;
    }

    private GETResponse processGetRequestWithList(GetRequestWithList requestWithList,
            InvokeIdAndPriority invokeIdPrio) {

        List<CosemAttributeDescriptorWithSelection> list = requestWithList.attributeDescriptorList.list();

        SubSeqOfResult result = new SubSeqOfResult();
        for (CosemAttributeDescriptorWithSelection attributeDescriptor : list) {

            GetDataResult element = tryGet(attributeDescriptor.cosemAttributeDescriptor,
                    attributeDescriptor.accessSelection);
            result.add(element);
        }

        GETResponse getResponse = new GETResponse();
        GetResponseWithList responseWithList = new GetResponseWithList(invokeIdPrio, result);
        getResponse.setGetResponseWithList(responseWithList);

        return getResponse;
    }

    private GETResponse processGetRequestNormal(GetRequestNormal normalRequest,
            InvokeIdAndPriority invokeIdAndPriority) {
        GetDataResult result = tryGet(normalRequest.cosemAttributeDescriptor, normalRequest.accessSelection);
        GETResponse getResponse = new GETResponse();
        getResponse.setGetResponseNormal(new GetResponseNormal(invokeIdAndPriority, result));

        return getResponse;
    }

    private GetDataResult tryGet(CosemAttributeDescriptor cosemAttributeDescriptor,
            AxdrOptional<SelectiveAccessDescriptor> accessSelection) {
        GetDataResult result = new GetDataResult();
        try {
            ObisCode instanceId = new ObisCode(cosemAttributeDescriptor.instanceId.getValue());

            SelectiveAccessDescription selectiveAccessDescription = null;
            if (accessSelection.isUsed()) {
                SelectiveAccessDescriptor accessDescriptor = accessSelection.getValue();

                int accessSelector = (int) accessDescriptor.accessSelector.getValue();
                DataObject accessParameter = DataConverter.convertDataToDataObject(accessDescriptor.accessParameters);
                selectiveAccessDescription = new SelectiveAccessDescription(accessSelector, accessParameter);
            }

            int logicalDeviceId = requestProcessorData.logicalDeviceId;

            long classId = cosemAttributeDescriptor.classId.getValue();
            long attributeId = cosemAttributeDescriptor.attributeId.getValue();

            AttributeAddress attributeAddress = new AttributeAddress((int) classId, instanceId, (int) attributeId,
                    selectiveAccessDescription);

            DataObject attributeData = this.requestProcessorData.getDirectory().get(logicalDeviceId, attributeAddress,
                    connectionId());

            Data convertedData = convertDataObjectToData(attributeData);
            result.setData(convertedData);
        } catch (IllegalAttributeAccessException e) {
            result.setDataAccessResult(new AxdrEnum(e.getAccessResultCode().getCode()));
        }
        return result;
    }

}
