package org.nuc.revedere.core.messages;

public class RegisterRequest extends CredentialsBasedRequest{

    private static final long serialVersionUID = 6901638574590198176L;

    public RegisterRequest(String username, String authInfo) {
        super(username, authInfo);
    }
}
