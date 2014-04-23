package org.nuc.revedere.usersmanager;

import java.io.Serializable;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.nuc.revedere.service.core.SupervisedService;
import org.nuc.revedere.service.core.Topics;
import org.nuc.revedere.service.core.messages.LoginRequest;
import org.nuc.revedere.service.core.messages.LogoutRequest;
import org.nuc.revedere.service.core.messages.RegisterRequest;
import org.nuc.revedere.service.core.messages.Response;
import org.nuc.revedere.service.core.messages.UserListRequest;

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
                        LoginRequest loginRequest = (LoginRequest) message;
                        Response<LoginRequest> response = usersHandler.login(loginRequest);
                        sendMessage(Topics.USERS_RESPONSE_TOPIC, response);
                        return;
                    }

                    if (message instanceof RegisterRequest) {
                        RegisterRequest registerRequest = (RegisterRequest) message;
                        Response<RegisterRequest> response = usersHandler.register(registerRequest);
                        sendMessage(Topics.USERS_RESPONSE_TOPIC, response);
                        return;
                    }

                    if (message instanceof LogoutRequest) {
                        LogoutRequest logoutRequest = (LogoutRequest) message;
                        usersHandler.logout(logoutRequest);
                    }

                    if (message instanceof UserListRequest) {
                        UserListRequest userListRequest = (UserListRequest) message;
                        Response<UserListRequest> response = usersHandler.listUsers(userListRequest);
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

    public static void main(String[] args) throws Exception {
        new UsersManager();
    }
}
