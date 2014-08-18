package org.nuc.revedere.client;

import java.util.ArrayList;
import java.util.List;

import org.nuc.revedere.core.User;
import org.nuc.revedere.core.messages.update.ReviewUpdate;
import org.nuc.revedere.review.Review;
import org.nuc.revedere.review.ReviewDocumentSectionsManager;
import org.nuc.revedere.review.ReviewPersistence;
import org.nuc.revedere.review.ReviewXMLPersistence;
import org.nuc.revedere.util.Collector;

public class ReviewCollector extends Collector<ReviewUpdate> {
    private final ReviewPersistence reviewPersistence;

    public ReviewCollector(User currentUser, ReviewDocumentSectionsManager sectionsManager) {
        this.reviewPersistence = new ReviewXMLPersistence("reviews_" + currentUser.getUsername() + ".xml", sectionsManager);
    }

    @Override
    public void agregate(ReviewUpdate update) {
        Review review = update.getReview();
        reviewPersistence.update(review);
        super.agregate(update);
    }

    @Override
    public ReviewUpdate getCurrentState() {
        return null;
    }

    public void addReviews(List<Review> reviews) {
        for (Review review : reviews) {
            reviewPersistence.update(review);
        }
    }

    public List<Review> getReviews() {
        return reviewPersistence.getReviews();
    }

    public List<Review> getReviews(User user) {
        final List<Review> response = new ArrayList<>();
        for (Review review : getReviews()) {
            if (review.getSourceUser().equals(user) || review.getDestinationUser().equals(user)) {
                response.add(review);
            }
        }
        return response;
    }

    public int getUnseenReviews() {
        int noUnseenReviews = 0;
        for (Review review : getReviews()) {
            if (!review.isSeen()) {
                noUnseenReviews++;
            }
        }
        return noUnseenReviews;
    }

    public String getFirstNotClosedReviewIndex() {
        return reviewPersistence.getFirstNotClosedReviewIndex();
    }

}
