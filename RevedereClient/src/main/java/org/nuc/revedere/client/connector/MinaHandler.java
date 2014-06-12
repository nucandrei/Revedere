package org.nuc.revedere.client.connector;

import java.util.ArrayList;
import java.util.List;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.nuc.revedere.util.Tuple;

public class MinaHandler extends IoHandlerAdapter {
    private List<IoHandler> handlers = new ArrayList<IoHandler>();
    private List<Tuple<IoSession, Object>> receivedButNotConsumedMessages = new ArrayList<Tuple<IoSession, Object>>();

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        if (handlers.isEmpty()) {
            receivedButNotConsumedMessages.add(new Tuple<IoSession, Object>(session, message));
        } else {
            for (IoHandler handler : handlers) {
                handler.messageReceived(session, message);
            }
        }
    }

    public void addHandler(IoHandler handler) {
        this.handlers.add(handler);
        for (Tuple<IoSession, Object> tuple : receivedButNotConsumedMessages) {
            try {
                messageReceived(tuple.getTItem(), tuple.getUItem());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void removeHandler(IoHandler handler) {
        this.handlers.remove(handler);
    }
}
