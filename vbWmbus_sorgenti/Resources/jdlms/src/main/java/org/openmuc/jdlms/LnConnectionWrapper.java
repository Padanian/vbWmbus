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
import java.util.Arrays;
import java.util.List;

abstract class LnConnectionWrapper implements DlmsConnection {

    protected abstract BaseConnection getWrappedConnection();

    protected abstract void connect() throws IOException;

    @Override
    public GetResult get(AttributeAddress attributeAddress) throws IOException {
        return get(false, attributeAddress);
    }

    @Override
    public List<GetResult> get(List<AttributeAddress> attributeAddress) throws IOException {
        return get(false, attributeAddress);
    }

    @Override
    public GetResult get(boolean priority, AttributeAddress attributeAddress) throws IOException {
        return get(priority, Arrays.asList(attributeAddress)).get(0);
    }

    @Override
    public AccessResultCode set(SetParameter setParameter) throws IOException {
        return set(false, setParameter);
    }

    @Override
    public AccessResultCode set(boolean priority, SetParameter setParameter) throws IOException {
        return set(priority, Arrays.asList(setParameter)).get(0);
    }

    @Override
    public List<AccessResultCode> set(List<SetParameter> params) throws IOException {
        return set(false, params);
    }

    @Override
    public MethodResult action(boolean priority, MethodParameter methodParameter) throws IOException {
        return action(priority, Arrays.asList(methodParameter)).get(0);
    }

    @Override
    public MethodResult action(MethodParameter methodParameter) throws IOException {
        return action(false, methodParameter);
    }

    @Override
    public List<MethodResult> action(List<MethodParameter> params) throws IOException {
        return action(false, params);
    }

    @Override
    public void changeClientGlobalAuthenticationKey(byte[] key) {
        getWrappedConnection().changeClientGlobalAuthenticationKey(key);
    }

    @Override
    public void changeClientGlobalEncryptionKey(byte[] key) {
        getWrappedConnection().changeClientGlobalEncryptionKey(key);

    }

    @Override
    public void disconnect() throws IOException {
        getWrappedConnection().disconnect();
    }

    @Override
    public void close() throws IOException {
        getWrappedConnection().close();
    }
}
