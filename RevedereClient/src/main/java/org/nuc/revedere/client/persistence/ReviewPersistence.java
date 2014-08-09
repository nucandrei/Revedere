package org.nuc.revedere.client.persistence;

import java.util.List;

import org.nuc.revedere.client.ReviewCollector;
import org.nuc.revedere.core.User;
import org.nuc.revedere.core.messages.Response;
import org.nuc.revedere.core.messages.request.ReviewHistoricalRequest;
import org.nuc.revedere.core.messages.update.ReviewUpdate;
import org.nuc.revedere.review.Review;
import org.nuc.revedere.util.Collector.CollectorListener;

public class ReviewPersistence {
    private final ReviewCollector reviewCollector = new ReviewCollector();
    private final User currentUser;

    public ReviewPersistence(User currentUser) {
        this.currentUser = currentUser;
    }

    public void addListenerToChange(CollectorListener<ReviewUpdate> listener) {
        this.reviewCollector.addListener(listener);
    }

    public void removeListenerToChange(CollectorListener<ReviewUpdate> listener) {
        this.reviewCollector.removeListener(listener);
    }

    public void onUpdate(ReviewUpdate update) {
        this.reviewCollector.agregate(update);
    }

    @SuppressWarnings("unchecked")
    public void init(Response<ReviewHistoricalRequest> response) {
        if (response != null) {
            final List<Review> reviews = (List<Review>) response.getAttachment();
            this.reviewCollector.addReviews(reviews);
        }
    }

    public ReviewHistoricalRequest getInitialRequest() {
        return new ReviewHistoricalRequest(currentUser);
    }

    public List<Review> getReviews(User user) {
        return this.reviewCollector.getReviews(user);
    }
}
