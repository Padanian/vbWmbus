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

abstract class ModificationParameter<T> {

    private final T address;

    private final DataObject data;

    public ModificationParameter(T address, DataObject data) {
        this.address = address;
        this.data = data;
    }

    /**
     * The new data to set.
     * 
     * @return the data.
     */
    public DataObject getData() {
        return data;
    }

    /**
     * Get the address of the element to be modified.
     * 
     * @return the address.
     */
    public T getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return String.format("{\"address\": %s, \"data\": %s}", address, data);
    }
}
