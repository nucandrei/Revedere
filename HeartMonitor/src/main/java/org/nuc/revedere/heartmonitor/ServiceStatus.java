package org.nuc.revedere.heartmonitor;

import javax.xml.transform.OutputKeys;

import org.nuc.revedere.service.core.hb.Heartbeat;

public class ServiceStatus {
    private ServiceState serviceState = ServiceState.UNKNOWN;
    private Heartbeat lastHeartbeat;
    private final boolean configured;
    private final String serviceName;

    public synchronized void updateHeartbeat(Heartbeat lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
        this.serviceState = ServiceState.OK;
    }

    public ServiceStatus(String serviceName, boolean configured) {
        this.serviceName = serviceName;
        this.configured = configured;
    }

    public synchronized void tick() {
        switch (serviceState) {
        case OK:
            this.serviceState = ServiceState.OKLATE;
            break;
        case OKLATE:
            this.serviceState = ServiceState.LATE;
            break;
        case LATE:
            this.serviceState = ServiceState.LOST;
            break;
        default:
            // unknown remains unknown, lost remains lost
            break;
        }
    }

    public Heartbeat getLastHeartbeat() {
        return this.lastHeartbeat;
    }

    public ServiceState getServiceState() {
        if (serviceName.equals(ServiceState.OK) || serviceName.equals(ServiceState.OKLATE)) {
            return ServiceState.OK;
        } else {
            return serviceState;
        }
    }

    public boolean isConfigured() {
        return this.configured;
    }

    public String getServiceName() {
        return this.serviceName;
    }
}
