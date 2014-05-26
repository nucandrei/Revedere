package org.nuc.revedere.core.messages;

import java.io.Serializable;

import org.nuc.revedere.core.messages.request.Request;

public class Response<T extends Request> implements Serializable {
    private static final long serialVersionUID = 4233992620922562786L;
    private final T request;
    private final boolean successfull;
    private final String message;
    private Serializable attachment;

    public Response(T request, boolean succesfull, String message) {
        this.request = request;
        this.successfull = succesfull;
        this.message = message;
    }

    public T getRequest() {
        return this.request;
    }

    public boolean isSuccessfull() {
        return this.successfull;
    }

    public String getMessage() {
        return this.message;
    }
    
    public void attach(Serializable attachment) {
        this.attachment = attachment;
    }

    public boolean hasAttachment() {
        return this.attachment != null;
    }

    public Serializable getAttachment() {
        return attachment;
    }
}
