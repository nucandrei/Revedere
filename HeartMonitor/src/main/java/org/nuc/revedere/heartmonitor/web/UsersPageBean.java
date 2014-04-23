package org.nuc.revedere.heartmonitor.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.nuc.revedere.heartmonitor.HeartMonitor;
import org.nuc.revedere.heartmonitor.UsersInfoListener;

@ManagedBean(name = "users")
@SessionScoped
public class UsersPageBean implements Serializable, UsersInfoListener {

    private static final long serialVersionUID = 8388751329694282175L;
    private final HeartMonitor heartMonitor;

    private List<String> connectedUsers;
    private List<String> disconnectedUsers;

    public UsersPageBean() {
        heartMonitor = HeartMonitor.getInstance();
        heartMonitor.setUserInfoListener(this);
    }

    @Override
    public void onUsersUpdate(List<String> connectedUsers, List<String> disconnectedUsers) {
        this.connectedUsers = connectedUsers;
        this.disconnectedUsers = disconnectedUsers;
    }

    public UserLine[] getUserLines() {
        final int arraySize = connectedUsers.size() + disconnectedUsers.size();
        final List<UserLine> list = new ArrayList<UserLine>(arraySize);
        for (String user : connectedUsers) {
            list.add(new UserLine(user, true));
        }

        for (String user : disconnectedUsers) {
            list.add(new UserLine(user, false));
        }

        Collections.sort(list);
        return list.toArray(new UserLine[arraySize]);
        
//        final int arraySize = connectedUsers.size() + disconnectedUsers.size();
//        final List<String> list = new ArrayList<>(arraySize);
//        list.addAll(connectedUsers);
//        list.addAll(disconnectedUsers);
//
//        Collections.sort(list);
//        return list.toArray(new String[arraySize]);
    }
}
