package org.nuc.revedere.core.messages.request;

public class RegisterRequest extends CredentialsBasedRequest {

    private static final long serialVersionUID = 6901638574590198176L;
    private final String realName;
    private final boolean publishRealName;
    private final String emailAddress;
    private final boolean allowEmails;

    public RegisterRequest(String username, String authInfo, String realName, boolean publishRealName, String emailAddress, boolean allowEmails) {
        super(username, authInfo);
        this.realName = realName;
        this.publishRealName = publishRealName;
        this.emailAddress = emailAddress;
        this.allowEmails = allowEmails;
    }

    public String getRealName() {
        return realName;
    }

    public boolean publishRealName() {
        return publishRealName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public boolean allowEmails() {
        return allowEmails;
    }
}
