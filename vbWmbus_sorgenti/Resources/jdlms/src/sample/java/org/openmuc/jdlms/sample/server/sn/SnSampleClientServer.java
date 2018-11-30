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
package org.openmuc.jdlms.sample.server.sn;

import java.io.IOException;

import org.openmuc.jdlms.CosemAttribute;
import org.openmuc.jdlms.CosemClass;
import org.openmuc.jdlms.CosemMethod;
import org.openmuc.jdlms.CosemSnInterfaceObject;
import org.openmuc.jdlms.DlmsServer;
import org.openmuc.jdlms.DlmsSnConnection;
import org.openmuc.jdlms.LogicalDevice;
import org.openmuc.jdlms.ObisCode;
import org.openmuc.jdlms.SnAddressSpec;
import org.openmuc.jdlms.TcpConnectionBuilder;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.settings.client.ReferencingMethod;

/**
 * This class demonstrates a jdlms SN server connected via the SN service jdlms client.
 */
public class SnSampleClientServer {

    public static void main(String[] args) throws IOException {

        int logicalDeviceId = 12;
        LogicalDevice logicalDevice = new LogicalDevice(logicalDeviceId, "AwesomeLD", "ISE", 12345);
        logicalDevice.registerCosemObject(new CosemO1());
        int port = 7777;
        DlmsServer server = DlmsServer.tcpServerBuilder(port)
                .setReferencingMethod(ReferencingMethod.SHORT)
                .registerLogicalDevice(logicalDevice)
                .build();

        DlmsSnConnection client = new TcpConnectionBuilder("127.0.0.1").setLogicalDeviceId(logicalDeviceId)
                .setPort(port)
                .buildSnConnection();

        byte[] instanceId = client.read(SnAddressSpec.newAttributeAddress(0xAA00)).getResultData().getValue();
        System.out.println(new ObisCode(instanceId));

        System.out.println(client.read(SnAddressSpec.newAttributeAddress(0xAA40)));
        System.out.println(client.read(SnAddressSpec.newMethodAddress(0xAA20)));

        client.close();
        server.close();
    }

    @CosemClass(id = 99, version = 0)
    public static class CosemO1 extends CosemSnInterfaceObject {

        private static final String INSTANCE_ID = "0.0.0.0.99.255";
        @CosemAttribute(id = 5, snOffset = 0x38)
        private DataObject do2 = DataObject.newVisibleStringData("Hello World D5".getBytes());
        @CosemAttribute(id = 6, snOffset = 0x40)
        private DataObject do1 = DataObject.newVisibleStringData("Hello World D6".getBytes());
        @CosemAttribute(id = 7, snOffset = 0x50)
        private DataObject do4 = DataObject.newVisibleStringData("Hello World D6".getBytes());

        public CosemO1() {
            super(0xAA00, INSTANCE_ID);
        }

        @CosemMethod(id = 1, snOffset = 0x20)
        public DataObject m1() {
            return DataObject.newVisibleStringData("Hello World from method".getBytes());
        }

    }
}
