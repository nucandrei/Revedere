package org.nuc.revedere.review;

import java.util.ArrayList;
import java.util.List;

import org.nuc.revedere.core.User;

public class ReviewManager {
    private int reviewIndex = 0;
    private final List<Review> reviews = new ArrayList<>();

    public Review createReview(User sourceUser, User destinationUser, ReviewData reviewData, ReviewDocument reviewDocument) {
        final Review review = new Review(sourceUser, destinationUser, reviewData, reviewDocument, reviewIndex + "");
        reviews.add(review);
        reviewIndex++;
        return review;
    }

    public void update(Review reviewToUpdate) {
        reviews.remove(reviewToUpdate);
        reviews.add(reviewToUpdate);
    }

    public List<Review> getReviews(User user) {
        final List<Review> reviewsForUser = new ArrayList<>();
        for (Review review : reviews) {
            if (review.getSourceUser().equals(user) || review.getDestinationUser().equals(user)) {
                reviewsForUser.add(review);
            }
        }
        return reviewsForUser;
    }

}
