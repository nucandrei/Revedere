package org.nuc.revedere.core.messages.request;

import org.nuc.revedere.core.User;

public class ShortMessageEmptyBoxRequest extends Request {
    private static final long serialVersionUID = -5074231039837942457L;
    private final User user;

    public ShortMessageEmptyBoxRequest(User user) {
        this.user = user;
    }

    public User getUser() {
        return this.user;
    }
}
