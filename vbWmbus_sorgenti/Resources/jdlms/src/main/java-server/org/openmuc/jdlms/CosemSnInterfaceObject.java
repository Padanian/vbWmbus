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
package org.openmuc.jdlms;

/**
 * COSEM interface object which is necessary to extend, if short naming is used.
 *
 * @see CosemInterfaceObject
 */
public abstract class CosemSnInterfaceObject extends CosemInterfaceObject {

    private final int objectName;

    /**
     * Create a new CosemSnInterfaceObject.
     * 
     * @param objectName
     *            the base name of the object. Addresses the instance ID.
     * @param instanceId
     *            the instance ID of the object.
     * @param interceptor
     *            the interceptor for this class.
     * 
     * @see CosemInterfaceObject#CosemInterfaceObject(ObisCode, DlmsInterceptor)
     */
    public CosemSnInterfaceObject(int objectName, ObisCode instanceId, DlmsInterceptor interceptor) {
        super(instanceId, interceptor);
        this.objectName = objectName;
    }

    public CosemSnInterfaceObject(int objectName, String instanceId, DlmsInterceptor interceptor) {
        this(objectName, new ObisCode(instanceId), interceptor);
    }

    /**
     * Create a new CosemSnInterfaceObject.
     * 
     * @param objectName
     *            the base name of the object. Addresses the instance ID.
     * @param instanceId
     *            the instance ID of the object.
     * 
     * @see #CosemSnInterfaceObject(int, ObisCode, DlmsInterceptor)
     */
    public CosemSnInterfaceObject(int objectName, ObisCode instanceId) {
        this(objectName, instanceId, null);
    }

    public CosemSnInterfaceObject(int objectName, String instanceId) {
        this(objectName, new ObisCode(instanceId));
    }

    /**
     * Get the short object name of the object.
     * 
     * @return the tow byte object name.
     */
    public final int getObjectName() {
        return this.objectName;
    }

}
