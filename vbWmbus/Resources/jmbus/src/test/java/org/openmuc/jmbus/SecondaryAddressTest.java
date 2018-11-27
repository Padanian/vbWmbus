package org.openmuc.jmbus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class SecondaryAddressTest {

    @Test
    public void testHashImplementation() {
        byte[] buffer1 = new byte[] { (byte) 0xee, 0x4d, 0x49, 0x53, 0x53, 0x21, 0x16, 0x06 };
        byte[] buffer2 = new byte[] { (byte) 0xee, 0x4d, 0x48, 0x72, 0x53, 0x21, 0x16, 0x06 };

        SecondaryAddress sa1 = SecondaryAddress.newFromLongHeader(buffer1, 0);
        SecondaryAddress sa2 = SecondaryAddress.newFromLongHeader(buffer2, 0);
        SecondaryAddress sa3 = SecondaryAddress.newFromLongHeader(buffer2, 0);

        Map<SecondaryAddress, Integer> testMap = new HashMap<>();

        testMap.put(sa1, 1);
        testMap.put(sa2, 2);
        assertNotEquals(testMap.get(sa1), testMap.get(sa2));
        testMap.put(sa3, 3);
        assertEquals(testMap.get(sa2), testMap.get(sa3));
        assertNotEquals(testMap.get(sa1), testMap.get(sa2));
        assertNotEquals(testMap.get(sa1), testMap.get(sa3));
    }

}
