package org.nuc.revedere.core;
import org.junit.Test;
import org.nuc.revedere.core.User;

import static org.junit.Assert.*;

public class UserTest {

    private static final String USERNAME = "username";
    private static final String AUTH_INFO = "authinfo123";
    private static final String ANOTHER_AUTH_INFO = "authinfo1234";

    @Test
    public void testUser() {
        User user = new User(USERNAME, AUTH_INFO);
        assertEquals(USERNAME, user.getUsername());
        assertTrue(user.matchesAuthInfo(AUTH_INFO));
        assertFalse(user.matchesAuthInfo(ANOTHER_AUTH_INFO));
    }
}