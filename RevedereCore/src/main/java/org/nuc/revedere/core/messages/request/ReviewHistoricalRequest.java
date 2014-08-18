package org.nuc.revedere.core.messages.request;

import org.nuc.revedere.core.User;

public class ReviewHistoricalRequest extends Request {
    private static final long serialVersionUID = 3646179506095912063L;
    private final User user;
    private final String fromReviewIndex;

    public ReviewHistoricalRequest(User user, String fromReviewIndex) {
        this.user = user;
        this.fromReviewIndex = fromReviewIndex;
    }

    public User getUser() {
        return this.user;
    }

    public String getFromReviewIndex() {
        return fromReviewIndex;
    }
}
