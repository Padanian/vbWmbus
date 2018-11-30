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
package org.openmuc.jdlms.sample.server;

import org.openmuc.jdlms.CosemAttribute;
import org.openmuc.jdlms.CosemClass;
import org.openmuc.jdlms.CosemInterfaceObject;
import org.openmuc.jdlms.CosemMethod;
import org.openmuc.jdlms.DlmsInterceptor;
import org.openmuc.jdlms.IllegalMethodAccessException;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.datatypes.DataObject.Type;

// tag::todoc[]
@CosemClass(id = 1, version = 0)
public class Data extends CosemInterfaceObject {

    @CosemAttribute(id = 2, type = Type.LONG64)
    private DataObject value;

    public Data(DlmsInterceptor interceptor) {
        super("0.0.0.2.1.255", interceptor);

        this.value = DataObject.newInteger64Data(864972689331191808L);
    }

    public DataObject getValue() {
        return value;
    }

    @CosemMethod(id = 1)
    public void operate() throws IllegalMethodAccessException {
        // implement this
    }

}
// end::todoc[]
