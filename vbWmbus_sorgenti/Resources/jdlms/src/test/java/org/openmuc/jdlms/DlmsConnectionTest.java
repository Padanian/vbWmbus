package org.openmuc.jdlms;

import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.openmuc.jdlms.settings.client.Settings;

public class DlmsConnectionTest {

    @Test
    public void testChangeClientGlobalAuthenticationKey() throws Exception {
        Settings settings = mock(Settings.class);

        @SuppressWarnings("resource")
        BaseDlmsConnection con = new DlmsLnConnectionImpl(settings, null);
        con.changeClientGlobalAuthenticationKey(new byte[10]);
        verify(settings, Mockito.times(1)).updateAuthenticationKey(Matchers.any(byte[].class));

    }

}
