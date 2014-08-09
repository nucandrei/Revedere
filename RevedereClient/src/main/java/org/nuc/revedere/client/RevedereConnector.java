package org.nuc.revedere.client;

import org.nuc.revedere.client.connector.MinaClient;
import org.nuc.revedere.core.messages.Response;
import org.nuc.revedere.core.messages.ack.LoginAcknowledgement;
import org.nuc.revedere.core.messages.request.LoginRequest;
import org.nuc.revedere.core.messages.request.RegisterRequest;
import org.nuc.revedere.core.messages.request.UnregisterRequest;

public class RevedereConnector {
    private final MinaClient minaClient;

    public RevedereConnector(String address) throws Exception {
        this.minaClient = new MinaClient(address);
    }

    public RevedereSession login(String username, String authInfo) throws Exception {
        final LoginRequest loginRequest = new LoginRequest(username, authInfo);
        final MinaRequestor<LoginRequest> requestor = new MinaRequestor<>(minaClient);
        final Response<LoginRequest> response = requestor.request(loginRequest);
        if (response.isSuccessfull()) {
            sendAck(response);
            return new RevedereSession(minaClient, username);
        }
        throw new Exception(response.getMessage());
    }

    public String register(String username, String authInfo) {
        final RegisterRequest registerRequest = new RegisterRequest(username, authInfo);
        final MinaRequestor<RegisterRequest> requestor = new MinaRequestor<>(minaClient);
        final Response<RegisterRequest> response = requestor.request(registerRequest);
        return response.getMessage();
    }

    public String unregister(String username, String authInfo) {
        final UnregisterRequest unregisterRequest = new UnregisterRequest(username, authInfo);
        final MinaRequestor<UnregisterRequest> requestor = new MinaRequestor<>(minaClient);
        final Response<UnregisterRequest> response = requestor.request(unregisterRequest);
        return response.getMessage();
    }

    public void disconnect() {
        minaClient.close();
    }

    private void sendAck(Response<LoginRequest> loginResponse) {
        minaClient.sendMessage(new LoginAcknowledgement(loginResponse));
    }
}
