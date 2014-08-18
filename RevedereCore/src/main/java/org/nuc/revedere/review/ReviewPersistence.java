package org.nuc.revedere.review;

import java.util.List;

public interface ReviewPersistence {

    public void save(Review review);

    public void update(Review review);

    public List<Review> getReviews();

    public String getLastReviewIndex();

    public String getFirstNotClosedReviewIndex();
}
