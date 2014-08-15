package org.nuc.revedere.core;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1935328134263444764L;
    private final String username;
    private final String authInfo;
    private final String realName;
    private final boolean isAdmin;

    public User(String username, String authInfo, String realName, boolean isAdmin) {
        this.username = username;
        this.authInfo = authInfo;
        this.realName = realName;
        this.isAdmin = isAdmin;
    }

    public User(String username) {
        this.username = username;
        this.authInfo = "";
        this.realName = null;
        this.isAdmin = false;
    }

    public String getUsername() {
        return this.username;
    }

    public boolean matchesAuthInfo(String triedAuthInfo) {
        return this.authInfo.equals(triedAuthInfo);
    }

    public String getAuthInfo() {
        return this.authInfo;
    }

    public String getRealName() {
        return this.realName;
    }

    public User getCleanInstance(boolean publishRealName) {
        return new User(username, "", realName, false);
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public String getName() {
        if (realName == null) {
            return username;
        }

        return realName;
    }

    @Override
    public String toString() {
        return this.username;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof User)) {
            return false;
        }
        User that = (User) object;
        return this.getUsername().equals(that.getUsername());
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }
}
