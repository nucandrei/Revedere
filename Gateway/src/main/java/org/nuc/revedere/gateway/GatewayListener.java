package org.nuc.revedere.gateway;

import org.apache.mina.core.session.IoSession;
import org.nuc.revedere.core.messages.LoginRequest;
import org.nuc.revedere.core.messages.LogoutRequest;
import org.nuc.revedere.core.messages.RegisterRequest;
import org.nuc.revedere.core.messages.UnregisterRequest;

public interface GatewayListener {
    public void onLoginRequest(LoginRequest request, IoSession session);

    public void onRegisterRequest(RegisterRequest request, IoSession session);
    
    public void onUnregisterRequest(UnregisterRequest request, IoSession session);

    public void onLogoutRequest(LogoutRequest request, IoSession session);

    public void onIdleSession(IoSession session);

    public void onClosedSession(IoSession session);

    public void onPing(IoSession session);
}
