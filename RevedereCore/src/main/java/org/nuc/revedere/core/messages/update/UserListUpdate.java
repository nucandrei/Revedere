package org.nuc.revedere.core.messages.update;

import java.util.Set;

import org.nuc.revedere.core.User;

public class UserListUpdate extends Update {
    private static final long serialVersionUID = -953050001722795846L;
    private final Set<User> usersWhoWentOnline;
    private final Set<User> usersWhoWentOffline;

    public UserListUpdate(boolean isDeltaUpdate, Set<User> usersWhoWentOnline, Set<User> usersWhoWentOffline) {
        super(isDeltaUpdate);
        this.usersWhoWentOnline = usersWhoWentOnline;
        this.usersWhoWentOffline = usersWhoWentOffline;
    }

    public Set<User> getUsersWhoWentOnline() {
        return this.usersWhoWentOnline;
    }

    public Set<User> getUsersWhoWentOffline() {
        return this.usersWhoWentOffline;
    }

}
