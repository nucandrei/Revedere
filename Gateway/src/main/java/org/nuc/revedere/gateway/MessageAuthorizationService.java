package org.nuc.revedere.gateway;

import org.apache.mina.core.session.IoSession;

public interface MessageAuthorizationService {
    public boolean isAllowed(IoSession session, Object request);
}
