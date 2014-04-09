package org.nuc.revedere.service.core;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.jms.MessageListener;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.nuc.revedere.util.LoggerUtil;

/**
 * Base class for all services in Review
 * @author Nuc
 *
 */
public class Service {
    private static final String DEFAULT_LOG4J_FILE = "log4j.properties";
    private final BrokerAdapter brokerAdapter;
    private final Map<String, String> settings;
    public final Logger LOGGER;
    private final String serviceName;
    
    /**
     * Create the underline service
     * @param serviceName the name of the service
     * @throws Exception if something was not in its place
     */
    public Service(final String serviceName) throws Exception {
        this.serviceName = serviceName;
        settings = loadSettingsFromFile(String.format("%s.xml", serviceName));
        LOGGER = Logger.getLogger(serviceName);
        String log4jFile = settings.get("log4jFile");
        if (log4jFile == null) {
            log4jFile = DEFAULT_LOG4J_FILE;
        }
        PropertyConfigurator.configure(log4jFile);
        LoggerUtil.startLoggingSession(LOGGER, "New run");

        String brokerAddress = settings.get("brokerAddress");
        if (brokerAddress == null) {
            brokerAddress = "failover://(tcp://localhost:61616)?initialReconnectDelay=2000&maxReconnectAttempts=2";
        }
        this.brokerAdapter = new ActiveMQBrokerAdapter(brokerAddress);
    }
    
    /**
     * Load settings from specified file
     * @param filename the name of settings file
     * @return settings loaded from file
     * @throws Exception if the file could not be read or parsing problems ocurred
     */
    private Map<String, String> loadSettingsFromFile(String filename) throws Exception {
        Map<String, String> settings = new HashMap<String, String>();
        final File file = new File(filename);
        final SAXBuilder builder = new SAXBuilder();
        final Document doc = (Document) builder.build(file);
        final Element rootNode = doc.getRootElement();
        for (Element e : rootNode.getChildren()) {
            final String propertyName = e.getName();
            final String propertyValue = e.getValue();
            settings.put(propertyName, propertyValue);
        }
        return settings;
    }
    
    public void setMessageListener(String topic, MessageListener listener) throws Exception {
        brokerAdapter.setMessageListener(topic, listener);
    }
    
    public void sendMessage(String topic, Serializable message) throws Exception {
        brokerAdapter.sendMessage(topic, message);
    }
    
    public Map<String, String> getSettings() {
        return settings;
    }
    
    public String getServiceName() {
        return this.serviceName;
    }
}
