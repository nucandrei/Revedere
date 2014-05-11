package org.nuc.revedere.heartmonitor;

import java.util.Set;

import org.nuc.revedere.core.User;

public interface UsersInfoListener {
    public void onUsersUpdate(Set<User> connectedUsers, Set<User> disconnectedUsers);
}
