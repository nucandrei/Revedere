package org.nuc.revedere.gateway;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.mina.core.session.IoSession;
import org.nuc.revedere.core.messages.ack.LoginAcknowledgement;
import org.nuc.revedere.core.messages.request.LoginRequest;
import org.nuc.revedere.core.messages.request.RegisterRequest;
import org.nuc.revedere.core.messages.request.Request;
import org.nuc.revedere.core.messages.request.UnregisterRequest;
import org.nuc.revedere.util.BidirectionMap;

public class SessionManager implements MessageAuthorizationService {
    private final BidirectionMap<String, IoSession> connectedUsers = new BidirectionMap<>();
    private final Map<IoSession, String> awaitingAcknowledgement = new HashMap<>();
    private final Set<IoSession> idleConnections = new HashSet<>();

    private final Set<Class<? extends Request>> requestsSpecificToNotLoggedInState = new HashSet<>();

    public SessionManager() {
        requestsSpecificToNotLoggedInState.add(LoginRequest.class);
        requestsSpecificToNotLoggedInState.add(RegisterRequest.class);
        requestsSpecificToNotLoggedInState.add(UnregisterRequest.class);
    }

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

    public void setAwaitingAcknowledgement(String user, IoSession session) {
        awaitingAcknowledgement.put(session, user);
    }

    public void markReceivedAcknowledgement(IoSession session) {
        final String correspondingUser = awaitingAcknowledgement.remove(session);
        connectedUsers.put(correspondingUser, session);
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

    @Override
    public boolean isAllowed(IoSession session, Object request) {
        if (awaitingAcknowledgement.containsKey(session)) {
            return request instanceof LoginAcknowledgement;
        }

        return connectedUsers.containsValue(session) != requestsSpecificToNotLoggedInState.contains(request.getClass());
    }
}
