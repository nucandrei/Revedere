package org.nuc.revedere.client;

import org.nuc.revedere.core.messages.Response;
import org.nuc.revedere.core.messages.request.UserListRequest;

public interface RevedereListener {
    public void onUserListUpdate(Response<UserListRequest> response);
}
