/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.openmuc.jmbus.wireless;

import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.openmuc.jmbus.DecodingException;
import org.openmuc.jmbus.DeviceType;
import org.openmuc.jmbus.SecondaryAddress;
import org.openmuc.jmbus.Utils;
import org.openmuc.jmbus.VariableDataStructure;

public class WMbusDemoMessageTest {

    @Test
    public void testMessage1() throws DecodingException {
        // device info as bytes:6532821851582c06
        byte[] testMessage = Utils.hexStringToByteArray(
                "2C446532821851582C067AE1000000046D1906D9180C1334120000426CBF1C4C1300000000326CFFFF01FD7300");

        Map<SecondaryAddress, byte[]> keyMap = emptyMap();
        SecondaryAddress messageSecondaryAddress = decodeWMBusMessage(keyMap, testMessage);

        assertEquals("LSE", messageSecondaryAddress.getManufacturerId());
        assertEquals(58511882, messageSecondaryAddress.getDeviceId().intValue());
        assertEquals(44, messageSecondaryAddress.getVersion());
        assertEquals(DeviceType.WARM_WATER_METER, messageSecondaryAddress.getDeviceType());
    }

    @Test
    public void testMessage2() throws DecodingException {
        byte[] testMessage = Utils.hexStringToByteArray("4D4424346855471650077AA5204005CBDBC661B08F97A2030904C7F72"
                + "4F8BA4EE2AD3DF64721F0C3B96DEC142750968836B66233AE629B63C4AAC392C42E61C85179EF1453F27EDDC2E88A99"
                + "0F8AFA0000");
        byte[] key = Utils.hexStringToByteArray("A1A1A1A1A1A1A1A1A1A1A1A1A1A1A1A1");
        String secondaryAddressString = "2434685547165007";

        SecondaryAddress secondaryAddress = getSecondaryAddressFromMessage(testMessage, key, secondaryAddressString);

        assertEquals("MAD", secondaryAddress.getManufacturerId());
        assertEquals(16475568, secondaryAddress.getDeviceId().intValue());
        assertEquals(80, secondaryAddress.getVersion());
        assertEquals(DeviceType.WATER_METER, secondaryAddress.getDeviceType());
    }

    @Test
    public void testMessage3() throws DecodingException {
        byte[] testMessage = Utils.hexStringToByteArray("3644496A0228004401377232597049496A01073500202518AC74B56F"
                + "3119F53981507265B808AF7D423C429550112536BDD6F25BBB63D971");
        byte[] key = Utils.hexStringToByteArray("A1A1A1A1A1A1A1A1A1A1A1A1A1A1A1A1");
        String secondaryAddressString = "496A022800440137";

        Map<SecondaryAddress, byte[]> keyMap = getKeyMap(secondaryAddressString, key);
        SecondaryAddress secondaryAddress = decodeWMBusMessage(keyMap, testMessage);
        // SecondaryAddress secondaryAddress = getSecondaryAddressFromMessage(testMessage, key, secondaryAddressString);

        assertEquals("ZRI", secondaryAddress.getManufacturerId());
        // assertEquals(49705932, secondaryAddress.getDeviceId().intValue());
        assertEquals(01, secondaryAddress.getVersion());
        assertEquals(DeviceType.RADIO_CONVERTER_METER_SIDE, secondaryAddress.getDeviceType());
    }

    private SecondaryAddress getSecondaryAddressFromMessage(byte[] testMessage, byte[] key,
            String secondaryAddressString) throws DecodingException {
        Map<SecondaryAddress, byte[]> keyMap = getKeyMap(secondaryAddressString, key);
        SecondaryAddress messageSecondaryAddress = decodeWMBusMessage(keyMap, testMessage);
        return messageSecondaryAddress;
    }

    private Map<SecondaryAddress, byte[]> getKeyMap(String secondaryAddressString, byte[] key) {
        Map<SecondaryAddress, byte[]> keyMap = new HashMap<>();
        SecondaryAddress secondaryAddress = SecondaryAddress
                .newFromLongHeader(Utils.hexStringToByteArray(secondaryAddressString), 0);
        keyMap.put(secondaryAddress, key);
        return keyMap;
    }

    public SecondaryAddress decodeWMBusMessage(Map<SecondaryAddress, byte[]> keyMap, byte[] message)
            throws DecodingException {
        WMBusMessage decodedMessage = WMBusMessage.decode(message, 100, keyMap);
        decodedMessage.getVariableDataResponse().decode();
        VariableDataStructure variableDataResponse = decodedMessage.getVariableDataResponse();
        assertNotNull(variableDataResponse);

        System.out.println(decodedMessage.toString() + '\n');

        return decodedMessage.getSecondaryAddress();
    }

}
