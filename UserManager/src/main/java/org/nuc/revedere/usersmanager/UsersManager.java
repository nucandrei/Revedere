package org.nuc.revedere.usersmanager;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.nuc.distry.service.DistryListener;
import org.nuc.distry.service.ServiceConfiguration;
import org.nuc.distry.service.messaging.ActiveMQAdapter;
import org.nuc.revedere.core.messages.Response;
import org.nuc.revedere.core.messages.ack.Acknowledgement;
import org.nuc.revedere.core.messages.request.LoginRequest;
import org.nuc.revedere.core.messages.request.LogoutRequest;
import org.nuc.revedere.core.messages.request.RegisterRequest;
import org.nuc.revedere.core.messages.request.UnregisterRequest;
import org.nuc.revedere.core.messages.request.UserListRequest;
import org.nuc.revedere.service.core.RevedereService;
import org.nuc.revedere.service.core.SupervisorTopics;
import org.nuc.revedere.service.core.Topics;

public class UsersManager extends RevedereService {
    private static final Logger LOGGER = Logger.getLogger(UsersManager.class);
    private final static String USERSMANAGER_SERVICE_NAME = "UsersManager";
    private final UsersHandler usersHandler = new UsersHandler(this);

    public UsersManager(ServiceConfiguration serviceConfiguration) throws Exception {
        super(USERSMANAGER_SERVICE_NAME, serviceConfiguration);
        super.start(false);
        startListeningForUsersEvents();
    }

    private void startListeningForUsersEvents() throws Exception {
        super.addMessageListener(Topics.USERS_REQUEST_TOPIC, new DistryListener() {
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
            final String serverAddress = parseArguments(args);
            final ServiceConfiguration serviceConfiguration = new ServiceConfiguration(new ActiveMQAdapter(serverAddress), true, 10000, SupervisorTopics.HEARTBEAT_TOPIC, true, SupervisorTopics.COMMAND_TOPIC, SupervisorTopics.PUBLISH_TOPIC);
            new UsersManager(serviceConfiguration);

        } catch (Exception e) {
            LOGGER.error("Failed to start users manager", e);
        }
    }
}
