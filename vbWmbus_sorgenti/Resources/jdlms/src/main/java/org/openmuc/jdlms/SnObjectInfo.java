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
import java.util.Map;

/**
 * This class represents the mapping between the instance ID (Obis) and base name (object name) of a COSEM SN object.
 * 
 * @see ConnectionBuilder#setLnToSnMapping(java.util.Collection)
 * @see SnClassInfo
 */
public class SnObjectInfo {

    private final int baseName;
    private final ObisCode instanceId;
    private final SnClassVersion snClassVersion;

    /**
     * Create a new SN object info.
     * 
     * @param baseName
     *            the base name of the object. The base name is referring to the instance ID.
     *            <p>
     *            Note: the base name is a
     *            </p>
     * @param instanceId
     *            the instance id.
     * @param snClassVersion
     *            the class version pair.
     */
    public SnObjectInfo(int baseName, ObisCode instanceId, SnClassVersion snClassVersion) {
        this.baseName = baseName;
        this.instanceId = instanceId;
        this.snClassVersion = snClassVersion;
    }

    /**
     * Get the tuple of class ID and version.
     * 
     * @return the tuple of class ID and version.
     */
    public SnClassVersion getSnClassVersion() {
        return snClassVersion;
    }

    /**
     * Get the base name of the SN object.
     * 
     * @return the base name.
     */
    public int getBaseName() {
        return baseName;
    }

    /**
     * Get the instance ID of the object.
     * 
     * @return the instance ID of the object.
     */
    public ObisCode getInstanceId() {
        return instanceId;
    }

    @Override
    public String toString() {
        String format = "{\"class-version\": %s, \"instance-id\": %s, \"base-name\": \"0x%02X\"}";
        return String.format(format, this.snClassVersion, this.instanceId, this.baseName);
    }

    /**
     * Retrieve the LN to SN mapping from the current connection.
     * 
     * @param connection
     *            a DlmsConnection using short naming.
     * @return the LN -&gt; SN mapping.
     * @throws IOException
     *             if the connection is no a short name connection or an error occurs.
     */
    public static Map<ObisCode, SnObjectInfo> retrieveLnToSnMappingFrom(DlmsConnection connection) throws IOException {
        if (!(connection instanceof DlmsLnToSnConnectionWrapper)) {
            throw new IOException("This operation in only available if you're using an SN connection.");
        }

        DlmsLnToSnConnectionWrapper snConnection = (DlmsLnToSnConnectionWrapper) connection;

        return snConnection.getLnSnInfoMapping();
    }
}
