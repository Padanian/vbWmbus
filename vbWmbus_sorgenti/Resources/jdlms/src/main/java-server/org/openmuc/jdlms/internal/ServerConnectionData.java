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
package org.openmuc.jdlms.internal;

import org.openmuc.jdlms.SecuritySuite;
import org.openmuc.jdlms.sessionlayer.server.ServerSessionLayer;

public class ServerConnectionData {
    private byte[] serverToClientChallenge;

    private byte[] clientToServerChallenge;

    private int clientId;

    private byte[] clientSystemTitle;

    private int frameCounter;

    private boolean authenticated;

    private long clientMaxReceivePduSize;

    private SecuritySuite securitySuite;

    private final ServerSessionLayer sessionLayer;

    private final Long connectionId;

    public ServerConnectionData(ServerSessionLayer sessionLayer, Long connectionId) {
        this.sessionLayer = sessionLayer;
        this.connectionId = connectionId;
        this.authenticated = false;
        this.frameCounter = 1;
        this.securitySuite = SecuritySuite.builder().build();
    }

    public SecuritySuite getSecuritySuite() {
        return securitySuite;
    }

    public int getClientId() {
        return clientId;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setSecuritySuite(SecuritySuite securitySuite) {
        this.securitySuite = securitySuite;
    }

    public Long getConnectionId() {
        return connectionId;
    }

    public ServerSessionLayer getSessionLayer() {
        return sessionLayer;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public byte[] getClientSystemTitle() {
        return clientSystemTitle;
    }

    public void setClientSystemTitle(byte[] clientSystemTitle) {
        this.clientSystemTitle = clientSystemTitle;
    }

    public int getAndIncrementFc() {
        return this.frameCounter++;
    }

    public int getFrameCounter() {
        return this.frameCounter;
    }

    public long getClientMaxReceivePduSize() {
        return clientMaxReceivePduSize;
    }

    public void setClientMaxReceivePduSize(long clientMaxReceivePduSize) {
        this.clientMaxReceivePduSize = clientMaxReceivePduSize;
    }

    public byte[] getClientToServerChallenge() {
        return clientToServerChallenge;
    }

    public void setClientToServerChallenge(byte[] clientToServerChallenge) {
        this.clientToServerChallenge = clientToServerChallenge;
    }

    public byte[] getServerToClientChallenge() {
        return serverToClientChallenge;
    }

    public void setServerToClientChallenge(byte[] serverToClientChallenge) {
        this.serverToClientChallenge = serverToClientChallenge;
    }

    public void setAuthenticated() {
        this.authenticated = true;
    }

}
