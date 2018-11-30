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

import org.openmuc.jdlms.interfaceclass.attribute.AttributeClass;
import org.openmuc.jdlms.internal.asn1.cosem.CosemAttributeDescriptor;
import org.openmuc.jdlms.internal.asn1.cosem.CosemObjectInstanceId;
import org.openmuc.jdlms.internal.asn1.cosem.Integer8;
import org.openmuc.jdlms.internal.asn1.cosem.Unsigned16;

/**
 * Set of data needed to address an attribute.
 */
@SuppressWarnings("deprecation")
public class AttributeAddress extends CosemResourceDescriptor {
    /**
     * Structure defining access to a subset of an attribute. Consort IEC 62056-6-2 to see which attribute has which
     * access selections. May be null if not needed. (A value of null reads the full attribute)
     */
    private final SelectiveAccessDescription accessSelection;

    /**
     * Creates a new attribute address.
     * 
     * @param classId
     *            Class of the object to read
     * @param instanceId
     *            Identifier of the remote object to read
     * @param attributeId
     *            Attribute of the object that is to read
     * @param access
     *            The filter to apply
     */
    public AttributeAddress(int classId, ObisCode instanceId, int attributeId, SelectiveAccessDescription access) {
        super(classId, instanceId, attributeId);
        this.accessSelection = access;
    }

    /**
     * Creates a new attribute address.
     * 
     * @param classId
     *            Class of the object to read
     * @param instanceId
     *            Identifier of the remote object to read
     * @param attributeId
     *            Attribute of the object that is to read
     */
    public AttributeAddress(int classId, String instanceId, int attributeId) {
        this(classId, new ObisCode(instanceId), attributeId);
    }

    /**
     * Creates a new attribute address.
     * 
     * @param classId
     *            Class of the object to read
     * @param instanceId
     *            Identifier of the remote object to read
     * @param attributeId
     *            Attribute of the object that is to read
     */
    public AttributeAddress(int classId, ObisCode instanceId, int attributeId) {
        this(classId, instanceId, attributeId, null);
    }

    /**
     * Creates a new attribute address.
     * 
     * @param classId
     *            Class of the object to read
     * @param instanceId
     *            Identifier of the remote object to read
     * @param attributeId
     *            Attribute of the object that is to read
     * @param access
     *            The filter to apply.
     */
    public AttributeAddress(int classId, String instanceId, int attributeId, SelectiveAccessDescription access) {
        this(classId, new ObisCode(instanceId), attributeId, access);
    }

    /**
     * Creates a new attribute address.
     * 
     * @param attributeClass
     *            the AttributeClass constant.
     * @param instanceId
     *            Identifier of the remote object to read
     * @param access
     *            The filter to apply.
     * @deprecated since 1.5.1. Use the none enum initializer of {@link org.openmuc.jdlms.AttributeAddress}.
     */
    @Deprecated
    public AttributeAddress(AttributeClass attributeClass, ObisCode instanceId, SelectiveAccessDescription access) {
        this(attributeClass.interfaceClass().id(), instanceId, attributeClass.attributeId(), access);
    }

    /**
     * Creates a new attribute address.
     * 
     * @param attributeClass
     *            the AttributeClass constant.
     * @param instanceId
     *            Identifier of the remote object to read
     * @deprecated since 1.5.1. Use the none enum initializer of {@link org.openmuc.jdlms.AttributeAddress}.
     */
    @Deprecated
    public AttributeAddress(AttributeClass attributeClass, ObisCode instanceId) {
        this(attributeClass, instanceId, null);
    }

    /**
     * Creates a new attribute address.
     * 
     * @param attributeClass
     *            the AttributeClass constant.
     * @param instanceId
     *            Identifier of the remote object to read
     * @deprecated since 1.5.1. Use the none enum initializer of {@link org.openmuc.jdlms.AttributeAddress}.
     */
    @Deprecated
    public AttributeAddress(AttributeClass attributeClass, String instanceId) {
        this(attributeClass, new ObisCode(instanceId));
    }

    /**
     * Creates a new attribute address.
     * 
     * @param attributeClass
     *            the AttributeClass constant.
     * @param instanceId
     *            Identifier of the remote object to read
     * @param access
     *            The filter to apply.
     * @deprecated since 1.5.1. Use the none enum initializer of {@link org.openmuc.jdlms.AttributeAddress}.
     */
    @Deprecated
    public AttributeAddress(AttributeClass attributeClass, String instanceId, SelectiveAccessDescription access) {
        this(attributeClass, new ObisCode(instanceId), access);
    }

    public SelectiveAccessDescription getAccessSelection() {
        return accessSelection;
    }

    @Override
    CosemAttributeDescriptor toDescriptor() {
        return new CosemAttributeDescriptor(new Unsigned16(getClassId()),
                new CosemObjectInstanceId(getInstanceId().bytes()), new Integer8(getId()));
    }

    @Override
    public String toString() {
        return String.format("{\"address\": %s, \"access-descriptor\": %s}", super.toString(), this.accessSelection);
    }

}
