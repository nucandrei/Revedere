package org.nuc.revedere.service.core.hb;

public class HeartbeatGenerator {
    private final String serviceName;
    private ServiceState serviceState;
    private String commment;
    private long time;

    public HeartbeatGenerator(String serviceName) {
        this.serviceName = serviceName;
    }

    public HeartbeatGenerator setServiceState(ServiceState serviceState) {
        this.serviceState = serviceState;
        return this;
    }

    public HeartbeatGenerator setComment(String comment) {
        this.commment = comment;
        return this;
    }

    private HeartbeatGenerator updateTime() {
        this.time = System.currentTimeMillis();
        return this;
    }

    public Heartbeat generateHeartbeat() {
        updateTime();
        return new Heartbeat(serviceName, serviceState, commment, time);
    }
}
