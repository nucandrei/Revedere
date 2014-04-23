package org.nuc.revedere.service.core;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.log4j.LogManager;
import org.nuc.revedere.service.core.cmd.Command;
import org.nuc.revedere.service.core.hb.Heartbeat;
import org.nuc.revedere.service.core.hb.HeartbeatGenerator;
import org.nuc.revedere.service.core.hb.ServiceState;

public class SupervisedService extends Service {
    public final static int HEARTBEAT_INTERVAL = 10000;
    final HeartbeatGenerator heartbeatGenerator = new HeartbeatGenerator(this.getServiceName());

    public SupervisedService(String serviceName) throws Exception {
        super(serviceName);
        startHeartbeatGenerator();
        startListeningForCommands();
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
            SupervisedService.this.sendMessage(SupervisorTopics.HEARTBEAT_TOPIC, heartbeat);
            LOGGER.info("Sent heartbeat");
        } catch (Exception e) {
            LOGGER.error("Could not send heartbeat.", e);
        }
    }

    private void startListeningForCommands() throws Exception {
        final MessageListener commandListener = new MessageListener() {
            public void onMessage(Message msg) {
                final ObjectMessage objectMessage = (ObjectMessage) msg;
                try {
                    Serializable message = objectMessage.getObject();
                    if (message instanceof Command) {
                        Command command = (Command) message;
                        if (SupervisedService.this.getServiceName().equals(command.getServiceName())) {
                            LOGGER.info("Received command");
                            shutdownGracefully();
                        }
                    } else {
                        LOGGER.warn("Received unwanted message on command topic : " + message.getClass().toString());
                    }
                } catch (JMSException e) {
                    LOGGER.error("Caught exception while processing received message ", e);
                }
            }
        };
        setMessageListener(SupervisorTopics.COMMAND_TOPIC, commandListener);
        
    }

    public void setServiceState(ServiceState state) {
        this.heartbeatGenerator.setServiceState(state);
    }

    public void setServiceComment(String comment) {
        this.heartbeatGenerator.setComment(comment);
    }
    
    public void shutdownGracefully() {
        sendHeartbeat();
        LOGGER.info("Sent last heartbeat");
        LogManager.shutdown();
        System.exit(0);
    }

    public static void main(String[] args) throws Exception {
        new SupervisedService(args[0]);
    }
}
