/**
 * This class file was automatically generated by jASN1 v1.9.1-SNAPSHOT (http://www.openmuc.org)
 */

package org.openmuc.jdlms.internal.asn1.iso.acse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.openmuc.jasn1.ber.BerTag;
import org.openmuc.jasn1.ber.ReverseByteArrayOutputStream;
import org.openmuc.jasn1.ber.types.BerBitString;
import org.openmuc.jasn1.ber.types.BerOctetString;

public class AuthenticationValue implements Serializable {

    private static final long serialVersionUID = 1L;

    public byte[] code = null;
    private BerOctetString charstring = null;
    private BerBitString bitstring = null;

    public AuthenticationValue() {
    }

    public AuthenticationValue(byte[] code) {
        this.code = code;
    }

    public void setCharstring(BerOctetString charstring) {
        this.charstring = charstring;
    }

    public BerOctetString getCharstring() {
        return charstring;
    }

    public void setBitstring(BerBitString bitstring) {
        this.bitstring = bitstring;
    }

    public BerBitString getBitstring() {
        return bitstring;
    }

    public int encode(OutputStream os) throws IOException {

        if (code != null) {
            for (int i = code.length - 1; i >= 0; i--) {
                os.write(code[i]);
            }
            return code.length;
        }

        int codeLength = 0;
        if (bitstring != null) {
            codeLength += bitstring.encode(os, false);
            // write tag: CONTEXT_CLASS, PRIMITIVE, 1
            os.write(0x81);
            codeLength += 1;
            return codeLength;
        }

        if (charstring != null) {
            codeLength += charstring.encode(os, false);
            // write tag: CONTEXT_CLASS, PRIMITIVE, 0
            os.write(0x80);
            codeLength += 1;
            return codeLength;
        }

        throw new IOException("Error encoding CHOICE: No element of CHOICE was selected.");
    }

    public int decode(InputStream is) throws IOException {
        return decode(is, null);
    }

    public int decode(InputStream is, BerTag berTag) throws IOException {

        int codeLength = 0;
        BerTag passedTag = berTag;

        if (berTag == null) {
            berTag = new BerTag();
            codeLength += berTag.decode(is);
        }

        if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.PRIMITIVE, 0)) {
            charstring = new BerOctetString();
            codeLength += charstring.decode(is, false);
            return codeLength;
        }

        if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.PRIMITIVE, 1)) {
            bitstring = new BerBitString();
            codeLength += bitstring.decode(is, false);
            return codeLength;
        }

        if (passedTag != null) {
            return 0;
        }

        throw new IOException("Error decoding CHOICE: Tag " + berTag + " matched to no item.");
    }

    public void encodeAndSave(int encodingSizeGuess) throws IOException {
        ReverseByteArrayOutputStream os = new ReverseByteArrayOutputStream(encodingSizeGuess);
        encode(os);
        code = os.getArray();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        appendAsString(sb, 0);
        return sb.toString();
    }

    public void appendAsString(StringBuilder sb, int indentLevel) {

        if (charstring != null) {
            sb.append("charstring: ").append(charstring);
            return;
        }

        if (bitstring != null) {
            sb.append("bitstring: ").append(bitstring);
            return;
        }

        sb.append("<none>");
    }

}
