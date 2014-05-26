package org.nuc.revedere.core;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1935328134263444764L;
    private final String username;
    private final String authInfo;

    public User(String username, String authInfo) {
        this.username = username;
        this.authInfo = authInfo;
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

    public User getCleanInstance() {
        return new User(username, "");
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
