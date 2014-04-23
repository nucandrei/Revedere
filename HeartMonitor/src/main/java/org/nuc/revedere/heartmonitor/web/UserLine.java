package org.nuc.revedere.heartmonitor.web;

import java.io.Serializable;

public class UserLine implements Comparable<UserLine>, Serializable {
    private static final long serialVersionUID = 2117503616026481969L;
    private String username;
    private boolean connected;

    public UserLine(String username, boolean connected) {
        this.username = username;
        this.connected = connected;
    }

    public String getUsername() {
        return this.username;
    }

    public boolean isConnected() {
        return connected;
    }

    public String getStatus() {
        return connected ? "Connected" : "Disconnected";
    }

    public int compareTo(UserLine that) {
        return this.username.compareTo(that.username);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof UserLine)) {
            return false;
        }
        
        final UserLine that = (UserLine) object;
        return this.username.equals(that.username);
    }
}