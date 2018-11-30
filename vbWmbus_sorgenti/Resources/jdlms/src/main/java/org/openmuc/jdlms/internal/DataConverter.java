/*
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
 *
 */
package org.openmuc.jdlms.internal;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.openmuc.jdlms.AttributeAddress;
import org.openmuc.jdlms.EventNotification;
import org.openmuc.jdlms.IllegalPametrizationError;
import org.openmuc.jdlms.ObisCode;
import org.openmuc.jdlms.datatypes.BitString;
import org.openmuc.jdlms.datatypes.CompactArray;
import org.openmuc.jdlms.datatypes.CompactArray.DescriptionArray;
import org.openmuc.jdlms.datatypes.CompactArray.TypeDesc;
import org.openmuc.jdlms.datatypes.CosemDate;
import org.openmuc.jdlms.datatypes.CosemDateFormat;
import org.openmuc.jdlms.datatypes.CosemDateTime;
import org.openmuc.jdlms.datatypes.CosemTime;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.datatypes.DataObject.Type;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrBitString;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrBoolean;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrNull;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrOctetString;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrSequenceOf;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrVisibleString;
import org.openmuc.jdlms.internal.asn1.cosem.Data;
import org.openmuc.jdlms.internal.asn1.cosem.Data.Choices;
import org.openmuc.jdlms.internal.asn1.cosem.Data.SubSeqCompactArray;
import org.openmuc.jdlms.internal.asn1.cosem.EVENTNOTIFICATIONRequest;
import org.openmuc.jdlms.internal.asn1.cosem.Enum;
import org.openmuc.jdlms.internal.asn1.cosem.Integer16;
import org.openmuc.jdlms.internal.asn1.cosem.Integer32;
import org.openmuc.jdlms.internal.asn1.cosem.Integer64;
import org.openmuc.jdlms.internal.asn1.cosem.Integer8;
import org.openmuc.jdlms.internal.asn1.cosem.TypeDescription;
import org.openmuc.jdlms.internal.asn1.cosem.TypeDescription.SubSeqArray;
import org.openmuc.jdlms.internal.asn1.cosem.Unsigned16;
import org.openmuc.jdlms.internal.asn1.cosem.Unsigned32;
import org.openmuc.jdlms.internal.asn1.cosem.Unsigned64;
import org.openmuc.jdlms.internal.asn1.cosem.Unsigned8;

public final class DataConverter {

    public static DataObject convertDataToDataObject(Data pdu) {

        Choices choice = pdu.getChoiceIndex();
        ByteBuffer buf;
        List<DataObject> innerData;

        switch (choice) {
        case ARRAY:
            innerData = new LinkedList<>();
            for (Data item : pdu.array.list()) {
                innerData.add(convertDataToDataObject(item));
            }
            return DataObject.newArrayData(innerData);

        case STRUCTURE:
            innerData = new LinkedList<>();
            for (Data item : pdu.structure.list()) {
                innerData.add(convertDataToDataObject(item));
            }
            return DataObject.newStructureData(innerData);

        case BOOL:
            return DataObject.newBoolData(pdu.bool.getValue());

        case BIT_STRING:
            return DataObject.newBitStringData(new BitString(pdu.bitString.getValue(), pdu.bitString.getNumBits()));

        case DOUBLE_LONG:
            return DataObject.newInteger32Data((int) pdu.doubleLong.getValue());

        case DOUBLE_LONG_UNSIGNED:
            return DataObject.newUInteger32Data(pdu.doubleLongUnsigned.getValue());

        case OCTET_STRING:
            return DataObject.newOctetStringData(pdu.octetString.getValue());

        case VISIBLE_STRING:
            return DataObject.newVisibleStringData(pdu.visibleString.getValue());

        case UTF8_STRING:
            return DataObject.newUtf8StringData(pdu.utf8String.getValue());

        case BCD:
            return DataObject.newBcdData((byte) pdu.bcd.getValue());

        case INTEGER:
            return DataObject.newInteger8Data((byte) pdu.integer.getValue());

        case LONG_INTEGER:
            return DataObject.newInteger16Data((short) pdu.longInteger.getValue());

        case UNSIGNED:
            return DataObject.newUInteger8Data((short) pdu.unsigned.getValue());

        case LONG_UNSIGNED:
            return DataObject.newUInteger16Data((int) pdu.longUnsigned.getValue());

        case LONG64:
            return DataObject.newInteger64Data(pdu.long64.getValue());
        case LONG64_UNSIGNED:
            return DataObject.newUInteger64Data(pdu.long64Unsigned.getValue());

        case ENUMERATE:
            return DataObject.newEnumerateData((int) pdu.enumerate.getValue());

        case FLOAT32:
            buf = ByteBuffer.wrap(pdu.float32.getValue());
            return DataObject.newFloat32Data(buf.getFloat());

        case FLOAT64:
            buf = ByteBuffer.wrap(pdu.float64.getValue());
            return DataObject.newFloat64Data(buf.getDouble());

        case DATE_TIME:
            CosemDateTime dateTime = CosemDateTime.decode(pdu.dateTime.getValue());
            return DataObject.newDateTimeData(dateTime);

        case DATE:
            CosemDate date = CosemDate.decode(pdu.date.getValue());
            return DataObject.newDateData(date);

        case TIME:
            CosemTime time = CosemTime.decode(pdu.time.getValue());
            return DataObject.newTimeData(time);

        case COMPACT_ARRAY:
            SubSeqCompactArray compactArray = pdu.compactArray;
            byte[] arrayContents = compactArray.arrayContents.getValue();
            TypeDescription contentsDescription = compactArray.contentsDescription;

            TypeDesc typeDescription = convert(contentsDescription);
            CompactArray compactArrayS = new CompactArray(typeDescription, arrayContents);
            return DataObject.newCompactArrayData(compactArrayS);

        case DONT_CARE:
        case NULL_DATA:
        default:
            return DataObject.newNullData();
        }

    }

    private static TypeDesc convert(TypeDescription contentsDescription) {
        if (contentsDescription.getChoiceIndex() == TypeDescription.Choices.ARRAY) {
            SubSeqArray array = contentsDescription.array;
            int numOfeElements = (int) array.numberOfElements.getValue();
            TypeDesc typeDescription = convert(array.typeDescription);
            return new TypeDesc(new DescriptionArray(numOfeElements, typeDescription), TypeDesc.Type.ARRAY);
        }
        else if (contentsDescription.getChoiceIndex() == TypeDescription.Choices.STRUCTURE) {
            TypeDescription.SubSeqOfStructure structure = contentsDescription.structure;
            List<TypeDescription> list = structure.list();

            List<TypeDesc> structList = new ArrayList<>(list.size());
            for (TypeDescription typeDescription : list) {
                structList.add(convert(typeDescription));
            }
            return new TypeDesc(structList, TypeDesc.Type.STRUCTURE);
        }
        else {
            return new TypeDesc(TypeDesc.Type.forValue(contentsDescription.getChoiceIndex().getValue()));
        }

    }

    public static Data convertDataObjectToData(DataObject data) {
        Data result = new Data();

        Type type;

        if (data == null || (type = data.getType()) == Type.DONT_CARE) {
            result.setDontCare(new AxdrNull());
        }
        else if (data.isNull()) {
            result.setNullData(new AxdrNull());
        }
        else if (data.isCosemDateFormat()) {
            CosemDateFormat cal = data.getValue();
            result.setOctetString(new AxdrOctetString(cal.encode()));

        }
        else if (data.isNumber()) {
            result = convertNumberToPduData(data, type);

        }
        else if (data.isByteArray()) {
            result = converByteArrayToPduData(data, type);

        }
        else if (data.isBitString()) {
            BitString value = data.getValue();
            result.setBitString(new AxdrBitString(value.getBitString(), value.getNumBits()));

        }
        else if (type == Type.BOOLEAN) {
            Boolean boolValue = data.getValue();
            result.setBool(new AxdrBoolean(boolValue));

        }
        else if (data.isComplex()) {
            result = convertComplexToPduData(data, type);
        }

        return result;
    }

    private static TypeDescription convertTypeDesc(TypeDesc typeDescription) {
        TypeDescription genTypeDescription = new TypeDescription(); // generated type
        switch (typeDescription.getType()) {
        case ARRAY:
            DescriptionArray array = typeDescription.getValue();
            genTypeDescription.setArray(new SubSeqArray(new Unsigned16(array.getNumOfeElements()),
                    convertTypeDesc(array.getTypeDescription())));
            break;
        case STRUCTURE:
            TypeDescription.SubSeqOfStructure newStruct = new TypeDescription.SubSeqOfStructure();
            List<TypeDesc> struct = typeDescription.getValue();
            for (TypeDesc typeDesc : struct) {
                newStruct.add(convertTypeDesc(typeDesc));
            }
            genTypeDescription.setStructure(newStruct);

            break;
        case BCD:
            genTypeDescription.setBcd(new AxdrNull());
            break;
        case BIT_STRING:
            genTypeDescription.setBitString(new AxdrNull());
            break;
        case BOOL:
            genTypeDescription.setBool(new AxdrNull());
            break;
        case DATE:
            genTypeDescription.setDate(new AxdrNull());
            break;
        case DATE_TIME:
            genTypeDescription.setDateTime(new AxdrNull());
            break;
        case DONT_CARE:
            genTypeDescription.setDontCare(new AxdrNull());
            break;
        case DOUBLE_LONG:
            genTypeDescription.setDoubleLong(new AxdrNull());
            break;
        case DOUBLE_LONG_UNSIGNED:
            genTypeDescription.setDoubleLongUnsigned(new AxdrNull());
            break;
        case ENUMERATE:
            genTypeDescription.setEnumerate(new AxdrNull());
            break;
        case FLOAT32:
            genTypeDescription.setFloat32(new AxdrNull());
            break;
        case FLOAT64:
            genTypeDescription.setFloat64(new AxdrNull());
            break;
        case INTEGER:
            genTypeDescription.setInteger(new AxdrNull());
            break;
        case LONG64:
            genTypeDescription.setLong64(new AxdrNull());
            break;
        case LONG64_UNSIGNED:
            genTypeDescription.setLong64Unsigned(new AxdrNull());
            break;
        case LONG_INTEGER:
            genTypeDescription.setLongInteger(new AxdrNull());
            break;
        case LONG_UNSIGNED:
            genTypeDescription.setLongUnsigned(new AxdrNull());
            break;
        case NULL_DATA:
            genTypeDescription.setNullData(new AxdrNull());
            break;
        case OCTET_STRING:
            genTypeDescription.setOctetString(new AxdrNull());
            break;
        case TIME:
            genTypeDescription.setTime(new AxdrNull());
            break;
        case UNSIGNED:
            genTypeDescription.setUnsigned(new AxdrNull());
            break;
        case UTF8_STRING:
            genTypeDescription.setUtf8String(new AxdrNull());
            break;
        case VISIBLE_STRING:
            genTypeDescription.setVisibleString(new AxdrNull());
            break;

        case ERR_NONE_SELECTED:
        default:
            throw new IllegalPametrizationError("Unknown type, can't convert.");
        }
        return genTypeDescription;
    }

    private static Data convertComplexToPduData(DataObject data, Type type) {
        Data result = new Data();

        if (data.getType() == Type.COMPACT_ARRAY) {
            CompactArray compactArray = data.getValue();

            TypeDescription contentsDescription = convertTypeDesc(compactArray.getTypeDescription());
            AxdrOctetString arrayContents = new AxdrOctetString(compactArray.getArrayContents());
            SubSeqCompactArray compVal = new SubSeqCompactArray(contentsDescription, arrayContents);
            result.setCompactArray(compVal);
        }
        else {

            List<DataObject> dataList = data.getValue();
            if (type == Type.STRUCTURE) {
                result.setStructure(new Data.SubSeqOfStructure());
                setSeq(result.structure, dataList);
            }
            else if (type == Type.ARRAY) {
                result.setArray(new Data.SubSeqOfArray());
                setSeq(result.array, dataList);
            }
        }

        return result;
    }

    private static void setSeq(AxdrSequenceOf<Data> seq, List<DataObject> dataList) {
        for (DataObject element : dataList) {
            seq.add(convertDataObjectToData(element));
        }
    }

    private static Data converByteArrayToPduData(DataObject data, Type type) {
        byte[] value = data.getValue();

        Data result = new Data();

        switch (type) {
        case OCTET_STRING:
            result.setOctetString(new AxdrOctetString(value));
            break;
        case VISIBLE_STRING:
            result.setVisibleString(new AxdrVisibleString(value));
            break;
        case UTF8_STRING:
            result.setUtf8String(new AxdrOctetString(value));
            break;
        default:
            // can't be reached
            throw new IllegalPametrizationError("No such type: " + type);
        }

        return result;
    }

    private static Data convertNumberToPduData(DataObject data, Type type) {
        ByteBuffer buffer;
        Number value = data.getValue();

        Data result = new Data();

        switch (type) {
        case FLOAT64:
            buffer = ByteBuffer.allocate(8).putDouble(value.doubleValue());
            buffer.flip();

            result.setFloat64(new AxdrOctetString(buffer.array()));
            break;

        case FLOAT32:
            buffer = ByteBuffer.allocate(4).putFloat(value.floatValue());
            buffer.flip();

            result.setFloat32(new AxdrOctetString(buffer.array()));
            break;

        case ENUMERATE:
            result.setEnumerate(new Enum(value.longValue()));
            break;

        case LONG64_UNSIGNED:
            result.setLong64Unsigned(new Unsigned64(value.longValue()));
            break;

        case LONG64:
            result.setLong64(new Integer64(value.longValue()));
            break;

        case LONG_UNSIGNED:
            result.setLongUnsigned(new Unsigned16(value.longValue()));
            break;

        case UNSIGNED:
            result.setUnsigned(new Unsigned8(value.longValue()));
            break;

        case LONG_INTEGER:
            result.setLongInteger(new Integer16(value.longValue()));
            break;
        case INTEGER:
            result.setInteger(new Integer8(value.longValue()));
            break;
        case BCD:
            result.setBcd(new Integer8(value.longValue()));
            break;
        case DOUBLE_LONG_UNSIGNED:
            result.setDoubleLongUnsigned(new Unsigned32(value.longValue()));
            break;
        case DOUBLE_LONG:
            result.setDoubleLong(new Integer32(value.longValue()));

            break;
        default:
            // can't be rached
            throw new IllegalArgumentException("No such number: " + type);
        }
        return result;
    }

    public static EventNotification convertNotiReqToNotification(EVENTNOTIFICATIONRequest pdu) {
        int classId = (int) pdu.cosemAttributeDescriptor.classId.getValue();
        int attributeId = (int) pdu.cosemAttributeDescriptor.attributeId.getValue();

        byte[] obisCodeBytes = pdu.cosemAttributeDescriptor.instanceId.getValue();

        Long timestamp = System.currentTimeMillis();

        /**
         * if (pdu.time.isUsed()) { CosemDateTime dateTime = CosemDateTime.decode(pdu.time.getValue().getValue());
         * timestamp = dateTime.toCalendar().getTimeInMillis(); }
         */

        DataObject newValue = null;
        if (pdu.attributeValue != null) {
            newValue = convertDataToDataObject(pdu.attributeValue);
        }

        return new EventNotification(new AttributeAddress(classId, new ObisCode(obisCodeBytes), attributeId), newValue,
                timestamp);
    }

    /**
     * Don't let anyone instantiate this class.
     */
    private DataConverter() {
    }
}
