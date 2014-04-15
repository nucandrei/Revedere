package org.nuc.revedere.heartmonitor;

import java.util.Map;

public interface HeartbeatInfoListener {
    public void onHeartbeatInfoUpdate(Map<String, ServiceHeartbeatCollector> servicesStatus);
}
