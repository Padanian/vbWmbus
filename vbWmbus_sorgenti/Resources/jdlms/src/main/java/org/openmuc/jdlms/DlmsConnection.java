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
package org.openmuc.jdlms;

import java.io.IOException;
import java.util.List;

/**
 * Interface used to interact with a DLMS/COSEM server using the LN services.
 * 
 * @see ConnectionBuilder
 * @see DlmsSnConnection
 */
public interface DlmsConnection extends BaseConnection {

    /**
     * Convenience method to call {@code #get(false, List)}
     * 
     * 
     * @param params
     *            args of specifiers which attributes to send (See {@link AttributeAddress})
     * @return List of results from the smart meter in the same order as the requests
     * 
     * @throws IOException
     *             if the connection breaks, while requesting.
     *             <p>
     *             May be of type {@link FatalJDlmsException} or {@link ResponseTimeoutException}
     *             </p>
     * 
     * @see #get(boolean, List)
     */
    List<GetResult> get(List<AttributeAddress> params) throws IOException;

    /**
     * Requests the remote smart meter to send the values of several attributes.
     * 
     * <p>
     * Convenience method to call {@code #get(false, AttributeAddress)}.
     * </p>
     * 
     * @param attributeAddress
     *            specifiers which attributes to send (See {@link AttributeAddress})
     * @return single result from the meter.
     * 
     * @throws IOException
     *             if the connection breaks, while requesting.
     *             <p>
     *             May be of type {@link FatalJDlmsException} or {@link ResponseTimeoutException}
     *             </p>
     * 
     * @see #get(boolean, AttributeAddress)
     */
    GetResult get(AttributeAddress attributeAddress) throws IOException;

    /**
     * Requests the remote smart meter to send the values of several attributes.
     * 
     * 
     * @param priority
     *            if true: sends this request with high priority, if supported
     * @param attributeAddress
     *            specifiers which attributes to send (See {@link AttributeAddress})
     * 
     * @return single results from the smart meter in the same order as the requests
     * 
     * @throws IOException
     *             if the connection breaks, while requesting.
     *             <p>
     *             May be of type {@link FatalJDlmsException} or {@link ResponseTimeoutException}
     *             </p>
     * 
     * @see #get(boolean, List)
     */
    GetResult get(boolean priority, AttributeAddress attributeAddress) throws IOException;

    /**
     * Requests the remote smart meter to send the values of one or several attributes
     * 
     * @param priority
     *            if true: sends this request with high priority, if supported
     * @param params
     *            args of specifiers which attributes to send (See {@link AttributeAddress})
     * @return List of results from the smart meter in the same order as the requests
     * 
     * @throws IOException
     *             if the connection breaks, while requesting.
     *             <p>
     *             May be of type {@link FatalJDlmsException} or {@link ResponseTimeoutException}
     *             </p>
     * 
     */
    List<GetResult> get(boolean priority, List<AttributeAddress> params) throws IOException;

    /**
     * Requests the remote smart meter to set one attribute to the committed value.
     * 
     * <p>
     * Convenience method to call {@code set(false, SetParameter...)}.
     * </p>
     * 
     * @param params
     *            args of specifier which attributes to set to which values (See {@link SetParameter})
     * @return List of results from the smart meter in the same order as the requests or null if confirmed has been set
     *         to false on creation of this object. A true value indicates that this particular value has been
     *         successfully set
     * @throws IOException
     *             if the connection breaks, while requesting.
     *             <p>
     *             May be of type {@link FatalJDlmsException} or {@link ResponseTimeoutException}
     *             </p>
     * 
     * 
     * @see #set(boolean, List)
     */
    List<AccessResultCode> set(List<SetParameter> params) throws IOException;

    /**
     * Requests the remote smart meter to set one or several attributes to the committed values
     * 
     * @param priority
     *            Sends this request with high priority, if supported
     * @param params
     *            Varargs of specifier which attributes to set to which values (See {@link SetParameter})
     * @return List of results from the smart meter in the same order as the requests or null if confirmed has been set
     *         to false on creation of this object. A true value indicates that this particular value has been
     *         successfully set
     * @throws IOException
     *             if the connection breaks, while requesting.
     *             <p>
     *             May be of type {@link FatalJDlmsException} or {@link ResponseTimeoutException}
     *             </p>
     * 
     */
    List<AccessResultCode> set(boolean priority, List<SetParameter> params) throws IOException;

    /**
     * Requests the remote smart meter to set one attributes to the committed values.
     * 
     * @param priority
     *            Sends this request with high priority, if supported
     * @param setParameter
     *            Varargs of specifier which attributes to set to which values (See {@link SetParameter})
     * @return results from the smart meter in the same order as the requests or null if confirmed has been set to false
     *         on creation of this object. A true value indicates that this particular value has been successfully set
     * @throws IOException
     *             if the connection breaks, while requesting.
     *             <p>
     *             May be of type {@link FatalJDlmsException} or {@link ResponseTimeoutException}
     *             </p>
     * 
     */
    AccessResultCode set(boolean priority, SetParameter setParameter) throws IOException;

    /**
     * Requests the remote smart meter to set one or several attributes to the committed values.
     * 
     * <p>
     * Convenience method to call {@code set(false, SetParameter)}
     * </p>
     * 
     * @param setParameter
     *            attribute and values (see {@link SetParameter})
     * @return results from the smart meter in the same order as the requests or null if confirmed has been set to false
     *         on creation of this object. A true value indicates that this particular value has been successfully set
     * @throws IOException
     *             if the connection breaks, while requesting.
     *             <p>
     *             May be of type {@link FatalJDlmsException} or {@link ResponseTimeoutException}
     *             </p>
     * 
     */
    AccessResultCode set(SetParameter setParameter) throws IOException;

    /**
     * Requests the remote smart meter to call one methods with or without committed parameters.
     * 
     * @param priority
     *            Sends this request with high priority, if supported
     * 
     * @param methodParameter
     *            method to be called and, if needed, what parameters to call (See {@link MethodParameter}
     * @return results from the smart meter in the same order as the requests or null if confirmed has been set to false
     *         on creation of this object.
     * 
     * @throws IOException
     *             if the connection breaks, while requesting.
     *             <p>
     *             May be of type {@link FatalJDlmsException} or {@link ResponseTimeoutException}
     *             </p>
     */
    MethodResult action(boolean priority, MethodParameter methodParameter) throws IOException;

    /**
     * 
     * Requests the remote smart meter to call one methods with or without committed parameters.
     * <p>
     * Convenience method to call {@code action(false, methodParameter)}
     * </p>
     * 
     * @param methodParameter
     *            specifier which method to be called and, if needed, what parameters to call (See
     *            {@link MethodParameter}
     * @return results from the smart meter in the same order as the requests or null if confirmed has been set to false
     *         on creation of this object.
     * 
     * @throws IOException
     *             if the connection breaks, while requesting.
     *             <p>
     *             May be of type {@link FatalJDlmsException} or {@link ResponseTimeoutException}
     *             </p>
     */
    MethodResult action(MethodParameter methodParameter) throws IOException;

    /**
     * 
     * Convenience method to call {@code action(false, params)}
     * 
     * @param params
     *            List of specifier which methods to be called and, if needed, what parameters to call (See
     *            {@link MethodParameter}
     * 
     * @return List of results from the smart meter in the same order as the requests or null if confirmed has been set
     *         to false on creation of this object
     * 
     * @throws IOException
     *             if the connection breaks, while requesting.
     *             <p>
     *             May be of type {@link FatalJDlmsException} or {@link ResponseTimeoutException}
     *             </p>
     */
    List<MethodResult> action(List<MethodParameter> params) throws IOException;

    /**
     * Requests the remote smart meter to call one or several methods with or without committed parameters
     * 
     * @param priority
     *            Sends this request with high priority, if supported
     * @param params
     *            List of specifier which methods to be called and, if needed, what parameters to call (See
     *            {@link MethodParameter}
     * @return List of results from the smart meter in the same order as the requests or null if confirmed has been set
     *         to false on creation of this object
     * @throws IOException
     *             if the connection breaks, while requesting.
     *             <p>
     *             May be of type {@link FatalJDlmsException} or {@link ResponseTimeoutException}
     *             </p>
     */
    List<MethodResult> action(boolean priority, List<MethodParameter> params) throws IOException;

}
