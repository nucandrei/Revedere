package org.nuc.revedere.client.connector;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

public class MinaHandler extends IoHandlerAdapter {
    private IoHandler ioHandler;

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        if (ioHandler != null) {
            ioHandler.messageReceived(session, message);
        }
    }

    public void setHandler(IoHandler handler) {
        this.ioHandler = handler;
    }

    public void removeHandler() {
        this.ioHandler = null;
    }
}
