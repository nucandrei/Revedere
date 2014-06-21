package org.nuc.revedere.core.messages.request;

import org.nuc.revedere.review.Review;

public class ReviewMarkAsSeenRequest extends Request {
    private static final long serialVersionUID = 6107081908313592614L;
    private final Review review;

    public ReviewMarkAsSeenRequest(Review review) {
        this.review = review;
    }

    public Review getReview() {
        return review;
    }
}
