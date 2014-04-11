package org.nuc.revedere.heartmonitor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.nuc.revedere.service.core.Service;
import org.nuc.revedere.service.core.SupervisedService;
import org.nuc.revedere.service.core.SupervisorTopics;
import org.nuc.revedere.service.core.hb.Heartbeat;

public class HeartMonitor extends Service {

    private final static String HEARTMONITOR_SERVICE_NAME = "HeartMonitor";
    private final Map<String, ServiceStatus> servicesStatus = new HashMap<String, ServiceStatus>();

    public HeartMonitor() throws Exception {
        super(HEARTMONITOR_SERVICE_NAME);
        
        loadConfiguredServices();
        startListeningForHeartbeats();
        startTick();
    }

    private void loadConfiguredServices() {
        final Map<String, String> services = getSettings();
        for (String service : services.values()) {
            servicesStatus.put(service, new ServiceStatus(service, true));
        }
    }
    
    private void startListeningForHeartbeats() throws Exception {
        final MessageListener heartbeatListener = new MessageListener() {
            public void onMessage(Message msg) {
                final ObjectMessage objectMessage = (ObjectMessage) msg;
                try {
                    Serializable message = objectMessage.getObject();
                    if (message instanceof Heartbeat) {
                        final Heartbeat receivedHeartbeat = (Heartbeat) message;
                        addHeartbeatToServicesStatus(receivedHeartbeat);
                    } else {
                        LOGGER.warn("Received unwanted message on heartbeat topic : " + message.getClass().toString());
                    }
                } catch (JMSException e) {
                    LOGGER.error("Caught exception while processing received message ", e);
                }
            }
        };
        setMessageListener(SupervisorTopics.HEARTBEAT_TOPIC, heartbeatListener);
    }

    private void addHeartbeatToServicesStatus(Heartbeat receivedHeartbeat) {
        final String serviceName = receivedHeartbeat.getServiceName();
        ServiceStatus serviceStatus = servicesStatus.get(serviceName);

        if (serviceStatus == null) {
            serviceStatus = new ServiceStatus(serviceName, false);
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
                for (ServiceStatus serviceStatus : servicesStatus.values()) {
                    serviceStatus.tick();
                }
            }
        };
        timer.scheduleAtFixedRate(tickTask, 0, SupervisedService.HEARTBEAT_INTERVAL);
    }

    public static void main(String[] args) throws Exception {
        new HeartMonitor();
    }

}
