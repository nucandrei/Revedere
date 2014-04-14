package org.nuc.revedere.heartmonitor;

import org.nuc.revedere.service.core.hb.Heartbeat;

public class ServiceHeartbeatCollector {
    private ServiceStatus serviceState = ServiceStatus.UNKNOWN;
    private Heartbeat lastHeartbeat;
    private final boolean configured;
    private final String serviceName;

    public synchronized void updateHeartbeat(Heartbeat lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
        this.serviceState = ServiceStatus.OK;
    }

    public ServiceHeartbeatCollector(String serviceName, boolean configured) {
        this.serviceName = serviceName;
        this.configured = configured;
    }

    public synchronized void tick() {
        switch (serviceState) {
        case OK:
            this.serviceState = ServiceStatus.OKLATE;
            break;
        case OKLATE:
            this.serviceState = ServiceStatus.LATE;
            break;
        case LATE:
            this.serviceState = ServiceStatus.LOST;
            break;
        default:
            // unknown remains unknown, lost remains lost
            break;
        }
    }

    public Heartbeat getLastHeartbeat() {
        return this.lastHeartbeat;
    }

    public ServiceStatus getServiceState() {
        if (serviceName.equals(ServiceStatus.OK) || serviceName.equals(ServiceStatus.OKLATE)) {
            return ServiceStatus.OK;
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
