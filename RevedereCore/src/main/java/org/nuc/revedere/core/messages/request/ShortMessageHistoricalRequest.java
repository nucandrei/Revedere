package org.nuc.revedere.core.messages.request;

import org.nuc.revedere.core.User;

public class ShortMessageHistoricalRequest extends Request {
    private static final long serialVersionUID = 8075011076524868655L;
    private final User user;
    private final boolean requestReceivedMessages;
    private final boolean requestSentMessages;
    private final long fromTimestamp;

    public ShortMessageHistoricalRequest(User user, boolean requestReceivedMessages, boolean requestSentMessages, long fromTimestamp) {
        super();
        this.user = user;
        this.requestReceivedMessages = requestReceivedMessages;
        this.requestSentMessages = requestSentMessages;
        this.fromTimestamp = fromTimestamp;
    }

    public User getUser() {
        return user;
    }

    public boolean isRequestReadMessages() {
        return requestReceivedMessages;
    }

    public boolean isRequestSentMessages() {
        return requestSentMessages;
    }

    public long getFromTimestamp() {
        return fromTimestamp;
    }
}
