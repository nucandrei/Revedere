package org.nuc.revedere.core;

public class User {
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
}
