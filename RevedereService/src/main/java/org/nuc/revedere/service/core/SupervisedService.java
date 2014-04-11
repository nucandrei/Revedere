package org.nuc.revedere.service.core;

import java.util.Timer;
import java.util.TimerTask;

import org.nuc.revedere.service.core.hb.Heartbeat;
import org.nuc.revedere.service.core.hb.HeartbeatGenerator;
import org.nuc.revedere.service.core.hb.ServiceState;

public class SupervisedService extends Service {
    public final static int HEARTBEAT_INTERVAL = 10000;
    final HeartbeatGenerator heartbeatGenerator = new HeartbeatGenerator(this.getServiceName());

    public SupervisedService(String serviceName) throws Exception {
        super(serviceName);
        startHeartbeatGenerator();
    }

    private void startHeartbeatGenerator() {
        Timer timer = new Timer();
        final TimerTask heartbeatTask = new TimerTask() {
            @Override
            public void run() {
                Heartbeat heartbeat = heartbeatGenerator.generateHeartbeat();
                try {
                    SupervisedService.this.sendMessage(SupervisorTopics.HEARTBEAT_TOPIC, heartbeat);
                    LOGGER.info("Sent heartbeat");
                } catch (Exception e) {
                    LOGGER.error("Could not send heartbeat.", e);
                }
            }
        };
        timer.scheduleAtFixedRate(heartbeatTask, 0, HEARTBEAT_INTERVAL);
    }

    public void setServiceState(ServiceState state) {
        this.heartbeatGenerator.setServiceState(state);
    }

    public void setServiceComment(String comment) {
        this.heartbeatGenerator.setComment(comment);
    }
    
    public static void main(String[] args) throws Exception {
        new SupervisedService(args[0]);
    }
}
