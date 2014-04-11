package org.nuc.revedere.service.core.hb;

import java.io.Serializable;

public class Heartbeat implements Serializable{
    private static final long serialVersionUID = -1224566748643029326L;
    private static final String TOSTRING_TEMPLATE = "[%s, %s, %s, %d]";
    private final String serviceName;
    private final ServiceState serviceState;
    private final String commment;
    private final long time;
    
    public Heartbeat(String serviceName, ServiceState serviceState, String comment, long time) {
        this.serviceName = serviceName;
        this.serviceState = serviceState;
        this.commment = comment;
        this.time = time;
    }

    public String getServiceName() {
        return serviceName;
    }

    public ServiceState getServiceState() {
        return serviceState;
    }

    public String getCommment() {
        return commment;
    }

    public long getTime() {
        return time;
    }
    
    @Override
    public String toString() {
        return String.format(TOSTRING_TEMPLATE, serviceName, serviceState, commment, time);
    }
}
