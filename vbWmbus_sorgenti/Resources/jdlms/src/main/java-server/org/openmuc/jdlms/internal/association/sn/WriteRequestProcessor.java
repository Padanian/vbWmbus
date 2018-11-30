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
package org.openmuc.jdlms.internal.association.sn;

import static org.openmuc.jdlms.internal.DlmsEnumFunctions.enumToAxdrEnum;

import java.io.IOException;
import java.util.Iterator;

import org.openmuc.jdlms.IllegalAttributeAccessException;
import org.openmuc.jdlms.IllegalMethodAccessException;
import org.openmuc.jdlms.internal.APdu;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrNull;
import org.openmuc.jdlms.internal.asn1.cosem.COSEMpdu;
import org.openmuc.jdlms.internal.asn1.cosem.Data;
import org.openmuc.jdlms.internal.asn1.cosem.VariableAccessSpecification;
import org.openmuc.jdlms.internal.asn1.cosem.WriteRequest;
import org.openmuc.jdlms.internal.asn1.cosem.WriteResponse;
import org.openmuc.jdlms.internal.asn1.cosem.WriteResponse.SubChoice;
import org.openmuc.jdlms.internal.association.AssociationMessenger;
import org.openmuc.jdlms.internal.association.RequestProcessorData;

public class WriteRequestProcessor extends SnRequestProcessorBase {

    public WriteRequestProcessor(AssociationMessenger associationMessenger, RequestProcessorData requestProcessorData) {
        super(associationMessenger, requestProcessorData);
    }

    @Override
    public void processRequest(COSEMpdu request) throws IOException {
        WriteRequest writeRequest = request.writeRequest;

        // sized should be the same
        Iterator<VariableAccessSpecification> varAccessSpecIter = writeRequest.variableAccessSpecification.iterator();
        Iterator<Data> dataIter = writeRequest.listOfData.iterator();

        WriteResponse writeResponse = new WriteResponse();
        while (varAccessSpecIter.hasNext() && dataIter.hasNext()) {
            VariableAccessSpecification vas = varAccessSpecIter.next();
            Data data = dataIter.next();

            switch (vas.getChoiceIndex()) {
            case VARIABLE_NAME:
                writeResponse.add(varNameReq(vas, data));
                break;
            case PARAMETERIZED_ACCESS:
                // TODO
                break;
            case WRITE_DATA_BLOCK_ACCESS:
                // also kind of illegal at this point.
                break;

            case BLOCK_NUMBER_ACCESS:
            case READ_DATA_BLOCK_ACCESS:
            case _ERR_NONE_SELECTED:
            default:
                // TODO illegal
                break;
            }

        }

        APdu aPdu = newAPdu();
        aPdu.getCosemPdu().setWriteResponse(writeResponse);

        this.associationMessenger.encodeAndSend(aPdu);

    }

    private SubChoice varNameReq(VariableAccessSpecification vas, Data data) {
        int varName = (int) vas.variableName.getValue() & 0xFFFF;
        SubChoice res = new SubChoice();

        try {
            this.requestProcessorData.getDirectory().snSetOrInvoke(logicalDeviceId(), varName, data, connectionId());
            res.setSuccess(new AxdrNull());
        } catch (IllegalAttributeAccessException e) {
            res.setDataAccessError(enumToAxdrEnum(e.getAccessResultCode()));
        } catch (IllegalMethodAccessException e) {
            res.setDataAccessError(enumToAxdrEnum(e.getMethodResultCode()));
        }

        return res;
    }

}
