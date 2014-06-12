package org.nuc.revedere.shortmessage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.nuc.revedere.core.User;

public class ShortMessage implements Serializable, Comparable<ShortMessage> {
    private static final long serialVersionUID = 1309244336710468632L;
    private final User sender;
    private final User receiver;
    private final String content;
    private final long timestamp;
    private boolean isRead = false;

    public ShortMessage(User sender, User receiver, String content, long timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = timestamp;
    }

    public User getSender() {
        return sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isRead() {
        return this.isRead;
    }

    public void markAsRead() {
        this.isRead = true;
    }

    @Override
    public int compareTo(ShortMessage that) {
        if (this.timestamp < that.timestamp) {
            return -1;
        } else if (this.timestamp == that.timestamp) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public String toString() {
        return String.format("%s : %s", this.sender, this.content);
    }

    public List<ShortMessage> asList() {
        final List<ShortMessage> oneElementList = new ArrayList<ShortMessage>(1);
        oneElementList.add(this);
        return oneElementList;
    }
}