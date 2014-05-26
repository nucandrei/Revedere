package org.nuc.revedere.core.messages.request;

public class LogoutRequest extends Request {
    private static final long serialVersionUID = 60197011619683657L;
    private final String username;

    public LogoutRequest(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
