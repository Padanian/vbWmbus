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

import static org.openmuc.jdlms.MethodResultCode.OBJECT_UNDEFINED;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.openmuc.jdlms.JDlmsException.ExceptionId;
import org.openmuc.jdlms.JDlmsException.Fault;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.sessionlayer.client.SessionLayer;
import org.openmuc.jdlms.settings.client.Settings;

class DlmsLnToSnConnectionWrapper extends LnConnectionWrapper {

    /**
     * Short name referring to the list of all accessible COSEM Objects on the smart meter
     */
    private static final short ASSOCIATION_OBJECT_LIST = (short) 0xFA08;

    private Map<ObisCode, SnObjectInfo> lnSnMapping;
    private volatile boolean mapIsInitialized;

    private final Map<SnClassVersion, SnClassInfo> snClassInfos;

    private final DlmsSnConnectionImpl snConnection;

    DlmsLnToSnConnectionWrapper(Settings settings, SessionLayer transportLayerCon,
            Map<ObisCode, SnObjectInfo> lnSnMapping, Map<SnClassVersion, SnClassInfo> snClassInfos) {
        this.snConnection = new DlmsSnConnectionImpl(settings, transportLayerCon);

        this.snClassInfos = snClassInfos;

        this.mapIsInitialized = false;

        if (lnSnMapping != null && !lnSnMapping.isEmpty()) {
            this.lnSnMapping = lnSnMapping;
        }
        else {
            this.lnSnMapping = new HashMap<>();
        }
    }

    @Override
    protected void connect() throws IOException {
        this.snConnection.connect();
    }

    @Override
    public synchronized List<GetResult> get(boolean highPriority, List<AttributeAddress> params) throws IOException {
        if (nullableListIsEmpty(params)) {
            return Collections.emptyList();
        }

        GetResult[] res = new GetResult[params.size()];

        List<SnAddressSpec> addressSpecs = new ArrayList<>(params.size());

        ListIterator<AttributeAddress> addrIter = params.listIterator();

        while (addrIter.hasNext()) {
            int nextIndex = addrIter.nextIndex();
            AttributeAddress next = addrIter.next();
            try {
                addressSpecs.add(buildAddressSpec(next));
            } catch (AccessNotAllowedException e) {
                res[nextIndex] = new AccessResultImpl(AccessResultCode.OBJECT_UNDEFINED);
            }
        }

        ListIterator<ReadResult> iter = this.snConnection.read(addressSpecs).listIterator();

        int index = 0;
        while (iter.hasNext()) {
            GetResult r = (GetResult) iter.next();
            index = nextFreeResultIndex(res, index);

            res[index] = r;
        }

        return Arrays.asList(res);
    }

    @Override
    public synchronized List<AccessResultCode> set(boolean highPriority, List<SetParameter> params) throws IOException {
        if (nullableListIsEmpty(params)) {
            return Collections.emptyList();
        }

        AccessResultCode[] finalResList = new AccessResultCode[params.size()];

        List<SnWriteParameter> addressSpecs = new ArrayList<>(params.size());

        ListIterator<SetParameter> addrIter = params.listIterator();

        while (addrIter.hasNext()) {
            int nextIndex = addrIter.nextIndex();
            SetParameter setParam = addrIter.next();
            try {
                SnAddressSpec addressSpec = buildAddressSpec(setParam.getAttributeAddress());
                addressSpecs.add(SnWriteParameter.newAttributeWriteParameter(addressSpec, setParam.getData()));
            } catch (AccessNotAllowedException e) {
                finalResList[nextIndex] = AccessResultCode.OBJECT_UNDEFINED;
            }
        }

        ListIterator<AccessResultCode> iter = this.snConnection.write(addressSpecs).listIterator();

        int index = 0;
        while (iter.hasNext()) {
            AccessResultCode resultCode = iter.next();
            index = nextFreeResultIndex(finalResList, index);
            finalResList[index++] = resultCode;
        }

        return Arrays.asList(finalResList);

    }

    @Override
    public synchronized List<MethodResult> action(boolean priority, List<MethodParameter> params) throws IOException {
        if (nullableListIsEmpty(params)) {
            return Collections.emptyList();
        }

        MethodResult[] finalResult = new MethodResult[params.size()];

        ListIterator<MethodParameter> paramsIter = params.listIterator();

        List<SnAddressSpec> readParameter = new ArrayList<>(params.size());
        while (paramsIter.hasNext()) {

            int index = paramsIter.nextIndex();
            MethodParameter param = paramsIter.next();

            SnObjectInfo snObjectInfo;
            try {
                snObjectInfo = accessSnObjectInfo(param);
            } catch (AccessNotAllowedException e) {
                finalResult[index] = new MethodResult(OBJECT_UNDEFINED);
                continue;
            }

            SnClassInfo classInfo = this.snClassInfos.get(snObjectInfo.getSnClassVersion());
            if (classInfo == null) {
                finalResult[index] = new MethodResult(OBJECT_UNDEFINED);
            }
            else {
                int snOffset = classInfo.computeMethodSnOffsetFor(param.getId());
                if (snOffset == -1) {
                    finalResult[index] = new MethodResult(OBJECT_UNDEFINED);
                }
                else {
                    int variableName = snObjectInfo.getBaseName() + snOffset;

                    SnAddressSpec snAddress = SnAddressSpec.newMethodAddress(variableName, param.getParameter());
                    readParameter.add(snAddress);

                }

            }
        }

        List<ReadResult> readResults = this.snConnection.read(readParameter);

        return convertAndMergeActionResults(finalResult, readResults);
    }

    private static List<MethodResult> convertAndMergeActionResults(MethodResult[] finalResult,
            List<ReadResult> readResults) {
        int index = 0;
        for (ReadResult readResult : readResults) {
            index = nextFreeResultIndex(finalResult, index);

            MethodResultCode resultCode = readResult.getResultCode() == AccessResultCode.SUCCESS
                    ? MethodResultCode.SUCCESS
                    : MethodResultCode.OTHER_REASON;
            finalResult[index++] = new MethodResult(resultCode, readResult.getResultData());
        }

        return Arrays.asList(finalResult);
    }

    private static int nextFreeResultIndex(Object[] result, int resId) {
        int startIndex = resId;

        while (startIndex < result.length && result[startIndex] != null) {
            ++startIndex;
        }
        return startIndex;
    }

    private SnAddressSpec buildAddressSpec(AttributeAddress attributeAddress)
            throws IOException, AccessNotAllowedException {
        SnObjectInfo objectInfo = accessSnObjectInfo(attributeAddress);

        SnClassInfo classInfo = this.snClassInfos.get(objectInfo.getSnClassVersion());

        int snOffset;
        if (classInfo == null) {
            snOffset = (attributeAddress.getId() - 1) * 0x08;
        }
        else {
            snOffset = classInfo.computeAttributeSnOffsetFor(attributeAddress.getId());
        }

        int variableName = snOffset + objectInfo.getBaseName();
        return SnAddressSpec.newAttributeAddress(variableName, attributeAddress.getAccessSelection());

    }

    @SuppressWarnings("serial")
    private class AccessNotAllowedException extends Exception {
        public AccessNotAllowedException(String message) {
            super(message);
        }
    }

    private SnObjectInfo accessSnObjectInfo(CosemResourceDescriptor cosemResourceDescriptor)
            throws IOException, AccessNotAllowedException {
        ObisCode instanceId = cosemResourceDescriptor.getInstanceId();
        SnObjectInfo snObjectInfo = lnSnMapping.get(instanceId);

        if (snObjectInfo == null && !mapIsInitialized) {
            try {
                snObjectInfo = retrieveVariableInfoFor(cosemResourceDescriptor);
                lnSnMapping.put(instanceId, snObjectInfo);
            } catch (IOException e) {
                initializeLnMap();
                snObjectInfo = lnSnMapping.get(instanceId);
            }
        }

        if (snObjectInfo != null) {
            return snObjectInfo;
        }

        String msg = "Object " + instanceId + " unknown to the smart meter. Try an other address.";
        throw new AccessNotAllowedException(msg);
    }

    private static boolean nullableListIsEmpty(List<?> params) {
        return params == null || params.isEmpty();
    }

    private SnObjectInfo retrieveVariableInfoFor(CosemResourceDescriptor resourceDescriptor) throws IOException {

        int selector = 2;
        DataObject filter = DataObject.newStructureData(DataObject.newUInteger16Data(resourceDescriptor.getClassId()),
                DataObject.newOctetStringData(resourceDescriptor.getInstanceId().bytes()));

        SnAddressSpec params = SnAddressSpec.newAttributeAddress(ASSOCIATION_OBJECT_LIST,
                new SelectiveAccessDescription(selector, filter));
        ReadResult res = this.snConnection.read(params);

        if (res.getResultCode() != AccessResultCode.SUCCESS) {
            throw new NonFatalJDlmsException(ExceptionId.UNKNOWN, Fault.SYSTEM, "Could not load variable info.");
        }

        List<DataObject> objectListElement = res.getResultData().getValue();

        Number baseName = objectListElement.get(0).getValue();
        Number classId = objectListElement.get(1).getValue();
        Number version = objectListElement.get(2).getValue();

        SnClassVersion classVersion = new SnClassVersion(classId.intValue(), version.intValue());
        return new SnObjectInfo(baseName.intValue(), resourceDescriptor.getInstanceId(), classVersion);
    }

    Map<ObisCode, SnObjectInfo> getLnSnInfoMapping() throws IOException {
        synchronized (this.lnSnMapping) {
            initializeLnMap();
            return new HashMap<>(this.lnSnMapping);
        }
    }

    private void initializeLnMap() throws IOException {
        synchronized (lnSnMapping) {
            if (mapIsInitialized) {
                return;
            }

            ReadResult readResult = this.snConnection.read(SnAddressSpec.newAttributeAddress(ASSOCIATION_OBJECT_LIST));

            if (readResult.getResultCode() != AccessResultCode.SUCCESS) {
                throw new FatalJDlmsException(ExceptionId.CONNECTION_ESTABLISH_ERROR, Fault.SYSTEM,
                        "Could not access the mapping list.");
            }

            List<DataObject> resultData = readResult.getResultData().getValue();
            for (DataObject object : resultData) {
                List<DataObject> objectStructur = object.getValue();
                Number baseName = objectStructur.get(0).getValue();
                Number classId = objectStructur.get(1).getValue();
                Number version = objectStructur.get(2).getValue();

                byte[] instancIdBytes = objectStructur.get(3).getValue();
                ObisCode instanceId = new ObisCode(instancIdBytes);

                SnObjectInfo oldV = this.lnSnMapping.get(instanceId);
                if (oldV == null || oldV.getBaseName() != baseName.intValue()) {
                    SnClassVersion snClassVersion = new SnClassVersion(classId.intValue(), version.intValue());
                    SnObjectInfo value = new SnObjectInfo(baseName.intValue(), instanceId, snClassVersion);

                    this.lnSnMapping.put(instanceId, value);
                }

            }

            this.mapIsInitialized = true;
        }
    }

    @Override
    protected BaseConnection getWrappedConnection() {
        return this.snConnection;
    }

}
