package org.nuc.revedere.core.messages;

import java.io.Serializable;
import java.util.UUID;

public class Request implements Serializable {
    private static final long serialVersionUID = 1963141029735822613L;
    private final String id = UUID.randomUUID().toString();

    public Request() {

    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Request)) {
            return false;
        }
        Request that = (Request) object;
        return this.id.equals(that.id);
    }
}
