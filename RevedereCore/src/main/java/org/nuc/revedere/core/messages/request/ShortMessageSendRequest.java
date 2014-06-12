package org.nuc.revedere.core.messages.request;

import org.nuc.revedere.shortmessage.ShortMessage;

public class ShortMessageSendRequest extends Request {
    private static final long serialVersionUID = -8740939618094483221L;
    private final ShortMessage shortMessage;

    public ShortMessageSendRequest(ShortMessage shortMessage) {
        this.shortMessage = shortMessage;
    }

    public ShortMessage getShortMessage() {
        return shortMessage;
    }

}
