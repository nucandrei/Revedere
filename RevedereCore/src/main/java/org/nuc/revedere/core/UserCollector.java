package org.nuc.revedere.core;

import java.util.HashSet;
import java.util.Set;

import org.nuc.revedere.core.messages.update.UserListUpdate;
import org.nuc.revedere.util.Collector;

public class UserCollector extends Collector<UserListUpdate> {
    private Set<User> connectedUsers = new HashSet<>();
    private Set<User> disconnectedUsers = new HashSet<>();

    @Override
    public void agregate(UserListUpdate update) {
        if (update.isDeltaUpdate()) {
            for (User user : update.getUsersWhoWentOnline()) {
                connectedUsers.add(user);
                disconnectedUsers.remove(user);
            }

            for (User user : update.getUsersWhoWentOffline()) {
                disconnectedUsers.add(user);
                connectedUsers.remove(user);
            }
        } else {
            connectedUsers = update.getUsersWhoWentOnline();
            disconnectedUsers = update.getUsersWhoWentOffline();
        }
        super.agregate(update);
    }

    public Set<User> getConnectedUsers() {
        return connectedUsers;
    }

    public Set<User> getDisconnectedUsers() {
        return disconnectedUsers;
    }

    public boolean isConnected(User user) {
        return connectedUsers.contains(user);
    }
    @Override
    public UserListUpdate getCurrentState() {
        return new UserListUpdate(false, connectedUsers, disconnectedUsers);
    }
}
