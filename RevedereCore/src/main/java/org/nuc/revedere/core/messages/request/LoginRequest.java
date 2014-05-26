package org.nuc.revedere.core.messages.request;

public class LoginRequest extends CredentialsBasedRequest {

    private static final long serialVersionUID = -4732598462818756875L;

    public LoginRequest(String username, String authInfo) {
        super(username, authInfo);
    }

}
