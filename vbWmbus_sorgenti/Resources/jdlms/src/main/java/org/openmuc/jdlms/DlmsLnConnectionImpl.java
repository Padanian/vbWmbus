/*
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
 *
 */
package org.openmuc.jdlms;

import static java.util.Collections.emptyList;
import static org.openmuc.jdlms.ConformanceSetting.ACTION;
import static org.openmuc.jdlms.ConformanceSetting.ATTRIBUTE0_SUPPORTED_WITH_GET;
import static org.openmuc.jdlms.ConformanceSetting.ATTRIBUTE0_SUPPORTED_WITH_SET;
import static org.openmuc.jdlms.ConformanceSetting.BLOCK_TRANSFER_WITH_ACTION;
import static org.openmuc.jdlms.ConformanceSetting.BLOCK_TRANSFER_WITH_GET_OR_READ;
import static org.openmuc.jdlms.ConformanceSetting.BLOCK_TRANSFER_WITH_SET_OR_WRITE;
import static org.openmuc.jdlms.ConformanceSetting.GET;
import static org.openmuc.jdlms.ConformanceSetting.MULTIPLE_REFERENCES;
import static org.openmuc.jdlms.ConformanceSetting.PRIORITY_MGMT_SUPPORTED;
import static org.openmuc.jdlms.ConformanceSetting.SELECTIVE_ACCESS;
import static org.openmuc.jdlms.ConformanceSetting.SET;
import static org.openmuc.jdlms.internal.DataConverter.convertDataObjectToData;
import static org.openmuc.jdlms.internal.DlmsEnumFunctions.enumValueFrom;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.openmuc.jdlms.JDlmsException.ExceptionId;
import org.openmuc.jdlms.JDlmsException.Fault;
import org.openmuc.jdlms.SecuritySuite.EncryptionMechanism;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.internal.ContextId;
import org.openmuc.jdlms.internal.DataConverter;
import org.openmuc.jdlms.internal.WellKnownInstanceIds;
import org.openmuc.jdlms.internal.asn1.axdr.AxdrType;
import org.openmuc.jdlms.internal.asn1.axdr.NullOutputStream;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrEnum;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrOptional;
import org.openmuc.jdlms.internal.asn1.cosem.ACTIONRequest;
import org.openmuc.jdlms.internal.asn1.cosem.ACTIONResponse;
import org.openmuc.jdlms.internal.asn1.cosem.ActionRequestNextPblock;
import org.openmuc.jdlms.internal.asn1.cosem.ActionRequestNormal;
import org.openmuc.jdlms.internal.asn1.cosem.ActionRequestWithList;
import org.openmuc.jdlms.internal.asn1.cosem.ActionResponseWithList.SubSeqOfListOfResponses;
import org.openmuc.jdlms.internal.asn1.cosem.ActionResponseWithOptionalData;
import org.openmuc.jdlms.internal.asn1.cosem.COSEMpdu;
import org.openmuc.jdlms.internal.asn1.cosem.CosemAttributeDescriptor;
import org.openmuc.jdlms.internal.asn1.cosem.CosemAttributeDescriptorWithSelection;
import org.openmuc.jdlms.internal.asn1.cosem.Data;
import org.openmuc.jdlms.internal.asn1.cosem.GETRequest;
import org.openmuc.jdlms.internal.asn1.cosem.GETResponse;
import org.openmuc.jdlms.internal.asn1.cosem.GetDataResult;
import org.openmuc.jdlms.internal.asn1.cosem.GetRequestNext;
import org.openmuc.jdlms.internal.asn1.cosem.GetRequestNormal;
import org.openmuc.jdlms.internal.asn1.cosem.GetRequestWithList;
import org.openmuc.jdlms.internal.asn1.cosem.GetResponseWithList.SubSeqOfResult;
import org.openmuc.jdlms.internal.asn1.cosem.InvokeIdAndPriority;
import org.openmuc.jdlms.internal.asn1.cosem.SETRequest;
import org.openmuc.jdlms.internal.asn1.cosem.SETResponse;
import org.openmuc.jdlms.internal.asn1.cosem.SelectiveAccessDescriptor;
import org.openmuc.jdlms.internal.asn1.cosem.SetRequestNormal;
import org.openmuc.jdlms.internal.asn1.cosem.SetRequestWithList;
import org.openmuc.jdlms.internal.asn1.cosem.Unsigned8;
import org.openmuc.jdlms.sessionlayer.client.SessionLayer;
import org.openmuc.jdlms.settings.client.Settings;

class DlmsLnConnectionImpl extends BaseDlmsConnection {

    DlmsLnConnectionImpl(Settings settings, SessionLayer sessionlayer) throws IOException {
        super(settings, sessionlayer);
    }

    public synchronized List<GetResult> get(boolean priority, List<AttributeAddress> params) throws IOException {
        if (params.isEmpty()) {
            return emptyList();
        }

        InvokeIdAndPriority id = invokeIdAndPriorityFor(priority);
        COSEMpdu pdu = createGetPdu(id, params);

        int pduSize = pduSizeOf(pdu);
        if (maxSendPduSize() != 0 && pduSize > maxSendPduSize()) {

            if (params.size() > 1) {
                return callEachGetIndividually(priority, params);
            }
            else {
                // IEC 62056-5-3 2013, Section 6.6 The GET service, Page 52:
                // A GET.request service primitive shall always fit in a single APDU
                throw new NonFatalJDlmsException(ExceptionId.GET_REQUEST_TOO_LARGE, Fault.USER,
                        MessageFormat.format(
                                "PDU ({0} byte) is too long for single GET.request. Max send PDU size is {1} byte.",
                                pduSize, maxSendPduSize()));
            }
        }

        GETResponse response = send(pdu);

        switch (response.getChoiceIndex()) {
        case GET_RESPONSE_NORMAL:
            return Arrays.asList(convertPduToGetResult(response.getResponseNormal.result));
        case GET_RESPONSE_WITH_DATABLOCK:
            return readDataBlockG(response, params);
        case GET_RESPONSE_WITH_LIST:
            return convertListToDataObject(response.getResponseWithList.result.list());
        default:
            String msg = String.format(
                    "Unknown response type with Choice Index %s. Please report to developer of the stack.",
                    response.getChoiceIndex());
            throw new IllegalStateException(msg);
        }
    }

    private List<GetResult> callEachGetIndividually(boolean priority, List<AttributeAddress> params)
            throws IOException {
        List<GetResult> res = new ArrayList<>(params.size());
        for (AttributeAddress param : params) {
            res.add(get(priority, Arrays.asList(param)).get(0));
        }
        return res;
    }

    private List<GetResult> readDataBlockG(GETResponse response, List<AttributeAddress> params) throws IOException {
        byte[] byteArray = readBlocksGet(response);
        InputStream dataByteStream = new ByteArrayInputStream(byteArray);

        if (params.size() == 1) {
            Data resultPduData = new Data();
            resultPduData.decode(dataByteStream);

            GetDataResult getResult = new GetDataResult();
            getResult.setData(resultPduData);

            return Arrays.asList(convertPduToGetResult(getResult));
        }
        else {
            SubSeqOfResult subSeqOfResult = new SubSeqOfResult();
            subSeqOfResult.decode(dataByteStream);
            return convertListToDataObject(subSeqOfResult.list());
        }
    }

    private byte[] readBlocksGet(GETResponse response) throws IOException {
        final InvokeIdAndPriority invokeIdAndPriority = response.getResponseWithDatablock.invokeIdAndPriority;

        ByteArrayOutputStream datablocks = new ByteArrayOutputStream();
        GETRequest getRequest = new GETRequest();
        COSEMpdu pdu = new COSEMpdu();

        GetRequestNext nextBlock = new GetRequestNext();
        GETResponse newRes = response;
        while (!newRes.getResponseWithDatablock.result.lastBlock.getValue()) {
            datablocks.write(newRes.getResponseWithDatablock.result.result.rawData.getValue());

            nextBlock.blockNumber = newRes.getResponseWithDatablock.result.blockNumber;
            nextBlock.invokeIdAndPriority = invokeIdAndPriority;

            getRequest.setGetRequestNext(nextBlock);
            pdu.setGetRequest(getRequest);

            try {
                newRes = send(pdu);
            } catch (ResponseTimeoutException e) {
                // Send PDU with wrong block number to indicate the
                // device that the block transfer is
                // aborted.
                // This is the well defined behavior to abort a block
                // transfer as in IEC 62056-53 section
                // 7.4.1.8.2
                // receiveTimedOut(pdu)
                send(pdu);

                throw e;
            }
        }
        // if (response.getChoiceIndex().equals(Choices.GET_RESPONSE_NORMAL)) {
        // throw new IOException("Meter response with error, access result code: "
        // + response.get_response_normal.result.data_access_result);
        // }
        // if (response.get_response_with_datablock.result.result.raw_data == null) {
        // AccessResultCode accessResultCode = AccessResultCode
        // .forValue(response.get_response_with_datablock.result.result.data_access_result.getValue());
        // }

        datablocks.write(newRes.getResponseWithDatablock.result.result.rawData.getValue());

        return datablocks.toByteArray();
    }

    public synchronized List<AccessResultCode> set(boolean priority, List<SetParameter> params) throws IOException {
        if (params.isEmpty()) {
            return emptyList();
        }

        InvokeIdAndPriority invokeIdAndPriority = invokeIdAndPriorityFor(priority);
        SETResponse response = createAndSendSetPdu(invokeIdAndPriority, params);

        switch (response.getChoiceIndex()) {
        case SET_RESPONSE_NORMAL:
            return axdrEnumToAccessResultCode(response.setResponseNormal.result);

        case SET_RESPONSE_WITH_LIST:
            return axdrEnumsToAccessResultCodes(response.setResponseWithList.result.list());

        case SET_RESPONSE_LAST_DATABLOCK:
            return axdrEnumToAccessResultCode(response.setResponseLastDatablock.result);

        case SET_RESPONSE_LAST_DATABLOCK_WITH_LIST:
            return axdrEnumsToAccessResultCodes(response.setResponseLastDatablockWithList.result.list());

        default:
            // should not occur.
            throw new IllegalStateException("Unknown response type");
        }

    }

    private static List<AccessResultCode> axdrEnumToAccessResultCode(AxdrEnum axdrEnum) {
        return Arrays.asList(enumValueFrom(axdrEnum, AccessResultCode.class));
    }

    private static List<AccessResultCode> axdrEnumsToAccessResultCodes(List<AxdrEnum> enums) {
        List<AccessResultCode> result = new ArrayList<>(enums.size());
        for (AxdrEnum axdrEnum : enums) {
            result.add(enumValueFrom(axdrEnum, AccessResultCode.class));
        }
        return result;
    }

    public synchronized List<MethodResult> action(boolean priority, List<MethodParameter> params) throws IOException {
        if (params.isEmpty()) {
            return emptyList();
        }

        final InvokeIdAndPriority id = invokeIdAndPriorityFor(priority);

        ACTIONResponse response = createAndSendActionPdu(id, params);

        switch (response.getChoiceIndex()) {
        case ACTION_RESPONSE_NORMAL:
            return processActionNormal(response);
        case ACTION_RESPONSE_WITH_LIST:
            return processActionWithList(response);
        case ACTION_RESPONSE_WITH_PBLOCK:
            return processActionWithPblock(response);

        case ACTION_RESPONSE_NEXT_PBLOCK:
        case _ERR_NONE_SELECTED:
        default:
            throw new IllegalStateException("Server answered with an illegal response.");
        }

    }

    private List<MethodResult> processActionNormal(ACTIONResponse response) {
        ActionResponseWithOptionalData resWithOpt = response.actionResponseNormal.singleResponse;
        MethodResult methodResult = convertActionResponseToMethodResult(resWithOpt);
        return Arrays.asList(methodResult);
    }

    private List<MethodResult> processActionWithList(ACTIONResponse response) {
        SubSeqOfListOfResponses listOfResponses = response.actionResponseWithList.listOfResponses;
        List<MethodResult> result = new ArrayList<>(listOfResponses.size());

        Iterator<ActionResponseWithOptionalData> iter = listOfResponses.iterator();

        while (iter.hasNext()) {
            MethodResult methodResult = convertActionResponseToMethodResult(iter.next());
            result.add(methodResult);
        }
        return result;
    }

    private List<MethodResult> processActionWithPblock(ACTIONResponse response) throws IOException {
        ByteArrayOutputStream datablocks = new ByteArrayOutputStream();
        COSEMpdu pdu = new COSEMpdu();
        ACTIONRequest request = new ACTIONRequest();
        ActionRequestNextPblock nextBlock = new ActionRequestNextPblock();

        ACTIONResponse intermediateResponse = response;
        nextBlock.invokeIdAndPriority = intermediateResponse.actionResponseWithPblock.invokeIdAndPriority;

        while (!intermediateResponse.actionResponseWithPblock.pblock.lastBlock.getValue()) {
            datablocks.write(intermediateResponse.actionResponseWithPblock.pblock.rawData.getValue());

            nextBlock.blockNumber = intermediateResponse.actionResponseWithPblock.pblock.blockNumber;
            request.setActionRequestNextPblock(nextBlock);
            pdu.setActionRequest(request);

            intermediateResponse = send(pdu);
        }
        datablocks.write(intermediateResponse.actionResponseWithPblock.pblock.rawData.getValue());
        InputStream dataByteStream = new ByteArrayInputStream(datablocks.toByteArray());

        return decodeAndConvertActionStream(dataByteStream);
    }

    private static List<MethodResult> decodeAndConvertActionStream(InputStream is) throws IOException {
        List<MethodResult> result = new LinkedList<>();
        while (is.available() > 0) {
            GetDataResult dataResult = new GetDataResult();
            dataResult.decode(is);
            // If remote Method call returns a pdu that must be
            // segmented into blocks of data, the assumption, that
            // the result was successful is always correct.
            DataObject resultData = DataConverter.convertDataToDataObject(dataResult.data);
            result.add(new MethodResult(MethodResultCode.SUCCESS, resultData));
        }
        return result;
    }

    private COSEMpdu createGetPdu(InvokeIdAndPriority id, List<AttributeAddress> params) {
        if (!negotiatedFeatures().contains(ATTRIBUTE0_SUPPORTED_WITH_GET)) {
            checkAttributeId(params);
        }
        if (!negotiatedFeatures().contains(ConformanceSetting.SELECTIVE_ACCESS)) {
            for (AttributeAddress param : params) {
                if (param.getAccessSelection() != null) {
                    throw new IllegalArgumentException("Selective Access not supported on this connection");
                }
            }
        }

        GETRequest getRequest = new GETRequest();
        if (params.size() == 1) {
            GetRequestNormal requestNormal = new GetRequestNormal();
            requestNormal.invokeIdAndPriority = id;
            AttributeAddress attributeAddress = params.get(0);
            requestNormal.cosemAttributeDescriptor = attributeAddress.toDescriptor();
            SelectiveAccessDescription accessSelection = attributeAddress.getAccessSelection();

            SelectiveAccessDescriptor access = selToSelectivAccessDesciptor(accessSelection);

            requestNormal.accessSelection.setValue(access);
            getRequest.setGetRequestNormal(requestNormal);
        }
        else {
            GetRequestWithList requestList = new GetRequestWithList();
            requestList.invokeIdAndPriority = id;
            requestList.attributeDescriptorList = new GetRequestWithList.SubSeqOfAttributeDescriptorList();
            for (AttributeAddress param : params) {
                SelectiveAccessDescription accessSelection = param.getAccessSelection();
                SelectiveAccessDescriptor access = selToSelectivAccessDesciptor(accessSelection);

                CosemAttributeDescriptorWithSelection element = new CosemAttributeDescriptorWithSelection(
                        param.toDescriptor(), access);
                requestList.attributeDescriptorList.add(element);
            }

            getRequest.setGetRequestWithList(requestList);
        }

        COSEMpdu pdu = new COSEMpdu();
        pdu.setGetRequest(getRequest);

        return pdu;
    }

    private SETResponse createAndSendSetPdu(InvokeIdAndPriority id, List<SetParameter> params) throws IOException {
        if (!negotiatedFeatures().contains(ATTRIBUTE0_SUPPORTED_WITH_SET)) {
            for (SetParameter param : params) {
                if (param.getAttributeAddress().getId() == 0) {
                    throw new IllegalArgumentException("No Attribute 0 on set allowed");
                }
            }
        }

        SETRequest request = new SETRequest();

        if (params.size() == 1) {
            SetRequestNormal requestNormal = new SetRequestNormal();
            requestNormal.invokeIdAndPriority = id;
            SetParameter setParameter = params.get(0);
            AttributeAddress attributeAddress = setParameter.getAttributeAddress();
            SelectiveAccessDescription accessSelection = attributeAddress.getAccessSelection();
            SelectiveAccessDescriptor access = selToSelectivAccessDesciptor(accessSelection);

            requestNormal.cosemAttributeDescriptor = attributeAddress.toDescriptor();
            requestNormal.value = DataConverter.convertDataObjectToData(setParameter.getData());
            requestNormal.accessSelection.setValue(access);
            request.setSetRequestNormal(requestNormal);
        }
        else {
            SetRequestWithList requestList = new SetRequestWithList();
            requestList.invokeIdAndPriority = id;
            requestList.attributeDescriptorList = new SetRequestWithList.SubSeqOfAttributeDescriptorList();
            requestList.valueList = new SetRequestWithList.SubSeqOfValueList();
            for (SetParameter param : params) {
                AttributeAddress attributeAddress = param.getAttributeAddress();
                SelectiveAccessDescription accessSelection = attributeAddress.getAccessSelection();
                SelectiveAccessDescriptor access = selToSelectivAccessDesciptor(accessSelection);
                CosemAttributeDescriptor desc = attributeAddress.toDescriptor();

                requestList.attributeDescriptorList.add(new CosemAttributeDescriptorWithSelection(desc, access));
                requestList.valueList.add(DataConverter.convertDataObjectToData(param.getData()));
            }
            request.setSetRequestWithList(requestList);
        }

        if (maxSendPduSize() == 0 || pduSizeOf(request) <= maxSendPduSize()) {
            COSEMpdu pdu = new COSEMpdu();
            pdu.setSetRequest(request);

            return send(pdu);
        }
        else {
            // TODO send fragments - implements this
            throw new IOException("Receiving fragments not yet implemented..");
        }

    }

    private static SelectiveAccessDescriptor selToSelectivAccessDesciptor(SelectiveAccessDescription accessSelection) {
        if (accessSelection == null) {
            return null;
        }

        Unsigned8 accessSelector = new Unsigned8(accessSelection.getAccessSelector());
        Data dataObjectToData = DataConverter.convertDataObjectToData(accessSelection.getAccessParameter());
        return new SelectiveAccessDescriptor(accessSelector, dataObjectToData);
    }

    private ACTIONResponse createAndSendActionPdu(InvokeIdAndPriority invokeIdAndPrio, List<MethodParameter> params)
            throws IOException {
        for (MethodParameter param : params) {
            if (param.getId() == 0) {
                throw new IllegalArgumentException("Method ID 0 not allowed on action");
            }
        }

        ACTIONRequest request = new ACTIONRequest();

        if (params.size() == 1) {
            MethodParameter methodParameter = params.get(0);

            ActionRequestNormal requestNormal = new ActionRequestNormal();

            boolean paramIsUsed = !methodParameter.getParameter().isNull();

            requestNormal.invokeIdAndPriority = invokeIdAndPrio;
            requestNormal.cosemMethodDescriptor = methodParameter.toDescriptor();

            AxdrOptional<Data> invocationParam = requestNormal.methodInvocationParameters;
            invocationParam.setUsed(paramIsUsed);
            if (paramIsUsed) {
                Data convertedData = convertDataObjectToData(methodParameter.getParameter());
                invocationParam.setValue(convertedData);
            }

            request.setActionRequestNormal(requestNormal);
        }
        else {
            ActionRequestWithList requestList = new ActionRequestWithList();
            requestList.invokeIdAndPriority = invokeIdAndPrio;
            requestList.cosemMethodDescriptorList = new ActionRequestWithList.SubSeqOfCosemMethodDescriptorList();
            requestList.methodInvocationParameters = new ActionRequestWithList.SubSeqOfMethodInvocationParameters();
            for (MethodParameter param : params) {
                requestList.cosemMethodDescriptorList.add(param.toDescriptor());
                requestList.methodInvocationParameters.add(convertDataObjectToData(param.getParameter()));
            }
            request.setActionRequestWithList(requestList);
        }

        if (maxSendPduSize() == 0 || pduSizeOf(request) <= maxSendPduSize()) {
            COSEMpdu pdu = new COSEMpdu();
            pdu.setActionRequest(request);

            return send(pdu);
        }
        else {
            // TODO send fragments
            throw new IOException("this is not yet implemented..");
        }
    }

    private static void checkAttributeId(List<AttributeAddress> params) {
        for (AttributeAddress param : params) {
            if (param.getId() == 0) {
                throw new IllegalArgumentException("No Attribute 0 on get allowed");
            }
        }
    }

    private static int pduSizeOf(AxdrType pdu) throws IOException {
        return pdu.encode(new NullOutputStream());
    }

    private static MethodResult convertActionResponseToMethodResult(ActionResponseWithOptionalData resp) {
        DataObject resultData = null;
        if (resp.returnParameters.isUsed()) {
            resultData = DataConverter.convertDataToDataObject(resp.returnParameters.getValue().data);
        }
        MethodResultCode methodResultCode = enumValueFrom(resp.result, MethodResultCode.class);
        return new MethodResult(methodResultCode, resultData);
    }

    private static List<GetResult> convertListToDataObject(List<GetDataResult> resultList) {
        List<GetResult> result = new ArrayList<>(resultList.size());
        for (GetDataResult resultPdu : resultList) {
            GetResult res = convertPduToGetResult(resultPdu);
            result.add(res);
        }

        return result;
    }

    private static GetResult convertPduToGetResult(GetDataResult pdu) {
        if (pdu.getChoiceIndex() == GetDataResult.Choices.DATA) {
            return new AccessResultImpl(DataConverter.convertDataToDataObject(pdu.data));
        }
        else {
            AccessResultCode resultCode = enumValueFrom(pdu.dataAccessResult, AccessResultCode.class);
            return new AccessResultImpl(resultCode);
        }
    }

    @Override
    void processEventPdu(COSEMpdu pdu) {
        // implement event listening
    }

    @Override
    Set<ConformanceSetting> proposedConformance() {
        return new HashSet<>(Arrays.asList(GET, SET, ACTION, /* EVENT_NOTIFICATION, */ SELECTIVE_ACCESS,
                PRIORITY_MGMT_SUPPORTED, MULTIPLE_REFERENCES, BLOCK_TRANSFER_WITH_ACTION,
                BLOCK_TRANSFER_WITH_GET_OR_READ, BLOCK_TRANSFER_WITH_SET_OR_WRITE, ATTRIBUTE0_SUPPORTED_WITH_GET,
                ATTRIBUTE0_SUPPORTED_WITH_SET));
    }

    @Override
    MethodResult authenticateViaHls(byte[] processedChallenge) throws IOException {
        DataObject param = DataObject.newOctetStringData(processedChallenge);

        MethodParameter authenticate = new MethodParameter(15, WellKnownInstanceIds.CURRENT_ASSOCIATION_ID, 1, param);

        return action(true, Arrays.asList(authenticate)).get(0);
    }

    @Override
    void validateReferencingMethod() throws IOException {
        if (negotiatedFeatures().contains(SET) || negotiatedFeatures().contains(ConformanceSetting.GET)) {
            return;
        }
        close();

        ExceptionId exceptionId = ExceptionId.WRONG_REFERENCING_METHOD;
        String msg = "Wrong referencing method. Remote smart meter can't use LN referencing.";
        throw new FatalJDlmsException(exceptionId, Fault.USER, msg);
    }

    @Override
    ContextId getContextId() {

        if (connectionSettings().securitySuite().getEncryptionMechanism() != EncryptionMechanism.NONE) {
            return ContextId.LOGICAL_NAME_REFERENCING_WITH_CIPHERING;
        }
        else {
            return ContextId.LOGICAL_NAME_REFERENCING_NO_CIPHERING;
        }
    }

}
