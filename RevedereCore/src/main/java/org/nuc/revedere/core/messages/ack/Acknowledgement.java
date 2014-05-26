package org.nuc.revedere.core.messages.ack;

import java.io.Serializable;

import org.nuc.revedere.core.messages.Response;
import org.nuc.revedere.core.messages.request.Request;

public class Acknowledgement<T extends Request> implements Serializable {
    private static final long serialVersionUID = 5852342897548709630L;
    private final Response<T> response;

    public Acknowledgement(Response<T> response) {
        this.response = response;
    }

    public Response<T> getResponse() {
        return this.response;
    }
}
