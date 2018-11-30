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
package org.openmuc.jdlms.internal.transportlayer.server;

import org.openmuc.jdlms.ServerConnectionInfo;

public abstract class ServerConnectionInformationImpl implements ServerConnectionInfo {
    private Status status;
    private int clientId;
    private int logicalDeviceAddress;

    @Override
    public Status getConnectionStatus() {
        return this.status;
    }

    @Override
    public int getLogicalDeviceAddress() {
        return this.logicalDeviceAddress;
    }

    @Override
    public int getClientId() {
        return this.clientId;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public void setLogicalDeviceAddress(int logicalDeviceAddress) {
        this.logicalDeviceAddress = logicalDeviceAddress;
    }
}
