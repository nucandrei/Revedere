package org.nuc.revedere.core.messages.update;

import java.util.List;

import org.nuc.revedere.core.User;
import org.nuc.revedere.review.Review;

public class ReviewUpdate extends Update {
    private static final long serialVersionUID = 3849394028906796364L;
    private final Review review;
    private final List<User> intendedUsers;

    public ReviewUpdate(Review review, List<User> intendedUsers) {
        super(true);
        this.review = review;
        this.intendedUsers = intendedUsers;
    }

    public Review getReview() {
        return review;
    }

    public List<User> getIntendedUsers() {
        return intendedUsers;
    }
}
