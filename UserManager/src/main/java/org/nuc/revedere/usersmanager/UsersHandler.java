package org.nuc.revedere.usersmanager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.nuc.revedere.core.User;
import org.nuc.revedere.service.core.messages.LoginRequest;
import org.nuc.revedere.service.core.messages.LogoutRequest;
import org.nuc.revedere.service.core.messages.RegisterRequest;
import org.nuc.revedere.service.core.messages.Response;

public class UsersHandler {
    private static final String USER_DOES_NOT_EXIST = "User does not exist";
    private static final String USER_ALREADY_EXISTS = "User already exists";
    private static final String AUTH_SUCCESS = "Authentification succedded";
    private static final String AUTH_FAILED = "Authentification failed";
    private static final String REGISTER_SUCCEDDED = "Register succedded";

    private final Map<String, User> users = new HashMap<>();
    private final Set<User> connectedUsers = new HashSet<>();
    private final Set<User> disconnectedUsers = new HashSet<>();
    private static final Logger LOGGER = Logger.getLogger(UsersHandler.class.getName());

    public UsersHandler() {
        loadUsersFromPersistence();
    }

    public Response<LoginRequest> login(LoginRequest request) {
        final String username = request.getUsername();
        final String authInfo = request.getAuthInfo();
        LOGGER.info(String.format("Received login request from \"%s\"", username));
        User correspondingUser = users.get(username);

        if (correspondingUser == null) {
            return new Response<LoginRequest>(request, false, USER_DOES_NOT_EXIST);
        }

        if (correspondingUser.matchesAuthInfo(authInfo)) {
            connectedUsers.add(correspondingUser);
            disconnectedUsers.remove(correspondingUser);
            LOGGER.info(String.format("Authentification succedded for \"%s\"", username));
            return new Response<LoginRequest>(request, true, AUTH_SUCCESS);
        } else {
            LOGGER.info(String.format("Authentification failed for \"%s\"", username));
            return new Response<LoginRequest>(request, false, AUTH_FAILED);
        }
    }

    public void logout(LogoutRequest request) {
        final String username = request.getUsername();
        LOGGER.info(String.format("Received logout request from \"%s\"", username));
        User user = users.get(username);
        if (user == null) {
            LOGGER.error(String.format("Unknown user \"%s\"", username));
            return;
        }

        if (!connectedUsers.remove(user)) {
            LOGGER.error(String.format("User \"%s\" was already disconnected", username));
        }
        disconnectedUsers.add(user);
    }

    public Response<RegisterRequest> register(RegisterRequest request) {
        final String username = request.getUsername();
        final String authInfo = request.getAuthInfo();
        LOGGER.info(String.format("Received register request from \"%s\"", username));
        if (users.containsKey(username)) {
            LOGGER.info(String.format("User \"%s\" is already taken", username));
            return new Response<RegisterRequest>(request, false, USER_ALREADY_EXISTS);
        }

        users.put(username, new User(username, authInfo));
        saveUsers();
        LOGGER.info(String.format("User \"%s\" registered succesfully", username));
        return new Response<RegisterRequest>(request, true, REGISTER_SUCCEDDED);
    }

    private void saveUsers() {
        // TODO Auto-generated method stub

    }

    private void loadUsersFromPersistence() {
        // TODO Auto-generated method stub

    }
}
