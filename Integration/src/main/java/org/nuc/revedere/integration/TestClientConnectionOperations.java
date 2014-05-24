package org.nuc.revedere.integration;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.nuc.revedere.client.RevedereConnector;

public class TestClientConnectionOperations {

    private final int TEST_PORT = 6045;
    private final String GENERATED_USERNAME = String.format("user_%d", System.currentTimeMillis());
    private final String PASSWORD = "p@ssW0rD";
    private final String WRONG_PASSWORD = PASSWORD + "___";

    private RevedereConnector revedereConnector;

    @Before
    public void setUp() throws Exception {
        revedereConnector = new RevedereConnector(String.format("%s:%d", "127.0.0.1", TEST_PORT));
    }

    @Test
    public void testTryLoginWithoutRegister() throws Exception {
        try {
            revedereConnector.login(GENERATED_USERNAME, PASSWORD);
            fail();
        } catch (Exception e) {
            assertEquals("User does not exist", e.getMessage());
        }
    }

    @Test
    public void testRegisterLogin() {
        final String response = revedereConnector.register(GENERATED_USERNAME, PASSWORD);
        assertEquals("Register succedded", response);
        removeUser(GENERATED_USERNAME, PASSWORD);
    }

    private void removeUser(String username, String password) {
        final String response = revedereConnector.unregister(GENERATED_USERNAME, PASSWORD);
        assertEquals("Unregister succedded", response);
    }
}
