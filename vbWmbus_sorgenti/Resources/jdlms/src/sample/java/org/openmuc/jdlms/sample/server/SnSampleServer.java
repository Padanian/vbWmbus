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

import static org.openmuc.jdlms.datatypes.DataObject.newInteger16Data;
import static org.openmuc.jdlms.datatypes.DataObject.newOctetStringData;
import static org.openmuc.jdlms.sessionlayer.server.ServerSessionLayerFactories.newHdlcSessionLayerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.openmuc.jdlms.AccessResultCode;
import org.openmuc.jdlms.AttributeAddress;
import org.openmuc.jdlms.DlmsConnection;
import org.openmuc.jdlms.DlmsServer;
import org.openmuc.jdlms.DlmsServer.TcpServerBuilder;
import org.openmuc.jdlms.GetResult;
import org.openmuc.jdlms.LogicalDevice;
import org.openmuc.jdlms.MethodParameter;
import org.openmuc.jdlms.MethodResult;
import org.openmuc.jdlms.MethodResultCode;
import org.openmuc.jdlms.RawMessageData;
import org.openmuc.jdlms.RawMessageListener;
import org.openmuc.jdlms.SelectiveAccessDescription;
import org.openmuc.jdlms.SetParameter;
import org.openmuc.jdlms.SnClassInfo;
import org.openmuc.jdlms.SnMemberRange;
import org.openmuc.jdlms.TcpConnectionBuilder;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.settings.client.ReferencingMethod;

public class SnSampleServer {

    public static void main(String[] args) throws IOException {

        int port = 9999;
        ReferencingMethod refMethod = ReferencingMethod.SHORT;

        String manufacturerId = "ISE";
        long deviceId = 12;
        LogicalDevice logicalDevice = new LogicalDevice(1, "ise", manufacturerId, deviceId);
        logicalDevice.registerCosemObject(new SnSampleClass());

        TcpServerBuilder serverBuilder = DlmsServer.tcpServerBuilder(port)
                .setReferencingMethod(refMethod)
                .registerLogicalDevice(logicalDevice)
                .setSessionLayerFactory(newHdlcSessionLayerFactory());

        RawMessageListener rawMessageListener = new RawMessageListener() {

            @Override
            public void messageCaptured(RawMessageData rawMessageData) {
                // TODO do something with the data..
            }
        };
        Collection<SnClassInfo> snClassInfos = Arrays.asList(new SnClassInfo(99, 12, SnMemberRange.from(0x30)));
        TcpConnectionBuilder connectionBuiler = new TcpConnectionBuilder("localhost").setPort(port)
                .useHdlc()
                .setReferencingMethod(refMethod)
                .setSnClassInfo(snClassInfos)
                .setRawMessageListener(rawMessageListener);

        try (DlmsServer dlmsServer = serverBuilder.build()) {

            try (DlmsConnection connection = connectionBuiler.build()) {

                DataObject accessParameter = newInteger16Data((short) 0xFB00);
                SelectiveAccessDescription access = new SelectiveAccessDescription(3, accessParameter);
                // IC_AssociationSn#OBJECT_LIST
                AttributeAddress address = new AttributeAddress(12, "0.0.40.0.0.255", 2, access);
                GetResult getResult = connection.get(address);

                if (getResult.getResultCode() == AccessResultCode.SUCCESS) {
                    System.out.println(getResult.getResultData());
                }
                else {
                    System.out.println("Call failed");
                    System.out.println(getResult.getResultCode());
                }

                MethodParameter methodParameter = new MethodParameter(99, SnSampleClass.INSTANCE_ID, 3);

                MethodResult methodRes = connection.action(methodParameter);

                if (methodRes.getResultCode() == MethodResultCode.SUCCESS) {
                    byte[] value = methodRes.getResultData().getValue();
                    System.out.println(new String(value));
                }
                else {
                    System.out.println(methodRes.getResultCode());
                }

                AttributeAddress attributeAddress = new AttributeAddress(99, SnSampleClass.INSTANCE_ID, 2);
                GetResult getResult2 = connection.get(attributeAddress);

                if (getResult2.getResultCode() == AccessResultCode.SUCCESS) {
                    Number val = getResult2.getResultData().getValue();
                    System.out.println(val.doubleValue());
                }
                else {
                    System.out.println("huch: " + getResult2.getResultCode());
                }

                byte[] string = new byte[10000];
                string[0] = 1;
                string[string.length - 1] = 1;
                DataObject data = newOctetStringData(string);

                SetParameter setParameter = new SetParameter(attributeAddress, data);
                AccessResultCode setResult = connection.set(setParameter);

                System.out.println(setResult);
            }

        }

    }
}
