package org.openmuc.jdlms.itest.sn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.openmuc.jdlms.settings.client.ReferencingMethod.SHORT;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openmuc.jdlms.AccessResultCode;
import org.openmuc.jdlms.AttributeAddress;
import org.openmuc.jdlms.CosemAttribute;
import org.openmuc.jdlms.CosemClass;
import org.openmuc.jdlms.CosemMethod;
import org.openmuc.jdlms.CosemSnInterfaceObject;
import org.openmuc.jdlms.DlmsConnection;
import org.openmuc.jdlms.DlmsServer;
import org.openmuc.jdlms.GetResult;
import org.openmuc.jdlms.LogicalDevice;
import org.openmuc.jdlms.MethodParameter;
import org.openmuc.jdlms.MethodResult;
import org.openmuc.jdlms.MethodResultCode;
import org.openmuc.jdlms.ObisCode;
import org.openmuc.jdlms.SetParameter;
import org.openmuc.jdlms.SnClassInfo;
import org.openmuc.jdlms.SnMemberRange;
import org.openmuc.jdlms.TcpConnectionBuilder;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.internal.WellKnownInstanceIds;

public class ClientServerSnTest {
    private static final int TCP_PORT = 9999;
    private DlmsServer dlmsServer;
    private DemoClass demoClass;

    @Before
    public void setup() throws Exception {
        this.demoClass = new DemoClass();

        LogicalDevice ld = new LogicalDevice(1, "LD1", "ABC", 99);
        ld.registerCosemObject(this.demoClass);
        this.dlmsServer = DlmsServer.tcpServerBuilder(TCP_PORT)
                .setReferencingMethod(SHORT)
                .registerLogicalDevice(ld)
                .build();
    }

    @After
    public void shutdown() throws Exception {
        this.dlmsServer.shutdown();
    }

    @Test
    public void test_GET_read() throws Exception {
        TcpConnectionBuilder connectionBuilder = setupConnectionParams();
        try (DlmsConnection dlmsConnection = connectionBuilder.build()) {
            GetResult result = dlmsConnection.get(new AttributeAddress(17, "0.0.40.0.0.255", 2));
            assertTrue("Get request was not successful.", result.requestSuccessful());
        }
    }

    @Test
    public void test_SET_write() throws Exception {
        TcpConnectionBuilder connectionBuilder = setupConnectionParams();
        try (DlmsConnection dlmsConnection = connectionBuilder.build()) {
            assertEquals(this.demoClass.dataObject.getRawValue(), -1);
            int value = 99;
            AccessResultCode resultCode = dlmsConnection.set(new SetParameter(
                    new AttributeAddress(99, DemoClass.INSTANCE_ID, 2), DataObject.newInteger32Data(value)));
            assertEquals(AccessResultCode.SUCCESS, resultCode);
            assertEquals(this.demoClass.dataObject.getRawValue(), value);
        }
    }

    @Test
    public void test_ACTION() throws Exception {
        List<SnClassInfo> snClassInfos = Arrays.asList(new SnClassInfo(99, 0, SnMemberRange.from(0x20)));

        TcpConnectionBuilder connectionBuilder = setupConnectionParams().setSnClassInfo(snClassInfos);

        try (DlmsConnection dlmsConnection = connectionBuilder.build()) {
            MethodResult result = dlmsConnection.action(new MethodParameter(99, DemoClass.INSTANCE_ID, 1));
            assertEquals(MethodResultCode.SUCCESS, result.getResultCode());
            assertEquals(1, this.demoClass.callCounter);

            DataObject classId = DataObject.newUInteger16Data(12);
            DataObject instanceId = DataObject
                    .newOctetStringData(new ObisCode(WellKnownInstanceIds.CURRENT_ASSOCIATION_ID).bytes());
            DataObject attributeId = DataObject.newInteger8Data((byte) 1);

            DataObject dO = DataObject
                    .newArrayData(Arrays.asList(DataObject.newStructureData(classId, instanceId, attributeId)));
            MethodResult result2 = dlmsConnection
                    .action(new MethodParameter(12, WellKnownInstanceIds.CURRENT_ASSOCIATION_ID, 3, dO));

            System.out.println(result2.getResultCode());
            if (result2.getResultCode() == MethodResultCode.SUCCESS) {
                LinkedList<DataObject> list = result2.getResultData().getValue();

                for (DataObject dataObject : list) {
                    byte[] value = dataObject.getValue();
                    System.out.println(new ObisCode(value).asDecimalString());
                }
            }
        }

    }

    private static TcpConnectionBuilder setupConnectionParams() throws UnknownHostException {
        return new TcpConnectionBuilder("127.0.0.1").setPort(TCP_PORT).setReferencingMethod(SHORT);
    }

    @CosemClass(id = 99)
    public static class DemoClass extends CosemSnInterfaceObject {

        private static final String INSTANCE_ID = "99.0.10.0.0.255";

        @CosemAttribute(id = 2, type = DataObject.Type.DOUBLE_LONG, snOffset = 0x08)
        private DataObject dataObject;

        private int callCounter;

        public DemoClass() {
            super(0x820, INSTANCE_ID);
            this.dataObject = DataObject.newInteger32Data(-1);
            this.callCounter = 0;
        }

        @CosemMethod(id = 1, snOffset = 0x20)
        public void m1() {
            callCounter++;
        }

    }
}
