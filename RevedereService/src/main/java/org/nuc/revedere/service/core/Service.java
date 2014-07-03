package org.nuc.revedere.service.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.nuc.revedere.util.LoggerUtil;

/**
 * Base class for all services in Review
 * @author Nuc
 *
 */
public class Service {
    private static final String DEFAULT_LOG4J_FILE = "log4j.properties";
    private static final Object NEW_LINE = "\r\n";
    private final BrokerAdapter brokerAdapter;
    private final Map<String, String> settings;
    public static Logger LOGGER;
    public final static Logger BACKUP_LOGGER = Logger.getLogger("backupLogger");
    private final String serviceName;
    
    /**
     * Create the underline service
     * @param serviceName the name of the service
     * @param settingsPath the path of the settings file
     * @throws IOException 
     * @throws JDOMException 
     * @throws JMSException 
     * @throws Exception if something was not in its place
     */
    public Service(final String serviceName, String settingsPath) throws JDOMException, IOException, JMSException {
        this.serviceName = serviceName;
        settings = loadSettingsFromFile(settingsPath);
        LOGGER = Logger.getLogger(serviceName);
        String log4jFile = settings.get("log4jFile");
        if (log4jFile == null) {
            log4jFile = DEFAULT_LOG4J_FILE;
        }
        PropertyConfigurator.configure(log4jFile);
        LoggerUtil.startLoggingSession("New run");

        String brokerAddress = settings.get("brokerAddress");
        if (brokerAddress == null) {
            brokerAddress = "failover://(tcp://localhost:61616)?initialReconnectDelay=2000&maxReconnectAttempts=2";
        }
        this.brokerAdapter = new ActiveMQBrokerAdapter(brokerAddress);
    }
    
    public Document loadXMLDocument(String documentName) throws JDOMException, IOException {
        final File file = new File(documentName);
        final SAXBuilder builder = new SAXBuilder();
        return builder.build(file);
    }
    
    public String loadTextFile(String filepath) throws IOException {
        final File file = new File(filepath);
        final BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        String line;
        final StringBuilder stringBuilder = new StringBuilder();
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append(NEW_LINE);
        }
        bufferedReader.close();
        return stringBuilder.toString();
    }
    
    /**
     * Load settings from specified file
     * @param filename the name of settings file
     * @return settings loaded from file
     * @throws IOException 
     * @throws JDOMException 
     * @throws Exception if the file could not be read or parsing problems ocurred
     */
    private Map<String, String> loadSettingsFromFile(String filename) throws JDOMException, IOException {
        Map<String, String> loadedSettings = new HashMap<>();
        final Document settingsDocument = loadXMLDocument(filename);
        final Element rootNode = settingsDocument.getRootElement();
        for (Element e : rootNode.getChildren()) {
            final String propertyName = e.getName();
            final String propertyValue = e.getValue();
            loadedSettings.put(propertyName, propertyValue);
        }
        return loadedSettings;
    }
    
    public void addMessageListener(String topic, BrokerMessageListener brokerMessageListener) throws JMSException {
        brokerAdapter.addMessageListener(topic, brokerMessageListener);
    }
    
    public void removeMessageListener(String topic, BrokerMessageListener brokerMessageListener) {
        brokerAdapter.removeMessageListener(topic, brokerMessageListener);
    }
    
    public void sendMessage(String topic, Serializable message) throws JMSException {
        brokerAdapter.sendMessage(topic, message);
    }
    
    public Map<String, String> getSettings() {
        return settings;
    }
    
    public String getServiceName() {
        return this.serviceName;
    }
}
