package org.nuc.revedere.service.core.messages;

public class LoginRequest extends CredentialsBasedRequest {

    private static final long serialVersionUID = -4732598462818756875L;

    public LoginRequest(String username, String authInfo) {
        super(username, authInfo);
    }

}
