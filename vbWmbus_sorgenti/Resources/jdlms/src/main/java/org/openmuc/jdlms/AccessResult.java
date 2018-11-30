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

import org.openmuc.jdlms.datatypes.DataObject;

interface AccessResult {

    /**
     * Returns the data of return data of the request.
     * <p>
     * NOTE: if the value of {@linkplain #getResultCode()} is not {@linkplain AccessResultCode#SUCCESS}, the result data
     * is <code>null</code>.
     * </p>
     * 
     * @return returns the data of return data
     */
    DataObject getResultData();

    /**
     * The result code of the the request.
     * 
     * @return The result code of the get operation
     */
    AccessResultCode getResultCode();

    /**
     * The success of preceding request.
     * 
     * @return {@code true} if it was successful. If {@code false}, {@linkplain #getResultData()} returns {@code null}.
     */
    boolean requestSuccessful();
}
