package org.nuc.revedere.client;

import java.util.Collections;
import java.util.List;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.nuc.revedere.client.connector.MinaClient;
import org.nuc.revedere.core.User;
import org.nuc.revedere.core.UserCollector;
import org.nuc.revedere.core.messages.Response;
import org.nuc.revedere.core.messages.request.LogoutRequest;
import org.nuc.revedere.core.messages.request.ShortMessageEmptyBoxRequest;
import org.nuc.revedere.core.messages.request.ShortMessageHistoricalRequest;
import org.nuc.revedere.core.messages.request.ShortMessageMarkAsReadRequest;
import org.nuc.revedere.core.messages.request.ShortMessageSendRequest;
import org.nuc.revedere.core.messages.request.UserListRequest;
import org.nuc.revedere.core.messages.update.ShortMessageUpdate;
import org.nuc.revedere.core.messages.update.UserListUpdate;
import org.nuc.revedere.shortmessage.MessageBox;
import org.nuc.revedere.shortmessage.MessageBoxPersistence;
import org.nuc.revedere.shortmessage.MessageBoxXMLPersistence;
import org.nuc.revedere.shortmessage.ShortMessage;
import org.nuc.revedere.shortmessage.ShortMessageCollector;
import org.nuc.revedere.util.Collector;
import org.nuc.revedere.util.Collector.CollectorListener;

public class RevedereSession {
    private final MinaClient minaClient;
    private final String username;
    private final UserCollector userCollector;
    private final ShortMessageCollector shortMessageCollector;
    private final MessageBox clientMessageBox;

    public RevedereSession(MinaClient minaClient, String username) {
        this.minaClient = minaClient;
        this.username = username;
        this.userCollector = new UserCollector();
        this.shortMessageCollector = new ShortMessageCollector();
        final MessageBoxPersistence persistence = new MessageBoxXMLPersistence("messages.xml");
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

    public MessageBox getMessageBox() {
        return clientMessageBox;
    }

    public void sendMessage(ShortMessage shortMessage) {
        this.minaClient.sendMessage(new ShortMessageSendRequest(shortMessage));
    }

    public void emptyMessageBox() {
        final MinaRequestor<ShortMessageEmptyBoxRequest> requestor = new MinaRequestor<ShortMessageEmptyBoxRequest>(minaClient);
        final Response<ShortMessageEmptyBoxRequest> response = requestor.request(new ShortMessageEmptyBoxRequest(new User(username)));
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
        final MinaRequestor<ShortMessageHistoricalRequest> requestor = new MinaRequestor<ShortMessageHistoricalRequest>(minaClient);
        final Response<ShortMessageHistoricalRequest> response = requestor.request(new ShortMessageHistoricalRequest(new User(username), read, sent, unread));
        if (response != null) {
            return (List<ShortMessage>) response.getAttachment();
        } else {
            return Collections.emptyList();
        }
    }

    public void markMessagesAsRead(List<ShortMessage> listToMark) {
        this.minaClient.sendMessage(new ShortMessageMarkAsReadRequest(new User(username), listToMark));
    }

    public void logout() {
        this.minaClient.sendMessage(new LogoutRequest(username));
    }

    public void close() {
        this.minaClient.close();
    }

    private void initialize() {
        this.minaClient.addHandler(new IoHandlerAdapter() {
            @Override
            public void messageReceived(IoSession session, Object message) {
                try {
                    @SuppressWarnings("unchecked")
                    final Response<UserListRequest> userListResponse = (Response<UserListRequest>) message;
                    userCollector.agregate((UserListUpdate) userListResponse.getAttachment());
                    return;
                } catch (Exception e) {
                    // ignore this exception
                }

                if (message instanceof ShortMessageUpdate) {
                    shortMessageCollector.agregate((ShortMessageUpdate) message);
                    return;
                }
            }
        });
    }
}
