package org.nuc.revedere.heartmonitor;

import java.util.List;

public interface UsersInfoListener {
    public void onUsersUpdate(List<String> connectedUsers, List<String> disconnectedUsers);
}
