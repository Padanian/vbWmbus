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
package org.openmuc.jdlms.itest.ln;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.openmuc.jdlms.AccessResultCode.SUCCESS;
import static org.openmuc.jdlms.AuthenticationMechanism.HLS5_GMAC;
import static org.openmuc.jdlms.SecuritySuite.EncryptionMechanism.AES_GCM_128;
import static org.openmuc.jdlms.datatypes.DataObject.newArrayData;
import static org.openmuc.jdlms.datatypes.DataObject.newUInteger16Data;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openmuc.jdlms.AccessResultCode;
import org.openmuc.jdlms.AttributeAddress;
import org.openmuc.jdlms.AuthenticationMechanism;
import org.openmuc.jdlms.DlmsConnection;
import org.openmuc.jdlms.DlmsServer;
import org.openmuc.jdlms.GetResult;
import org.openmuc.jdlms.LogicalDevice;
import org.openmuc.jdlms.MethodParameter;
import org.openmuc.jdlms.MethodResult;
import org.openmuc.jdlms.MethodResultCode;
import org.openmuc.jdlms.SecuritySuite;
import org.openmuc.jdlms.SecuritySuite.EncryptionMechanism;
import org.openmuc.jdlms.SecurityUtils;
import org.openmuc.jdlms.SecurityUtils.KeyId;
import org.openmuc.jdlms.SelectiveAccessDescription;
import org.openmuc.jdlms.TcpConnectionBuilder;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.internal.WellKnownInstanceIds;

public class ClientServerLnTest {
    private static final byte[] AUTHENTICATION_KEY = SecurityUtils.generateAES128Key();

    private static final byte[] MASTER_KEY = SecurityUtils.generateAES128Key();

    private static final SecuritySuite AUTHENTICATION_S;

    private DlmsServer server;

    private LogicalDevice logicalDevice1;

    static {
        byte[] globalEncryptionKey = SecurityUtils.generateAES128Key();
        AUTHENTICATION_S = SecuritySuite.builder()
                .setAuthenticationKey(AUTHENTICATION_KEY)
                .setGlobalUnicastEncryptionKey(globalEncryptionKey)
                .setAuthenticationMechanism(AuthenticationMechanism.HLS5_GMAC)
                .setEncryptionMechanism(EncryptionMechanism.AES_GCM_128)
                .build();
    }

    @Before
    public void setup() throws Exception {
        logicalDevice1 = new LogicalDevice(1, "LDI", "ISE", 9999L);
        logicalDevice1.setMasterKey(MASTER_KEY);
        logicalDevice1.addRestriction(16, AUTHENTICATION_S);

        logicalDevice1.registerCosemObject(new TestCosemClass(null));

        server = DlmsServer.tcpServerBuilder().registerLogicalDevice(logicalDevice1).build();
    }

    @After
    public void tearDown() throws Exception {
        server.close();
    }

    @Test
    public void test() throws Exception {
        SecuritySuite clienSec = SecuritySuite.newSecuritySuiteFrom(AUTHENTICATION_S);
        TcpConnectionBuilder connectionBuilder = new TcpConnectionBuilder(InetAddress.getLocalHost())
                .setLogicalDeviceId(1)
                .setClientId(16)
                .setSecuritySuite(clienSec)
                .setSystemTitle("ISE", new Random().nextLong());

        try (DlmsConnection client = connectionBuilder.build()) {
            GetResult r = client.get(new AttributeAddress(99, "0.0.0.2.1.255", 3));
            float f = r.getResultData().getValue();
            assertEquals(99f, f, .01f);

            GetResult rd = client.get(new AttributeAddress(99, "0.0.0.2.1.255", 4));
            double d = rd.getResultData().getValue();
            assertEquals(1620d, d, .01d);

            client.close();
        }
    }

    @Test(timeout = 1000)
    public void test1() throws Exception {

        SecuritySuite clienSec = SecuritySuite.newSecuritySuiteFrom(AUTHENTICATION_S);
        TcpConnectionBuilder connectionBuilder = new TcpConnectionBuilder(InetAddress.getLocalHost())
                .setLogicalDeviceId(1)
                .setClientId(16)
                .setSecuritySuite(clienSec)
                .setSystemTitle("ISE", new Random().nextLong());

        DlmsConnection client = connectionBuilder.build();

        List<DataObject> reqList = Arrays.asList(newUInteger16Data(15), // 15 = ASSOCIATION_LN
                newUInteger16Data(17)); // 17 = SAP_ASSIGNMENT
        SelectiveAccessDescription access = new SelectiveAccessDescription(2, newArrayData(reqList));

        GetResult objListResult = client
                .get(new AttributeAddress(15, WellKnownInstanceIds.CURRENT_ASSOCIATION_ID, 2, access)); // IC_AssociationLN#OBJECT_LIST

        assertEquals(SUCCESS, objListResult.getResultCode());

        List<DataObject> resList = objListResult.getResultData().getValue();
        assertEquals(reqList.size(), resList.size());

        final byte[] newEncKey = SecurityUtils.generateAES128Key();

        MethodParameter keyChangeMethodParam = SecurityUtils.keyChangeMethodParamFor(MASTER_KEY, newEncKey,
                KeyId.GLOBAL_UNICAST_ENCRYPTION_KEY);

        assertNotEquals(newEncKey, logicalDevice1.getRestrictions().get(16).getGlobalUnicastEncryptionKey());

        MethodResult methodResult = client.action(keyChangeMethodParam);

        assertEquals(MethodResultCode.SUCCESS, methodResult.getResultCode());

        client.changeClientGlobalEncryptionKey(newEncKey);

        assertArrayEquals("Failed to set the encryption key.", newEncKey,
                logicalDevice1.getRestrictions().get(16).getGlobalUnicastEncryptionKey());

        AccessResultCode resultCode = client.get(new AttributeAddress(15, "0.0.40.0.0.255", 2)).getResultCode(); // IC_AssociationLN#OBJECT_LIST
        assertEquals("Encryption does not work..", SUCCESS, resultCode);

        client.close();

        SecuritySuite newAuth = SecuritySuite.builder()
                .setAuthenticationKey(AUTHENTICATION_KEY)
                .setGlobalUnicastEncryptionKey(newEncKey)
                .setAuthenticationMechanism(HLS5_GMAC)
                .setEncryptionMechanism(AES_GCM_128)
                .build();

        connectionBuilder.setSecuritySuite(newAuth);
        try (DlmsConnection conn2 = connectionBuilder.build()) {
            MethodResult result = conn2.action(new MethodParameter(TestCosemClass.CLASS_ID, TestCosemClass.ID, 1));
            assertEquals(MethodResultCode.SUCCESS, result.getResultCode());

            AttributeAddress attributeAddress = new AttributeAddress(TestCosemClass.CLASS_ID, TestCosemClass.ID, 2);
            GetResult getResult = conn2.get(attributeAddress);

            assertEquals(SUCCESS, getResult.getResultCode());

            byte[] resultData = getResult.getResultData().getValue();
            assertArrayEquals(TestCosemClass.D1_DATA, resultData);

            conn2.close();
        }

    }

    @Test(timeout = 1000)
    public void testWrongEncryptionChangeKey() throws Exception {
        SecuritySuite clienSec = SecuritySuite.newSecuritySuiteFrom(AUTHENTICATION_S);
        TcpConnectionBuilder connectionBuilder = new TcpConnectionBuilder(InetAddress.getLocalHost())
                .setLogicalDeviceId(1)
                .setClientId(16)
                .setSecuritySuite(clienSec)
                .setSystemTitle("ISE", new Random().nextLong());

        DlmsConnection client = connectionBuilder.build();

        byte[] newEncKey = SecurityUtils.generateAES128Key();
        byte[] masterKey = generateWrongMasterKey();

        MethodParameter keyChangeMethodParam = SecurityUtils.keyChangeMethodParamFor(masterKey, newEncKey,
                KeyId.GLOBAL_UNICAST_ENCRYPTION_KEY);

        assertNotEquals(newEncKey, logicalDevice1.getRestrictions().get(16).getGlobalUnicastEncryptionKey());

        MethodResult methodResult = client.action(keyChangeMethodParam);

        assertNotEquals(MethodResultCode.SUCCESS, methodResult.getResultCode());

        assertArrayEquals("Have changed, this should not hava happended.", clienSec.getGlobalUnicastEncryptionKey(),
                logicalDevice1.getRestrictions().get(16).getGlobalUnicastEncryptionKey());

        client.close();
    }

    private byte[] generateWrongMasterKey() {
        byte[] masterKey;
        do {
            masterKey = SecurityUtils.generateAES128Key();
        } while (Arrays.equals(masterKey, MASTER_KEY));
        return masterKey;
    }

}
