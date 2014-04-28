package org.nuc.revedere.gateway.connectors;

import org.nuc.revedere.core.messages.LoginRequest;
import org.nuc.revedere.core.messages.LogoutRequest;
import org.nuc.revedere.core.messages.RegisterRequest;
import org.nuc.revedere.core.messages.Response;
import org.nuc.revedere.core.messages.UnregisterRequest;
import org.nuc.revedere.service.core.Requestor;
import org.nuc.revedere.service.core.Service;
import org.nuc.revedere.service.core.Topics;

public class UsersManagerConnector {
    private Service supportService;

    public UsersManagerConnector(Service supportService) {
        this.supportService = supportService;
    }

    public Response<LoginRequest> login(LoginRequest request) {
        Requestor<LoginRequest> requestor = new Requestor<LoginRequest>(supportService);
        return requestor.request(Topics.USERS_TOPIC, request);
    }

    public Response<RegisterRequest> register(RegisterRequest request) {
        Requestor<RegisterRequest> requestor = new Requestor<RegisterRequest>(supportService);
        return requestor.request(Topics.USERS_TOPIC, request);
    }
    
    public Response<UnregisterRequest> unregister(UnregisterRequest request) {
        Requestor<UnregisterRequest> requestor = new Requestor<UnregisterRequest>(supportService);
        return requestor.request(Topics.USERS_TOPIC, request);
    }

    public void logout(LogoutRequest request) {
        new Requestor<LogoutRequest>(supportService).inform(Topics.USERS_TOPIC, request);
    }
}
