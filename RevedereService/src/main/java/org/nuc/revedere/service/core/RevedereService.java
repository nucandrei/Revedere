package org.nuc.revedere.service.core;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.nuc.distry.service.DistryListener;
import org.nuc.distry.service.DistryService;
import org.nuc.distry.service.Service;
import org.nuc.distry.service.ServiceConfiguration;
import org.nuc.distry.service.messaging.ActiveMQAdapter;
import org.nuc.revedere.core.UserCollector;
import org.nuc.revedere.core.messages.Response;
import org.nuc.revedere.core.messages.request.UserListRequest;
import org.nuc.revedere.core.messages.update.UserListUpdate;

public class RevedereService extends DistryService {
    private static final Logger LOGGER = Logger.getLogger(RevedereService.class);
    private static final String SERVER_ADDRESS = "serverAddress";
    private static final String LOG4J = "log4j";
    private final UserCollector userCollector = new UserCollector();

    public RevedereService(String serviceName, ServiceConfiguration serviceConfiguration) throws Exception {
        super(serviceName, serviceConfiguration);
    }

    public void start(boolean listenForUsers) throws Exception {
        start();
        if (listenForUsers) {
            startListeningForUsers(this, userCollector);
        }
    }

    public static void startListeningForUsers(Service supportService, final UserCollector userCollector) throws JMSException, InterruptedException {
        LOGGER.info("Sending users list request");
        final JMSRequestor<UserListRequest> requestor = new JMSRequestor<>(supportService);
        final Response<UserListRequest> receivedResponse = requestor.request(Topics.USERS_TOPIC, new UserListRequest());
        if (receivedResponse == null) {
            LOGGER.error("Did not receive response in timeout");
            return;
        }
        if (receivedResponse.hasAttachment()) {
            final UserListUpdate userListUpdate = (UserListUpdate) receivedResponse.getAttachment();
            userCollector.agregate(userListUpdate);
        } else {
            LOGGER.error("Expected users list as attachement, received nothing instead");
            return;
        }

        supportService.addMessageListener(Topics.USERS_TOPIC, new DistryListener() {
            public void onMessage(Serializable message) {
                try {
                    @SuppressWarnings("unchecked")
                    final Response<UserListRequest> userListResponse = (Response<UserListRequest>) message;
                    if (userListResponse.hasAttachment()) {
                        final UserListUpdate userListUpdate = (UserListUpdate) userListResponse.getAttachment();
                        userCollector.agregate(userListUpdate);
                    } else {
                        LOGGER.error("Expected users list as attachement, received nothing instead");
                    }
                } catch (ClassCastException e) {
                    // ignore the exception
                }
            }
        });
    }

    public UserCollector getUserCollector() {
        return userCollector;
    }
    
    public Map<String, String> getSettings(String filepath) throws JDOMException, IOException {
        Map<String, String> loadedSettings = new HashMap<>();
        final Document settingsDocument = loadXMLDocument(filepath);
        final Element rootNode = settingsDocument.getRootElement();
        for (Element e : rootNode.getChildren()) {
            final String propertyName = e.getName();
            final String propertyValue = e.getValue();
            loadedSettings.put(propertyName, propertyValue);
        }
        return loadedSettings;
    }

    public static String parseArguments(String[] args) throws Exception {
        final Options options = new Options();
        options.addOption(LOG4J, true, "Log4j configuration file");
        options.addOption(SERVER_ADDRESS, true, "Server address");

        final CommandLineParser parser = new BasicParser();
        final CommandLine commandLine = parser.parse(options, args);

        if (commandLine.hasOption(LOG4J)) {
            PropertyConfigurator.configure(commandLine.getOptionValue(LOG4J));

        } else {
            throw new IllegalArgumentException("log4j argument is missing");
        }

        if (commandLine.hasOption(SERVER_ADDRESS)) {
            return commandLine.getOptionValue(SERVER_ADDRESS);

        } else {
            throw new IllegalArgumentException("serverAddress argument is missing");
        }
    }

    public static void main(String[] args) {
        try {
            final String serverAddress = parseArguments(args);
            final ServiceConfiguration serviceConfiguration = new ServiceConfiguration(new ActiveMQAdapter(serverAddress), true, 10000, SupervisorTopics.HEARTBEAT_TOPIC, true, SupervisorTopics.COMMAND_TOPIC, SupervisorTopics.PUBLISH_TOPIC);
            new RevedereService("Service", serviceConfiguration);
            
        } catch (Exception e) {
            LOGGER.error("Failed to start service", e);
        }
    }
}
