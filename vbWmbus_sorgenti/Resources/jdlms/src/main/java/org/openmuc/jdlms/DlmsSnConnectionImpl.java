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

import static org.openmuc.jdlms.AccessResultCode.SCOPE_OF_ACCESS_VIOLATED;
import static org.openmuc.jdlms.ConformanceSetting.MULTIPLE_REFERENCES;
import static org.openmuc.jdlms.ConformanceSetting.PARAMETERIZED_ACCESS;
import static org.openmuc.jdlms.ConformanceSetting.READ;
import static org.openmuc.jdlms.ConformanceSetting.WRITE;
import static org.openmuc.jdlms.internal.DlmsEnumFunctions.enumValueFrom;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.openmuc.jdlms.SecuritySuite.EncryptionMechanism;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.internal.ContextId;
import org.openmuc.jdlms.internal.DataConverter;
import org.openmuc.jdlms.internal.asn1.cosem.BlockNumberAccess;
import org.openmuc.jdlms.internal.asn1.cosem.COSEMpdu;
import org.openmuc.jdlms.internal.asn1.cosem.Data;
import org.openmuc.jdlms.internal.asn1.cosem.DataBlockResult;
import org.openmuc.jdlms.internal.asn1.cosem.Integer16;
import org.openmuc.jdlms.internal.asn1.cosem.ParameterizedAccess;
import org.openmuc.jdlms.internal.asn1.cosem.ReadRequest;
import org.openmuc.jdlms.internal.asn1.cosem.ReadResponse;
import org.openmuc.jdlms.internal.asn1.cosem.Unsigned16;
import org.openmuc.jdlms.internal.asn1.cosem.Unsigned8;
import org.openmuc.jdlms.internal.asn1.cosem.VariableAccessSpecification;
import org.openmuc.jdlms.internal.asn1.cosem.WriteRequest;
import org.openmuc.jdlms.internal.asn1.cosem.WriteResponse;
import org.openmuc.jdlms.sessionlayer.client.SessionLayer;
import org.openmuc.jdlms.settings.client.Settings;

class DlmsSnConnectionImpl extends BaseDlmsConnection implements DlmsSnConnection {

    DlmsSnConnectionImpl(Settings settings, SessionLayer sessionLayer) {
        super(settings, sessionLayer);
    }

    @Override
    public ReadResult read(SnAddressSpec params) throws IOException {
        return read(Arrays.asList(params)).get(0);
    }

    @Override
    public synchronized List<ReadResult> read(List<SnAddressSpec> params) throws IOException {
        if (nullableListIsEmpty(params)) {
            return Collections.emptyList();
        }

        if (!multipleReferencesAllowed(params)) {
            return callEachReadIndividually(params);
        }

        ReadRequest request = new ReadRequest();

        ReadResult[] res = new ReadResult[params.size()];

        ListIterator<SnAddressSpec> iterator = params.listIterator();
        while (iterator.hasNext()) {
            SnAddressSpec addrSpec = iterator.next();
            int indexOfCurrentVal = iterator.previousIndex();

            VariableAccessSpecification varSpec = createVarAccessSpecFor(res, addrSpec, indexOfCurrentVal,
                    new AccessResultImpl(AccessResultCode.SCOPE_OF_ACCESS_VIOLATED));
            request.add(varSpec);
        }

        COSEMpdu pdu = new COSEMpdu();
        pdu.setReadRequest(request);

        ReadResponse response = sendReadPoll(pdu);

        Iterator<ReadResponse.SubChoice> resIter = response.iterator();
        int resIndex = 0;
        while (resIter.hasNext()) {
            ReadResponse.SubChoice data = resIter.next();

            ReadResult resultItem = convertReadResponseToReadResult(data);

            resIndex = nextFreeResultIndex(res, resIndex);

            res[resIndex++] = resultItem;
        }

        return Arrays.asList(res);
    }

    private <T> VariableAccessSpecification createVarAccessSpecFor(T[] res, SnAddressSpec addrSpec,
            int indexOfCurrentVal, T errVal) {
        VariableAccessSpecification varSpec = new VariableAccessSpecification();

        SelectiveAccessDescription accessDescription = addrSpec.getParameterizedAccessDescriptor();
        Integer16 variableName = new Integer16((short) addrSpec.getVariableName());
        if (accessDescription == null) {
            varSpec.setVariableName(variableName);
        }
        else if (negotiatedFeatures().contains(PARAMETERIZED_ACCESS)) {

            Unsigned8 selector = new Unsigned8(accessDescription.getAccessSelector());
            Data data = DataConverter.convertDataObjectToData(accessDescription.getAccessParameter());

            ParameterizedAccess paramAccess = new ParameterizedAccess(variableName, selector, data);

            varSpec.setParameterizedAccess(paramAccess);
        }
        else {
            res[indexOfCurrentVal] = errVal;
        }
        return varSpec;
    }

    private static ReadResult convertReadResponseToReadResult(ReadResponse.SubChoice data) {
        ReadResult resultItem;
        if (data.getChoiceIndex() == ReadResponse.SubChoice.Choices.DATA) {
            DataObject dat = DataConverter.convertDataToDataObject(data.data);
            resultItem = new AccessResultImpl(dat);
        }
        else {
            AccessResultCode resultCode = enumValueFrom(data.dataAccessError, AccessResultCode.class);
            resultItem = new AccessResultImpl(resultCode);
        }
        return resultItem;
    }

    private boolean multipleReferencesAllowed(List<?> params) {
        return params.size() == 1 || negotiatedFeatures().contains(MULTIPLE_REFERENCES);
    }

    private List<ReadResult> callEachReadIndividually(List<SnAddressSpec> params) throws IOException {
        List<ReadResult> resultList = new ArrayList<>(params.size());
        for (SnAddressSpec snAddress : params) {
            resultList.add(read(snAddress));
        }
        return resultList;
    }

    private ReadResponse sendReadPoll(COSEMpdu pdu) throws IOException {
        ReadResponse readResponse = send(pdu);

        ReadResponse.SubChoice.Choices choiceIndex = readResponse.get(0).getChoiceIndex();

        if (choiceIndex != ReadResponse.SubChoice.Choices.DATA_BLOCK_RESULT) {
            return readResponse;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        while (true) {
            DataBlockResult dataBlockResult = readResponse.get(0).dataBlockResult;

            baos.write(dataBlockResult.rawData.getValue());

            if (dataBlockResult.lastBlock.getValue()) {
                readResponse = new ReadResponse();
                readResponse.decode(new ByteArrayInputStream(baos.toByteArray()));
                return readResponse;
            }

            Unsigned16 blockNumber = dataBlockResult.blockNumber;

            readResponse = requestNexBlock(blockNumber);
        }

    }

    private ReadResponse requestNexBlock(Unsigned16 blockNumber) throws IOException {
        ReadRequest readRequest = new ReadRequest();
        VariableAccessSpecification blockNumAccess = new VariableAccessSpecification();
        blockNumAccess.setBlockNumberAccess(new BlockNumberAccess(blockNumber));
        readRequest.add(blockNumAccess);

        COSEMpdu cosemPdu = new COSEMpdu();
        cosemPdu.setReadRequest(readRequest);
        return send(cosemPdu);
    }

    @Override
    public AccessResultCode write(SnWriteParameter param) throws IOException {
        return write(Arrays.asList(param)).get(0);
    }

    @Override
    public synchronized List<AccessResultCode> write(List<SnWriteParameter> params) throws IOException {

        if (nullableListIsEmpty(params)) {
            return Collections.emptyList();
        }
        if (!multipleReferencesAllowed(params)) {
            return callEachWriteIndividually(params);
        }
        if (!negotiatedFeatures().contains(ConformanceSetting.WRITE)) {
            return answerWithWriteNotAllowed(params);
        }

        AccessResultCode[] result = new AccessResultCode[params.size()];

        ListIterator<SnWriteParameter> paramsIter = params.listIterator();

        WriteRequest request = new WriteRequest();
        request.listOfData = new WriteRequest.SubSeqOfListOfData();
        request.variableAccessSpecification = new WriteRequest.SubSeqOfVariableAccessSpecification();
        while (paramsIter.hasNext()) {
            int nextListIndex = paramsIter.previousIndex();
            SnWriteParameter writeSpec = paramsIter.next();
            AccessResultCode defaultErrorValue = AccessResultCode.SCOPE_OF_ACCESS_VIOLATED;
            VariableAccessSpecification varSpec = createVarAccessSpecFor(result, writeSpec.getAddress(), nextListIndex,
                    defaultErrorValue);

            request.listOfData.add(DataConverter.convertDataObjectToData(writeSpec.getData()));
            request.variableAccessSpecification.add(varSpec);
        }

        if (request.variableAccessSpecification.size() == 0) {
            return Arrays.asList(result);
        }

        COSEMpdu pdu = new COSEMpdu();
        pdu.setWriteRequest(request);

        WriteResponse writeRes = send(pdu);
        Iterator<WriteResponse.SubChoice> responseIter = writeRes.iterator();

        int resultIndex = 0;
        while (responseIter.hasNext()) {
            WriteResponse.SubChoice response = responseIter.next();
            AccessResultCode item;
            if (response.getChoiceIndex() == WriteResponse.SubChoice.Choices.SUCCESS) {
                item = AccessResultCode.SUCCESS;
            }
            else {
                item = enumValueFrom(response.dataAccessError, AccessResultCode.class);
            }

            resultIndex = nextFreeResultIndex(result, resultIndex);

            result[resultIndex++] = item;
        }
        return Arrays.asList(result);

    }

    private List<AccessResultCode> answerWithWriteNotAllowed(List<SnWriteParameter> params) {
        List<AccessResultCode> resList = new ArrayList<>(params.size());
        for (int i = 0; i < params.size(); ++i) {
            resList.add(SCOPE_OF_ACCESS_VIOLATED);
        }
        return resList;
    }

    private List<AccessResultCode> callEachWriteIndividually(List<SnWriteParameter> params) throws IOException {
        List<AccessResultCode> resList = new ArrayList<>(params.size());

        for (SnWriteParameter param : params) {
            resList.add(write(param));
        }
        return resList;
    }

    private static int nextFreeResultIndex(Object[] result, int resIndex) {
        int startIndex = resIndex;

        while (startIndex < result.length && result[startIndex] != null) {
            ++startIndex;
        }
        return startIndex;
    }

    @Override
    void processEventPdu(COSEMpdu pdu) {
        // TODO add event listening INFORMATIONREPORTREQUEST
    }

    @Override
    Set<ConformanceSetting> proposedConformance() {
        return new HashSet<>(
                Arrays.asList(READ, WRITE, MULTIPLE_REFERENCES, PARAMETERIZED_ACCESS /* , INFORMTION_REPORT */));
    }

    private static boolean nullableListIsEmpty(List<?> params) {
        return params == null || params.isEmpty();
    }

    @Override
    MethodResult authenticateViaHls(byte[] processedChallenge) throws IOException {
        DataObject param = DataObject.newOctetStringData(processedChallenge);

        // IC_Association_SN#REPLY_TO_HLS_AUTHENTICATION
        int replyToHlsAuthOffset = 0x58;
        SnAddressSpec snAddress = SnAddressSpec.newMethodAddress(0Xfa00 + replyToHlsAuthOffset, param);
        ReadResult result = read(snAddress);

        AccessResultCode resultCode = result.getResultCode();

        MethodResultCode methodCode = MethodResultCode.SUCCESS;
        if (resultCode != AccessResultCode.SUCCESS) {
            methodCode = MethodResultCode.OBJECT_UNAVAILABLE;
        }
        return new MethodResult(methodCode, result.getResultData());
    }

    @Override
    void validateReferencingMethod() throws IOException {
        /*
         * If the Conformance bit string does not contain read nor write -> this smart meter cannot communicate with SN
         * referencing.
         */
        if (negotiatedFeatures().contains(READ) || negotiatedFeatures().contains(WRITE)) {
            return;
        }

        close();
        throw new IOException("Wrong referencing method. Remote smart meter can't use SN referencing");
    }

    @Override
    ContextId getContextId() {
        EncryptionMechanism encryptionMechanism = connectionSettings().securitySuite().getEncryptionMechanism();
        if (encryptionMechanism != EncryptionMechanism.NONE) {
            return ContextId.SHORT_NAME_REFERENCING_WITH_CIPHERING;
        }
        else {
            return ContextId.SHORT_NAME_REFERENCING_NO_CIPHERING;
        }
    }

}
