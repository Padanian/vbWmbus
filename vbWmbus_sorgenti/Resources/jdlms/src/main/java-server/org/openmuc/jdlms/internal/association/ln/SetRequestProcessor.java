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
package org.openmuc.jdlms.internal.association.ln;

import java.io.IOException;
import java.util.Iterator;

import org.openmuc.jdlms.AccessResultCode;
import org.openmuc.jdlms.AttributeAddress;
import org.openmuc.jdlms.ObisCode;
import org.openmuc.jdlms.SelectiveAccessDescription;
import org.openmuc.jdlms.SetParameter;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.internal.APdu;
import org.openmuc.jdlms.internal.DataConverter;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrEnum;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrOptional;
import org.openmuc.jdlms.internal.asn1.cosem.COSEMpdu;
import org.openmuc.jdlms.internal.asn1.cosem.CosemAttributeDescriptor;
import org.openmuc.jdlms.internal.asn1.cosem.CosemAttributeDescriptorWithSelection;
import org.openmuc.jdlms.internal.asn1.cosem.Data;
import org.openmuc.jdlms.internal.asn1.cosem.InvokeIdAndPriority;
import org.openmuc.jdlms.internal.asn1.cosem.SETRequest;
import org.openmuc.jdlms.internal.asn1.cosem.SETResponse;
import org.openmuc.jdlms.internal.asn1.cosem.SelectiveAccessDescriptor;
import org.openmuc.jdlms.internal.asn1.cosem.SetRequestNormal;
import org.openmuc.jdlms.internal.asn1.cosem.SetRequestWithList;
import org.openmuc.jdlms.internal.asn1.cosem.SetResponseNormal;
import org.openmuc.jdlms.internal.asn1.cosem.SetResponseWithList;
import org.openmuc.jdlms.internal.association.AssociationMessenger;
import org.openmuc.jdlms.internal.association.RequestProcessorBase;
import org.openmuc.jdlms.internal.association.RequestProcessorData;

public class SetRequestProcessor extends RequestProcessorBase {

    public SetRequestProcessor(AssociationMessenger associationMessenger, RequestProcessorData requestProcessorData) {
        super(associationMessenger, requestProcessorData);
    }

    @Override
    public void processRequest(COSEMpdu request) throws IOException {
        SETRequest setRequest = request.setRequest;
        SETResponse setResponse;
        switch (setRequest.getChoiceIndex()) {
        case SET_REQUEST_NORMAL:
            setResponse = processSetRequestNormal(setRequest.setRequestNormal);
            break;
        case SET_REQUEST_WITH_LIST:
            setResponse = processSetRequestWithList(setRequest.setRequestWithList);
            break;
        case SET_REQUEST_WITH_DATABLOCK:
            // TODO implement SET_REQUEST_WITH_DATABLOCK
        default:
            throw new IOException("Not yet implemented");
        }

        COSEMpdu cosemPdu = new COSEMpdu();
        cosemPdu.setSetResponse(setResponse);
        APdu aPdu = new APdu(null, cosemPdu);
        this.associationMessenger.encodeAndSend(aPdu);

    }

    private SETResponse processSetRequestWithList(SetRequestWithList requestWithList) {
        InvokeIdAndPriority invokeIdAndPriority = requestWithList.invokeIdAndPriority;

        Iterator<CosemAttributeDescriptorWithSelection> descriptorListIter = requestWithList.attributeDescriptorList
                .list()
                .iterator();
        Iterator<Data> valueListIter = requestWithList.valueList.list().iterator();

        SetResponseWithList.SubSeqOfResult result = new SetResponseWithList.SubSeqOfResult();
        while (descriptorListIter.hasNext() && valueListIter.hasNext()) {
            CosemAttributeDescriptorWithSelection descriptor = descriptorListIter.next();

            AccessResultCode resultCode = convertAndSet(valueListIter.next(), descriptor.cosemAttributeDescriptor,
                    descriptor.accessSelection);
            result.add(new AxdrEnum(resultCode.getCode()));
        }
        SETResponse setResponse = new SETResponse();
        SetResponseWithList responseWithList = new SetResponseWithList(invokeIdAndPriority, result);
        setResponse.setSetResponseWithList(responseWithList);

        return setResponse;
    }

    private SETResponse processSetRequestNormal(SetRequestNormal normalRequest) {
        InvokeIdAndPriority invokeIdAndPriority = normalRequest.invokeIdAndPriority;

        AccessResultCode accessResultCode = convertAndSet(normalRequest.value, normalRequest.cosemAttributeDescriptor,
                normalRequest.accessSelection);

        SetResponseNormal setResponseNormal = new SetResponseNormal(invokeIdAndPriority,
                new AxdrEnum(accessResultCode.getCode()));

        SETResponse setResponse = new SETResponse();
        setResponse.setSetResponseNormal(setResponseNormal);
        return setResponse;
    }

    private AccessResultCode convertAndSet(Data newValue, CosemAttributeDescriptor cosemAttributeAescriptor,
            AxdrOptional<SelectiveAccessDescriptor> accessSelection) {
        DataObject dataObject = DataConverter.convertDataToDataObject(newValue);

        ObisCode instanceId = new ObisCode(cosemAttributeAescriptor.instanceId.getValue());
        SelectiveAccessDescription selectiveAccessDescription = null;

        if (accessSelection.isUsed()) {
            SelectiveAccessDescriptor accessDescriptor = accessSelection.getValue();
            int accessSelector = (int) accessDescriptor.accessSelector.getValue();
            DataObject accessParameter = DataConverter.convertDataToDataObject(accessDescriptor.accessParameters);
            selectiveAccessDescription = new SelectiveAccessDescription(accessSelector, accessParameter);
        }

        int classId = (int) cosemAttributeAescriptor.classId.getValue();
        int attributeId = (int) cosemAttributeAescriptor.attributeId.getValue();
        AttributeAddress attributeAddress = new AttributeAddress(classId, instanceId, attributeId,
                selectiveAccessDescription);
        SetParameter setParam = new SetParameter(attributeAddress, dataObject);

        return this.requestProcessorData.getDirectory().set(this.requestProcessorData.logicalDeviceId, setParam,
                connectionId());
    }

}
