package org.nuc.revedere.review;

import java.util.ArrayList;
import java.util.List;

import org.nuc.revedere.core.User;
import org.nuc.revedere.util.SeriesGenerator;

public class ReviewManager {
    private final ReviewPersistence persistence;
    private final SeriesGenerator reviewIDGenerator;

    public ReviewManager(ReviewDocumentSectionsManager sectionsManager) {
        this.persistence = new ReviewXMLPersistence("reviews.xml", sectionsManager);
        reviewIDGenerator = new SeriesGenerator(this.persistence.getLastReviewIndex());
    }

    public Review createReview(User sourceUser, User destinationUser, ReviewData reviewData, ReviewDocument reviewDocument) {
        final Review review = new Review(sourceUser, destinationUser, reviewData, reviewDocument, reviewIDGenerator.getNext());
        this.persistence.save(review);
        return review;
    }

    public void update(Review reviewToUpdate) {
        this.persistence.update(reviewToUpdate);
    }

    public List<Review> getReviews(User user, String fromReviewIndex) {
        final List<Review> reviewsForUser = new ArrayList<>();
        for (Review review : persistence.getReviews()) {
            if (review.getSourceUser().equals(user) || review.getDestinationUser().equals(user) && review.getID().compareTo(fromReviewIndex) > 0) {
                reviewsForUser.add(review);
            }
        }
        return reviewsForUser;
    }

}
