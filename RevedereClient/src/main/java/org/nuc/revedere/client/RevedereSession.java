package org.nuc.revedere.client;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.nuc.revedere.client.connector.MinaClient;
import org.nuc.revedere.core.messages.LogoutRequest;
import org.nuc.revedere.core.messages.Response;
import org.nuc.revedere.core.messages.UserListRequest;
import org.nuc.revedere.util.Convertor;

public class RevedereSession {
    private final MinaClient minaClient;
    private final String username;

    public RevedereSession(MinaClient minaClient, String username) {
        this.minaClient = minaClient;
        this.username = username;
    }

    public void attachListener(final RevedereListener listener) {
        this.minaClient.setHandler(new IoHandlerAdapter() {
            @Override
            public void messageReceived(IoSession session, Object message) {
                final Response<UserListRequest> possibleResponse = new Convertor<Response<UserListRequest>>().convert(message);
                if (possibleResponse != null) {
                    listener.onUserListUpdate(possibleResponse);
                }
            }
        });
    }

    public void logout() {
        this.minaClient.sendMessage(new LogoutRequest(username));
    }
}
