package org.nuc.revedere.core.messages.request;

import org.nuc.revedere.core.User;

public class ShortMessageHistoricalRequest extends Request {
    private static final long serialVersionUID = 8075011076524868655L;
    private final User user;
    private final boolean requestReadMessages;
    private final boolean requestSentMessages;
    private final boolean requestUnreadMessages;

    public ShortMessageHistoricalRequest(User user, boolean requestReadMessages, boolean requestSentMessages, boolean requestUnreadMessages) {
        super();
        this.user = user;
        this.requestReadMessages = requestReadMessages;
        this.requestSentMessages = requestSentMessages;
        this.requestUnreadMessages = requestUnreadMessages;
    }

    public User getUser() {
        return user;
    }

    public boolean isRequestReadMessages() {
        return requestReadMessages;
    }

    public boolean isRequestSentMessages() {
        return requestSentMessages;
    }

    public boolean isRequestUnreadMessages() {
        return requestUnreadMessages;
    }
}
