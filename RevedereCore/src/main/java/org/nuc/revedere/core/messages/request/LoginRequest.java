package org.nuc.revedere.core.messages.request;

public class LoginRequest extends CredentialsBasedRequest {
    public static final String FROM_HEARTMONITOR = "HeartMonitor";

    private static final long serialVersionUID = -4732598462818756875L;
    private final String loginSource;

    public LoginRequest(String username, String authInfo, String loginSource) {
        super(username, authInfo);
        this.loginSource = loginSource;
    }

    public String getLoginSource() {
        return this.loginSource;
    }

}
