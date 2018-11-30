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
 * This class represents the the struct of parameters used to invoke the DLMS write service.
 */
public class SnWriteParameter extends ModificationParameter<SnAddressSpec> {
    private SnWriteParameter(SnAddressSpec address, DataObject data) {
        super(address, data);
    }

    /**
     * Create a new write parameter to alter the value of a COSEM attribute.
     * 
     * @param attributeAddress
     *            the address specification of the attribute.
     * @param data
     *            the data to be set.
     * @return the new write parameter.
     */
    public static SnWriteParameter newAttributeWriteParameter(SnAddressSpec attributeAddress, DataObject data) {
        return new SnWriteParameter(attributeAddress, data);
    }

    /**
     * Create a new write parameter to alter the value of a COSEM attribute.
     * 
     * @param attributeAddress
     *            the address specification of the attribute.
     * @param data
     *            the data to be set.
     * @return the new write parameter.
     */
    public static SnWriteParameter newAttributeWriteParameter(int attributeAddress, DataObject data) {
        return new SnWriteParameter(SnAddressSpec.newAttributeAddress(attributeAddress), data);
    }

    /**
     * Create a new write parameter to invoke a COSEM method with no arguments and no return value.
     * 
     * @param methodName
     *            the short name of the method.
     * @return the new write parameter.
     */
    public static SnWriteParameter newMethodWriteParameter(int methodName) {
        return newMethodWriteParameter(methodName, DataObject.newNullData());
    }

    /**
     * Create a new write parameter to invoke a COSEM method with arguments, but no return value.
     * 
     * @param methodName
     *            the short name of the method.
     * @param invocationParameter
     *            the method invocation parameter.
     * @return the new write parameter.
     */
    public static SnWriteParameter newMethodWriteParameter(int methodName, DataObject invocationParameter) {
        return new SnWriteParameter(new SnAddressSpec(methodName), invocationParameter);
    }

}
