package org.nuc.revedere.gateway;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.nuc.revedere.core.messages.Ping;
import org.nuc.revedere.core.messages.ack.Acknowledgement;
import org.nuc.revedere.core.messages.request.LoginRequest;
import org.nuc.revedere.core.messages.request.LogoutRequest;
import org.nuc.revedere.core.messages.request.RegisterRequest;
import org.nuc.revedere.core.messages.request.ReviewHistoricalRequest;
import org.nuc.revedere.core.messages.request.ReviewMarkAsSeenRequest;
import org.nuc.revedere.core.messages.request.ReviewUpdateRequest;
import org.nuc.revedere.core.messages.request.ReviewRequest;
import org.nuc.revedere.core.messages.request.ShortMessageEmptyBoxRequest;
import org.nuc.revedere.core.messages.request.ShortMessageHistoricalRequest;
import org.nuc.revedere.core.messages.request.ShortMessageMarkAsReadRequest;
import org.nuc.revedere.core.messages.request.ShortMessageSendRequest;
import org.nuc.revedere.core.messages.request.UnregisterRequest;

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

        if (message instanceof ShortMessageSendRequest) {
            listener.onShortMessageSendRequest((ShortMessageSendRequest) message, session);
        }

        if (message instanceof ShortMessageEmptyBoxRequest) {
            listener.onShortMessageEmptyBoxRequest((ShortMessageEmptyBoxRequest) message, session);
        }

        if (message instanceof ShortMessageHistoricalRequest) {
            listener.onShortMessageHistoricalRequest((ShortMessageHistoricalRequest) message, session);
        }

        if (message instanceof ShortMessageMarkAsReadRequest) {
            listener.onShortMessageMarkAsRead((ShortMessageMarkAsReadRequest) message, session);
        }

        if (message instanceof ReviewRequest) {
            listener.onRequestReview((ReviewRequest) message, session);
        }

        if (message instanceof ReviewMarkAsSeenRequest) {
            listener.onReviewMarkAsSeen((ReviewMarkAsSeenRequest) message, session);
        }
        
        if (message instanceof ReviewUpdateRequest) {
            listener.onReviewUpdate((ReviewUpdateRequest) message, session);
        }
        
        if (message instanceof ReviewHistoricalRequest) {
            listener.onReviewHistoricalRequest((ReviewHistoricalRequest) message, session);
        }

        if (message instanceof Ping) {
            listener.onPing(session);
            return;
        }

        try {
            @SuppressWarnings("unchecked")
            Acknowledgement<LoginRequest> acknowledgement = (Acknowledgement<LoginRequest>) message;
            listener.onAcknowledgement(acknowledgement, session);
            return;
        } catch (ClassCastException e) {
            // ignore this exception
        }
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        listener.onIdleSession(session);
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        listener.onClosedSession(session);
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        if (cause.getMessage().equals("An existing connection was forcibly closed by the remote host")) {
            listener.onClosedSession(session);
        }
    }
}
