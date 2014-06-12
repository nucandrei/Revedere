package org.nuc.revedere.core.messages.update;

import java.util.List;

import org.nuc.revedere.core.User;
import org.nuc.revedere.shortmessage.ShortMessage;

public class ShortMessageUpdate extends Update {
    private static final long serialVersionUID = -3353543330368047211L;
    private final List<ShortMessage> messages;
    private final User intendedReceiver;

    public ShortMessageUpdate(List<ShortMessage> messages, User intendedReceiver) {
        super(true);
        this.messages = messages;
        this.intendedReceiver = intendedReceiver;
    }

    public List<ShortMessage> getUpdate() {
        return this.messages;
    }
    
    public User getIntendedReceiver() {
        return intendedReceiver;
    }
}
