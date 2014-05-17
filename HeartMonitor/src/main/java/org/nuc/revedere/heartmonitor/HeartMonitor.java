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

import org.nuc.revedere.core.UserCollector;
import org.nuc.revedere.core.messages.Response;
import org.nuc.revedere.core.messages.UserListRequest;
import org.nuc.revedere.core.messages.update.UserListUpdate;
import org.nuc.revedere.service.core.JMSRequestor;
import org.nuc.revedere.service.core.Service;
import org.nuc.revedere.service.core.RevedereService;
import org.nuc.revedere.service.core.SupervisorTopics;
import org.nuc.revedere.service.core.Topics;
import org.nuc.revedere.service.core.hb.Heartbeat;
import org.nuc.revedere.util.Convertor;
import org.nuc.revedere.util.Collector.CollectorListener;

public class HeartMonitor extends RevedereService {
    private static HeartMonitor instance;

    private final static String HEARTMONITOR_SERVICE_NAME = "HeartMonitor";
    private final Map<String, ServiceHeartbeatCollector> servicesStatus = new HashMap<String, ServiceHeartbeatCollector>();
    private HeartbeatInfoListener heartbeatInfoListener;
    private UserCollector userCollector = new UserCollector();

    private HeartMonitor() throws Exception {
        super(HEARTMONITOR_SERVICE_NAME);
        loadConfiguredServices();
        startListeningForHeartbeats();
        startListeningForUsers();
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
        final Map<String, String> services = getSettings();
        for (String service : services.values()) {
            servicesStatus.put(service, new ServiceHeartbeatCollector(service, true));
        }
    }

    private void startListeningForHeartbeats() throws JMSException {
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

    private void startListeningForUsers() throws JMSException, InterruptedException {
        LOGGER.info("Sending users list request");
        final JMSRequestor<UserListRequest> requestor = new JMSRequestor<UserListRequest>(this);
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

        this.setMessageListener(Topics.USERS_TOPIC, new MessageListener() {
            public void onMessage(Message msg) {
                final ObjectMessage objectMessage = (ObjectMessage) msg;
                try {
                    Serializable message = objectMessage.getObject();
                    final Response<UserListRequest> covertedMessage = new Convertor<Response<UserListRequest>>().convert(message);
                    if (covertedMessage != null) {
                        if (covertedMessage.hasAttachment()) {
                            final UserListUpdate userListUpdate = (UserListUpdate) receivedResponse.getAttachment();
                            userCollector.agregate(userListUpdate);
                        } else {
                            LOGGER.error("Expected users list as attachement, received nothing instead");
                        }
                    }
                } catch (JMSException e) {
                    LOGGER.error("Caught exception while processing received message ", e);
                }
            }
        });
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
        userCollector.addListener(listener);
    }

    public void removeUserCollectorListener(CollectorListener<UserListUpdate> listener) {
        userCollector.removeListener(listener);
    }
}
