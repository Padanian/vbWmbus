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
 * DLMS connection class providing methods to directly access SN services READ and WRITE.
 * <p>
 * SN meters can also be accessed using the {@linkplain DlmsConnection}. In this case LN to SN service mapping is
 * applied.
 * </p>
 * 
 * @see DlmsConnection
 * @see ConnectionBuilder#buildSnConnection()
 */
public interface DlmsSnConnection extends BaseConnection {

    /**
     * Read a value from a COSEM IC object attribute or invoke a IC method (only when a return value is expected).
     * 
     * @param varAddressSpec
     *            the "Variable Access Specification" of the COSEM attribute or method.
     * @return the read result. If the service invocation failed, or the IC method did not return any value, the result
     *         won't contain data.
     * @throws IOException
     *             if an IO exception occurred on invocation of the read service.
     */
    ReadResult read(SnAddressSpec varAddressSpec) throws IOException;

    /**
     * Read multiple values from a COSEM IC object attribute or invoke multiple IC method (only when a return value is
     * expected).
     * 
     * @param varAddressSpecs
     *            a list of "Variable Access Specification" of the COSEM attribute or method.
     * @return the read result. If the service invocation failed, or the IC method did not return any value, the result
     *         won't contain data.
     * @throws IOException
     *             if an IO exception occurred on invocation of the read service.
     */
    List<ReadResult> read(List<SnAddressSpec> varAddressSpecs) throws IOException;

    /**
     * Write a value to a COSEM IC object attribute or invoke a IC method .
     * 
     * @param writeParameter
     *            method/attribute address and data.
     * @return the result code resulting the write service invocation.
     * @throws IOException
     *             if an IO exception occurred on invocation of the read service.
     */
    AccessResultCode write(SnWriteParameter writeParameter) throws IOException;

    /**
     * Write multiple COSEM IC object attributes or invoke multiple IC method .
     * 
     * @param writeParameters
     *            set of method/attribute address and data.
     * @return list of result code resulting the write service invocation.
     * @throws IOException
     *             if an IO exception occurred on invocation of the read service.
     */
    List<AccessResultCode> write(List<SnWriteParameter> writeParameters) throws IOException;

}
