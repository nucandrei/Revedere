package org.nuc.revedere.review;

import java.io.Serializable;

import org.nuc.revedere.core.User;

public class Review implements Serializable {
    private static final long serialVersionUID = -7212035049401390402L;
    private final User sourceUser;
    private final User destinationUser;
    private ReviewState reviewState;
    private boolean lastChangeSeen = false;
    private final String reviewID;
    private final ReviewData reviewData;

    public Review(User sourceUser, User destinationUser, ReviewData reviewData, String reviewID) {
        this.sourceUser = sourceUser;
        this.destinationUser = destinationUser;
        this.reviewData = reviewData;
        this.reviewID = reviewID;
        this.reviewState = ReviewState.REQUEST;
    }

    public User getSourceUser() {
        return this.sourceUser;
    }

    public User getDestinationUser() {
        return this.destinationUser;
    }

    public ReviewState getState() {
        return this.reviewState;
    }

    public ReviewData getData() {
        return this.reviewData;
    }

    public String getID() {
        return this.reviewID;
    }

    public void markLastChangeSeen() {
        this.lastChangeSeen = true;
    }

    public void accept() {
        if (reviewState.equals(ReviewState.REQUEST)) {
            this.reviewState = ReviewState.ACCEPT;
            lastChangeSeen = false;
        }
    }

    public void deny() {
        if (reviewState.equals(ReviewState.REQUEST)) {
            this.reviewState = ReviewState.DENY;
            lastChangeSeen = false;
        }
    }

    public void markAsDone() {
        if (reviewState.equals(ReviewState.ACCEPT)) {
            this.reviewState = ReviewState.DONE;
            lastChangeSeen = false;
        }
    }

    public void markAsClosed() {
        this.reviewState = ReviewState.CLOSED;
        lastChangeSeen = false;
    }

    public boolean isSeen() {
        return this.lastChangeSeen;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Review)) {
            return false;
        }

        Review that = (Review) object;
        return this.reviewID.equals(that.reviewID);
    }

    @Override
    public int hashCode() {
        return this.reviewID.hashCode();
    }
}
