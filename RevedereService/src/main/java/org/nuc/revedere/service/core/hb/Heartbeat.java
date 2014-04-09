package org.nuc.revedere.service.core.hb;

import java.io.Serializable;

public class Heartbeat implements Serializable{
    private static final long serialVersionUID = -1224566748643029326L;
    private final String serviceName;
    private final ServiceState serverState;
    private final String commment;
    private final long time;
    
    public Heartbeat(String serviceName, ServiceState serverState, String comment, long time) {
        this.serviceName = serviceName;
        this.serverState = serverState;
        this.commment = comment;
        this.time = time;
    }

    public String getServiceName() {
        return serviceName;
    }

    public ServiceState getServerState() {
        return serverState;
    }

    public String getCommment() {
        return commment;
    }

    public long getTime() {
        return time;
    }
}
