package org.nuc.revedere.service.core;

public class Topics {

    public static final String USERS_TOPIC = "Revedere.Users";

    public static final String USERS_REQUEST_TOPIC = "Revedere.Users.Request";

    public static final String USERS_RESPONSE_TOPIC = "Revedere.Users.Response";
    
    public static final String SHORT_MESSAGE_TOPIC = "Revedere.ShortMessage";
    
    public static final String SHORT_MESSAGE_REQUEST_TOPIC = "Revedere.ShortMessage.Request";
    
    public static final String SHORT_MESSAGE_RESPONSE_TOPIC = "Revedere.ShortMessage.Response";    
    
    public static final String REVIEW_TOPIC = "Revedere.Review";
    
    public static final String REVIEW_REQUEST_TOPIC = "Revedere.Review.Request";
    
    public static final String REVIEW_RESPONSE_TOPIC = "Revedere.Review.Response";

    public static final String MAIL_REQUEST_TOPIC = "Revedere.Mail.Request";

    public static final String MAIL_RESPONSE_TOPIC = "Revedere.Mail.Response";

    private Topics() {
        // empty constructor. It protects from creating instances of Topics
    }

}
