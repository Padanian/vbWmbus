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

import static javax.xml.bind.DatatypeConverter.parseHexBinary;
import static javax.xml.bind.DatatypeConverter.printHexBinary;
import static org.openmuc.jdlms.SecuritySuite.newSecuritySuiteFrom;
import static org.openmuc.jdlms.datatypes.DataObject.newUInteger16Data;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;

import org.openmuc.jdlms.AccessResultCode;
import org.openmuc.jdlms.AttributeAddress;
import org.openmuc.jdlms.AuthenticationMechanism;
import org.openmuc.jdlms.DlmsAccessException;
import org.openmuc.jdlms.DlmsConnection;
import org.openmuc.jdlms.DlmsInterceptor;
import org.openmuc.jdlms.DlmsInvocationContext;
import org.openmuc.jdlms.DlmsServer;
import org.openmuc.jdlms.GetResult;
import org.openmuc.jdlms.LogicalDevice;
import org.openmuc.jdlms.MethodParameter;
import org.openmuc.jdlms.MethodResult;
import org.openmuc.jdlms.SecuritySuite;
import org.openmuc.jdlms.SecuritySuite.EncryptionMechanism;
import org.openmuc.jdlms.SecurityUtils;
import org.openmuc.jdlms.SecurityUtils.KeyId;
import org.openmuc.jdlms.SelectiveAccessDescription;
import org.openmuc.jdlms.TcpConnectionBuilder;
import org.openmuc.jdlms.datatypes.DataObject;

public class SampleClientServer {
    private static final byte[] AUTHENTICATION_KEY = parseHexBinary("5468697349734150617373776f726431");

    private static final byte[] GLOBAL_ENCRYPTION_KEY = parseHexBinary("000102030405060708090a0b0c0d0e0f");
    private static final byte[] MASTER_KEY = parseHexBinary("aa0102030405060738090a0b0c0d0eff");

    private static final int PORT = 6789;
    private static final String MANUFACTURE_ID = "ISE";
    private static final long DEVICE_ID = 9999L;
    private static final String LOGICAL_DEVICE_ID = "L_D_I";

    private static final SecuritySuite AUTHENTICATION_C = SecuritySuite.builder()
            .setAuthenticationKey(AUTHENTICATION_KEY)
            .setGlobalUnicastEncryptionKey(GLOBAL_ENCRYPTION_KEY)
            .setAuthenticationMechanism(AuthenticationMechanism.HLS5_GMAC)
            .setEncryptionMechanism(EncryptionMechanism.AES_GCM_128)
            .build();

    private static final SecuritySuite AUTHENTICATION_S = newSecuritySuiteFrom(AUTHENTICATION_C);

    public static void main(String[] args) throws IOException {
        DlmsServer serverConnection;

        try {
            printServer("starting");
            LogicalDevice logicalDevice = new LogicalDevice(1, LOGICAL_DEVICE_ID, "HMM", DEVICE_ID);
            logicalDevice.setMasterKey(MASTER_KEY);
            logicalDevice.addRestriction(16, AUTHENTICATION_S);
            logicalDevice.registerCosemObject(new SampleClass(new DlmsInterceptor() {

                @Override
                public DataObject intercept(DlmsInvocationContext ctx) throws DlmsAccessException {
                    printServer("--------------------------------------");
                    printServer(ctx.getXDlmsServiceType());
                    printServer(ctx.getCosemResourceDescriptor().getClassId());
                    printServer(ctx.getCosemResourceDescriptor().getInstanceId());
                    printServer(ctx.getCosemResourceDescriptor().getId());
                    printServer(ctx.getSecurityPolicy());
                    printServer("--------------------------------------");

                    return ctx.proceed();
                }
            }));

            serverConnection = DlmsServer.tcpServerBuilder(PORT).registerLogicalDevice(logicalDevice).build();

            printServer("started");
        } catch (IOException e) {
            throw new IOException("DemoServer: " + e);
        }

        try {
            runDemoClient();

        } finally {
            if (serverConnection != null) {
                serverConnection.close();
            }
            printServer("closed");
        }

    }

    static void runDemoClient() throws IOException {
        TcpConnectionBuilder connectionBuilder = new TcpConnectionBuilder(InetAddress.getLocalHost())
                .setLogicalDeviceId(1)
                .setPort(PORT)
                .setSecuritySuite(AUTHENTICATION_C)
                .setSystemTitle(MANUFACTURE_ID, DEVICE_ID);

        printClient("connecting to server");

        DlmsConnection client = connectionBuilder.build();
        printClient("connected");

        int accessSelector = 2;
        DataObject accessParameter = DataObject.newArrayData(Arrays.asList(newUInteger16Data(15), // ASSOCIATION_LN
                newUInteger16Data(17))); // SAP_ASSIGNMENT
        SelectiveAccessDescription access = new SelectiveAccessDescription(accessSelector, accessParameter);
        // IC_AssociationLn#OBJECT_LIST;
        GetResult getResult2 = client.get(new AttributeAddress(15, "0.0.40.0.0.255", 2, access));

        if (getResult2.requestSuccessful()) {
            System.out.println(getResult2.getResultData());
        }

        byte[] newKey = generateNewKey();

        printClient("new enckey " + printHexBinary(newKey));
        MethodParameter keyChangeMethodParam = SecurityUtils.keyChangeMethodParamFor(MASTER_KEY, newKey,
                KeyId.GLOBAL_UNICAST_ENCRYPTION_KEY);

        MethodResult methodResult = client.action(keyChangeMethodParam);
        printClient("Change key: " + methodResult.getResultCode());

        client.changeClientGlobalEncryptionKey(newKey);

        // IC_AssociationLn#OBJECT_LIST
        AccessResultCode resultCode = client.get(new AttributeAddress(15, "0.0.40.0.0.255", 2)).getResultCode();
        printClient("Req after change key: " + resultCode);

        client.disconnect();

        SecuritySuite newAuth = SecuritySuite.builder()
                .setAuthenticationKey(AUTHENTICATION_KEY)
                .setGlobalUnicastEncryptionKey(newKey)
                .setAuthenticationMechanism(AuthenticationMechanism.HLS5_GMAC)
                .setEncryptionMechanism(EncryptionMechanism.AES_GCM_128)
                .build();
        connectionBuilder.setSecuritySuite(newAuth);
        try (DlmsConnection con = connectionBuilder.build()) {
            MethodResult result = con.action(new MethodParameter(99, "0.0.0.2.1.255", 1));
            printClient(result.getResultCode());

            AttributeAddress attributeAddress = new AttributeAddress(99, "0.0.0.2.1.255", 2);
            GetResult getResult = con.get(attributeAddress);
            DataObject resultData = getResult.getResultData();
            printClient("--------------------------------");
            printClient(resultData);
            printClient("--------------------------------");

        }

        printClient("closed");
    }

    private static byte[] generateNewKey() {
        byte[] newKey = GLOBAL_ENCRYPTION_KEY.clone();
        for (int i = 0; i < newKey.length; i++) {
            newKey[i] = (byte) (newKey[i] ^ 42);
        }
        return newKey;
    }

    private static void printClient(Object message) {
        System.out.println("DemoClient: " + message);
    }

    private static void printServer(Object message) {
        System.out.println("DemoServer: " + message);
    }
}
