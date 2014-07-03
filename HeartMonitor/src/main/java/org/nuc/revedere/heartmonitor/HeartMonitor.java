package org.nuc.revedere.heartmonitor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.jms.JMSException;

import org.jdom2.Document;
import org.jdom2.Element;
import org.nuc.revedere.core.messages.update.UserListUpdate;
import org.nuc.revedere.service.core.BrokerMessageListener;
import org.nuc.revedere.service.core.Service;
import org.nuc.revedere.service.core.RevedereService;
import org.nuc.revedere.service.core.SupervisorTopics;
import org.nuc.revedere.service.core.hb.Heartbeat;
import org.nuc.revedere.util.Collector.CollectorListener;

public class HeartMonitor extends RevedereService {
    private static final String SETTINGS_PATH = "HeartMonitor.xml";
    private final static String HEARTMONITOR_SERVICE_NAME = "HeartMonitor";

    private static HeartMonitor instance;
    private final Map<String, ServiceHeartbeatCollector> servicesStatus = new HashMap<>();
    private HeartbeatInfoListener heartbeatInfoListener;

    private HeartMonitor() throws Exception {
        super(HEARTMONITOR_SERVICE_NAME, SETTINGS_PATH);
        super.start(true, true, true);

        loadConfiguredServices();
        startListeningForHeartbeats();
        startTick();
    }

    public static HeartMonitor getInstance() {
        if (instance == null) {
            try {
                instance = new HeartMonitor();
            } catch (Exception e) {
                Service.BACKUP_LOGGER.error("Could not start heartMonitor instance", e);
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
        
        for (String service : configuredServices) {
            servicesStatus.put(service, new ServiceHeartbeatCollector(service, true));
        }
    }

    private void startListeningForHeartbeats() throws JMSException {
        final BrokerMessageListener heartbeatListener = new BrokerMessageListener() {
            public void onMessage(Serializable message) {
                if (message instanceof Heartbeat) {
                    final Heartbeat receivedHeartbeat = (Heartbeat) message;
                    addHeartbeatToServicesStatus(receivedHeartbeat);
                } else {
                    LOGGER.warn("Received unwanted message on heartbeat topic : " + message.getClass().toString());
                }
            }
        };
        addMessageListener(SupervisorTopics.HEARTBEAT_TOPIC, heartbeatListener);
    }

    private void addHeartbeatToServicesStatus(Heartbeat receivedHeartbeat) {
        final String serviceName = receivedHeartbeat.getServiceName();
        ServiceHeartbeatCollector serviceStatus = servicesStatus.get(serviceName);

        if (serviceStatus == null) {
            serviceStatus = new ServiceHeartbeatCollector(serviceName, false);
            servicesStatus.put(serviceName, serviceStatus);
        }

        LOGGER.info("Received heartbeat for service " + serviceName);
        serviceStatus.updateHeartbeat(receivedHeartbeat);
    }

    private void startTick() {
        final Timer timer = new Timer();
        final TimerTask tickTask = new TimerTask() {
            @Override
            public void run() {
                for (ServiceHeartbeatCollector serviceStatus : servicesStatus.values()) {
                    serviceStatus.tick();
                }
                notifyHeartbeatInfoListener();
            }
        };
        timer.scheduleAtFixedRate(tickTask, 0, RevedereService.HEARTBEAT_INTERVAL);
    }

    public void notifyHeartbeatInfoListener() {
        if (this.heartbeatInfoListener != null) {
            this.heartbeatInfoListener.onHeartbeatInfoUpdate(servicesStatus);
        }
    }

    public void setHeartbeatInfoListener(HeartbeatInfoListener listener) {
        this.heartbeatInfoListener = listener;
        listener.onHeartbeatInfoUpdate(servicesStatus);
    }

    public void addUserCollectorListener(CollectorListener<UserListUpdate> listener) {
        getUserCollector().addListener(listener);
    }

    public void removeUserCollectorListener(CollectorListener<UserListUpdate> listener) {
        getUserCollector().removeListener(listener);
    }
}
