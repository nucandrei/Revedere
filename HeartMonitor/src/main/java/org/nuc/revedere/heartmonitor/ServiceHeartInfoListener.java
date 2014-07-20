package org.nuc.revedere.heartmonitor;

import java.util.Map;

import org.nuc.distry.monitor.ServiceHeartInfo;

public interface ServiceHeartInfoListener {
    public void onUpdate(Map<String, ServiceHeartInfo> update);
}
