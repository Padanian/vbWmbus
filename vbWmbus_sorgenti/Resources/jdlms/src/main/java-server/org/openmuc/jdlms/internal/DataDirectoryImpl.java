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
package org.openmuc.jdlms.internal;

import static java.util.Collections.sort;
import static org.openmuc.jdlms.AccessResultCode.OBJECT_UNDEFINED;
import static org.openmuc.jdlms.internal.DataConverter.convertDataToDataObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmuc.jdlms.AccessResultCode;
import org.openmuc.jdlms.AttributeAddress;
import org.openmuc.jdlms.AuthenticationMechanism;
import org.openmuc.jdlms.CosemAttribute;
import org.openmuc.jdlms.CosemClass;
import org.openmuc.jdlms.CosemInterfaceObject;
import org.openmuc.jdlms.CosemResourceDescriptor;
import org.openmuc.jdlms.DataDirectory;
import org.openmuc.jdlms.IllegalAttributeAccessException;
import org.openmuc.jdlms.IllegalMethodAccessException;
import org.openmuc.jdlms.LogicalDevice;
import org.openmuc.jdlms.MethodAccessMode;
import org.openmuc.jdlms.MethodParameter;
import org.openmuc.jdlms.MethodResultCode;
import org.openmuc.jdlms.ObisCode;
import org.openmuc.jdlms.SecuritySuite;
import org.openmuc.jdlms.SecuritySuite.SecurityPolicy;
import org.openmuc.jdlms.SelectiveAccessDescription;
import org.openmuc.jdlms.SetParameter;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.datatypes.DataObject.Type;
import org.openmuc.jdlms.internal.Accessor.AccessorType;
import org.openmuc.jdlms.internal.asn1.cosem.Data;
import org.openmuc.jdlms.internal.asn1.cosem.ParameterizedAccess;

public class DataDirectoryImpl implements DataDirectory {

    private final Map<Integer, CosemLogicalDevice> logicalDeviceMap;

    private final Map<Long, ServerConnectionData> connectionsData;

    public DataDirectoryImpl() {
        this.logicalDeviceMap = new HashMap<>();
        this.connectionsData = new HashMap<>();
    }

    private DataObject invokeMethod(CosemClassInstance dlmsClassInstance, MethodAccessor method, DataObject params,
            Long connectionId) throws IllegalMethodAccessException {

        SecuritySuite sec = getConnectionData(connectionId).getSecuritySuite();
        SecurityPolicy securityPolicy = sec.getSecurityPolicy();

        MethodAccessMode accessMode = method.getCosemMethod().accessMode();

        if (accessMode == MethodAccessMode.NO_ACCESS) {
            throw new IllegalMethodAccessException(MethodResultCode.READ_WRITE_DENIED);
        }

        if (accessMode == MethodAccessMode.AUTHENTICATED_ACCESS && !securityPolicy.isAuthenticated()) {
            throw new IllegalMethodAccessException(MethodResultCode.READ_WRITE_DENIED);
        }

        return method.invoke(dlmsClassInstance, params, connectionId, securityPolicy);
    }

    public Set<Integer> getLogicalDeviceIds() {
        return this.logicalDeviceMap.keySet();
    }

    public boolean doesLogicalDeviceExists(int logicalDeviceId) {
        return this.logicalDeviceMap.containsKey(logicalDeviceId);
    }

    public CosemLogicalDevice addLogicalDevice(int logicalDeviceId, CosemLogicalDevice logicalDevice) {
        return this.logicalDeviceMap.put(logicalDeviceId, logicalDevice);
    }

    private AccessResultCode set(CosemClassInstance dlmsClassInstance, AttributeAccessor accessor, DataObject data,
            SelectiveAccessDescription accessSelection, Long connectionId) {

        CosemAttribute attributeProperties = accessor.getCosemAttribute();

        try {
            checkSetAccess(attributeProperties, data.getType());
        } catch (IllegalAttributeAccessException e) {
            return e.getAccessResultCode();
        }

        ServerConnectionData connectionData = getConnectionData(connectionId);
        try {
            accessor.set(data, dlmsClassInstance, accessSelection, connectionId,
                    connectionData.getSecuritySuite().getSecurityPolicy());
        } catch (IllegalAttributeAccessException e) {
            return e.getAccessResultCode();
        }

        return AccessResultCode.SUCCESS;
    }

    private void checkSetAccess(CosemAttribute attributeProperties, Type type) throws IllegalAttributeAccessException {
        switch (attributeProperties.accessMode()) {
        case READ_ONLY:
        case NO_ACCESS:
            throw new IllegalAttributeAccessException(AccessResultCode.READ_WRITE_DENIED);
        default:
            // TODO: Handle other cases!
            break;
        }

        if (attributeProperties.type() != Type.DONT_CARE && attributeProperties.type() != type) {
            // TODO: error wrong dataobject provided!
        }
    }

    public synchronized void snSetOrInvoke(int logicalDeviceId, int variableName, Data data, Long connectionId)
            throws IllegalAttributeAccessException, IllegalMethodAccessException {
        BaseNameRangeSet rangeSet = baseNameRangesFor(logicalDeviceId);
        BaseNameRange range = rangeSet.getIntersectingRange(variableName);

        Accessor accessor = accessorFor(variableName, range);
        if (accessor.getAccessorType() != AccessorType.ATTRIBUTE) {
            throw new IllegalAttributeAccessException(AccessResultCode.OBJECT_UNDEFINED);
        }

        CosemSnClassInstance classInstance = range.getClassInstance();
        DataObject dataObject = convert(data);

        switch (accessor.getAccessorType()) {
        case METHOD:
            invokeMethod(classInstance, (MethodAccessor) accessor, dataObject, connectionId);
            break;
        case ATTRIBUTE:
        default:
            set(classInstance, (AttributeAccessor) accessor, dataObject, null, connectionId);
            break;
        }
    }

    private static DataObject convert(Data data) {
        if (data != null) {
            return DataConverter.convertDataToDataObject(data);
        }
        else {
            return DataObject.newNullData();
        }
    }

    public synchronized DataObject snGetOrInvoke(int logicalDeviceId, int variableName,
            ParameterizedAccess parameterizedAccess, Long connectionId)
            throws IllegalAttributeAccessException, IllegalMethodAccessException {

        BaseNameRangeSet rangeSet = baseNameRangesFor(logicalDeviceId);
        BaseNameRange range = rangeSet.getIntersectingRange(variableName);

        Accessor accessor = accessorFor(variableName, range);
        SelectiveAccessDescription selection = convertToAccessDescription(parameterizedAccess);

        CosemSnClassInstance classInstance = range.getClassInstance();
        switch (accessor.getAccessorType()) {
        case METHOD:
            if (selection == null) {
                throw new IllegalMethodAccessException(MethodResultCode.READ_WRITE_DENIED);
            }

            DataObject params = selection.getAccessParameter();
            return invokeMethod(classInstance, (MethodAccessor) accessor, params, connectionId);
        case ATTRIBUTE:
        default:
            return get(classInstance, (AttributeAccessor) accessor, selection, connectionId);
        }
    }

    private Accessor accessorFor(final int variableName, BaseNameRange range) throws IllegalAttributeAccessException {

        if (range == null) {
            throw new IllegalAttributeAccessException(AccessResultCode.OBJECT_UNDEFINED);
        }

        CosemSnClassInstance classInstance = range.getClassInstance();

        if (variableName % 0x08 != 0) {
            throw new IllegalAttributeAccessException(OBJECT_UNDEFINED);
        }

        Accessor accessor = classInstance.accessorFor(variableName - range.getBaseName());
        if (accessor == null) {
            throw new IllegalAttributeAccessException(AccessResultCode.OBJECT_UNDEFINED);
        }
        return accessor;
    }

    private static SelectiveAccessDescription convertToAccessDescription(ParameterizedAccess parameterizedAccess) {
        if (parameterizedAccess == null) {
            return null;
        }

        DataObject parameter = convertDataToDataObject(parameterizedAccess.parameter);
        int selector = (int) (parameterizedAccess.selector.getValue() & 0xff);

        return new SelectiveAccessDescription(selector, parameter);
    }

    private DataObject get(CosemClassInstance dlmsClassInstance, AttributeAccessor attributeAccessor,
            SelectiveAccessDescription accessSelection, Long connectionId) throws IllegalAttributeAccessException {

        ServerConnectionData connectionData = this.connectionsData.get(connectionId);

        checkGetAccess(attributeAccessor.getCosemAttribute(), connectionData);

        return attributeAccessor.get(dlmsClassInstance, accessSelection, connectionId,
                connectionData.getSecuritySuite().getSecurityPolicy());
    }

    public synchronized DataObject invokeMethod(int logicalDeviceId, MethodParameter params, Long connectionId)
            throws IllegalMethodAccessException {

        CosemClassInstance dlmsClassInstance = retrieveDlmsClassInstance(logicalDeviceId, params);

        if (dlmsClassInstance == null) {
            throw new IllegalMethodAccessException(MethodResultCode.OBJECT_UNDEFINED);
        }

        MethodAccessor method = findMethodAccessorFor(params.getId(), dlmsClassInstance);

        return invokeMethod(dlmsClassInstance, method, params.getParameter(), connectionId);
    }

    public synchronized DataObject get(int logicalDeviceId, AttributeAddress attributeAddress, Long connectionId)
            throws IllegalAttributeAccessException {
        CosemClassInstance dlmsClassInstance = retrieveDlmsClassInstance(logicalDeviceId, attributeAddress);

        if (dlmsClassInstance == null) {
            throw new IllegalAttributeAccessException(AccessResultCode.OBJECT_UNDEFINED);

        }
        AttributeAccessor accessor = findAttributeAccessorFor(dlmsClassInstance, attributeAddress.getId());

        return get(dlmsClassInstance, accessor, attributeAddress.getAccessSelection(), connectionId);
    }

    public synchronized AccessResultCode set(int logicalDeviceId, SetParameter setParameter, Long connectionId) {
        AttributeAccessor accessor;
        CosemClassInstance dlmsClassInstance;
        AttributeAddress attributeAddress = setParameter.getAttributeAddress();
        try {
            dlmsClassInstance = retrieveDlmsClassInstance(logicalDeviceId, attributeAddress);
            if (dlmsClassInstance == null) {
                return AccessResultCode.OBJECT_UNDEFINED;
            }

            accessor = findAttributeAccessorFor(dlmsClassInstance, attributeAddress.getId());
        } catch (IllegalAttributeAccessException e) {
            return e.getAccessResultCode();
        }

        return set(dlmsClassInstance, accessor, setParameter.getData(), attributeAddress.getAccessSelection(),
                connectionId);
    }

    private MethodAccessor findMethodAccessorFor(long methodId, CosemClassInstance dlmsClassInstance)
            throws IllegalMethodAccessException {
        if (dlmsClassInstance == null) {
            throw new IllegalMethodAccessException(MethodResultCode.OBJECT_UNDEFINED);
        }
        return dlmsClassInstance.getMethod((byte) methodId);
    }

    private AttributeAccessor findAttributeAccessorFor(CosemClassInstance dlmsClassInstance, int attributeId)
            throws IllegalAttributeAccessException {

        AttributeAccessor entry = dlmsClassInstance.getAttribute((byte) attributeId);

        if (entry == null) {
            throw new IllegalAttributeAccessException(AccessResultCode.READ_WRITE_DENIED);
        }

        return entry;
    }

    private CosemClassInstance retrieveDlmsClassInstance(int logicalDeviceId,
            CosemResourceDescriptor cosemResourceDescriptor) {
        CosemLogicalDevice logicalDevice = getLogicalDeviceFor(logicalDeviceId);
        CosemClassInstance dlmsClassInstance = logicalDevice.get(cosemResourceDescriptor.getInstanceId());

        if (dlmsClassInstance == null
                || cosemResourceDescriptor.getClassId() != dlmsClassInstance.getCosemClass().id()) {
            return null;
        }

        return dlmsClassInstance;
    }

    private void checkGetAccess(CosemAttribute attributeProperties, ServerConnectionData connectionData)
            throws IllegalAttributeAccessException {
        switch (attributeProperties.accessMode()) {
        case AUTHENTICATED_WRITE_ONLY:
        case WRITE_ONLY:
        case NO_ACCESS:
            throw new IllegalAttributeAccessException(AccessResultCode.READ_WRITE_DENIED);

        case AUTHENTICATED_READ_ONLY:
        case AUTHENTICATED_READ_AND_WRITE:
            SecuritySuite securitySuite = connectionData.getSecuritySuite();
            if (securitySuite.getAuthenticationMechanism() == AuthenticationMechanism.NONE) {
                throw new IllegalAttributeAccessException(AccessResultCode.READ_WRITE_DENIED);
            }
            break;

        case READ_AND_WRITE:
        case READ_ONLY:
        default:
            break;
        }
    }

    public static class CosemLogicalDevice {

        private final Map<ObisCode, CosemClassInstance> classes;
        private final LogicalDevice logicalDevice;
        private final BaseNameRangeSet baseNameRanges;

        public CosemLogicalDevice(LogicalDevice logicalDevice, BaseNameRangeSet baseNameRanges) {
            this.baseNameRanges = baseNameRanges;
            this.classes = new HashMap<>();
            this.logicalDevice = logicalDevice;
        }

        public CosemClassInstance put(ObisCode key, CosemClassInstance classInstance) {
            return this.classes.put(key, classInstance);
        }

        public LogicalDevice getLogicalDevice() {
            return logicalDevice;
        }

        public BaseNameRangeSet getBaseNameRanges() {
            return baseNameRanges;
        }

        public Set<ObisCode> getInstanceIds() {
            return this.classes.keySet();
        }

        public CosemClassInstance get(ObisCode key) {
            return this.classes.get(key);
        }
    }

    public static class CosemSnClassInstance extends CosemClassInstance {
        private final Map<Integer, Accessor> m;

        public CosemSnClassInstance(CosemClass cosemClass, CosemInterfaceObject instance) {
            super(cosemClass, instance);
            this.m = new HashMap<>();
        }

        private Accessor accessorFor(int snOffset) {
            return this.m.get(snOffset);
        }

        public int getMaxSnOffset() {
            return Collections.max(this.m.keySet());
        }

        @Override
        public Accessor putMethod(Byte methodId, MethodAccessor methodAccessor) {
            Accessor accessor = this.m.put(methodAccessor.getCosemMethod().snOffset(), methodAccessor);

            if (accessor != null) {
                return accessor;
            }
            else {
                return super.putMethod(methodId, methodAccessor);
            }

        }

        @Override
        public Accessor putAttribute(Byte attributeId, AttributeAccessor value) {
            Accessor accessor = this.m.put(value.getCosemAttribute().snOffset(), value);

            if (accessor != null) {
                return accessor;
            }
            else {
                return super.putAttribute(attributeId, value);
            }
        }

    }

    public static class CosemClassInstance {
        private final Map<Byte, AttributeAccessor> attributesMap;
        private final Map<Byte, MethodAccessor> methodsMap;

        private final CosemInterfaceObject instance;
        private final CosemClass cosemClass;

        public CosemClassInstance(CosemClass cosemClass, CosemInterfaceObject instance) {
            this.attributesMap = new HashMap<>();
            this.methodsMap = new HashMap<>();

            this.instance = instance;
            this.cosemClass = cosemClass;
        }

        public CosemClass getCosemClass() {
            return cosemClass;
        }

        public CosemInterfaceObject getInstance() {
            return instance;
        }

        public Accessor putAttribute(Byte attributeId, AttributeAccessor value) {
            return this.attributesMap.put(attributeId, value);
        }

        protected AttributeAccessor getAttribute(Byte attributeId) {
            return this.attributesMap.get(attributeId);
        }

        public Accessor putMethod(Byte methodId, MethodAccessor value) {
            return this.methodsMap.put(methodId, value);
        }

        private MethodAccessor getMethod(Byte methodId) {
            return this.methodsMap.get(methodId);
        }

        public Collection<MethodAccessor> getMethods() {
            return this.methodsMap.values();
        }

        public Collection<AttributeAccessor> getAttributes() {
            return this.attributesMap.values();
        }

        public Collection<MethodAccessor> getSortedMethods() {
            List<MethodAccessor> sortedEntries = new ArrayList<>(getMethods());
            sort(sortedEntries, new Comparator<MethodAccessor>() {

                @Override
                public int compare(MethodAccessor o1, MethodAccessor o2) {
                    return Integer.compare(o1.getCosemMethod().id(), o2.getCosemMethod().id());
                }
            });
            return sortedEntries;
        }

        public Collection<AttributeAccessor> getSortedAttributes() {
            List<AttributeAccessor> sortedEntries = new ArrayList<>(getAttributes());
            sort(sortedEntries, new Comparator<AttributeAccessor>() {

                @Override
                public int compare(AttributeAccessor o1, AttributeAccessor o2) {
                    return Integer.compare(o1.getCosemAttribute().id(), o2.getCosemAttribute().id());
                }
            });
            return sortedEntries;
        }

    }

    public CosemLogicalDevice getLogicalDeviceFor(Integer logicalDeviceId) {
        return this.logicalDeviceMap.get(logicalDeviceId);
    }

    public ServerConnectionData addConnection(Long connectionId, ServerConnectionData connectionData) {
        return this.connectionsData.put(connectionId, connectionData);
    }

    public ServerConnectionData getConnectionData(Long connectionId) {
        return connectionsData.get(connectionId);
    }

    public ServerConnectionData removeConnection(Long connectionId) {
        return this.connectionsData.remove(connectionId);
    }

    public BaseNameRangeSet baseNameRangesFor(Integer logicalDeviceId) {
        return this.logicalDeviceMap.get(logicalDeviceId).getBaseNameRanges();
    }

}
