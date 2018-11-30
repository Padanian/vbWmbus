package org.openmuc.jdlms;

import org.junit.Test;

public class AuthenticationMechanismTest {

    @Test(expected = IllegalArgumentException.class)
    public void testFailedIdConversion() throws Exception {
        AuthenticationMechanism.forId(-1);
    }
}
