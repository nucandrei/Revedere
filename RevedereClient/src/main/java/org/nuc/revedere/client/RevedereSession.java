package org.nuc.revedere.client;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.nuc.revedere.client.connector.MinaClient;
import org.nuc.revedere.client.persistence.ReviewPersistence;
import org.nuc.revedere.client.persistence.ShortMessagePersistence;
import org.nuc.revedere.client.persistence.UsersHandler;
import org.nuc.revedere.core.User;
import org.nuc.revedere.core.messages.Response;
import org.nuc.revedere.core.messages.request.LogoutRequest;
import org.nuc.revedere.core.messages.request.ReviewDocumentRequest;
import org.nuc.revedere.core.messages.request.ReviewHistoricalRequest;
import org.nuc.revedere.core.messages.request.ReviewMarkAsSeenRequest;
import org.nuc.revedere.core.messages.request.ReviewUpdateRequest;
import org.nuc.revedere.core.messages.request.ReviewRequest;
import org.nuc.revedere.core.messages.request.ShortMessageHistoricalRequest;
import org.nuc.revedere.core.messages.request.ShortMessageMarkAsReadRequest;
import org.nuc.revedere.core.messages.request.ShortMessageSendRequest;
import org.nuc.revedere.core.messages.request.UserListRequest;
import org.nuc.revedere.core.messages.update.ReviewUpdate;
import org.nuc.revedere.core.messages.update.ShortMessageUpdate;
import org.nuc.revedere.core.messages.update.UserListUpdate;
import org.nuc.revedere.review.Review;
import org.nuc.revedere.review.ReviewData;
import org.nuc.revedere.review.ReviewDocument;
import org.nuc.revedere.review.ReviewDocumentSection;
import org.nuc.revedere.review.ReviewState;
import org.nuc.revedere.shortmessage.ShortMessage;
import org.nuc.revedere.util.Collector.CollectorListener;

public class RevedereSession {
    private static final Logger LOGGER = Logger.getLogger(RevedereSession.class);
    private final MinaClient minaClient;
    private final User currentUser;
    private final UsersHandler usersHandler;
    private final ShortMessagePersistence shortMessagePersistence;
    private final ReviewPersistence reviewPersistence;

    public RevedereSession(MinaClient minaClient, String username) {
        this.minaClient = minaClient;
        this.currentUser = new User(username);
        this.usersHandler = new UsersHandler(currentUser);
        this.shortMessagePersistence = new ShortMessagePersistence(currentUser);
        this.reviewPersistence = new ReviewPersistence(currentUser);
        initialize();
    }

    public void addListenerToUserCollector(CollectorListener<UserListUpdate> listener) {
        this.usersHandler.addListenerToChange(listener);
        LOGGER.debug("Attached listener to user collector");
    }

    public void removeListenerFromUserCollector(CollectorListener<UserListUpdate> listener) {
        this.usersHandler.addListenerToChange(listener);
        LOGGER.debug("Removed listener from user collector");
    }

    public String[] getUsers() {
        return usersHandler.getUsers();
    }

    public void sendMessage(User receiver, String content) {
        final long timestamp = System.currentTimeMillis();
        final ShortMessage shortMessage = new ShortMessage(currentUser, receiver, content, timestamp);
        this.minaClient.sendMessage(new ShortMessageSendRequest(shortMessage));
        LOGGER.debug("Sent short message to " + receiver.getUsername());
    }

    public void addListenerToShortMessageCollector(CollectorListener<ShortMessageUpdate> listener) {
        this.shortMessagePersistence.addListenerToChange(listener);
        LOGGER.debug("Attached listener to short message collector");
    }

    public void removeListenerFromShortMessageCollector(CollectorListener<ShortMessageUpdate> listener) {
        this.shortMessagePersistence.addListenerToChange(listener);
        LOGGER.debug("Removed listener from short message collector");
    }

    public Set<ShortMessage> getMessagesForUser(User user) {
        return this.shortMessagePersistence.getMessagesForUser(user);
    }

    public void addListenerToReviewCollector(CollectorListener<ReviewUpdate> listener) {
        this.reviewPersistence.addListenerToChange(listener);
        LOGGER.debug("Attached listener to review collector");
    }

    public void removeListenerFromReviewCollector(CollectorListener<ReviewUpdate> listener) {
        this.reviewPersistence.addListenerToChange(listener);
        LOGGER.debug("Removed listener from review collector");
    }

    public List<Review> getReviews(User user) {
        return this.reviewPersistence.getReviews(user);
    }
    
    public Set<ReviewDocumentSection> getReviewDocumentSections() {
        return reviewPersistence.getReviewDocumentSections();
    }

    public void markMessagesAsRead(List<ShortMessage> listToMark) {
        this.minaClient.sendMessage(new ShortMessageMarkAsReadRequest(currentUser, listToMark));
    }

    public void logout() {
        this.minaClient.sendMessage(new LogoutRequest(currentUser.getUsername()));
    }

    public void close() {
        this.minaClient.close();
    }

    public void requestReview(User destinationUser, ReviewData reviewData, ReviewDocument reviewDocument) {
        this.minaClient.sendMessage(new ReviewRequest(currentUser, destinationUser, reviewData, reviewDocument));
    }

    public void markReviewAsSeen(Review review) {
        this.minaClient.sendMessage(new ReviewMarkAsSeenRequest(review));
    }

    public void updateReview(Review review, ReviewState newState) {
        this.minaClient.sendMessage(new ReviewUpdateRequest(review, newState));
    }

    public User getCurrentUser() {
        return this.currentUser;
    }

    private void initialize() {
        this.minaClient.addHandler(new IoHandlerAdapter() {
            @Override
            public void messageReceived(IoSession session, Object message) {
                try {
                    @SuppressWarnings("unchecked")
                    final Response<UserListRequest> userListResponse = (Response<UserListRequest>) message;
                    final UserListUpdate userListUpdate = (UserListUpdate) userListResponse.getAttachment();
                    usersHandler.onUpdate(userListUpdate);
                    LOGGER.debug("Received users update");
                    return;
                } catch (Exception e) {
                    // ignore this exception
                }

                if (message instanceof ShortMessageUpdate) {
                    shortMessagePersistence.onUpdate((ShortMessageUpdate) message);
                    LOGGER.debug("Received short message update " + ((ShortMessageUpdate) message).getUpdate());
                    return;
                }

                if (message instanceof ReviewUpdate) {
                    reviewPersistence.onUpdate((ReviewUpdate) message);
                    LOGGER.debug("Received review update");
                    return;
                }
            }
        });

        final ShortMessageHistoricalRequest shortMessageRequest = shortMessagePersistence.getInitialRequest();
        final MinaRequestor<ShortMessageHistoricalRequest> shortMessageRequestor = new MinaRequestor<>(minaClient);
        shortMessagePersistence.init(shortMessageRequestor.request(shortMessageRequest));

        final ReviewHistoricalRequest reviewRequest = reviewPersistence.getInitialRequest();
        final MinaRequestor<ReviewHistoricalRequest> reviewRequestor = new MinaRequestor<>(minaClient);
        reviewPersistence.init(reviewRequestor.request(reviewRequest));

        final ReviewDocumentRequest reviewDocumentRequest = new ReviewDocumentRequest();
        final MinaRequestor<ReviewDocumentRequest> reviewMinaRequestor = new MinaRequestor<>(minaClient);
        reviewPersistence.setReviewDocumentSections(reviewMinaRequestor.request(reviewDocumentRequest));
    }
}
