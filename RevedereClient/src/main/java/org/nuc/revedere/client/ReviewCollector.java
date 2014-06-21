package org.nuc.revedere.client;

import java.util.ArrayList;
import java.util.List;

import org.nuc.revedere.core.User;
import org.nuc.revedere.core.messages.update.ReviewUpdate;
import org.nuc.revedere.review.Review;
import org.nuc.revedere.util.Collector;

public class ReviewCollector extends Collector<ReviewUpdate> {
    private final List<Review> reviewList = new ArrayList<>();

    @Override
    public void agregate(ReviewUpdate update) {
        Review review = update.getReview();
        reviewList.remove(review);
        reviewList.add(review);
        super.agregate(update);
    }

    @Override
    public ReviewUpdate getCurrentState() {
        return null;
    }

    public List<Review> getReviews(User user) {
        final List<Review> response = new ArrayList<>();
        for (Review review : reviewList) {
            if (review.getSourceUser().equals(user) || review.getDestinationUser().equals(user)) {
                response.add(review);
            }
        }
        return response;
    }

    public int getUnseenReviews(User user) {
        int noUnseenReviews = 0;
        for (Review review : reviewList) {
            if (!review.isSeen()) {
                noUnseenReviews++;
            }
        }
        return noUnseenReviews;
    }

}
