package org.nuc.revedere.gateway.usersmanager;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.nuc.distry.service.DistryListener;
import org.nuc.distry.service.Service;
import org.nuc.revedere.core.messages.Response;
import org.nuc.revedere.core.messages.ack.LoginAcknowledgement;
import org.nuc.revedere.core.messages.request.LoginRequest;
import org.nuc.revedere.core.messages.request.LogoutRequest;
import org.nuc.revedere.core.messages.request.RegisterRequest;
import org.nuc.revedere.core.messages.request.UnregisterRequest;
import org.nuc.revedere.core.messages.request.UserListRequest;
import org.nuc.revedere.gateway.SessionManager;
import org.nuc.revedere.service.core.Topics;

public class UsersManager {
    private static final Logger LOGGER = Logger.getLogger(UsersManager.class);
    private final Service supportService;
    private final UsersHandler usersHandler = new UsersHandler(this);
    private SessionManager sessionManager;

    public UsersManager(Service supportService, SessionManager sessionManager) throws Exception {
        this.supportService = supportService;
        this.sessionManager = sessionManager;
        startListeningForUsersEvents();
    }

    public Response<LoginRequest> login(LoginRequest request) {
        return usersHandler.login(request);
    }

    public Response<RegisterRequest> register(RegisterRequest request) {
        return usersHandler.register(request);
    }

    public Response<UnregisterRequest> unregister(UnregisterRequest request) {
        return usersHandler.unregister(request);
    }

    public void logout(LogoutRequest request) {
        usersHandler.logout(request);
    }

    public void acknowledgeLogin(LoginAcknowledgement ack) {
        usersHandler.ack(ack);
    }

    public void sendUpdateToSubscribers(Serializable update) {
        try {
            supportService.sendMessage(Topics.USERS_TOPIC, update);
            sessionManager.broadcastMessage(update);
        } catch (Exception e) {
            LOGGER.error("Could not send update", e);
        }
    }

    private void startListeningForUsersEvents() throws Exception {
        supportService.addMessageListener(Topics.USERS_REQUEST_TOPIC, new DistryListener() {
            @Override
            public void onMessage(Serializable message) {
                try {
                    if (message instanceof LoginRequest) {
                        final LoginRequest loginRequest = (LoginRequest) message;
                        final Response<LoginRequest> response = usersHandler.login(loginRequest);
                        supportService.sendMessage(Topics.USERS_RESPONSE_TOPIC, response);
                        return;
                    }

                    if (message instanceof RegisterRequest) {
                        final RegisterRequest registerRequest = (RegisterRequest) message;
                        final Response<RegisterRequest> response = usersHandler.register(registerRequest);
                        supportService.sendMessage(Topics.USERS_RESPONSE_TOPIC, response);
                        return;
                    }

                    if (message instanceof UnregisterRequest) {
                        final UnregisterRequest unregisterRequest = (UnregisterRequest) message;
                        final Response<UnregisterRequest> response = usersHandler.unregister(unregisterRequest);
                        supportService.sendMessage(Topics.USERS_RESPONSE_TOPIC, response);
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
                        supportService.sendMessage(Topics.USERS_RESPONSE_TOPIC, response);
                    }

                    if (message instanceof LoginAcknowledgement) {
                        final LoginAcknowledgement acknowledgement = (LoginAcknowledgement) message;
                        usersHandler.ack(acknowledgement);
                    }
                    
                } catch (Exception e) {
                    LOGGER.error("Caught exception while processing received message ", e);
                }
            }
        });
    }
}
