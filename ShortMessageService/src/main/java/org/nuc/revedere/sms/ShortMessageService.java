package org.nuc.revedere.sms;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;

import org.nuc.revedere.core.User;
import org.nuc.revedere.core.messages.Response;
import org.nuc.revedere.core.messages.request.ShortMessageSendRequest;
import org.nuc.revedere.core.messages.update.ShortMessageUpdate;
import org.nuc.revedere.core.messages.update.UserListUpdate;
import org.nuc.revedere.service.core.BrokerMessageListener;
import org.nuc.revedere.service.core.RevedereService;
import org.nuc.revedere.service.core.Topics;
import org.nuc.revedere.shortmessage.MessageBox;
import org.nuc.revedere.shortmessage.MessageBoxPersistence;
import org.nuc.revedere.shortmessage.MessageBoxXMLPersistence;
import org.nuc.revedere.shortmessage.ShortMessage;
import org.nuc.revedere.util.Collector;
import org.nuc.revedere.util.Collector.CollectorListener;

public class ShortMessageService extends RevedereService {

    private final static String SHORT_MESSAGE_SERVICE_NAME = "ShortMessageService";
    private final Map<String, MessageBox> msgBoxes = new HashMap<String, MessageBox>();
    private final MessageBoxPersistence persistence = new MessageBoxXMLPersistence("messages.xml");

    public ShortMessageService() throws Exception {
        super(SHORT_MESSAGE_SERVICE_NAME);
        super.start(true, true, true);
        super.getUserCollector().addListener(new CollectorListener<UserListUpdate>() {
            public void onUpdate(Collector<UserListUpdate> source, UserListUpdate update) {
                for (User newOnlineUser : update.getUsersWhoWentOnline()) {
                    final String username = newOnlineUser.getUsername();
                    createMessageBoxIfMissing(username);
                    final MessageBox messageBox = msgBoxes.get(username);
                    try {
                        sendMessage(Topics.SHORT_MESSAGE_TOPIC, new ShortMessageUpdate(messageBox.getUnreadMessages(), newOnlineUser));
                        LOGGER.info(String.format("Sent update message for user %s", username));
                    } catch (JMSException e) {
                        LOGGER.error(String.format("Could not send update message for user %s, reason", username), e);
                    }
                }
            }
        });

        addMessageListener(Topics.SHORT_MESSAGE_REQUEST_TOPIC, new BrokerMessageListener() {
            public void onMessage(Serializable message) {
                try {
                    if (message instanceof ShortMessageSendRequest) {
                        final ShortMessageSendRequest shortMessageSendRequest = (ShortMessageSendRequest) message;
                        final ShortMessage shortMessage = shortMessageSendRequest.getShortMessage();
                        putInInbox(shortMessage);
                        sendResponse(shortMessageSendRequest);
                        forwardMessageIfReceiverIsOnline(shortMessage);
                    }
                } catch (Exception e) {
                    LOGGER.error("Caught exception while processing received message", e);
                }
            }
        });
    }

    private void putInInbox(ShortMessage shortMessage) {
        final String username = shortMessage.getSender().getUsername();
        createMessageBoxIfMissing(username);
        final MessageBox senderMessageBox = msgBoxes.get(username);
        senderMessageBox.add(shortMessage);

        createMessageBoxIfMissing(shortMessage.getReceiver().getUsername());
        final MessageBox receiverMessageBox = msgBoxes.get(shortMessage.getReceiver().getUsername());
        receiverMessageBox.add(shortMessage);
    }

    private void createMessageBoxIfMissing(String messageBoxName) {
        if (!msgBoxes.containsKey(messageBoxName)) {
            final MessageBox newMessageBox = new MessageBox(messageBoxName, persistence);
            msgBoxes.put(messageBoxName, newMessageBox);
        }
    }

    private void sendResponse(ShortMessageSendRequest request) throws JMSException {
        final Response<ShortMessageSendRequest> response = new Response<ShortMessageSendRequest>(request, true, "");
        sendMessage(Topics.SHORT_MESSAGE_RESPONSE_TOPIC, response);
        LOGGER.info("send response back");
    }

    private void forwardMessageIfReceiverIsOnline(ShortMessage shortMessage) throws JMSException {
        final User intendedReceiver = shortMessage.getReceiver();
        if (this.getUserCollector().isConnected(intendedReceiver)) {
            sendMessage(Topics.SHORT_MESSAGE_TOPIC, new ShortMessageUpdate(shortMessage.asList(), intendedReceiver));
        }
    }

    public static void main(String[] args) {
        try {
            new ShortMessageService();
        } catch (Exception e) {
            BACKUP_LOGGER.error("Failed to start short message service", e);
        }
    }
}
