package org.nuc.revedere.core.messages.request;

import java.util.List;

import org.nuc.revedere.core.User;
import org.nuc.revedere.shortmessage.ShortMessage;

public class ShortMessageMarkAsReadRequest extends Request {
    private static final long serialVersionUID = 7921983862467190112L;
    private final User user;
    private final List<ShortMessage> messages;

    public ShortMessageMarkAsReadRequest(User user, List<ShortMessage> messages) {
        this.user = user;
        this.messages = messages;
    }

    public User getUser() {
        return user;
    }

    public List<ShortMessage> getMessages() {
        return messages;
    }

}
