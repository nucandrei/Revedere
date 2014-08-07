package org.nuc.revedere.client.persistence;

import java.util.ArrayList;
import java.util.List;

import org.nuc.revedere.core.User;
import org.nuc.revedere.core.UserCollector;
import org.nuc.revedere.core.messages.update.UserListUpdate;
import org.nuc.revedere.util.Collector.CollectorListener;

public class UsersHandler {
    private final UserCollector userCollector;
    private final User currentUser;

    public UsersHandler(User currentUser) {
        this.userCollector = new UserCollector();
        this.currentUser = currentUser;
    }

    public void addListenerToChange(CollectorListener<UserListUpdate> listener) {
        this.userCollector.addListener(listener);
    }

    public void removeListenerToChange(CollectorListener<UserListUpdate> listener) {
        this.userCollector.removeListener(listener);
    }

    public String[] getUsers() {
        final int noUsers = userCollector.getConnectedUsers().size() + userCollector.getDisconnectedUsers().size();
        final List<String> users = new ArrayList<>(noUsers);

        for (User connectedUser : userCollector.getConnectedUsers()) {
            users.add(connectedUser.getUsername());
        }

        for (User disconnectedUser : userCollector.getDisconnectedUsers()) {
            users.add(disconnectedUser.getUsername());
        }

        return users.toArray(new String[noUsers]);
    }

    public void onUpdate(UserListUpdate userListUpdate) {
        userListUpdate.getUsersWhoWentOnline().remove(currentUser);
        userCollector.agregate(userListUpdate);
    }
}
