/**
 * This class file was automatically generated by the AXDR compiler that is part of jDLMS (http://www.openmuc.org)
 */

package org.openmuc.jdlms.internal.asn1.cosem;

import java.io.IOException;
import java.io.InputStream;

import org.openmuc.jasn1.ber.ReverseByteArrayOutputStream;
import org.openmuc.jdlms.internal.asn1.axdr.AxdrType;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrEnum;

public class ACTIONRequest implements AxdrType {

    public byte[] code = null;

    public static enum Choices {
        _ERR_NONE_SELECTED(-1),
        ACTION_REQUEST_NORMAL(1),
        ACTION_REQUEST_NEXT_PBLOCK(2),
        ACTION_REQUEST_WITH_LIST(3),
        ACTION_REQUEST_WITH_FIRST_PBLOCK(4),
        ACTION_REQUEST_WITH_LIST_AND_FIRST_PBLOCK(5),
        ACTION_REQUEST_WITH_PBLOCK(6),;

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

    public ActionRequestNormal actionRequestNormal = null;

    public ActionRequestNextPblock actionRequestNextPblock = null;

    public ActionRequestWithList actionRequestWithList = null;

    public ActionRequestWithFirstPblock actionRequestWithFirstPblock = null;

    public ActionRequestWithListAndFirstPblock actionRequestWithListAndFirstPblock = null;

    public ActionRequestWithPblock actionRequestWithPblock = null;

    public ACTIONRequest() {
    }

    public ACTIONRequest(byte[] code) {
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

        if (choice == Choices.ACTION_REQUEST_WITH_PBLOCK) {
            codeLength += actionRequestWithPblock.encode(axdrOStream);
            AxdrEnum c = new AxdrEnum(6);
            codeLength += c.encode(axdrOStream);
            return codeLength;
        }

        if (choice == Choices.ACTION_REQUEST_WITH_LIST_AND_FIRST_PBLOCK) {
            codeLength += actionRequestWithListAndFirstPblock.encode(axdrOStream);
            AxdrEnum c = new AxdrEnum(5);
            codeLength += c.encode(axdrOStream);
            return codeLength;
        }

        if (choice == Choices.ACTION_REQUEST_WITH_FIRST_PBLOCK) {
            codeLength += actionRequestWithFirstPblock.encode(axdrOStream);
            AxdrEnum c = new AxdrEnum(4);
            codeLength += c.encode(axdrOStream);
            return codeLength;
        }

        if (choice == Choices.ACTION_REQUEST_WITH_LIST) {
            codeLength += actionRequestWithList.encode(axdrOStream);
            AxdrEnum c = new AxdrEnum(3);
            codeLength += c.encode(axdrOStream);
            return codeLength;
        }

        if (choice == Choices.ACTION_REQUEST_NEXT_PBLOCK) {
            codeLength += actionRequestNextPblock.encode(axdrOStream);
            AxdrEnum c = new AxdrEnum(2);
            codeLength += c.encode(axdrOStream);
            return codeLength;
        }

        if (choice == Choices.ACTION_REQUEST_NORMAL) {
            codeLength += actionRequestNormal.encode(axdrOStream);
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

        if (choice == Choices.ACTION_REQUEST_NORMAL) {
            actionRequestNormal = new ActionRequestNormal();
            codeLength += actionRequestNormal.decode(iStream);
            return codeLength;
        }

        if (choice == Choices.ACTION_REQUEST_NEXT_PBLOCK) {
            actionRequestNextPblock = new ActionRequestNextPblock();
            codeLength += actionRequestNextPblock.decode(iStream);
            return codeLength;
        }

        if (choice == Choices.ACTION_REQUEST_WITH_LIST) {
            actionRequestWithList = new ActionRequestWithList();
            codeLength += actionRequestWithList.decode(iStream);
            return codeLength;
        }

        if (choice == Choices.ACTION_REQUEST_WITH_FIRST_PBLOCK) {
            actionRequestWithFirstPblock = new ActionRequestWithFirstPblock();
            codeLength += actionRequestWithFirstPblock.decode(iStream);
            return codeLength;
        }

        if (choice == Choices.ACTION_REQUEST_WITH_LIST_AND_FIRST_PBLOCK) {
            actionRequestWithListAndFirstPblock = new ActionRequestWithListAndFirstPblock();
            codeLength += actionRequestWithListAndFirstPblock.decode(iStream);
            return codeLength;
        }

        if (choice == Choices.ACTION_REQUEST_WITH_PBLOCK) {
            actionRequestWithPblock = new ActionRequestWithPblock();
            codeLength += actionRequestWithPblock.decode(iStream);
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

    public void setActionRequestNormal(ActionRequestNormal newVal) {
        resetChoices();
        choice = Choices.ACTION_REQUEST_NORMAL;
        actionRequestNormal = newVal;
    }

    public void setActionRequestNextPblock(ActionRequestNextPblock newVal) {
        resetChoices();
        choice = Choices.ACTION_REQUEST_NEXT_PBLOCK;
        actionRequestNextPblock = newVal;
    }

    public void setActionRequestWithList(ActionRequestWithList newVal) {
        resetChoices();
        choice = Choices.ACTION_REQUEST_WITH_LIST;
        actionRequestWithList = newVal;
    }

    public void setActionRequestWithFirstPblock(ActionRequestWithFirstPblock newVal) {
        resetChoices();
        choice = Choices.ACTION_REQUEST_WITH_FIRST_PBLOCK;
        actionRequestWithFirstPblock = newVal;
    }

    public void setActionRequestWithListAndFirstPblock(ActionRequestWithListAndFirstPblock newVal) {
        resetChoices();
        choice = Choices.ACTION_REQUEST_WITH_LIST_AND_FIRST_PBLOCK;
        actionRequestWithListAndFirstPblock = newVal;
    }

    public void setActionRequestWithPblock(ActionRequestWithPblock newVal) {
        resetChoices();
        choice = Choices.ACTION_REQUEST_WITH_PBLOCK;
        actionRequestWithPblock = newVal;
    }

    private void resetChoices() {
        choice = Choices._ERR_NONE_SELECTED;
        actionRequestNormal = null;
        actionRequestNextPblock = null;
        actionRequestWithList = null;
        actionRequestWithFirstPblock = null;
        actionRequestWithListAndFirstPblock = null;
        actionRequestWithPblock = null;
    }

    @Override
    public String toString() {
        if (choice == Choices.ACTION_REQUEST_NORMAL) {
            return "choice: {actionRequestNormal: " + actionRequestNormal + "}";
        }

        if (choice == Choices.ACTION_REQUEST_NEXT_PBLOCK) {
            return "choice: {actionRequestNextPblock: " + actionRequestNextPblock + "}";
        }

        if (choice == Choices.ACTION_REQUEST_WITH_LIST) {
            return "choice: {actionRequestWithList: " + actionRequestWithList + "}";
        }

        if (choice == Choices.ACTION_REQUEST_WITH_FIRST_PBLOCK) {
            return "choice: {actionRequestWithFirstPblock: " + actionRequestWithFirstPblock + "}";
        }

        if (choice == Choices.ACTION_REQUEST_WITH_LIST_AND_FIRST_PBLOCK) {
            return "choice: {actionRequestWithListAndFirstPblock: " + actionRequestWithListAndFirstPblock + "}";
        }

        if (choice == Choices.ACTION_REQUEST_WITH_PBLOCK) {
            return "choice: {actionRequestWithPblock: " + actionRequestWithPblock + "}";
        }

        return "unknown";
    }

}
