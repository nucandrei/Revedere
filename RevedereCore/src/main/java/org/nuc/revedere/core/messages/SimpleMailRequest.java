package org.nuc.revedere.core.messages;

public class SimpleMailRequest extends Request {
    private static final long serialVersionUID = -2485315669367301785L;

    private final String destination;
    private final String subject;
    private final String content;

    public SimpleMailRequest(String destination, String subject, String content) {
        this.destination = destination;
        this.subject = subject;
        this.content = content;
    }

    public String getDestination() {
        return destination;
    }

    public String getSubject() {
        return subject;
    }

    public String getContent() {
        return content;
    }

}
