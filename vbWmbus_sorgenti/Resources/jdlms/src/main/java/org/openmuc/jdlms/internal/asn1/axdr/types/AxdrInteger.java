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
package org.openmuc.jdlms.internal.asn1.axdr.types;

import java.io.IOException;
import java.io.InputStream;

import org.openmuc.jasn1.ber.ReverseByteArrayOutputStream;
import org.openmuc.jdlms.internal.asn1.axdr.AxdrType;

public class AxdrInteger implements AxdrType {
    private final boolean lengthFixed;
    private final boolean unsigned;

    private long val = 0;

    private Long maxVal = null;

    private Long minVal = null;

    protected byte[] code = null;

    public AxdrInteger() {
        this.lengthFixed = false;
        this.unsigned = false;
    }

    public AxdrInteger(long val) {
        this();
        setValue(val);
    }

    public AxdrInteger(byte[] code) {
        this();
        this.code = code;
    }

    protected AxdrInteger(long min, long max, long val) {
        minVal = min;
        maxVal = max;
        setValue(val);
        lengthFixed = true;
        unsigned = (min >= 0);
    }

    @Override
    public int encode(ReverseByteArrayOutputStream axdrOStream) throws IOException {
        if (code != null) {
            axdrOStream.write(code);
            return code.length;
        }
        else {
            return encodeValue(axdrOStream);
        }
    }

    private int encodeValue(ReverseByteArrayOutputStream axdrOStream) throws IOException {
        if (lengthFixed) {
            int codeLength = numOfBytesOf(minVal, maxVal);

            writyValToStream(axdrOStream, codeLength);

            return codeLength;
        }
        else {
            int codeLength = numOfBytesOf(val);

            writyValToStream(axdrOStream, codeLength);

            byte addCodeLengthTag = (byte) ((codeLength & 0xff) | 0x80);
            axdrOStream.write(addCodeLengthTag);
            return codeLength + 1;
        }
    }

    private void writyValToStream(ReverseByteArrayOutputStream axdrOStream, int codeLength) throws IOException {
        for (int i = 0; i < codeLength; i++) {
            int value = ((int) (val >> 8 * (i))) & 0xff;
            axdrOStream.write(value);
        }
    }

    @Override
    public int decode(InputStream iStream) throws IOException {
        int codeLength = 0;
        byte[] byteCode;
        byte length = 0;

        if (lengthFixed) {
            length = (byte) numOfBytesOf(minVal, maxVal);
            codeLength = length;
        }
        else {
            length = (byte) iStream.read();

            if ((length & 0x80) == 0x80) {
                length = (byte) (length ^ 0x80);
                codeLength = length + 1;
            }
            else {
                val = length;
                return 1;
            }
        }

        byteCode = new byte[length];
        Util.readFully(iStream, byteCode);

        if ((byteCode[0] & 0x80) == 0x80 && !unsigned) {
            val = -1;
            for (int i = 0; i < length; i++) {
                int numShiftBits = 8 * (length - i - 1);
                val &= ((byteCode[i]) << numShiftBits) | ~(0xff << numShiftBits);
            }

        }
        else {
            val = 0;
            for (int i = 0; i < length; i++) {
                val |= (long) (byteCode[i] & 0xff) << (8 * (length - i - 1));
            }
        }

        return codeLength;
    }

    public void encodeAndSave(int encodingSizeGuess) throws IOException {
        ReverseByteArrayOutputStream revOStream = new ReverseByteArrayOutputStream(encodingSizeGuess);
        encode(revOStream);
        code = revOStream.getArray();
    }

    private static int numOfBytesOf(long minVal, long maxVal) {
        return Math.max(numOfBytesOf(minVal), numOfBytesOf(maxVal));
    }

    private static int numOfBytesOf(long val) {
        long sVal = val;
        if (val < 0) {
            sVal = -val - 1L;
        }

        return (int) Math.ceil((Math.floor(log2(sVal) + 1) / 8D));
    }

    private static double log2(long sVal) {
        return Math.log(sVal) / Math.log(2);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof AxdrInteger)) {
            return false;
        }

        AxdrInteger other = (AxdrInteger) o;
        return other.val == val && other.maxVal.equals(maxVal) && other.minVal.equals(minVal);

    }

    @Override
    public int hashCode() {
        if (lengthFixed) {
            return minVal.hashCode() ^ maxVal.hashCode() ^ (int) val;
        }
        return (int) val;
    }

    public Long getMax() {
        return maxVal;
    }

    public Long getMin() {
        return minVal;
    }

    public long getValue() {
        return val;
    }

    public void setValue(long newVal) {
        if (minVal != null && minVal > newVal) {
            throw new IllegalArgumentException("Value " + newVal + " is smaller than minimum " + minVal);
        }
        if (maxVal != null && maxVal < newVal) {
            throw new IllegalArgumentException("Value " + newVal + " is greater than maximum " + maxVal);
        }

        val = newVal;
    }

    @Override
    public String toString() {
        return Long.toString(val);
    }

}
