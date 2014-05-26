package org.nuc.revedere.client;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.nuc.revedere.client.connector.MinaClient;
import org.nuc.revedere.core.UserCollector;
import org.nuc.revedere.core.messages.Response;
import org.nuc.revedere.core.messages.request.LogoutRequest;
import org.nuc.revedere.core.messages.request.UserListRequest;
import org.nuc.revedere.core.messages.update.UserListUpdate;
import org.nuc.revedere.util.Convertor;
import org.nuc.revedere.util.Collector.CollectorListener;

public class RevedereSession {
    private final MinaClient minaClient;
    private final String username;
    private final UserCollector userCollector;

    public RevedereSession(MinaClient minaClient, String username) {
        this.minaClient = minaClient;
        this.username = username;
        this.userCollector = new UserCollector();
        initialize();
    }

    public void addListenerToUserCollector(CollectorListener<UserListUpdate> listener) {
        this.userCollector.addListener(listener);
    }

    public void removeListenerFromUserCollector(CollectorListener<UserListUpdate> listener) {
        this.userCollector.removeListener(listener);
    }

    public void logout() {
        this.minaClient.sendMessage(new LogoutRequest(username));
    }

    private void initialize() {
        this.minaClient.setHandler(new IoHandlerAdapter() {
            @Override
            public void messageReceived(IoSession session, Object message) {
                final Response<UserListRequest> possibleResponse = new Convertor<Response<UserListRequest>>().convert(message);
                if (possibleResponse != null) {
                    userCollector.agregate((UserListUpdate) possibleResponse.getAttachment());
                }
            }
        });
    }
}
