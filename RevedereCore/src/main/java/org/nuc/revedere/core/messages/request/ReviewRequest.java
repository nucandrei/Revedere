package org.nuc.revedere.core.messages.request;

import org.nuc.revedere.core.User;
import org.nuc.revedere.review.ReviewData;
import org.nuc.revedere.review.ReviewDocument;

public class ReviewRequest extends Request {
    private static final long serialVersionUID = -5541266710718695343L;
    private final User sourceUser;
    private final User destinationUser;
    private final ReviewData reviewData;
    private final ReviewDocument reviewDocument;

    public ReviewRequest(User sourceUser, User destinationUser, ReviewData reviewData, ReviewDocument reviewDocument) {
        this.sourceUser = sourceUser;
        this.destinationUser = destinationUser;
        this.reviewData = reviewData;
        this.reviewDocument = reviewDocument;
    }

    public User getSourceUser() {
        return sourceUser;
    }

    public User getDestinationUser() {
        return destinationUser;
    }

    public ReviewData getReviewData() {
        return reviewData;
    }

    public ReviewDocument getReviewDocument() {
        return this.reviewDocument;
    }
}
