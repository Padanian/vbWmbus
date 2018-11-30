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

import org.openmuc.jdlms.datatypes.DataObject;

/**
 * Variable Access Specification of COSEM attributes or methods.
 */
public class SnAddressSpec {
    private final int variableName;
    private final SelectiveAccessDescription parameterizedAccess;

    /**
     * Create a new SN attribute address.
     * 
     * @param variableName
     *            the variable name of the COSEM attribute.
     * @return a new SN attribute address.
     */
    public static SnAddressSpec newAttributeAddress(int variableName) {
        return new SnAddressSpec(variableName);
    }

    /**
     * Create a new SN attribute address with parameterized access specification.
     * 
     * @param variableName
     *            the variable name of the COSEM attribute.
     * @param parameterizedAccess
     *            the parameterized access descriptor.
     * @return a new SN attribute address.
     */
    public static SnAddressSpec newAttributeAddress(int variableName, SelectiveAccessDescription parameterizedAccess) {
        return new SnAddressSpec(variableName, parameterizedAccess);
    }

    /**
     * Create a new SN method address with invocation parameter.
     * 
     * @param methodName
     *            the short method (variable) name.
     * @param invocationParameter
     *            the invocation parameter of the COSEM method.
     * @return a new SN method address.
     */
    public static SnAddressSpec newMethodAddress(int methodName, DataObject invocationParameter) {
        return new SnAddressSpec(methodName, new SelectiveAccessDescription(0, invocationParameter));
    }

    /**
     * Create a new SN method address without method parameter.
     * 
     * @param methodName
     *            the short method (variable) name.
     * @return a new SN method address.
     */
    public static SnAddressSpec newMethodAddress(int methodName) {
        return newMethodAddress(methodName, DataObject.newNullData());
    }

    SnAddressSpec(int variableName) {
        this(variableName, null);
    }

    SnAddressSpec(int variableName, SelectiveAccessDescription parameterizedAccess) {
        this.variableName = variableName;
        this.parameterizedAccess = parameterizedAccess;
    }

    /**
     * Get the variable name.
     * 
     * @return the two byte variable name.
     */
    public int getVariableName() {
        return this.variableName;
    }

    /**
     * Get the parameterized access descriptor.
     * 
     * @return the parameterized access descriptor.
     */
    public SelectiveAccessDescription getParameterizedAccessDescriptor() {
        return this.parameterizedAccess;
    }

    @Override
    public String toString() {
        return String.format("{\"variable-name\": \"0x%02X\", \"parameterized-access\": %s}", this.variableName,
                this.parameterizedAccess);
    }

}
