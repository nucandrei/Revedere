package org.nuc.revedere.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.nuc.revedere.client.connector.MinaClient;
import org.nuc.revedere.core.User;
import org.nuc.revedere.core.UserCollector;
import org.nuc.revedere.core.messages.Response;
import org.nuc.revedere.core.messages.request.LogoutRequest;
import org.nuc.revedere.core.messages.request.ReviewHistoricalRequest;
import org.nuc.revedere.core.messages.request.ReviewMarkAsSeenRequest;
import org.nuc.revedere.core.messages.request.ReviewUpdateRequest;
import org.nuc.revedere.core.messages.request.ReviewRequest;
import org.nuc.revedere.core.messages.request.ShortMessageEmptyBoxRequest;
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
import org.nuc.revedere.review.ReviewState;
import org.nuc.revedere.shortmessage.DoNothingMessageBoxPersistence;
import org.nuc.revedere.shortmessage.MessageBox;
import org.nuc.revedere.shortmessage.MessageBoxPersistence;
import org.nuc.revedere.shortmessage.ShortMessage;
import org.nuc.revedere.shortmessage.ShortMessageCollector;
import org.nuc.revedere.util.Collector;
import org.nuc.revedere.util.Collector.CollectorListener;

public class RevedereSession {
    private final MinaClient minaClient;
    private final User currentUser;
    private final UserCollector userCollector;
    private final ShortMessageCollector shortMessageCollector;
    private final ReviewCollector reviewCollector;
    private final MessageBox clientMessageBox;

    public RevedereSession(MinaClient minaClient, String username) {
        this.minaClient = minaClient;
        this.currentUser = new User(username);
        this.userCollector = new UserCollector();
        this.shortMessageCollector = new ShortMessageCollector();
        this.reviewCollector = new ReviewCollector();
        final MessageBoxPersistence persistence = new DoNothingMessageBoxPersistence();
        this.clientMessageBox = new MessageBox(username, persistence);
        this.shortMessageCollector.addListener(new CollectorListener<ShortMessageUpdate>() {
            @Override
            public void onUpdate(Collector<ShortMessageUpdate> source, ShortMessageUpdate update) {
                clientMessageBox.addAll(update.getUpdate());
            }
        });

        initialize();
    }

    public void addListenerToUserCollector(CollectorListener<UserListUpdate> listener) {
        this.userCollector.addListener(listener);
    }

    public void removeListenerFromUserCollector(CollectorListener<UserListUpdate> listener) {
        this.userCollector.removeListener(listener);
    }

    public String[] getUsers() {
        final int noUsers = userCollector.getConnectedUsers().size() + userCollector.getDisconnectedUsers().size();
        final List<String> users = new ArrayList<>(noUsers);
        
        for (User connectedUser : userCollector.getConnectedUsers()) {
            users.add(connectedUser.getUsername());
        }

        for (User disconnectedUser : userCollector.getDisconnectedUsers()) {
            users.add(disconnectedUser.getUsername());
        }
        
        return users.toArray(new String[noUsers]);
    }

    public void addListenerToShortMessageCollector(CollectorListener<ShortMessageUpdate> listener) {
        this.shortMessageCollector.addListener(listener);
    }

    public void removeListenerFromShortMessageCollector(CollectorListener<ShortMessageUpdate> listener) {
        this.shortMessageCollector.removeListener(listener);
    }

    public MessageBox getMessageBox() {
        return clientMessageBox;
    }

    public void addListenerToReviewCollector(CollectorListener<ReviewUpdate> listener) {
        this.reviewCollector.addListener(listener);
    }

    public void removeListenerFromReviewCollector(CollectorListener<ReviewUpdate> listener) {
        this.reviewCollector.removeListener(listener);
    }

    public ReviewCollector getReviewCollector() {
        return this.reviewCollector;
    }

    public void sendMessage(User receiver, String content) {
        final long timestamp = System.currentTimeMillis();
        final ShortMessage shortMessage = new ShortMessage(currentUser, receiver, content, timestamp);
        this.minaClient.sendMessage(new ShortMessageSendRequest(shortMessage));
    }

    public void emptyMessageBox() {
        final MinaRequestor<ShortMessageEmptyBoxRequest> requestor = new MinaRequestor<>(minaClient);
        final Response<ShortMessageEmptyBoxRequest> response = requestor.request(new ShortMessageEmptyBoxRequest(currentUser));
        if (response != null) {
            clientMessageBox.removeAll();
        }
    }

    public List<ShortMessage> getUnreadMessages() {
        return requestShortMessages(false, false, true);
    }

    public List<ShortMessage> getReadMessages() {
        return requestShortMessages(true, false, false);
    }

    public List<ShortMessage> getSentMessages() {
        return requestShortMessages(false, true, false);
    }

    @SuppressWarnings("unchecked")
    private List<ShortMessage> requestShortMessages(boolean read, boolean sent, boolean unread) {
        final MinaRequestor<ShortMessageHistoricalRequest> requestor = new MinaRequestor<>(minaClient);
        final Response<ShortMessageHistoricalRequest> response = requestor.request(new ShortMessageHistoricalRequest(currentUser, read, sent, unread));
        if (response != null) {
            return (List<ShortMessage>) response.getAttachment();
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private List<Review> getReviews() {
        final MinaRequestor<ReviewHistoricalRequest> requestor = new MinaRequestor<>(minaClient);
        final Response<ReviewHistoricalRequest> response = requestor.request(new ReviewHistoricalRequest(currentUser));
        if (response != null) {
            return (List<Review>) response.getAttachment();
        }
        return Collections.emptyList();
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
                    userListUpdate.getUsersWhoWentOnline().remove(currentUser);
                    userCollector.agregate(userListUpdate);
                    return;
                } catch (Exception e) {
                    // ignore this exception
                }

                if (message instanceof ShortMessageUpdate) {
                    shortMessageCollector.agregate((ShortMessageUpdate) message);
                    return;
                }

                if (message instanceof ReviewUpdate) {
                    reviewCollector.agregate((ReviewUpdate) message);
                    return;
                }
            }
        });
        this.clientMessageBox.addAll(this.getReadMessages());
        this.clientMessageBox.addAll(this.getSentMessages());
        this.clientMessageBox.addAll(this.getUnreadMessages());

        reviewCollector.addReviews(this.getReviews());
    }
}
