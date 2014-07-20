package org.nuc.revedere.heartmonitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.nuc.distry.monitor.DistryMonitor;
import org.nuc.distry.monitor.ServiceHeartInfo;
import org.nuc.distry.service.ServiceConfiguration;
import org.nuc.distry.service.messaging.ActiveMQAdapter;
import org.nuc.distry.util.Observable;
import org.nuc.distry.util.Observer;
import org.nuc.revedere.core.UserCollector;
import org.nuc.revedere.core.messages.update.UserListUpdate;
import org.nuc.revedere.service.core.RevedereService;
import org.nuc.revedere.service.core.SupervisorTopics;
import org.nuc.revedere.util.Collector.CollectorListener;

public class HeartMonitor extends DistryMonitor {
    private static final Logger LOGGER = Logger.getLogger(HeartMonitor.class);
    private static final String SETTINGS_PATH = "HeartMonitor.xml";
    private final static String HEARTMONITOR_SERVICE_NAME = "HeartMonitor";
    private final UserCollector userCollector = new UserCollector();
    private final Map<String, ServiceHeartInfo> persistence = new HashMap<>();
    private List<ServiceHeartInfoListener> listeners = new ArrayList<>();

    private static HeartMonitor instance;

    private HeartMonitor(ServiceConfiguration serviceConfiguration) throws Exception {
        super(HEARTMONITOR_SERVICE_NAME, serviceConfiguration);
        loadConfiguredServices();
        RevedereService.startListeningForUsers(this, userCollector);
        serviceInfo.addObserver(new Observer<ServiceHeartInfo>() {
            @Override
            public void update(Observable<ServiceHeartInfo> observable, ServiceHeartInfo update) {
                persistence.put(update.getServiceName(), update);
                notifyAllServiceHeartInfoListeners();
            }

            @Override
            public void update(Observable<ServiceHeartInfo> observable) {
                // Do nothing.
            }
        });
    }

    public static HeartMonitor getInstance() {
        if (instance == null) {
            try {
                final String serverAddress = RevedereService.parseArguments(new String[] { "-log4j", "heartmonitor-log4j.properties", "-serverAddress", "failover://(tcp://localhost:61616)?initialReconnectDelay=2000&maxReconnectAttempts=2" });
                final ServiceConfiguration serviceConfiguration = new ServiceConfiguration(new ActiveMQAdapter(serverAddress), true, 10000, SupervisorTopics.HEARTBEAT_TOPIC, true, SupervisorTopics.COMMAND_TOPIC, SupervisorTopics.PUBLISH_TOPIC);
                instance = new HeartMonitor(serviceConfiguration);
            } catch (Exception e) {
                LOGGER.error("Failed to start heartMonitor instance", e);
            }
        }
        return instance;
    }

    private void loadConfiguredServices() {
        final List<String> configuredServices = new ArrayList<>();
        try {
            final Document settings = loadXMLDocument(SETTINGS_PATH);
            final Element servicesElement = settings.getRootElement().getChild("services");
            if (servicesElement == null) {
                throw new Exception("Services node not found");
            }

            for (Element serviceElement : servicesElement.getChildren("service")) {
                configuredServices.add(serviceElement.getText());
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load configured servers", e);
        }
        initConfiguredServices(configuredServices);
    }

    public void addUserCollectorListener(CollectorListener<UserListUpdate> listener) {
        getUserCollector().addListener(listener);
    }

    public void removeUserCollectorListener(CollectorListener<UserListUpdate> listener) {
        getUserCollector().removeListener(listener);
    }

    public UserCollector getUserCollector() {
        return userCollector;
    }

    public void addServiceHeartInfoListener(ServiceHeartInfoListener listener) {
        listeners.add(listener);
        listener.onUpdate(persistence);
    }

    public void notifyAllServiceHeartInfoListeners() {
        for (ServiceHeartInfoListener listener : listeners) {
            listener.onUpdate(persistence);
        }
    }
}
