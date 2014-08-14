package org.nuc.revedere.gateway;

import org.apache.mina.core.session.IoSession;
import org.nuc.revedere.core.messages.ack.LoginAcknowledgement;
import org.nuc.revedere.core.messages.request.LoginRequest;
import org.nuc.revedere.core.messages.request.LogoutRequest;
import org.nuc.revedere.core.messages.request.RegisterRequest;
import org.nuc.revedere.core.messages.request.ReviewDocumentRequest;
import org.nuc.revedere.core.messages.request.ReviewHistoricalRequest;
import org.nuc.revedere.core.messages.request.ReviewMarkAsSeenRequest;
import org.nuc.revedere.core.messages.request.ReviewUpdateRequest;
import org.nuc.revedere.core.messages.request.ReviewRequest;
import org.nuc.revedere.core.messages.request.ShortMessageEmptyBoxRequest;
import org.nuc.revedere.core.messages.request.ShortMessageHistoricalRequest;
import org.nuc.revedere.core.messages.request.ShortMessageMarkAsReadRequest;
import org.nuc.revedere.core.messages.request.ShortMessageSendRequest;
import org.nuc.revedere.core.messages.request.UnregisterRequest;

public interface GatewayListener {
    public void onLoginRequest(LoginRequest request, IoSession session);

    public void onAcknowledgement(LoginAcknowledgement acknowledgement, IoSession session);

    public void onRegisterRequest(RegisterRequest request, IoSession session);

    public void onUnregisterRequest(UnregisterRequest request, IoSession session);

    public void onLogoutRequest(LogoutRequest request, IoSession session);

    public void onIdleSession(IoSession session);

    public void onClosedSession(IoSession session);

    public void onShortMessageSendRequest(ShortMessageSendRequest request, IoSession session);

    public void onShortMessageEmptyBoxRequest(ShortMessageEmptyBoxRequest request, IoSession session);

    public void onShortMessageHistoricalRequest(ShortMessageHistoricalRequest request, IoSession session);

    public void onShortMessageMarkAsRead(ShortMessageMarkAsReadRequest request, IoSession session);

    public void onRequestReview(ReviewRequest request, IoSession session);

    public void onReviewMarkAsSeen(ReviewMarkAsSeenRequest request, IoSession session);
    
    public void onReviewUpdate(ReviewUpdateRequest request, IoSession session);

    public void onPing(IoSession session);

    public void onReviewHistoricalRequest(ReviewHistoricalRequest message, IoSession session);
    
    public void onReviewDocumentRequest(ReviewDocumentRequest message, IoSession session);
}
