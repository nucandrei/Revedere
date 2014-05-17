package org.nuc.revedere.client.connector;

import java.util.ArrayList;
import java.util.List;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.nuc.revedere.util.Tuple;

public class MinaHandler extends IoHandlerAdapter {
    private IoHandler ioHandler;
    private List<Tuple<IoSession, Object>> receivedButNotConsumedMessages = new ArrayList<Tuple<IoSession, Object>>();

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        if (ioHandler != null) {
            ioHandler.messageReceived(session, message);
        } else {
            receivedButNotConsumedMessages.add(new Tuple<IoSession, Object>(session, message));
        }
    }

    public void setHandler(IoHandler handler) {
        this.ioHandler = handler;
        if (this.ioHandler != null) {
            for (Tuple<IoSession, Object> tuple : receivedButNotConsumedMessages) {
                try {
                    messageReceived(tuple.getTItem(), tuple.getUItem());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void removeHandler() {
        this.ioHandler = null;
    }
}
