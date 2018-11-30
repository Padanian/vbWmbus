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

interface BaseConnection extends AutoCloseable {
    /**
     * Change the global authentication key used by the client.
     * 
     * @param key
     *            the new key
     */
    void changeClientGlobalAuthenticationKey(byte[] key);

    /**
     * Change the global encryption used by the client.
     * 
     * @param key
     *            the new key
     */
    void changeClientGlobalEncryptionKey(byte[] key);

    /**
     * Disconnects gracefully from the server.
     * 
     * @throws IOException
     *             if an I/O Exception occurs while closing
     */
    void disconnect() throws IOException;

    /**
     * Closes the connection.
     * 
     * @throws IOException
     *             if an I/O Exception occurs while closing
     */
    @Override
    public void close() throws IOException;
}
