package org.nuc.revedere.gateway;

import java.io.Serializable;

import org.apache.mina.core.session.IoSession;
import org.nuc.revedere.core.User;
import org.nuc.revedere.core.messages.Ping;
import org.nuc.revedere.core.messages.Response;
import org.nuc.revedere.core.messages.ack.Acknowledgement;
import org.nuc.revedere.core.messages.request.LoginRequest;
import org.nuc.revedere.core.messages.request.LogoutRequest;
import org.nuc.revedere.core.messages.request.RegisterRequest;
import org.nuc.revedere.core.messages.request.ReviewMarkAsSeenRequest;
import org.nuc.revedere.core.messages.request.ReviewUpdateRequest;
import org.nuc.revedere.core.messages.request.ReviewRequest;
import org.nuc.revedere.core.messages.request.ShortMessageEmptyBoxRequest;
import org.nuc.revedere.core.messages.request.ShortMessageHistoricalRequest;
import org.nuc.revedere.core.messages.request.ShortMessageMarkAsReadRequest;
import org.nuc.revedere.core.messages.request.ShortMessageSendRequest;
import org.nuc.revedere.core.messages.request.UnregisterRequest;
import org.nuc.revedere.core.messages.request.UserListRequest;
import org.nuc.revedere.core.messages.update.ReviewUpdate;
import org.nuc.revedere.core.messages.update.ShortMessageUpdate;
import org.nuc.revedere.core.messages.update.UserListUpdate;
import org.nuc.revedere.gateway.connectors.UsersManagerConnector;
import org.nuc.revedere.service.core.BrokerMessageListener;
import org.nuc.revedere.service.core.JMSRequestor;
import org.nuc.revedere.service.core.JMSShouter;
import org.nuc.revedere.service.core.Service;
import org.nuc.revedere.service.core.RevedereService;
import org.nuc.revedere.service.core.Topics;
import org.nuc.revedere.util.Collector;
import org.nuc.revedere.util.Collector.CollectorListener;

public class Gateway extends RevedereService {
    private final static String GATEWAY_SERVICE_NAME = "Gateway";

    public Gateway() throws Exception {
        super(GATEWAY_SERVICE_NAME);
        start();
    }

    public void start() throws Exception {
        super.start(true, true, true);
        final UsersManagerConnector usersManagerConnector = new UsersManagerConnector(this);
        final SessionManager sessionManager = new SessionManager();

        final GatewayListener gatewayListener = new GatewayListener() {
            @Override
            public void onLoginRequest(LoginRequest request, IoSession session) {
                final Response<LoginRequest> response = usersManagerConnector.login(request);
                if (response.isSuccessfull()) {
                    sessionManager.setAwaitingAcknowledgement(request.getUsername(), session);
                }
                session.write(response);
            }

            @Override
            public void onAcknowledgement(Acknowledgement<LoginRequest> acknowledgement, IoSession session) {
                sessionManager.markReceivedAcknowledgement(session);
                usersManagerConnector.acknowledgeLogin(acknowledgement);
            }

            @Override
            public void onLogoutRequest(LogoutRequest request, IoSession session) {
                usersManagerConnector.logout(request);
                sessionManager.setOffine(session);
            }

            @Override
            public void onRegisterRequest(RegisterRequest request, IoSession session) {
                final Response<RegisterRequest> response = usersManagerConnector.register(request);
                session.write(response);
            }

            @Override
            public void onUnregisterRequest(UnregisterRequest request, IoSession session) {
                final Response<UnregisterRequest> response = usersManagerConnector.unregister(request);
                session.write(response);

            }

            @Override
            public void onIdleSession(IoSession session) {
                sessionManager.noteIdle(session);
                session.write(new Ping());

            }

            @Override
            public void onClosedSession(IoSession session) {
                final String connectedUser = sessionManager.getUserFromSession(session);
                if (connectedUser != null) {
                    usersManagerConnector.logout(new LogoutRequest(connectedUser));
                    sessionManager.setOffine(session);
                }
            }

            @Override
            public void onShortMessageSendRequest(ShortMessageSendRequest request, IoSession session) {
                final JMSRequestor<ShortMessageSendRequest> requestor = new JMSRequestor<ShortMessageSendRequest>(Gateway.this);
                final Response<ShortMessageSendRequest> response = requestor.request(Topics.SHORT_MESSAGE_TOPIC, request);
                final ShortMessageUpdate shortMessageUpdate = new ShortMessageUpdate(response.getRequest().getShortMessage().asList(), null);
                session.write(shortMessageUpdate);
                LOGGER.info("sent short message send request");
            }

            @Override
            public void onShortMessageEmptyBoxRequest(ShortMessageEmptyBoxRequest request, IoSession session) {
                final JMSRequestor<ShortMessageEmptyBoxRequest> requestor = new JMSRequestor<ShortMessageEmptyBoxRequest>(Gateway.this);
                final Response<ShortMessageEmptyBoxRequest> response = requestor.request(Topics.SHORT_MESSAGE_TOPIC, request);
                session.write(response);
            }

            @Override
            public void onShortMessageHistoricalRequest(ShortMessageHistoricalRequest request, IoSession session) {
                final JMSRequestor<ShortMessageHistoricalRequest> requestor = new JMSRequestor<ShortMessageHistoricalRequest>(Gateway.this);
                final Response<ShortMessageHistoricalRequest> response = requestor.request(Topics.SHORT_MESSAGE_TOPIC, request);
                session.write(response);
            }

            @Override
            public void onShortMessageMarkAsRead(ShortMessageMarkAsReadRequest request, IoSession session) {
                final JMSShouter<ShortMessageMarkAsReadRequest> shouter = new JMSShouter<ShortMessageMarkAsReadRequest>(Gateway.this);
                shouter.shout(Topics.SHORT_MESSAGE_TOPIC, request);
            }

            @Override
            public void onRequestReview(ReviewRequest request, IoSession session) {
                final JMSShouter<ReviewRequest> shouter = new JMSShouter<ReviewRequest>(Gateway.this);
                shouter.shout(Topics.REVIEW_TOPIC, request);
            }

            @Override
            public void onReviewMarkAsSeen(ReviewMarkAsSeenRequest request, IoSession session) {
                final JMSShouter<ReviewMarkAsSeenRequest> shouter = new JMSShouter<ReviewMarkAsSeenRequest>(Gateway.this);
                shouter.shout(Topics.REVIEW_TOPIC, request);
            }

            @Override
            public void onReviewUpdate(ReviewUpdateRequest request, IoSession session) {
                final JMSShouter<ReviewUpdateRequest> shouter = new JMSShouter<ReviewUpdateRequest>(Gateway.this);
                shouter.shout(Topics.REVIEW_TOPIC, request);
            }

            @Override
            public void onPing(IoSession session) {
                sessionManager.notePing(session);
            }
        };
        new MinaServer(new ServerHandler(gatewayListener));

        getUserCollector().addListener(new CollectorListener<UserListUpdate>() {
            @Override
            public void onUpdate(Collector<UserListUpdate> source, UserListUpdate update) {
                final Response<UserListRequest> dummyResponse = new Response<UserListRequest>(null, true, "");
                dummyResponse.attach(source.getCurrentState());
                sessionManager.broadcastMessage(dummyResponse);
            }
        });

        this.addMessageListener(Topics.SHORT_MESSAGE_TOPIC, new BrokerMessageListener() {
            @Override
            public void onMessage(Serializable message) {
                if (message instanceof ShortMessageUpdate) {
                    LOGGER.info("Received message update");
                    final ShortMessageUpdate shortMessageUpdate = (ShortMessageUpdate) message;
                    sessionManager.sendMessageIfOnline(shortMessageUpdate.getIntendedReceiver().getUsername(), shortMessageUpdate);
                }
            }
        });

        this.addMessageListener(Topics.REVIEW_TOPIC, new BrokerMessageListener() {
            @Override
            public void onMessage(Serializable message) {
                if (message instanceof ReviewUpdate) {
                    LOGGER.info("Received review update");
                    final ReviewUpdate reviewUpdate = (ReviewUpdate) message;
                    for (User intendedUser : reviewUpdate.getIntendedUsers()) {
                        sessionManager.sendMessageIfOnline(intendedUser.getUsername(), reviewUpdate);
                    }
                }
            }
        });
    }

    public static void main(String[] args) {
        try {
            new Gateway();
        } catch (Exception e) {
            Service.BACKUP_LOGGER.error("Could not start gateway", e);
        }
    }
}
