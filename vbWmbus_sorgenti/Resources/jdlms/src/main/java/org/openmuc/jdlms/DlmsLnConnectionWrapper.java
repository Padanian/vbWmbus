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

import org.openmuc.jdlms.sessionlayer.client.SessionLayer;
import org.openmuc.jdlms.settings.client.Settings;

class DlmsLnConnectionWrapper extends LnConnectionWrapper {

    private final DlmsLnConnectionImpl connection;

    @Override
    protected void connect() throws IOException {
        this.connection.connect();
    }

    public DlmsLnConnectionWrapper(Settings settings, SessionLayer sessionlayer) throws IOException {
        connection = new DlmsLnConnectionImpl(settings, sessionlayer);
    }

    @Override
    public List<GetResult> get(boolean priority, List<AttributeAddress> params) throws IOException {
        return getWrappedConnection().get(priority, params);
    }

    @Override
    public List<AccessResultCode> set(boolean priority, List<SetParameter> params) throws IOException {
        return getWrappedConnection().set(priority, params);
    }

    @Override
    public List<MethodResult> action(boolean priority, List<MethodParameter> params) throws IOException {
        return getWrappedConnection().action(priority, params);
    }

    @Override
    protected DlmsLnConnectionImpl getWrappedConnection() {
        return this.connection;
    }

}
