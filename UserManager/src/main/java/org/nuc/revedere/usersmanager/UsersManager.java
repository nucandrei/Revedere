package org.nuc.revedere.usersmanager;

import java.io.Serializable;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.nuc.revedere.core.messages.LoginRequest;
import org.nuc.revedere.core.messages.LogoutRequest;
import org.nuc.revedere.core.messages.RegisterRequest;
import org.nuc.revedere.core.messages.Response;
import org.nuc.revedere.core.messages.UnregisterRequest;
import org.nuc.revedere.core.messages.UserListRequest;
import org.nuc.revedere.service.core.Service;
import org.nuc.revedere.service.core.SupervisedService;
import org.nuc.revedere.service.core.Topics;

public class UsersManager extends SupervisedService {
    private final static String USERSMANAGER_SERVICE_NAME = "UsersManager";
    private final UsersHandler usersHandler = new UsersHandler(this);

    public UsersManager() throws Exception {
        super(USERSMANAGER_SERVICE_NAME);
        startListeningForUsersEvents();
    }

    private void startListeningForUsersEvents() throws Exception {
        super.setMessageListener(Topics.USERS_REQUEST_TOPIC, new MessageListener() {

            @Override
            public void onMessage(Message msg) {
                final ObjectMessage objectMessage = (ObjectMessage) msg;
                try {
                    Serializable message = objectMessage.getObject();
                    if (message instanceof LoginRequest) {
                        final LoginRequest loginRequest = (LoginRequest) message;
                        final Response<LoginRequest> response = usersHandler.login(loginRequest);
                        sendMessage(Topics.USERS_RESPONSE_TOPIC, response);
                        return;
                    }

                    if (message instanceof RegisterRequest) {
                        final RegisterRequest registerRequest = (RegisterRequest) message;
                        final Response<RegisterRequest> response = usersHandler.register(registerRequest);
                        sendMessage(Topics.USERS_RESPONSE_TOPIC, response);
                        return;
                    }

                    if (message instanceof UnregisterRequest) {
                        final UnregisterRequest unregisterRequest = (UnregisterRequest) message;
                        final Response<UnregisterRequest> response = usersHandler.unregister(unregisterRequest);
                        sendMessage(Topics.USERS_RESPONSE_TOPIC, response);
                        return;
                    }

                    if (message instanceof LogoutRequest) {
                        final LogoutRequest logoutRequest = (LogoutRequest) message;
                        usersHandler.logout(logoutRequest);
                    }

                    if (message instanceof UserListRequest) {
                        final UserListRequest userListRequest = (UserListRequest) message;
                        final Response<UserListRequest> response = usersHandler.listUsers(userListRequest);
                        sendMessage(Topics.USERS_RESPONSE_TOPIC, response);
                    }

                } catch (Exception e) {
                    LOGGER.error("Caught exception while processing received message ", e);
                }
            }
        });
    }

    public void sendUpdateToSubscribers(Serializable update) {
        try {
            sendMessage(Topics.USERS_TOPIC, update);
        } catch (Exception e) {
            LOGGER.error("Could not send update", e);
        }
    }

    public static void main(String[] args) {
        try {
            new UsersManager();
        } catch (Exception e) {
            Service.BACKUP_LOGGER.error("Could not start users manager", e);
        }
    }
}
