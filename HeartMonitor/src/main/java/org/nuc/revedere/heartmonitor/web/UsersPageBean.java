package org.nuc.revedere.heartmonitor.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.nuc.revedere.core.User;
import org.nuc.revedere.heartmonitor.HeartMonitor;
import org.nuc.revedere.heartmonitor.UsersInfoListener;

@ManagedBean(name = "users")
@SessionScoped
public class UsersPageBean implements Serializable, UsersInfoListener {

    private static final long serialVersionUID = 8388751329694282175L;
    private final HeartMonitor heartMonitor;

    private Set<User> connectedUsers;
    private Set<User> disconnectedUsers;

    public UsersPageBean() {
        heartMonitor = HeartMonitor.getInstance();
        heartMonitor.setUserInfoListener(this);
    }

    public void onUsersUpdate(Set<User> connectedUsers, Set<User> disconnectedUsers) {
        this.connectedUsers = connectedUsers;
        this.disconnectedUsers = disconnectedUsers;
    }

    public UserLine[] getUserLines() {
        final int arraySize = connectedUsers.size() + disconnectedUsers.size();
        final List<UserLine> list = new ArrayList<UserLine>(arraySize);
        for (User user : connectedUsers) {
            list.add(new UserLine(user.getUsername(), true));
        }

        for (User user : disconnectedUsers) {
            list.add(new UserLine(user.getUsername(), false));
        }

        Collections.sort(list);
        return list.toArray(new UserLine[arraySize]);
    }
}
