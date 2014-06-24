package org.nuc.revedere.heartmonitor.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.nuc.revedere.core.User;
import org.nuc.revedere.core.messages.update.UserListUpdate;
import org.nuc.revedere.heartmonitor.HeartMonitor;
import org.nuc.revedere.util.Collector;
import org.nuc.revedere.util.Collector.CollectorListener;

@ManagedBean(name = "users")
@SessionScoped
public class UsersPageBean implements Serializable, CollectorListener<UserListUpdate> {

    private static final long serialVersionUID = 8388751329694282175L;
    private final HeartMonitor heartMonitor;

    private Set<User> connectedUsers = new HashSet<>();
    private Set<User> disconnectedUsers = new HashSet<>();

    public UsersPageBean() {
        heartMonitor = HeartMonitor.getInstance();
        heartMonitor.addUserCollectorListener(this);
    }

    public UserLine[] getUserLines() {
        final int arraySize = connectedUsers.size() + disconnectedUsers.size();
        final List<UserLine> list = new ArrayList<>(arraySize);
        for (User user : connectedUsers) {
            list.add(new UserLine(user.getUsername(), true));
        }

        for (User user : disconnectedUsers) {
            list.add(new UserLine(user.getUsername(), false));
        }

        Collections.sort(list);
        return list.toArray(new UserLine[arraySize]);
    }

    public void onUpdate(UserListUpdate update) {
        this.connectedUsers = update.getUsersWhoWentOnline();
        this.disconnectedUsers = update.getUsersWhoWentOffline();

    }

    public void onUpdate(Collector<UserListUpdate> source, UserListUpdate update) {
        this.connectedUsers.removeAll(source.getCurrentState().getUsersWhoWentOffline());
        this.connectedUsers.addAll(source.getCurrentState().getUsersWhoWentOnline());

        this.disconnectedUsers.removeAll(source.getCurrentState().getUsersWhoWentOnline());
        this.disconnectedUsers.addAll(source.getCurrentState().getUsersWhoWentOffline());
    }
}
