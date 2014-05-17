package org.nuc.revedere.usersmanager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.nuc.revedere.core.User;
import org.nuc.revedere.core.messages.LoginRequest;
import org.nuc.revedere.core.messages.LogoutRequest;
import org.nuc.revedere.core.messages.RegisterRequest;
import org.nuc.revedere.core.messages.Response;
import org.nuc.revedere.core.messages.UnregisterRequest;
import org.nuc.revedere.core.messages.UserListRequest;
import org.nuc.revedere.core.messages.update.UserListUpdate;

public class UsersHandler {
    private static final String USER_DOES_NOT_EXIST = "User does not exist";
    private static final String USER_ALREADY_EXISTS = "User already exists";
    private static final String AUTH_SUCCESS = "Authentification succedded";
    private static final String AUTH_FAILED = "Authentification failed";
    private static final String REGISTER_SUCCEDDED = "Register succedded";
    private static final String UNREGISTER_SUCCEDDED = "Unregister succedded";

    private static final String USERS_FILE = "users.xml";
    private static final Logger LOGGER = Logger.getLogger(UsersHandler.class.getName());

    private final Map<String, User> users = new HashMap<>();
    private final Set<User> connectedUsers = new HashSet<>();
    private final Set<User> disconnectedUsers = new HashSet<>();

    private final UsersManager parentManager;

    public UsersHandler(UsersManager parentManager) {
        this.parentManager = parentManager;
        loadUsersFromPersistence();
    }

    public Response<LoginRequest> login(LoginRequest request) {
        final String username = request.getUsername();
        final String authInfo = request.getAuthInfo();
        LOGGER.info(String.format("Received login request from \"%s\"", username));
        final User correspondingUser = users.get(username);

        if (correspondingUser == null) {
            return new Response<LoginRequest>(request, false, USER_DOES_NOT_EXIST);
        }

        if (correspondingUser.matchesAuthInfo(authInfo)) {
            connectedUsers.add(correspondingUser);
            disconnectedUsers.remove(correspondingUser);
            LOGGER.info(String.format("Authentification succedded for \"%s\"", username));
            notifySubscribers();
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

        if (connectedUsers.remove(user)) {
            notifySubscribers();
        } else {
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

    public Response<UnregisterRequest> unregister(UnregisterRequest request) {
        final String username = request.getUsername();
        final String authInfo = request.getAuthInfo();
        LOGGER.info(String.format("Received unregister request from \"%s\"", username));
        final User userToUnregister = users.get(username);
        if (userToUnregister == null) {
            return new Response<UnregisterRequest>(request, false, USER_DOES_NOT_EXIST);
        }
        if (userToUnregister.matchesAuthInfo(authInfo)) {
            users.remove(username);
            saveUsers();
            return new Response<UnregisterRequest>(request, true, UNREGISTER_SUCCEDDED);
        } else {
            return new Response<UnregisterRequest>(request, false, AUTH_FAILED);
        }
    }

    public Response<UserListRequest> listUsers(UserListRequest userListRequest) {
        final Set<User> onlineUsers = new HashSet<User>();
        final Set<User> offlineUsers = new HashSet<User>();
        for (User user : connectedUsers) {
            onlineUsers.add(user.getCleanInstance());
        }

        for (User user : disconnectedUsers) {
            offlineUsers.add(user.getCleanInstance());
        }

        final UserListUpdate userListUpdate = new UserListUpdate(false, onlineUsers, offlineUsers);
        final Response<UserListRequest> response = new Response<UserListRequest>(userListRequest, true, "");
        response.attach(userListUpdate);
        return response;
    }

    private void saveUsers() {
        final File usersFile = new File(USERS_FILE);
        final Element rootElement = new Element("users");
        final Document document = new Document(rootElement);
        for (User user : users.values()) {
            final Element userElement = new Element("user");
            userElement.setAttribute("username", user.getUsername());
            userElement.setAttribute("authinfo", user.getAuthInfo());
            rootElement.addContent(userElement);
        }

        final XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        try {
            outputter.output(document, new FileWriter(usersFile));
        } catch (IOException e) {
            LOGGER.error("Could not save users file", e);
        }

    }

    private void loadUsersFromPersistence() {
        final File usersFile = new File(USERS_FILE);
        if (!usersFile.exists()) {
            createEmptyUsersFile();
            return;
        }

        try {
            final Document document = new SAXBuilder().build(usersFile);
            final Element rootNode = document.getRootElement();
            for (Element e : rootNode.getChildren("user")) {
                final String username = e.getAttributeValue("username");
                final String authInfo = e.getAttributeValue("authinfo");
                final User newUser = new User(username, authInfo);
                users.put(username, newUser);
                disconnectedUsers.add(newUser);
                LOGGER.info(String.format("Loaded user \"%s\" ", username));
            }
            LOGGER.info("Loaded users manager persistence");
        } catch (JDOMException | IOException exception) {
            LOGGER.error("Error while loading the users file", exception);
        }
    }

    private void createEmptyUsersFile() {
        final File usersFile = new File(USERS_FILE);
        final Element rootElement = new Element("users");
        final Document document = new Document(rootElement);
        final XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        try {
            outputter.output(document, new FileWriter(usersFile));
        } catch (IOException e) {
            LOGGER.error("Could not create users file", e);
        }
    }

    private void notifySubscribers() {
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    parentManager.sendUpdateToSubscribers(listUsers(null));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

}
