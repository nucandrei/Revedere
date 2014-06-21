package org.nuc.revedere.core.messages.request;

import org.nuc.revedere.review.Review;
import org.nuc.revedere.review.ReviewState;

public class ReviewUpdateRequest extends Request {
    private static final long serialVersionUID = 2434069458626033709L;
    private final Review review;
    private final ReviewState newState;

    public ReviewUpdateRequest(Review review, ReviewState newState) {
        this.review = review;
        this.newState = newState;
    }

    public Review getReview() {
        return review;
    }

    public ReviewState getNewState() {
        return newState;
    }

}
