package org.nuc.revedere.gateway;

import java.io.IOException;

import javax.jms.JMSException;

import org.apache.mina.core.session.IoSession;
import org.jdom2.JDOMException;
import org.nuc.revedere.core.messages.LoginRequest;
import org.nuc.revedere.core.messages.LogoutRequest;
import org.nuc.revedere.core.messages.RegisterRequest;
import org.nuc.revedere.core.messages.UnregisterRequest;
import org.nuc.revedere.service.core.Service;
import org.nuc.revedere.service.core.SupervisedService;

public class Gateway extends SupervisedService {
    private final static String GATEWAY_SERVICE_NAME = "Gateway";

    public Gateway() throws JDOMException, IOException, JMSException {
        super(GATEWAY_SERVICE_NAME);
        start();
    }

    private void start() throws IOException {
        final GatewayListener gatewayListener = new GatewayListener() {
            @Override
            public void onLoginRequest(LoginRequest request, IoSession session) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onLogoutRequest(LogoutRequest request, IoSession session) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onRegisterRequest(RegisterRequest request, IoSession session) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onUnregisterRequest(UnregisterRequest request, IoSession session) {
                // TODO Auto-generated method stub

            }
        };

        new MinaServer(new ServerHandler(gatewayListener));
    }

    public static void main(String[] args) {
        try {
            new Gateway();
        } catch (JDOMException | IOException | JMSException e) {
            Service.BACKUP_LOGGER.error("Could not start gateway", e);
        }
    }
}
