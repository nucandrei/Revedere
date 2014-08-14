package org.nuc.revedere.client.persistence;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.nuc.revedere.client.ReviewCollector;
import org.nuc.revedere.core.User;
import org.nuc.revedere.core.messages.Response;
import org.nuc.revedere.core.messages.request.ReviewDocumentRequest;
import org.nuc.revedere.core.messages.request.ReviewHistoricalRequest;
import org.nuc.revedere.core.messages.update.ReviewUpdate;
import org.nuc.revedere.review.Review;
import org.nuc.revedere.review.ReviewDocumentSection;
import org.nuc.revedere.util.Collector.CollectorListener;

public class ReviewPersistence {
    private final ReviewCollector reviewCollector = new ReviewCollector();
    private final User currentUser;
    private Set<ReviewDocumentSection> reviewDocumentSections = Collections.emptySet();

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

    public Set<ReviewDocumentSection> getReviewDocumentSections() {
        return this.reviewDocumentSections;
    }

    @SuppressWarnings("unchecked")
    public void init(Response<ReviewHistoricalRequest> response) {
        if (response != null && response.isSuccessfull()) {
            final List<Review> reviews = (List<Review>) response.getAttachment();
            this.reviewCollector.addReviews(reviews);
        }
    }

    @SuppressWarnings("unchecked")
    public void setReviewDocumentSections(Response<ReviewDocumentRequest> response) {
        if (response != null && response.isSuccessfull()) {
            reviewDocumentSections = (Set<ReviewDocumentSection>) response.getAttachment();
        }
    }

    public ReviewHistoricalRequest getInitialRequest() {
        return new ReviewHistoricalRequest(currentUser);
    }

    public List<Review> getReviews(User user) {
        return this.reviewCollector.getReviews(user);
    }
}
