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
package org.openmuc.jdlms.internal.systemclasses;

import org.openmuc.jdlms.CosemAttribute;
import org.openmuc.jdlms.CosemClass;
import org.openmuc.jdlms.CosemSnInterfaceObject;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.datatypes.DataObject.Type;

@CosemClass(id = 8, version = 0)
public class Clock extends CosemSnInterfaceObject {

    @CosemAttribute(id = 2, type = Type.OCTET_STRING, snOffset = 0x08)
    private DataObject time;

    @CosemAttribute(id = 3, type = Type.LONG64, snOffset = 0x10) // TODO
    private DataObject timeZone;

    @CosemAttribute(id = 4, type = Type.UNSIGNED, snOffset = 0x18)
    private DataObject status;

    @CosemAttribute(id = 5, type = Type.OCTET_STRING, snOffset = 0x20)
    private DataObject daylightSavingBegin;

    @CosemAttribute(id = 6, type = Type.OCTET_STRING, snOffset = 0x28)
    private DataObject daylightSavingEnd;

    @CosemAttribute(id = 7, type = Type.INTEGER, snOffset = 0x30)
    private DataObject daylightSavingDevication;

    @CosemAttribute(id = 8, type = Type.BOOLEAN, snOffset = 0x38)
    private DataObject daylightSavingEnabled;

    @CosemAttribute(id = 9, type = Type.ENUMERATE, snOffset = 0x40)
    private DataObject clockBase;

    public Clock() {
        super(0x2BC0, "0.0.1.0.0.255");
    }

}
