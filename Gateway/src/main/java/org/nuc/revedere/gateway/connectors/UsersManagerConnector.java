package org.nuc.revedere.gateway.connectors;

import org.nuc.distry.service.Service;
import org.nuc.revedere.core.messages.Response;
import org.nuc.revedere.core.messages.ack.Acknowledgement;
import org.nuc.revedere.core.messages.request.LoginRequest;
import org.nuc.revedere.core.messages.request.LogoutRequest;
import org.nuc.revedere.core.messages.request.RegisterRequest;
import org.nuc.revedere.core.messages.request.UnregisterRequest;
import org.nuc.revedere.service.core.JMSRequestor;
import org.nuc.revedere.service.core.JMSShouter;
import org.nuc.revedere.service.core.Topics;

public class UsersManagerConnector {
    private Service supportService;

    public UsersManagerConnector(Service supportService) {
        this.supportService = supportService;
    }

    public Response<LoginRequest> login(LoginRequest request) {
        final JMSRequestor<LoginRequest> requestor = new JMSRequestor<>(supportService);
        return requestor.request(Topics.USERS_TOPIC, request);
    }

    public Response<RegisterRequest> register(RegisterRequest request) {
        final JMSRequestor<RegisterRequest> requestor = new JMSRequestor<>(supportService);
        return requestor.request(Topics.USERS_TOPIC, request);
    }

    public Response<UnregisterRequest> unregister(UnregisterRequest request) {
        final JMSRequestor<UnregisterRequest> requestor = new JMSRequestor<>(supportService);
        return requestor.request(Topics.USERS_TOPIC, request);
    }

    public void logout(LogoutRequest request) {
        final JMSShouter<LogoutRequest> shouter = new JMSShouter<>(supportService);
        shouter.shout(Topics.USERS_TOPIC, request);
    }

    public void acknowledgeLogin(Acknowledgement<LoginRequest> ack) {
        final JMSShouter<Acknowledgement<LoginRequest>> shouter = new JMSShouter<>(supportService);
        shouter.shout(Topics.USERS_TOPIC, ack);
    }
}
