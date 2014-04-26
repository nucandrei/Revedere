package org.nuc.revedere.gateway;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.nuc.revedere.core.messages.LoginRequest;
import org.nuc.revedere.core.messages.LogoutRequest;
import org.nuc.revedere.core.messages.Ping;
import org.nuc.revedere.core.messages.RegisterRequest;

public class ServerHandler extends IoHandlerAdapter {
    private final GatewayListener listener;

    public ServerHandler(GatewayListener listener) {
        this.listener = listener;
    }

    @Override
    public void messageReceived(IoSession session, Object message) {
        if (message instanceof LoginRequest) {
            listener.onLoginRequest((LoginRequest) message, session);
            return;
        }

        if (message instanceof RegisterRequest) {
            listener.onRegisterRequest((RegisterRequest) message, session);
            return;
        }

        if (message instanceof LogoutRequest) {
            listener.onLogoutRequest((LogoutRequest) message, session);
            return;
        }

        if (message instanceof Ping) {
            listener.onPing(session);
        }
    }

    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        listener.onIdleSession(session);
    }

    public void sessionClosed(IoSession session) throws Exception {
        listener.onClosedSession(session);
    }

}
