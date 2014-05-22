package org.nuc.revedere.mail;

import java.io.Serializable;
import java.util.Map;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;
import org.nuc.revedere.core.messages.SimpleMailRequest;
import org.nuc.revedere.core.messages.Response;
import org.nuc.revedere.service.core.Service;
import org.nuc.revedere.service.core.RevedereService;
import org.nuc.revedere.service.core.Topics;
import org.nuc.revedere.service.core.hb.ServiceState;

public class MailService extends RevedereService {

    private String hostName;
    private int port;
    private String from;
    private String password;

    public MailService() throws Exception {
        super("MailService");
        super.start(true, true, false);
        
        loadSettings();
        startListeningForMailRequests();
    }

    private void loadSettings() {
        final Map<String, String> settings = getSettings();
        hostName = settings.get("hostname");
        port = Integer.parseInt(settings.get("port"));
        from = settings.get("from");
        password = settings.get("password");

        if (hostName == null || from == null || password == null) {
            LOGGER.error("A field was not found in config file");
            setServiceState(ServiceState.FATAL);
            setServiceComment("Invalid config");
            shutdownGracefully();
        } else {
            LOGGER.info("Loaded mail service settings");
        }
    }

    private boolean sendMail(String to, String subject, String content) {
        final Email email = generateSimpleMail(from, password);
        try {
            email.setFrom(from);
            email.setSubject(subject);
            email.setMsg(content);
            email.addTo(to);
            email.send();
            return true;
        } catch (Exception e) {
            LOGGER.error("Could not send e-mail", e);
            setServiceState(ServiceState.ERROR);
            setServiceComment("Could not send e-mail");
            return false;
        }
    }

    private Email generateSimpleMail(String from, String password) {
        final Email email = new SimpleEmail();
        email.setHostName(hostName);
        email.setSmtpPort(port);
        email.setSSLOnConnect(true);
        email.setAuthentication(from, password);
        return email;
    }

    private void startListeningForMailRequests() {
        try {
            this.setMessageListener(Topics.MAIL_REQUEST_TOPIC, new MessageListener() {
                public void onMessage(Message msg) {
                    final ObjectMessage objectMessage = (ObjectMessage) msg;
                    try {
                        final Serializable message = (Serializable) objectMessage.getObject();
                        if (message instanceof SimpleMailRequest) {
                            final SimpleMailRequest request = (SimpleMailRequest) message;
                            final Response<SimpleMailRequest> response = handleRequest(request);
                            sendMessage(Topics.MAIL_RESPONSE_TOPIC, response);
                        }
                    } catch (Exception e) {
                        LOGGER.error("Caught exception while processing received message", e);
                    }
                }
            });
        } catch (Exception e) {
            LOGGER.error("Could not set message listener on topic " + Topics.MAIL_REQUEST_TOPIC, e);
            setServiceState(ServiceState.ERROR);
            setServiceComment("Could not listen to mail requests");
        }
    }

    private Response<SimpleMailRequest> handleRequest(SimpleMailRequest request) {
        final String to = request.getDestination();
        final String subject = request.getSubject();
        final String content = request.getContent();
        boolean mailSent = sendMail(to, subject, content);
        return new Response<SimpleMailRequest>(request, mailSent, mailSent ? "Mail sent" : "Mail not sent");
    }

    public static void main(String[] args) {
        try {
            new MailService();
        } catch (Exception e) {
            Service.BACKUP_LOGGER.error("Could not start mail service", e);
        }
    }
}
