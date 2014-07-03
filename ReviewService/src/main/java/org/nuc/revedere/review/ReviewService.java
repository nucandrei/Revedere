package org.nuc.revedere.review;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;

import org.nuc.revedere.core.User;
import org.nuc.revedere.core.messages.request.ReviewMarkAsSeenRequest;
import org.nuc.revedere.core.messages.request.ReviewUpdateRequest;
import org.nuc.revedere.core.messages.request.ReviewRequest;
import org.nuc.revedere.core.messages.update.ReviewUpdate;
import org.nuc.revedere.service.core.BrokerMessageListener;
import org.nuc.revedere.service.core.RevedereService;
import org.nuc.revedere.service.core.Topics;

public class ReviewService extends RevedereService {

    private static final String SETTINGS_PATH = "ReviewService.xml";
    private final static String REVIEW_SERVICE_NAME = "ReviewService";
    private final ReviewManager reviewManager = new ReviewManager();

    public ReviewService() throws Exception {
        super(REVIEW_SERVICE_NAME, SETTINGS_PATH);
        super.start(true, true, true);
        // TODO link to user collector and send updates when user connect

        addMessageListener(Topics.REVIEW_REQUEST_TOPIC, new BrokerMessageListener() {
            public void onMessage(Serializable message) {
                try {
                    if (message instanceof ReviewRequest) {
                        final ReviewRequest reviewRequest = (ReviewRequest) message;
                        final User sourceUser = reviewRequest.getSourceUser();
                        final User destinationUser = reviewRequest.getDestinationUser();
                        final ReviewData reviewData = reviewRequest.getReviewData();
                        final Review review = reviewManager.createReview(sourceUser, destinationUser, reviewData);
                        notifyActors(review);
                        return;
                    }

                    if (message instanceof ReviewMarkAsSeenRequest) {
                        final ReviewMarkAsSeenRequest request = (ReviewMarkAsSeenRequest) message;
                        final Review reviewToUpdate = request.getReview();
                        reviewToUpdate.markLastChangeSeen();
                        reviewManager.update(reviewToUpdate);
                        notifyActors(reviewToUpdate);
                    }

                    if (message instanceof ReviewUpdateRequest) {
                        final ReviewUpdateRequest request = (ReviewUpdateRequest) message;
                        final Review reviewToUpdate = request.getReview();
                        switch (request.getNewState()) {
                        case ACCEPT:
                            reviewToUpdate.accept();
                            break;
                        case DENY:
                            reviewToUpdate.deny();
                            break;
                        case CLOSED:
                            reviewToUpdate.markAsClosed();
                            break;
                        case DONE:
                            reviewToUpdate.markAsDone();
                            break;
                        case REQUEST:
                        default:
                        }
                        reviewManager.update(reviewToUpdate);
                        notifyActors(reviewToUpdate);
                    }
                } catch (Exception e) {
                    LOGGER.error("Caught exception while processing received message", e);
                }

            }
        });
    }

    private void notifyActors(Review review) throws JMSException {
        final List<User> intendedUsers = new ArrayList<>(2);
        final User sourceUser = review.getSourceUser();
        final User destinationUser = review.getDestinationUser();
        intendedUsers.add(sourceUser);
        intendedUsers.add(destinationUser);
        final ReviewUpdate reviewUpdate = new ReviewUpdate(review, intendedUsers);
        sendMessage(Topics.REVIEW_TOPIC, reviewUpdate);
    }

    public static void main(String[] args) {
        try {
            new ReviewService();
        } catch (Exception e) {
            BACKUP_LOGGER.error("Failed to start review service", e);
        }
    }

}
