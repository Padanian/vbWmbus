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

public class AxdrBoolean implements AxdrType {

    private boolean val;

    private byte[] code;

    public AxdrBoolean() {
    }

    public AxdrBoolean(boolean val) {
        this.val = val;
    }

    public AxdrBoolean(byte[] code) {
        this.code = code;
    }

    @Override
    public int encode(ReverseByteArrayOutputStream axdrOStream) throws IOException {

        if (code != null) {
            axdrOStream.write(code);
        }
        else if (val) {
            axdrOStream.write((byte) 0x01);
        }
        else {
            axdrOStream.write((byte) 0x00);
        }

        return 1;
    }

    @Override
    public int decode(InputStream iStream) throws IOException {
        if (iStream.available() == 0) {
            return 0;
        }

        val = iStream.read() != 0x00;

        return 1;
    }

    public void encodeAndSave(int encodingSizeGuess) throws IOException {
        ReverseByteArrayOutputStream revOStream = new ReverseByteArrayOutputStream(encodingSizeGuess);
        encode(revOStream);
        code = revOStream.getArray();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof AxdrBoolean)) {
            return false;
        }

        AxdrBoolean other = (AxdrBoolean) o;
        return other.val == val;
    }

    @Override
    public int hashCode() {
        return Boolean.valueOf(val).hashCode();
    }

    public boolean getValue() {
        return val;
    }

    @Override
    public String toString() {
        return Boolean.toString(val);
    }
}
