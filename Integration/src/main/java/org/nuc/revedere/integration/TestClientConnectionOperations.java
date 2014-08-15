package org.nuc.revedere.integration;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.nuc.revedere.client.RevedereConnector;

public class TestClientConnectionOperations {

    private static final int TEST_PORT = 6045;
    private static final String GENERATED_USERNAME = String.format("user_%d", System.currentTimeMillis());
    private static final String PASSWORD = "p@ssW0rD";
    private static final String EMAIL_ADDRESS = "test@test.com";
    private RevedereConnector revedereConnector;

    @Before
    public void setUp() throws Exception {
        revedereConnector = new RevedereConnector(String.format("%s:%d", "127.0.0.1", TEST_PORT));
    }

    @Test
    public void testTryLoginWithoutRegister() throws Exception {
        try {
            revedereConnector.login(GENERATED_USERNAME, PASSWORD, "Integration");
            fail();
        } catch (Exception e) {
            assertEquals("User does not exist", e.getMessage());
        }
    }

    @Test
    public void testRegisterLogin() {
        final String response = revedereConnector.register(GENERATED_USERNAME, PASSWORD, GENERATED_USERNAME, true, EMAIL_ADDRESS, true);
        assertEquals("Register succedded", response);
        removeUser(GENERATED_USERNAME, PASSWORD);
    }

    private void removeUser(String username, String password) {
        final String response = revedereConnector.unregister(GENERATED_USERNAME, PASSWORD);
        assertEquals("Unregister succedded", response);
    }
}
