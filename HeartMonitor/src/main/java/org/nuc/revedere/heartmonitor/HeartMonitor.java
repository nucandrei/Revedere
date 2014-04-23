package org.nuc.revedere.heartmonitor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.nuc.revedere.service.core.Service;
import org.nuc.revedere.service.core.SupervisedService;
import org.nuc.revedere.service.core.SupervisorTopics;
import org.nuc.revedere.service.core.Topics;
import org.nuc.revedere.service.core.hb.Heartbeat;
import org.nuc.revedere.service.core.messages.Response;
import org.nuc.revedere.service.core.messages.UserListRequest;

public class HeartMonitor extends Service {
    private static HeartMonitor instance;

    private final static String HEARTMONITOR_SERVICE_NAME = "HeartMonitor";
    private final Map<String, ServiceHeartbeatCollector> servicesStatus = new HashMap<String, ServiceHeartbeatCollector>();
    private HeartbeatInfoListener heartbeatInfoListener;
    private UsersInfoListener userInfoListener;

    private List<String> connectedUsers = new ArrayList<String>();
    private List<String> disconnectedUsers = new ArrayList<String>();

    public static HeartMonitor getInstance() {
        if (instance == null) {
            try {
                instance = new HeartMonitor();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return instance;
    }

    private HeartMonitor() throws Exception {
        super(HEARTMONITOR_SERVICE_NAME);
        loadConfiguredServices();
        startListeningForHeartbeats();
        startListeningForUsers();
        startTick();
    }

    private void loadConfiguredServices() {
        final Map<String, String> services = getSettings();
        for (String service : services.values()) {
            servicesStatus.put(service, new ServiceHeartbeatCollector(service, true));
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

    private void startListeningForUsers() throws Exception {
        LOGGER.info("Sending users list request");
        final CountDownLatch latch = new CountDownLatch(1);
        this.setMessageListener(Topics.USERS_RESPONSE_TOPIC, new MessageListener() {
            @Override
            public void onMessage(Message msg) {
                parseUsersReceivedMessage(latch, msg);
                notifyUserInfoListener();
            }
        });
        this.sendMessage(Topics.USERS_REQUEST_TOPIC, new UserListRequest());
        latch.await(10, TimeUnit.SECONDS);

        this.setMessageListener(Topics.USERS_TOPIC, new MessageListener() {
            @Override
            public void onMessage(Message message) {
                parseUsersReceivedMessage(new CountDownLatch(1), message);
                notifyUserInfoListener();
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

    @SuppressWarnings("unchecked")
    private void parseUsersReceivedMessage(final CountDownLatch latch, Message msg) {
        final ObjectMessage objectMessage = (ObjectMessage) msg;
        try {
            Serializable message = objectMessage.getObject();
            if (message instanceof Response<?>) {
                final Response<UserListRequest> receivedResponse = (Response<UserListRequest>) message;
                if (receivedResponse.hasAttachment()) {
                    connectedUsers = (List<String>) ((Serializable[]) receivedResponse.getAttachment())[0];
                    disconnectedUsers = (List<String>) ((Serializable[]) receivedResponse.getAttachment())[1];
                } else {
                    LOGGER.error("Expected users list as attachement, received nothing instead");
                }
                latch.countDown();
            }
        } catch (JMSException e) {
            LOGGER.error("Caught exception while processing received message ", e);
        }
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
        timer.scheduleAtFixedRate(tickTask, 0, SupervisedService.HEARTBEAT_INTERVAL);
    }

    public void notifyHeartbeatInfoListener() {
        if (this.heartbeatInfoListener != null) {
            this.heartbeatInfoListener.onHeartbeatInfoUpdate(servicesStatus);
        }
    }

    public void notifyUserInfoListener() {
        if (this.userInfoListener != null) {
            this.userInfoListener.onUsersUpdate(connectedUsers, disconnectedUsers);
        }
    }

    public void setHeartbeatInfoListener(HeartbeatInfoListener listener) {
        this.heartbeatInfoListener = listener;
        listener.onHeartbeatInfoUpdate(servicesStatus);
    }

    public void setUserInfoListener(UsersInfoListener listener) {
        this.userInfoListener = listener;
        listener.onUsersUpdate(connectedUsers, disconnectedUsers);
    }
}
