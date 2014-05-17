package org.nuc.revedere.gateway;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.apache.mina.core.session.IoSession;
import org.nuc.revedere.util.BidirectionMap;

public class SessionManager {
    private final BidirectionMap<String, IoSession> connectedUsers = new BidirectionMap<>();
    private final Set<IoSession> idleConnections = new HashSet<>();

    public void setOnline(String user, IoSession activeSession) {
        connectedUsers.put(user, activeSession);
    }

    public boolean isOnline(String user) {
        return connectedUsers.containsKey(user);
    }

    public void setOffline(String user) {
        connectedUsers.removeKey(user);
    }

    public void setOffine(IoSession session) {
        connectedUsers.removeValue(session);
    }

    public void sendMessageIfOnline(String user, Serializable message) {
        final IoSession correspondingSession = connectedUsers.getValue(user);
        if (correspondingSession != null) {
            correspondingSession.write(message);
        }
    }

    public String getUserFromSession(IoSession session) {
        return connectedUsers.getKey(session);
    }

    public void broadcastMessage(Serializable serializable) {
        for (IoSession activeSession : connectedUsers.values()) {
            activeSession.write(serializable);
        }
    }

    public void noteIdle(IoSession session) {
        if (idleConnections.contains(session)) {
            connectedUsers.removeValue(session);
            idleConnections.remove(session);
        } else {
            idleConnections.add(session);
        }
    }

    public void notePing(IoSession session) {
        idleConnections.remove(session);
    }
}