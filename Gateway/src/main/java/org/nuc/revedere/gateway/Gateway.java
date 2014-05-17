package org.nuc.revedere.gateway;

import java.io.IOException;
import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.mina.core.session.IoSession;
import org.jdom2.JDOMException;
import org.nuc.revedere.core.messages.LoginRequest;
import org.nuc.revedere.core.messages.LogoutRequest;
import org.nuc.revedere.core.messages.Ping;
import org.nuc.revedere.core.messages.RegisterRequest;
import org.nuc.revedere.core.messages.Response;
import org.nuc.revedere.core.messages.UnregisterRequest;
import org.nuc.revedere.core.messages.UserListRequest;
import org.nuc.revedere.gateway.connectors.UsersManagerConnector;
import org.nuc.revedere.service.core.Service;
import org.nuc.revedere.service.core.SupervisedService;
import org.nuc.revedere.service.core.Topics;
import org.nuc.revedere.util.Convertor;

public class Gateway extends SupervisedService {
    private final static String GATEWAY_SERVICE_NAME = "Gateway";

    public Gateway() throws JDOMException, IOException, JMSException {
        super(GATEWAY_SERVICE_NAME);
        start();
    }

    private void start() throws IOException, JMSException {
        final UsersManagerConnector usersManagerConnector = new UsersManagerConnector(this);
        final SessionManager sessionManager = new SessionManager();

        final GatewayListener gatewayListener = new GatewayListener() {
            @Override
            public void onLoginRequest(LoginRequest request, IoSession session) {
                final Response<LoginRequest> response = usersManagerConnector.login(request);
                if (response.isSuccessfull()) {
                    sessionManager.setOnline(request.getUsername(), session);
                }
                session.write(response);
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
            public void onPing(IoSession session) {
                sessionManager.notePing(session);
            }
        };
        new MinaServer(new ServerHandler(gatewayListener));

        this.setMessageListener(Topics.USERS_TOPIC, new MessageListener() {
            public void onMessage(Message msg) {
                final ObjectMessage objectMessage = (ObjectMessage) msg;
                try {
                    Serializable message = objectMessage.getObject();
                    final Response<UserListRequest> convertedMessage = new Convertor<Response<UserListRequest>>().convert(message);
                    if (convertedMessage != null) {
                        sessionManager.broadcastMessage(convertedMessage);
                    }
                } catch (JMSException e) {
                    LOGGER.error("Caught exception while processing received message ", e);
                }
            }
        });
    }

    public static void main(String[] args) {
        try {
            new Gateway();
        } catch (JDOMException | IOException | JMSException e) {
            Service.BACKUP_LOGGER.error("Could not start gateway", e);
        }
    }
}
