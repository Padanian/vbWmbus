/**
 * This class file was automatically generated by the AXDR compiler that is part of jDLMS (http://www.openmuc.org)
 */

package org.openmuc.jdlms.internal.asn1.cosem;

import java.io.IOException;
import java.io.InputStream;

import org.openmuc.jasn1.ber.ReverseByteArrayOutputStream;
import org.openmuc.jdlms.internal.asn1.axdr.AxdrType;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrEnum;

public class ACTIONResponse implements AxdrType {

    public byte[] code = null;

    public static enum Choices {
        _ERR_NONE_SELECTED(-1),
        ACTION_RESPONSE_NORMAL(1),
        ACTION_RESPONSE_WITH_PBLOCK(2),
        ACTION_RESPONSE_WITH_LIST(3),
        ACTION_RESPONSE_NEXT_PBLOCK(4),;

        private int value;

        private Choices(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }

        public static Choices valueOf(long tagValue) {
            Choices[] values = Choices.values();

            for (Choices c : values) {
                if (c.value == tagValue) {
                    return c;
                }
            }
            return _ERR_NONE_SELECTED;
        }
    }

    private Choices choice;

    public ActionResponseNormal actionResponseNormal = null;

    public ActionResponseWithPblock actionResponseWithPblock = null;

    public ActionResponseWithList actionResponseWithList = null;

    public ActionResponseNextPblock actionResponseNextPblock = null;

    public ACTIONResponse() {
    }

    public ACTIONResponse(byte[] code) {
        this.code = code;
    }

    @Override
    public int encode(ReverseByteArrayOutputStream axdrOStream) throws IOException {
        if (code != null) {
            for (int i = code.length - 1; i >= 0; i--) {
                axdrOStream.write(code[i]);
            }
            return code.length;

        }
        if (choice == Choices._ERR_NONE_SELECTED) {
            throw new IOException("Error encoding AxdrChoice: No item in choice was selected.");
        }

        int codeLength = 0;

        if (choice == Choices.ACTION_RESPONSE_NEXT_PBLOCK) {
            codeLength += actionResponseNextPblock.encode(axdrOStream);
            AxdrEnum c = new AxdrEnum(4);
            codeLength += c.encode(axdrOStream);
            return codeLength;
        }

        if (choice == Choices.ACTION_RESPONSE_WITH_LIST) {
            codeLength += actionResponseWithList.encode(axdrOStream);
            AxdrEnum c = new AxdrEnum(3);
            codeLength += c.encode(axdrOStream);
            return codeLength;
        }

        if (choice == Choices.ACTION_RESPONSE_WITH_PBLOCK) {
            codeLength += actionResponseWithPblock.encode(axdrOStream);
            AxdrEnum c = new AxdrEnum(2);
            codeLength += c.encode(axdrOStream);
            return codeLength;
        }

        if (choice == Choices.ACTION_RESPONSE_NORMAL) {
            codeLength += actionResponseNormal.encode(axdrOStream);
            AxdrEnum c = new AxdrEnum(1);
            codeLength += c.encode(axdrOStream);
            return codeLength;
        }

        // This block should be unreachable
        throw new IOException("Error encoding AxdrChoice: No item in choice was encoded.");
    }

    @Override
    public int decode(InputStream iStream) throws IOException {
        int codeLength = 0;
        AxdrEnum choosen = new AxdrEnum();

        codeLength += choosen.decode(iStream);
        resetChoices();
        this.choice = Choices.valueOf(choosen.getValue());

        if (choice == Choices.ACTION_RESPONSE_NORMAL) {
            actionResponseNormal = new ActionResponseNormal();
            codeLength += actionResponseNormal.decode(iStream);
            return codeLength;
        }

        if (choice == Choices.ACTION_RESPONSE_WITH_PBLOCK) {
            actionResponseWithPblock = new ActionResponseWithPblock();
            codeLength += actionResponseWithPblock.decode(iStream);
            return codeLength;
        }

        if (choice == Choices.ACTION_RESPONSE_WITH_LIST) {
            actionResponseWithList = new ActionResponseWithList();
            codeLength += actionResponseWithList.decode(iStream);
            return codeLength;
        }

        if (choice == Choices.ACTION_RESPONSE_NEXT_PBLOCK) {
            actionResponseNextPblock = new ActionResponseNextPblock();
            codeLength += actionResponseNextPblock.decode(iStream);
            return codeLength;
        }

        throw new IOException("Error decoding AxdrChoice: Identifier matched to no item.");
    }

    public void encodeAndSave(int encodingSizeGuess) throws IOException {
        ReverseByteArrayOutputStream axdrOStream = new ReverseByteArrayOutputStream(encodingSizeGuess);
        encode(axdrOStream);
        code = axdrOStream.getArray();
    }

    public Choices getChoiceIndex() {
        return this.choice;
    }

    public void setActionResponseNormal(ActionResponseNormal newVal) {
        resetChoices();
        choice = Choices.ACTION_RESPONSE_NORMAL;
        actionResponseNormal = newVal;
    }

    public void setActionResponseWithPblock(ActionResponseWithPblock newVal) {
        resetChoices();
        choice = Choices.ACTION_RESPONSE_WITH_PBLOCK;
        actionResponseWithPblock = newVal;
    }

    public void setActionResponseWithList(ActionResponseWithList newVal) {
        resetChoices();
        choice = Choices.ACTION_RESPONSE_WITH_LIST;
        actionResponseWithList = newVal;
    }

    public void setActionResponseNextPblock(ActionResponseNextPblock newVal) {
        resetChoices();
        choice = Choices.ACTION_RESPONSE_NEXT_PBLOCK;
        actionResponseNextPblock = newVal;
    }

    private void resetChoices() {
        choice = Choices._ERR_NONE_SELECTED;
        actionResponseNormal = null;
        actionResponseWithPblock = null;
        actionResponseWithList = null;
        actionResponseNextPblock = null;
    }

    @Override
    public String toString() {
        if (choice == Choices.ACTION_RESPONSE_NORMAL) {
            return "choice: {actionResponseNormal: " + actionResponseNormal + "}";
        }

        if (choice == Choices.ACTION_RESPONSE_WITH_PBLOCK) {
            return "choice: {actionResponseWithPblock: " + actionResponseWithPblock + "}";
        }

        if (choice == Choices.ACTION_RESPONSE_WITH_LIST) {
            return "choice: {actionResponseWithList: " + actionResponseWithList + "}";
        }

        if (choice == Choices.ACTION_RESPONSE_NEXT_PBLOCK) {
            return "choice: {actionResponseNextPblock: " + actionResponseNextPblock + "}";
        }

        return "unknown";
    }

}
