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
import org.openmuc.jasn1.ber.types.BerOctetString;

public class APTitle implements Serializable {

    private static final long serialVersionUID = 1L;

    public byte[] code = null;
    private APTitleForm2 apTitleForm2 = null;

    public APTitle() {
    }

    public APTitle(byte[] code) {
        this.code = code;
    }

    public void setApTitleForm2(APTitleForm2 apTitleForm2) {
        this.apTitleForm2 = apTitleForm2;
    }

    public APTitleForm2 getApTitleForm2() {
        return apTitleForm2;
    }

    public int encode(OutputStream os) throws IOException {

        if (code != null) {
            for (int i = code.length - 1; i >= 0; i--) {
                os.write(code[i]);
            }
            return code.length;
        }

        int codeLength = 0;
        if (apTitleForm2 != null) {
            codeLength += apTitleForm2.encode(os, true);
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

        if (berTag.equals(BerOctetString.tag)) {
            apTitleForm2 = new APTitleForm2();
            codeLength += apTitleForm2.decode(is, false);
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

        if (apTitleForm2 != null) {
            sb.append("apTitleForm2: ").append(apTitleForm2);
            return;
        }

        sb.append("<none>");
    }

}