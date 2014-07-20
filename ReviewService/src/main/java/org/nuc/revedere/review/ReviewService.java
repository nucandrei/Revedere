package org.nuc.revedere.review;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;

import org.apache.log4j.Logger;
import org.nuc.distry.service.DistryListener;
import org.nuc.distry.service.ServiceConfiguration;
import org.nuc.distry.service.messaging.ActiveMQAdapter;
import org.nuc.revedere.core.User;
import org.nuc.revedere.core.messages.Response;
import org.nuc.revedere.core.messages.request.ReviewHistoricalRequest;
import org.nuc.revedere.core.messages.request.ReviewMarkAsSeenRequest;
import org.nuc.revedere.core.messages.request.ReviewUpdateRequest;
import org.nuc.revedere.core.messages.request.ReviewRequest;
import org.nuc.revedere.core.messages.update.ReviewUpdate;
import org.nuc.revedere.service.core.RevedereService;
import org.nuc.revedere.service.core.SupervisorTopics;
import org.nuc.revedere.service.core.Topics;

public class ReviewService extends RevedereService {
    private static final Logger LOGGER = Logger.getLogger(ReviewService.class);
    private final static String REVIEW_SERVICE_NAME = "ReviewService";
    private final ReviewManager reviewManager = new ReviewManager();

    public ReviewService(ServiceConfiguration serviceConfiguration) throws Exception {
        super(REVIEW_SERVICE_NAME, serviceConfiguration);
        super.start(true);

        addMessageListener(Topics.REVIEW_REQUEST_TOPIC, new DistryListener() {
            public void onMessage(Serializable message) {
                try {
                    if (message instanceof ReviewRequest) {
                        final ReviewRequest reviewRequest = (ReviewRequest) message;
                        final User sourceUser = reviewRequest.getSourceUser();
                        final User destinationUser = reviewRequest.getDestinationUser();
                        LOGGER.info(String.format("Received new review request from %s to %s", sourceUser, destinationUser));
                        final ReviewData reviewData = reviewRequest.getReviewData();
                        final ReviewDocument reviewDocument = reviewRequest.getReviewDocument();
                        final Review review = reviewManager.createReview(sourceUser, destinationUser, reviewData, reviewDocument);
                        notifyActors(review);
                        LOGGER.info(String.format("Created new review request with ID %s and notified actors", review.getID()));
                        return;
                    }

                    if (message instanceof ReviewMarkAsSeenRequest) {
                        final ReviewMarkAsSeenRequest request = (ReviewMarkAsSeenRequest) message;
                        final Review reviewToUpdate = request.getReview();
                        reviewToUpdate.markLastChangeSeen();
                        reviewManager.update(reviewToUpdate);
                        notifyActors(reviewToUpdate);
                        return;
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
                        LOGGER.info(String.format("Changed state for review with ID %s to %s", reviewToUpdate.getID(), reviewToUpdate.getState()));
                        return;
                    }

                    if (message instanceof ReviewHistoricalRequest) {
                        final ReviewHistoricalRequest request = (ReviewHistoricalRequest) message;
                        final Response<ReviewHistoricalRequest> response = new Response<>(request, true, "");
                        response.attach((Serializable) reviewManager.getReviews(request.getUser()));
                        sendMessage(Topics.REVIEW_RESPONSE_TOPIC, response);
                        LOGGER.info(String.format("Sent all reviews for user %s", request.getUser()));
                        return;
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
            final String serverAddress = parseArguments(args);
            final ServiceConfiguration serviceConfiguration = new ServiceConfiguration(new ActiveMQAdapter(serverAddress), true, 10000, SupervisorTopics.HEARTBEAT_TOPIC, true, SupervisorTopics.COMMAND_TOPIC, SupervisorTopics.PUBLISH_TOPIC);
            new ReviewService(serviceConfiguration);
            
        } catch (Exception e) {
            LOGGER.error("Failed to start review service", e);
        }
    }

}
