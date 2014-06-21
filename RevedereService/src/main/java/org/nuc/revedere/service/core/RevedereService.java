package org.nuc.revedere.service.core;

import java.io.IOException;
import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

import javax.jms.JMSException;
import org.apache.log4j.LogManager;
import org.jdom2.JDOMException;
import org.nuc.revedere.core.UserCollector;
import org.nuc.revedere.core.messages.Response;
import org.nuc.revedere.core.messages.request.UserListRequest;
import org.nuc.revedere.core.messages.update.UserListUpdate;
import org.nuc.revedere.service.core.cmd.Command;
import org.nuc.revedere.service.core.hb.Heartbeat;
import org.nuc.revedere.service.core.hb.HeartbeatGenerator;
import org.nuc.revedere.service.core.hb.ServiceState;

public class RevedereService extends Service {
    public final static int HEARTBEAT_INTERVAL = 10000;
    private final HeartbeatGenerator heartbeatGenerator = new HeartbeatGenerator(this.getServiceName());
    private final UserCollector userCollector = new UserCollector();

    public RevedereService(String serviceName) throws JDOMException, IOException, JMSException {
        super(serviceName);
    }

    public void start(boolean sendHeartbeats, boolean listenForCommands, boolean listenForUsers) throws Exception {
        if (sendHeartbeats) {
            startHeartbeatGenerator();
        }

        if (listenForCommands) {
            startListeningForCommands();
        }

        if (listenForUsers) {
            startListeningForUsers();
        }
    }

    private void startListeningForUsers() throws JMSException, InterruptedException {
        LOGGER.info("Sending users list request");
        final JMSRequestor<UserListRequest> requestor = new JMSRequestor<>(this);
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

        this.addMessageListener(Topics.USERS_TOPIC, new BrokerMessageListener() {
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

    private void startHeartbeatGenerator() {
        Timer timer = new Timer();
        final TimerTask heartbeatTask = new TimerTask() {
            @Override
            public void run() {
                sendHeartbeat();
            }
        };
        timer.scheduleAtFixedRate(heartbeatTask, 0, HEARTBEAT_INTERVAL);
    }

    private void sendHeartbeat() {
        Heartbeat heartbeat = heartbeatGenerator.generateHeartbeat();
        try {
            RevedereService.this.sendMessage(SupervisorTopics.HEARTBEAT_TOPIC, heartbeat);
            LOGGER.info("Sent heartbeat");
        } catch (Exception e) {
            LOGGER.error("Could not send heartbeat.", e);
        }
    }

    private void startListeningForCommands() throws JMSException {
        final BrokerMessageListener commandListener = new BrokerMessageListener() {
            public void onMessage(Serializable message) {
                if (message instanceof Command) {
                    Command command = (Command) message;
                    if (RevedereService.this.getServiceName().equals(command.getServiceName())) {
                        LOGGER.info("Received command");
                        shutdownGracefully();
                    }
                } else {
                    LOGGER.warn("Received unwanted message on command topic : " + message.getClass().toString());
                }
            }
        };
        addMessageListener(SupervisorTopics.COMMAND_TOPIC, commandListener);
    }

    public void setServiceState(ServiceState state) {
        this.heartbeatGenerator.setServiceState(state);
    }

    public void setServiceComment(String comment) {
        this.heartbeatGenerator.setComment(comment);
    }

    public UserCollector getUserCollector() {
        return userCollector;
    }

    public void shutdownGracefully() {
        sendHeartbeat();
        LOGGER.info("Sent last heartbeat");
        LogManager.shutdown();
        System.exit(0);
    }

    public static void main(String[] args) {
        try {
            new RevedereService(args[0]);
        } catch (JDOMException | IOException | JMSException e) {
            Service.BACKUP_LOGGER.error("Could not start service", e);
        }
    }
}
