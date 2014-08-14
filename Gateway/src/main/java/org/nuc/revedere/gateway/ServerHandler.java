package org.nuc.revedere.gateway;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.nuc.revedere.core.messages.Ping;
import org.nuc.revedere.core.messages.Response;
import org.nuc.revedere.core.messages.ack.LoginAcknowledgement;
import org.nuc.revedere.core.messages.request.LoginRequest;
import org.nuc.revedere.core.messages.request.LogoutRequest;
import org.nuc.revedere.core.messages.request.RegisterRequest;
import org.nuc.revedere.core.messages.request.Request;
import org.nuc.revedere.core.messages.request.ReviewDocumentRequest;
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
    private static final Logger LOGGER = Logger.getLogger(ServerHandler.class);
    private final GatewayListener listener;
    private final MessageAuthorizationService authorizationService;

    public ServerHandler(GatewayListener listener, MessageAuthorizationService authorizationService) {
        this.listener = listener;
        this.authorizationService = authorizationService;
    }

    @Override
    public void messageReceived(IoSession session, Object message) {
        if (!authorizationService.isAllowed(session, message)) {
            if (!(message instanceof Request)) {
                LOGGER.warn("Received unwanted " + message.getClass() + " message");
                return;
            }
            final Response<? extends Request> response = new Response<>((Request) message, false, "Illegal request");
            session.write(response);
            LOGGER.warn("Received illegal request : " + message.getClass());
            return;
        }

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
            return;
        }

        if (message instanceof ShortMessageEmptyBoxRequest) {
            listener.onShortMessageEmptyBoxRequest((ShortMessageEmptyBoxRequest) message, session);
            return;
        }

        if (message instanceof ShortMessageHistoricalRequest) {
            listener.onShortMessageHistoricalRequest((ShortMessageHistoricalRequest) message, session);
            return;
        }

        if (message instanceof ShortMessageMarkAsReadRequest) {
            listener.onShortMessageMarkAsRead((ShortMessageMarkAsReadRequest) message, session);
            return;
        }

        if (message instanceof ReviewRequest) {
            listener.onRequestReview((ReviewRequest) message, session);
            return;
        }

        if (message instanceof ReviewMarkAsSeenRequest) {
            listener.onReviewMarkAsSeen((ReviewMarkAsSeenRequest) message, session);
            return;
        }

        if (message instanceof ReviewUpdateRequest) {
            listener.onReviewUpdate((ReviewUpdateRequest) message, session);
            return;
        }

        if (message instanceof ReviewHistoricalRequest) {
            listener.onReviewHistoricalRequest((ReviewHistoricalRequest) message, session);
            return;
        }

        if (message instanceof Ping) {
            listener.onPing(session);
            return;
        }

        if (message instanceof LoginAcknowledgement) {
            listener.onAcknowledgement((LoginAcknowledgement) message, session);
            return;
        }

        if (message instanceof ReviewDocumentRequest) {
            listener.onReviewDocumentRequest((ReviewDocumentRequest) message, session);
            return;
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
