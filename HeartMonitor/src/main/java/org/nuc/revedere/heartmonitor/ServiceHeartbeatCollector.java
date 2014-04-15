package org.nuc.revedere.heartmonitor;

import java.util.Observable;

import org.nuc.revedere.service.core.hb.Heartbeat;

public class ServiceHeartbeatCollector extends Observable implements Comparable<ServiceHeartbeatCollector> {
    private ServiceStatus serviceStatus = ServiceStatus.UNKNOWN;
    private Heartbeat lastHeartbeat;
    private final boolean configured;
    private final String serviceName;

    public synchronized void updateHeartbeat(Heartbeat lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
        this.serviceStatus = ServiceStatus.OK;
    }

    public ServiceHeartbeatCollector(String serviceName, boolean configured) {
        this.serviceName = serviceName;
        this.configured = configured;
    }

    public synchronized void tick() {
        switch (serviceStatus) {
        case OK:
            this.serviceStatus = ServiceStatus.OKLATE;
            break;
        case OKLATE:
            this.serviceStatus = ServiceStatus.LATE;
            break;
        case LATE:
            this.serviceStatus = ServiceStatus.LOST;
            break;
        default:
            // unknown remains unknown, lost remains lost
            break;
        }
        notifyObservers();
    }

    public Heartbeat getLastHeartbeat() {
        return this.lastHeartbeat;
    }

    public ServiceStatus getServiceStatus() {
        if (serviceStatus.equals(ServiceStatus.OK) || serviceStatus.equals(ServiceStatus.OKLATE)) {
            return ServiceStatus.OK;
        } else {
            return serviceStatus;
        }
    }

    public boolean isConfigured() {
        return this.configured;
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public int compareTo(ServiceHeartbeatCollector that) {
        return this.getServiceName().compareTo(that.getServiceName());
    }
}
