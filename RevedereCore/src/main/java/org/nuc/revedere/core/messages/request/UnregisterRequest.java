package org.nuc.revedere.core.messages.request;

public class UnregisterRequest extends CredentialsBasedRequest {

    private static final long serialVersionUID = 8032326873286918886L;

    public UnregisterRequest(String username, String authInfo) {
        super(username, authInfo);
    }
}
