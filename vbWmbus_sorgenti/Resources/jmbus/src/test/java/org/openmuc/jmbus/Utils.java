package org.openmuc.jmbus;

public class Utils {

    /**
     * Transfers a string with hex encoded values to a byte array
     * 
     * @param hexData
     *            data to convert empty spaces will be stripped
     * @return byte array out of the data
     */
    public static byte[] hexStringToByteArray(String hexData) {
        hexData = hexData.replaceAll(" ", "");
        int len = hexData.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexData.charAt(i), 16) << 4)
                    + Character.digit(hexData.charAt(i + 1), 16));
        }
        return data;
    }

    private Utils() {
        // hide this
    }
}
