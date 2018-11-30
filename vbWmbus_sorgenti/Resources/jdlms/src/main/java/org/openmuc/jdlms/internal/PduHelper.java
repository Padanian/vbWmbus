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

import org.openmuc.jdlms.internal.asn1.cosem.ACTIONResponse;
import org.openmuc.jdlms.internal.asn1.cosem.COSEMpdu;
import org.openmuc.jdlms.internal.asn1.cosem.GETResponse;
import org.openmuc.jdlms.internal.asn1.cosem.InvokeIdAndPriority;
import org.openmuc.jdlms.internal.asn1.cosem.SETResponse;

public class PduHelper {

    public static final int INVALID_INVOKE_ID = -1;

    private static int invokeIdFrom(GETResponse pdu) {
        switch (pdu.getChoiceIndex()) {
        case GET_RESPONSE_NORMAL:
            return invokeIdFrom(pdu.getResponseNormal.invokeIdAndPriority);

        case GET_RESPONSE_WITH_DATABLOCK:
            return invokeIdFrom(pdu.getResponseWithDatablock.invokeIdAndPriority);

        case GET_RESPONSE_WITH_LIST:
            return invokeIdFrom(pdu.getResponseWithList.invokeIdAndPriority);

        default:
            return INVALID_INVOKE_ID;
        }

    }

    private static int invokeIdFrom(SETResponse pdu) {

        switch (pdu.getChoiceIndex()) {
        case SET_RESPONSE_NORMAL:
            return invokeIdFrom(pdu.setResponseNormal.invokeIdAndPriority);

        case SET_RESPONSE_WITH_LIST:
            return invokeIdFrom(pdu.setResponseWithList.invokeIdAndPriority);

        case SET_RESPONSE_DATABLOCK:
            return invokeIdFrom(pdu.setResponseDatablock.invokeIdAndPriority);

        case SET_RESPONSE_LAST_DATABLOCK:
            return invokeIdFrom(pdu.setResponseLastDatablock.invokeIdAndPriority);

        case SET_RESPONSE_LAST_DATABLOCK_WITH_LIST:
            return invokeIdFrom(pdu.setResponseLastDatablockWithList.invokeIdAndPriority);

        default:
            return INVALID_INVOKE_ID;
        }

    }

    private static int invokeIdFrom(ACTIONResponse pdu) {
        switch (pdu.getChoiceIndex()) {
        case ACTION_RESPONSE_NORMAL:
            return invokeIdFrom(pdu.actionResponseNormal.invokeIdAndPriority);

        case ACTION_RESPONSE_WITH_LIST:
            return invokeIdFrom(pdu.actionResponseWithList.invokeIdAndPriority);

        case ACTION_RESPONSE_NEXT_PBLOCK:
            return invokeIdFrom(pdu.actionResponseNextPblock.invokeIdAndPriority);

        case ACTION_RESPONSE_WITH_PBLOCK:
            return invokeIdFrom(pdu.actionResponseWithPblock.invokeIdAndPriority);

        default:
            return INVALID_INVOKE_ID;
        }

    }

    public static int invokeIdFrom(COSEMpdu cosemPdu) {
        switch (cosemPdu.getChoiceIndex()) {
        case ACTION_RESPONSE:
            return invokeIdFrom(cosemPdu.actionResponse);
        case GET_RESPONSE:
            return invokeIdFrom(cosemPdu.getResponse);
        case SET_RESPONSE:
            return invokeIdFrom(cosemPdu.setResponse);
        default:
            return INVALID_INVOKE_ID;
        }

    }

    public static int invokeIdFrom(InvokeIdAndPriority invokeIdAndPriority) {
        return invokeIdAndPriority.getValue()[0] & 0xf;
    }

    /**
     * Don't let anyone instantiate this class.
     */
    private PduHelper() {
    }

}
