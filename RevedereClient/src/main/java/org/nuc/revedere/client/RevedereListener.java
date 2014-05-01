package org.nuc.revedere.client;

import org.nuc.revedere.core.messages.Response;
import org.nuc.revedere.core.messages.UserListRequest;

public interface RevedereListener {
    public void onUserListUpdate(Response<UserListRequest> response);
}
