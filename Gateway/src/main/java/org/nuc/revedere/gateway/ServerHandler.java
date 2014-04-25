package org.nuc.revedere.gateway;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.nuc.revedere.core.messages.LoginRequest;
import org.nuc.revedere.core.messages.LogoutRequest;
import org.nuc.revedere.core.messages.RegisterRequest;
import org.nuc.revedere.core.messages.UnregisterRequest;

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

        if (message instanceof UnregisterRequest) {
            listener.onUnregisterRequest((UnregisterRequest) message, session);
            return;
        }

        if (message instanceof LogoutRequest) {
            listener.onLogoutRequest((LogoutRequest) message, session);
            return;
        }
    }
}
