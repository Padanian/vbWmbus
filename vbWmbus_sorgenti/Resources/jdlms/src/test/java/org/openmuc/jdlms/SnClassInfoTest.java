package org.openmuc.jdlms;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.openmuc.jdlms.datatypes.DataObject;

public class SnClassInfoTest {
    @Test
    public void test1() throws Exception {

        List<SnMemberRange> attributeRange = Arrays.asList(SnMemberRange.between(0x00, 0x18),
                SnMemberRange.between(0x28, 0x30), SnMemberRange.from(0x50, 10));

        List<SnMemberRange> mRange = Arrays.asList(SnMemberRange.is(0x20), SnMemberRange.between(0x38, 0x40));

        SnClassInfo snClassInfo = new SnClassInfo(new SnClassVersion(99, 0), attributeRange, mRange);

        assertEquals(0x00, snClassInfo.computeAttributeSnOffsetFor(1));
        assertEquals(0x08, snClassInfo.computeAttributeSnOffsetFor(2));
        assertEquals(0x10, snClassInfo.computeAttributeSnOffsetFor(3));
        assertEquals(0x18, snClassInfo.computeAttributeSnOffsetFor(4));

        assertEquals(0x20, snClassInfo.computeMethodSnOffsetFor(1));

        assertEquals(0x28, snClassInfo.computeAttributeSnOffsetFor(5));
        assertEquals(0x30, snClassInfo.computeAttributeSnOffsetFor(6));

        assertEquals(0x38, snClassInfo.computeMethodSnOffsetFor(2));
        assertEquals(0x40, snClassInfo.computeMethodSnOffsetFor(3));

        assertEquals(0x50, snClassInfo.computeAttributeSnOffsetFor(10));
        assertEquals(0x60, snClassInfo.computeAttributeSnOffsetFor(12));
        assertEquals(0x50 + 11 * 0x08, snClassInfo.computeAttributeSnOffsetFor(21));
    }

    @Test
    public void testCosemObjMapping() throws Exception {
        SnClassInfo info = (SnClassInfo) SnClassInfo.mapIcToClassInfo(O1.class).toArray()[0];

        SnClassVersion classVersion = info.getSnClassVersion();

        assertEquals(22, classVersion.getClassid());
        assertEquals(8, classVersion.getVersion());

        assertEquals(0x00, info.computeAttributeSnOffsetFor(1));
    }

    @Test
    public void testCosemObjMapping2() throws Exception {
        SnClassInfo info = (SnClassInfo) SnClassInfo.mapIcToClassInfo(O2.class).toArray()[0];

        SnClassVersion classVersion = info.getSnClassVersion();

        assertEquals(22, classVersion.getClassid());
        assertEquals(8, classVersion.getVersion());
        assertEquals(0x00, info.computeAttributeSnOffsetFor(1));
        assertEquals(0x08, info.computeAttributeSnOffsetFor(2));
        assertEquals(0x18, info.computeAttributeSnOffsetFor(3));
        assertEquals(0x20, info.computeAttributeSnOffsetFor(5));
        assertEquals(0x50, info.computeAttributeSnOffsetFor(9));
    }

    @Test
    public void testCosemObjMapping3() throws Exception {
        SnClassInfo info = (SnClassInfo) SnClassInfo.mapIcToClassInfo(O3.class).toArray()[0];

        SnClassVersion classVersion = info.getSnClassVersion();

        assertEquals(22, classVersion.getClassid());
        assertEquals(8, classVersion.getVersion());
        assertEquals(0x00, info.computeAttributeSnOffsetFor(1));
        assertEquals(0x28, info.computeAttributeSnOffsetFor(3));
        assertEquals(0x20, info.computeMethodSnOffsetFor(1));
        assertEquals(0x30, info.computeMethodSnOffsetFor(2));
        assertEquals(0x40, info.computeMethodSnOffsetFor(5));
    }

    @CosemClass(id = 22, version = 8)
    public static class O1 extends CosemSnInterfaceObject {

        public O1(int objectName, ObisCode instanceId) {
            super(objectName, instanceId);
        }

    }

    @CosemClass(id = 22, version = 8)
    public static class O2 extends CosemSnInterfaceObject {

        @CosemAttribute(id = 2, snOffset = 0x08)
        DataObject d1;
        @CosemAttribute(id = 3, snOffset = 0x18)
        DataObject d2;
        @CosemAttribute(id = 5, snOffset = 0x20)
        DataObject d3;
        @CosemAttribute(id = 9, snOffset = 0x50)
        DataObject d4;

        public O2(int objectName, ObisCode instanceId) {
            super(objectName, instanceId);
        }

    }

    @CosemClass(id = 22, version = 8)
    public static class O3 extends CosemSnInterfaceObject {

        @CosemAttribute(id = 2, snOffset = 0x08)
        DataObject d1;
        @CosemAttribute(id = 3, snOffset = 0x28)
        DataObject d2;

        public O3(int objectName, ObisCode instanceId) {
            super(objectName, instanceId);
        }

        @CosemMethod(id = 1, snOffset = 0x20)
        public void m1() {
        }

        @CosemMethod(id = 2, snOffset = 0x30)
        public void m2() {
        }

        @CosemMethod(id = 5, snOffset = 0x40)
        public void m3() {
        }

    }
}
