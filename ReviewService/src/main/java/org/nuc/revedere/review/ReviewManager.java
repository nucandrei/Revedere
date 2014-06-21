package org.nuc.revedere.review;

import java.util.ArrayList;
import java.util.List;

import org.nuc.revedere.core.User;

public class ReviewManager {
    private int reviewIndex = 0;
    private final List<Review> reviews = new ArrayList<>();

    public Review createReview(User sourceUser, User destinationUser, ReviewData reviewData) {
        final Review review = new Review(sourceUser, destinationUser, reviewData, reviewIndex + "");
        reviews.add(review);
        return review;
    }

    public void update(Review reviewToUpdate) {
        reviews.remove(reviewToUpdate);
        reviews.add(reviewToUpdate);
    }

}
