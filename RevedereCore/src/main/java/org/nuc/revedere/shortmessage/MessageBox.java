package org.nuc.revedere.shortmessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.nuc.revedere.core.User;
import org.nuc.revedere.shortmessage.ShortMessage;

public class MessageBox {
    private final String msgBoxName;
    private final MessageBoxPersistence persistence;
    private final List<ShortMessage> readMessages = new LinkedList<>();
    private final List<ShortMessage> unreadMessages = new LinkedList<>();
    private final List<ShortMessage> sentMessages = new LinkedList<>();
    private long lastTimestamp = 0;

    public MessageBox(String msgBoxName, MessageBoxPersistence persistence) {
        this.msgBoxName = msgBoxName;
        this.persistence = persistence;
        loadMessages();
    }

    public void add(ShortMessage message) {
        if (message.getSender().getUsername().equals(msgBoxName)) {
            this.sentMessages.add(message);
        } else {
            clearOldInstances(message);
            if (message.isRead()) {
                this.readMessages.add(message);
            } else {
                this.unreadMessages.add(message);
            }
        }
        if (lastTimestamp < message.getTimestamp()) {
            lastTimestamp = message.getTimestamp();
        }
        persistence.save(message, msgBoxName);
    }

    public void addAll(Collection<ShortMessage> collection) {
        for (ShortMessage shortMessage : collection) {
            this.add(shortMessage);
        }
    }

    public void markAsRead(ShortMessage message) {
        this.unreadMessages.remove(message);
        this.readMessages.add(message);
        message.markAsRead();
        persistence.update(message, msgBoxName);
    }

    /**
     * Remove all messages from message box. This action is irreversible
     */
    public void removeAll() {
        readMessages.clear();
        unreadMessages.clear();
        sentMessages.clear();
        persistence.clear(msgBoxName);
    }

    public List<ShortMessage> getUnreadMessages() {
        return unreadMessages;
    }

    public List<ShortMessage> getUnreadMessages(long fromDate) {
        final List<ShortMessage> fromDateUnreadMessages = new ArrayList<>();
        for (ShortMessage shortMessage : unreadMessages) {
            if (shortMessage.getTimestamp() > fromDate) {
                fromDateUnreadMessages.add(shortMessage);
            }
        }
        return fromDateUnreadMessages;
    }

    public List<ShortMessage> getReadMessages() {
        return readMessages;
    }

    public List<ShortMessage> getReadMessages(long fromDate) {
        final List<ShortMessage> fromDateReadMessages = new ArrayList<>();
        for (ShortMessage shortMessage : unreadMessages) {
            if (shortMessage.getTimestamp() > fromDate) {
                fromDateReadMessages.add(shortMessage);
            }
        }
        return fromDateReadMessages;
    }

    public List<ShortMessage> getSentMessages() {
        return sentMessages;
    }

    public Set<ShortMessage> getMessages(User otherUser) {
        final Set<ShortMessage> messages = new TreeSet<>();
        for (ShortMessage shortMessage : unreadMessages) {
            if (shortMessage.getSender().equals(otherUser)) {
                messages.add(shortMessage);
            }
        }
        for (ShortMessage shortMessage : readMessages) {
            if (shortMessage.getSender().equals(otherUser)) {
                messages.add(shortMessage);
            }
        }
        for (ShortMessage shortMessage : sentMessages) {
            if (shortMessage.getReceiver().equals(otherUser)) {
                messages.add(shortMessage);
            }
        }
        return messages;
    }

    public String getName() {
        return msgBoxName;
    }
    
    public long getLastTimestamp() {
        return lastTimestamp;
    }

    /**
     * Clear all old instances of the short message
     * 
     * @param shortMessage the message that will be inserted or updated.
     */
    private void clearOldInstances(ShortMessage shortMessage) {
        this.readMessages.remove(shortMessage);
        this.unreadMessages.remove(shortMessage);
    }

    private void loadMessages() {
        unreadMessages.addAll(persistence.getUnreadMessages(msgBoxName));
        readMessages.addAll(persistence.getReadMessages(msgBoxName));
        sentMessages.addAll(persistence.getSentMessages(msgBoxName));
        
        for (ShortMessage shortMessage : unreadMessages) {
            if (shortMessage.getTimestamp() > lastTimestamp) {
                this.lastTimestamp = shortMessage.getTimestamp();
            }
        }

        for (ShortMessage shortMessage : readMessages) {
            if (shortMessage.getTimestamp() > lastTimestamp) {
                this.lastTimestamp = shortMessage.getTimestamp();
            }
        }

        for (ShortMessage shortMessage : sentMessages) {
            if (shortMessage.getTimestamp() > lastTimestamp) {
                this.lastTimestamp = shortMessage.getTimestamp();
            }
        }
    }
}
