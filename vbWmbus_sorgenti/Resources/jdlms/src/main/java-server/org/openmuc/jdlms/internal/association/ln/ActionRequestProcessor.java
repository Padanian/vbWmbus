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

import static org.openmuc.jdlms.internal.DataConverter.convertDataToDataObject;

import java.io.IOException;
import java.util.Iterator;

import org.openmuc.jasn1.ber.ReverseByteArrayOutputStream;
import org.openmuc.jdlms.IllegalMethodAccessException;
import org.openmuc.jdlms.MethodParameter;
import org.openmuc.jdlms.MethodResultCode;
import org.openmuc.jdlms.ObisCode;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.internal.APdu;
import org.openmuc.jdlms.internal.DataConverter;
import org.openmuc.jdlms.internal.MessageFragment;
import org.openmuc.jdlms.internal.PduHelper;
import org.openmuc.jdlms.internal.ServiceError;
import org.openmuc.jdlms.internal.StateError;
import org.openmuc.jdlms.internal.WellKnownInstanceIds;
import org.openmuc.jdlms.internal.asn1.axdr.AxdrType;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrBoolean;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrEnum;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrOctetString;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrOptional;
import org.openmuc.jdlms.internal.asn1.cosem.ACTIONRequest;
import org.openmuc.jdlms.internal.asn1.cosem.ACTIONResponse;
import org.openmuc.jdlms.internal.asn1.cosem.ActionRequestNextPblock;
import org.openmuc.jdlms.internal.asn1.cosem.ActionRequestNormal;
import org.openmuc.jdlms.internal.asn1.cosem.ActionRequestWithList;
import org.openmuc.jdlms.internal.asn1.cosem.ActionResponseNormal;
import org.openmuc.jdlms.internal.asn1.cosem.ActionResponseWithList;
import org.openmuc.jdlms.internal.asn1.cosem.ActionResponseWithOptionalData;
import org.openmuc.jdlms.internal.asn1.cosem.ActionResponseWithPblock;
import org.openmuc.jdlms.internal.asn1.cosem.COSEMpdu;
import org.openmuc.jdlms.internal.asn1.cosem.CosemMethodDescriptor;
import org.openmuc.jdlms.internal.asn1.cosem.Data;
import org.openmuc.jdlms.internal.asn1.cosem.DataBlockSA;
import org.openmuc.jdlms.internal.asn1.cosem.GetDataResult;
import org.openmuc.jdlms.internal.asn1.cosem.InvokeIdAndPriority;
import org.openmuc.jdlms.internal.asn1.cosem.Unsigned32;
import org.openmuc.jdlms.internal.association.AssociationException;
import org.openmuc.jdlms.internal.association.AssociationMessenger;
import org.openmuc.jdlms.internal.association.RequestProcessorBase;
import org.openmuc.jdlms.internal.association.RequestProcessorData;

public class ActionRequestProcessor extends RequestProcessorBase {

    private static final ObisCode ASSOCIATION_LN_INSTANCE_ID = new ObisCode(
            WellKnownInstanceIds.CURRENT_ASSOCIATION_ID);

    public ActionRequestProcessor(AssociationMessenger associationMessenger,
            RequestProcessorData requestProcessorData) {
        super(associationMessenger, requestProcessorData);
    }

    @Override
    public void processRequest(COSEMpdu request) throws IOException {
        ACTIONRequest actionRequest = request.actionRequest;

        switch (actionRequest.getChoiceIndex()) {
        case ACTION_REQUEST_NORMAL:
            processActionRequestNormal(actionRequest.actionRequestNormal);
            break;

        case ACTION_REQUEST_WITH_LIST:
            processActionRequestWithList(actionRequest.actionRequestWithList);
            break;

        default:
            throw new IOException("Not yet implemented");
        }
    }

    private ActionResponseWithOptionalData invokeMethod(CosemMethodDescriptor methodDescriptor, DataObject param)
            throws AssociationException {

        GetDataResult getDataResult = new GetDataResult();

        ObisCode instanceId = new ObisCode(methodDescriptor.instanceId.getValue());
        long classId = methodDescriptor.classId.getValue();
        long methodId = methodDescriptor.methodId.getValue();

        if (!this.requestProcessorData.connectionData.isAuthenticated()
                && !(ASSOCIATION_LN_INSTANCE_ID.equals(instanceId) && classId == 15 && methodId == 1)) {
            throw new AssociationException(StateError.SERVICE_NOT_ALLOWED, ServiceError.OPERATION_NOT_POSSIBLE);
        }

        DataObject result = null;
        MethodResultCode resultCode = MethodResultCode.SUCCESS;
        try {
            MethodParameter methodParameter = new MethodParameter((int) classId, instanceId, (int) methodId, param);

            result = this.requestProcessorData.getDirectory().invokeMethod(requestProcessorData.logicalDeviceId,
                    methodParameter, connectionId());
        } catch (IllegalMethodAccessException e) {
            resultCode = e.getMethodResultCode();
            getDataResult = null;
        }

        if (result != null) {
            getDataResult.setData(DataConverter.convertDataObjectToData(result));
        }
        else {
            getDataResult = null;
        }
        return new ActionResponseWithOptionalData(new AxdrEnum(resultCode.getCode()), getDataResult);
    }

    private void processActionRequestNormal(ActionRequestNormal normalRequest) throws IOException {
        InvokeIdAndPriority invokeIdAndPriority = normalRequest.invokeIdAndPriority;
        CosemMethodDescriptor cosemMethodDescriptor = normalRequest.cosemMethodDescriptor;
        AxdrOptional<Data> methodInvocationParameters = normalRequest.methodInvocationParameters;

        DataObject param;
        if (methodInvocationParameters.isUsed()) {
            param = convertDataToDataObject(methodInvocationParameters.getValue());
        }
        else {

            param = null;
        }
        ActionResponseWithOptionalData singleResult = invokeMethod(cosemMethodDescriptor, param);

        ACTIONResponse actionResponse = new ACTIONResponse();
        ActionResponseNormal normalResponse = new ActionResponseNormal(invokeIdAndPriority, singleResult);
        actionResponse.setActionResponseNormal(normalResponse);

        if (!associationMessenger.pduSizeTooLarge(actionResponse)) {
            sendActionResponse(actionResponse);

            return;
        }

        byte[] rawData = encodePduRawDataBlockData(singleResult);
        sendActionResponseAsFragments(invokeIdAndPriority, rawData);
    }

    private void sendActionResponseAsFragments(InvokeIdAndPriority invokeIdAndPriority, byte[] rawData)
            throws IOException {
        ACTIONResponse actionResponse = new ACTIONResponse();

        final int fragmentSize = (int) this.requestProcessorData.connectionData.getClientMaxReceivePduSize() - 10;
        MessageFragment messageFragment = new MessageFragment(rawData, fragmentSize);

        long blockNumber = 1;
        boolean lastBlock = false;
        byte[] octetString = messageFragment.next();

        DataBlockSA pblock = pBlockFrom(blockNumber, lastBlock, octetString);
        ActionResponseWithPblock responseWithPblock = new ActionResponseWithPblock(invokeIdAndPriority, pblock);
        actionResponse.setActionResponseWithPblock(responseWithPblock);
        sendActionResponse(actionResponse);

        while (messageFragment.hasNext()) {
            COSEMpdu cosemPdu = associationMessenger.readNextApdu().getCosemPdu();
            if (cosemPdu.getChoiceIndex() != COSEMpdu.Choices.ACTION_REQUEST) {
                // TODO error
                throw new IOException("wrong request type.");
            }
            ACTIONRequest actionRequest = cosemPdu.actionRequest;

            if (actionRequest.getChoiceIndex() != ACTIONRequest.Choices.ACTION_REQUEST_NEXT_PBLOCK) {
                // TODO error
                throw new IOException("Wrong action type.");
            }

            ActionRequestNextPblock requestNextPblock = actionRequest.actionRequestNextPblock;
            InvokeIdAndPriority invokeIdAndPriorityRpl = requestNextPblock.invokeIdAndPriority;
            if (PduHelper.invokeIdFrom(invokeIdAndPriorityRpl) != PduHelper.invokeIdFrom(invokeIdAndPriority)) {
                throw new IOException("Wrong invoke id");
            }

            if (blockNumber++ != requestNextPblock.blockNumber.getValue()) {
                // TODO: error
                throw new IOException("Wrong pblock confimation.");
            }

            pblock = pBlockFrom(blockNumber, !messageFragment.hasNext(), messageFragment.next());
            responseWithPblock = new ActionResponseWithPblock(invokeIdAndPriority, pblock);

            actionResponse.setActionResponseWithPblock(responseWithPblock);
            sendActionResponse(actionResponse);
        }
    }

    private void processActionRequestWithList(ActionRequestWithList requestWithList) throws IOException {
        InvokeIdAndPriority invokeIdAndPriority = requestWithList.invokeIdAndPriority;

        Iterator<Data> invocationParametersIter = requestWithList.methodInvocationParameters.list().iterator();
        Iterator<CosemMethodDescriptor> methodDescriptorIter = requestWithList.cosemMethodDescriptorList.list()
                .iterator();

        ActionResponseWithList.SubSeqOfListOfResponses listOfResponses = new ActionResponseWithList.SubSeqOfListOfResponses();
        while (invocationParametersIter.hasNext() && methodDescriptorIter.hasNext()) {
            Data invokationParameter = invocationParametersIter.next();
            CosemMethodDescriptor methodDescriptor = methodDescriptorIter.next();

            DataObject param = convertDataToDataObject(invokationParameter);
            if (param.isNull()) {
                param = null;
            }

            ActionResponseWithOptionalData actionResult = invokeMethod(methodDescriptor, param);
            listOfResponses.add(actionResult);
        }

        ActionResponseWithList responseWithList = new ActionResponseWithList(invokeIdAndPriority, listOfResponses);

        ACTIONResponse actionResponse = new ACTIONResponse();

        actionResponse.setActionResponseWithList(responseWithList);
        if (!associationMessenger.pduSizeTooLarge(actionResponse)) {
            sendActionResponse(actionResponse);

            return;
        }

        byte[] rawData = encodePduRawDataBlockData(listOfResponses);

        sendActionResponseAsFragments(invokeIdAndPriority, rawData);
    }

    private static byte[] encodePduRawDataBlockData(AxdrType axdrType) throws IOException {
        // TODO set correct buffer size
        ReverseByteArrayOutputStream axdrOStream = new ReverseByteArrayOutputStream(0xffff);
        axdrType.encode(axdrOStream);
        return axdrOStream.getArray();
    }

    private static DataBlockSA pBlockFrom(long blockNumber, boolean lastBlock, byte[] octetString) {
        AxdrBoolean lastBlockAxdr = new AxdrBoolean(lastBlock);
        Unsigned32 blockNumberU32 = new Unsigned32(blockNumber);
        AxdrOctetString rawData = new AxdrOctetString(octetString);

        return new DataBlockSA(lastBlockAxdr, blockNumberU32, rawData);
    }

    private void sendActionResponse(ACTIONResponse actionResponse) throws IOException {
        COSEMpdu cosemPdu = new COSEMpdu();
        cosemPdu.setActionResponse(actionResponse);

        APdu aPdu = new APdu(null, cosemPdu);

        associationMessenger.encodeAndSend(aPdu);
    }
}
