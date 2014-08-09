package org.nuc.revedere.core.messages.ack;

import java.io.Serializable;

import org.nuc.revedere.core.messages.Response;
import org.nuc.revedere.core.messages.request.LoginRequest;

public class LoginAcknowledgement implements Serializable {
    private static final long serialVersionUID = 5852342897548709630L;
    private final Response<LoginRequest> response;

    public LoginAcknowledgement(Response<LoginRequest> response) {
        this.response = response;
    }

    public Response<LoginRequest> getResponse() {
        return this.response;
    }
}
