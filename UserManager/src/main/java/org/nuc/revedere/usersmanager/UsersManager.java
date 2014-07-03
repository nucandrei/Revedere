package org.nuc.revedere.usersmanager;

import java.io.Serializable;

import org.nuc.revedere.core.messages.Response;
import org.nuc.revedere.core.messages.ack.Acknowledgement;
import org.nuc.revedere.core.messages.request.LoginRequest;
import org.nuc.revedere.core.messages.request.LogoutRequest;
import org.nuc.revedere.core.messages.request.RegisterRequest;
import org.nuc.revedere.core.messages.request.UnregisterRequest;
import org.nuc.revedere.core.messages.request.UserListRequest;
import org.nuc.revedere.service.core.BrokerMessageListener;
import org.nuc.revedere.service.core.Service;
import org.nuc.revedere.service.core.RevedereService;
import org.nuc.revedere.service.core.Topics;

public class UsersManager extends RevedereService {
    private static final String SETTINGS_PATH = "UsersManager.xml";
    private final static String USERSMANAGER_SERVICE_NAME = "UsersManager";
    private final UsersHandler usersHandler = new UsersHandler(this);

    public UsersManager() throws Exception {
        super(USERSMANAGER_SERVICE_NAME, SETTINGS_PATH);
        super.start(true, true, false);

        startListeningForUsersEvents();
    }

    private void startListeningForUsersEvents() throws Exception {
        super.addMessageListener(Topics.USERS_REQUEST_TOPIC, new BrokerMessageListener() {
            @Override
            public void onMessage(Serializable message) {
                try {
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
                        return;
                    }

                    if (message instanceof UserListRequest) {
                        final UserListRequest userListRequest = (UserListRequest) message;
                        final Response<UserListRequest> response = usersHandler.listUsers(userListRequest);
                        sendMessage(Topics.USERS_RESPONSE_TOPIC, response);
                    }

                    try {
                        @SuppressWarnings("unchecked")
                        Acknowledgement<LoginRequest> acknowledgement = (Acknowledgement<LoginRequest>) message;
                        usersHandler.ack(acknowledgement);
                        return;
                    } catch (ClassCastException e) {
                        // ignore this exception
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
