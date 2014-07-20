package org.nuc.revedere.mail;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.SimpleEmail;
import org.apache.log4j.Logger;
import org.jdom2.JDOMException;
import org.nuc.distry.service.DistryListener;
import org.nuc.distry.service.ServiceConfiguration;
import org.nuc.distry.service.hb.ServiceState;
import org.nuc.distry.service.messaging.ActiveMQAdapter;
import org.nuc.revedere.core.messages.Response;
import org.nuc.revedere.core.messages.request.SimpleMailRequest;
import org.nuc.revedere.service.core.RevedereService;
import org.nuc.revedere.service.core.SupervisorTopics;
import org.nuc.revedere.service.core.Topics;

public class MailService extends RevedereService {
    private static final Logger LOGGER = Logger.getLogger(MailService.class);
    private static final String SETTINGS_PATH = "MailService.xml";
    private String hostName;
    private int port;
    private String from;
    private String password;

    public MailService(ServiceConfiguration serviceConfiguration) throws Exception {
        super("MailService", serviceConfiguration);
        super.start(false);

        loadSettings();
        startListeningForMailRequests();
    }

    private void loadSettings() throws JDOMException, IOException {
        final Map<String, String> settings = getSettings(SETTINGS_PATH);
        hostName = settings.get("hostname");
        port = Integer.parseInt(settings.get("port"));
        from = settings.get("from");
        password = settings.get("password");

        if (hostName == null || from == null || password == null) {
            LOGGER.error("A field was not found in config file");
            setServiceState(ServiceState.FATAL, false);
            setServiceComment("Invalid config", true);
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
            setServiceState(ServiceState.ERROR, false);
            setServiceComment("Could not send e-mail", true);
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
            this.addMessageListener(Topics.MAIL_REQUEST_TOPIC, new DistryListener() {
                public void onMessage(Serializable message) {
                    try {
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
            setServiceState(ServiceState.ERROR, false);
            setServiceComment("Could not listen to mail requests", true);
        }
    }

    private Response<SimpleMailRequest> handleRequest(SimpleMailRequest request) {
        final String to = request.getDestination();
        final String subject = request.getSubject();
        final String content = request.getContent();
        boolean mailSent = sendMail(to, subject, content);
        return new Response<>(request, mailSent, mailSent ? "Mail sent" : "Mail not sent");
    }

    public static void main(String[] args) {
        try {
            final String serverAddress = parseArguments(args);
            final ServiceConfiguration serviceConfiguration = new ServiceConfiguration(new ActiveMQAdapter(serverAddress), true, 10000, SupervisorTopics.HEARTBEAT_TOPIC, true, SupervisorTopics.COMMAND_TOPIC, SupervisorTopics.PUBLISH_TOPIC);
            new MailService(serviceConfiguration);

        } catch (Exception e) {
            LOGGER.error("Failed to start mail service", e);
        }
    }
}
