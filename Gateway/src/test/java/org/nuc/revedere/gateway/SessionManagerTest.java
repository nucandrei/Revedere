package org.nuc.revedere.gateway;

import java.io.Serializable;

import org.apache.mina.core.session.IoSession;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SessionManagerTest {
    private final static String USER_1 = "user_1";
    private final static String USER_2 = "user_2";

    private SessionManager sessionManager;

    @Before
    public void setUp() {
        this.sessionManager = new SessionManager();
    }

    @Test
    public void testAddAndCheck() {
        final IoSession session = mock(IoSession.class);
        this.sessionManager.setOnline(USER_1, session);

        assertTrue(sessionManager.isOnline(USER_1));
        assertEquals(USER_1, sessionManager.getUserFromSession(session));
    }

    @Test
    public void testRemoveUserAndCheck() {
        final IoSession session = mock(IoSession.class);
        this.sessionManager.setOnline(USER_1, session);

        this.sessionManager.setOffline(USER_1);
        assertFalse(this.sessionManager.isOnline(USER_1));
        assertNull(this.sessionManager.getUserFromSession(session));
    }

    @Test
    public void testRemoveSessionAndCheck() {
        final IoSession session = mock(IoSession.class);
        this.sessionManager.setOnline(USER_1, session);

        this.sessionManager.setOffine(session);
        assertFalse(this.sessionManager.isOnline(USER_1));
        assertNull(this.sessionManager.getUserFromSession(session));
    }

    @Test
    public void testSendMessageIfOnline_isOnline() {
        final IoSession session = mock(IoSession.class);
        this.sessionManager.setOnline(USER_1, session);

        final Serializable message = new String("message");
        this.sessionManager.sendMessageIfOnline(USER_1, message);
        verify(session).write(eq(message));
    }

    @Test
    public void testSendMessageIfOnline_wasOnlineButIsNowOffline() {
        final IoSession session = mock(IoSession.class);
        this.sessionManager.setOnline(USER_1, session);

        this.sessionManager.setOffline(USER_1);

        final Serializable message = new String("message");
        this.sessionManager.sendMessageIfOnline(USER_1, message);
        verify(session, never()).write(eq(message));
    }

    @Test
    public void testBroadcastMessage() {
        final IoSession user1session = mock(IoSession.class);
        final IoSession user2session = mock(IoSession.class);
        this.sessionManager.setOnline(USER_1, user1session);
        this.sessionManager.setOnline(USER_2, user2session);

        final Serializable message = new String("message");
        this.sessionManager.broadcastMessage(message);
        
        verify(user1session, times(1)).write(message);
        verify(user2session, times(1)).write(message);
    }
    
    @Test
    public void testPingMechanism_PingBackWhenIdleIsDetected() {
        final IoSession session = mock(IoSession.class);
        this.sessionManager.setOnline(USER_1, session);
        
        this.sessionManager.noteIdle(session);
        assertTrue(this.sessionManager.isOnline(USER_1));
        
        
        this.sessionManager.notePing(session);
        assertTrue(this.sessionManager.isOnline(USER_1));
    }
    
    @Test
    public void testPingMechanism_NoResponseWhenIdleIsDetected() {
        final IoSession session = mock(IoSession.class);
        this.sessionManager.setOnline(USER_1, session);
        
        this.sessionManager.noteIdle(session);
        assertTrue(this.sessionManager.isOnline(USER_1));
        
        
        this.sessionManager.noteIdle(session);
        assertFalse(this.sessionManager.isOnline(USER_1));
    }
}
