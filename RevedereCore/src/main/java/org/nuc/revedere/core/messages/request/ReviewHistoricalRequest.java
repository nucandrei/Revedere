package org.nuc.revedere.core.messages.request;

import org.nuc.revedere.core.User;

public class ReviewHistoricalRequest extends Request {
    private static final long serialVersionUID = 3646179506095912063L;
    private final User user;

    public ReviewHistoricalRequest(User user) {
        this.user = user;
    }

    public User getUser() {
        return this.user;
    }

}
