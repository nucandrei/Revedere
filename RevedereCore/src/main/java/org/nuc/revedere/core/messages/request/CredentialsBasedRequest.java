package org.nuc.revedere.core.messages.request;

public class CredentialsBasedRequest extends Request {

    private static final long serialVersionUID = -7095880986640496887L;
    private final String username;
    private final String authInfo;

    public CredentialsBasedRequest(String username, String authInfo) {
        this.username = username;
        this.authInfo = authInfo;
    }

    public String getUsername() {
        return username;
    }

    public String getAuthInfo() {
        return authInfo;
    }
}
