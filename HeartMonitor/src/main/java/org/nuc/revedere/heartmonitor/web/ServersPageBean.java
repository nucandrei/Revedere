package org.nuc.revedere.heartmonitor.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.nuc.revedere.heartmonitor.HeartMonitor;
import org.nuc.revedere.heartmonitor.HeartbeatInfoListener;
import org.nuc.revedere.heartmonitor.ServiceHeartbeatCollector;
import org.nuc.revedere.heartmonitor.ServiceStatus;
import org.nuc.revedere.service.core.hb.ServiceState;

@ManagedBean(name = "page")
@SessionScoped
public class ServersPageBean implements Serializable, HeartbeatInfoListener {

    private static final String CSS_CLASS_WARNING_SERVICE = "warningservice";
    private static final String CSS_CLASS_ERROR_SERVICE = "errorservice";
    private static final String CSS_CLASS_OK_SERVICE = "okservice";
    private static final long serialVersionUID = 8388751329694282175L;
    private final HeartMonitor heartMonitor;
    private Map<String, ServiceHeartbeatCollector> persistence = new HashMap<String, ServiceHeartbeatCollector>();

    public ServersPageBean() {
        heartMonitor = HeartMonitor.getInstance();
        heartMonitor.setHeartbeatInfoListener(this);
    }

    public ServiceHeartbeatCollector[] getServiceList(boolean configured) {
        final List<ServiceHeartbeatCollector> serviceList = new ArrayList<ServiceHeartbeatCollector>();
        for (ServiceHeartbeatCollector collector : persistence.values()) {
            if (collector.isConfigured() == configured) {
                serviceList.add(collector);
            }
        }
        Collections.sort(serviceList);
        return serviceList.toArray(new ServiceHeartbeatCollector[serviceList.size()]);
    }

    public boolean hasServices(boolean configured) {
        return getServiceList(configured).length != 0;
    }

    public String getRowClasses(boolean configured) {
        StringBuilder sBuilder = new StringBuilder();
        for (ServiceHeartbeatCollector collector : getServiceList(configured)) {
            sBuilder.append(getRowClass(collector));
            sBuilder.append(",");
        }
        final String rowClasses = sBuilder.toString();
        if (rowClasses.length() == 0) {
            return "";
        }
        return rowClasses.substring(0, rowClasses.length() - 1);
    }

    private String getRowClass(ServiceHeartbeatCollector collector) {
        if (collector.getServiceStatus().equals(ServiceStatus.UNKNOWN)) {
            return CSS_CLASS_OK_SERVICE;
        }

        if (collector.getServiceStatus().equals(ServiceStatus.LOST)) {
            return CSS_CLASS_ERROR_SERVICE;
        }

        if (collector.getLastHeartbeat().equals(ServiceState.ERROR) || collector.getLastHeartbeat().equals(ServiceState.FATAL)) {
            return CSS_CLASS_ERROR_SERVICE;
        }

        if (collector.getServiceStatus().equals(ServiceStatus.LATE) || collector.getLastHeartbeat().equals(ServiceState.WARNING)) {
            return CSS_CLASS_WARNING_SERVICE;
        }

        return CSS_CLASS_OK_SERVICE;
    }

    public void onHeartbeatInfoUpdate(Map<String, ServiceHeartbeatCollector> servicesStatus) {
        this.persistence = servicesStatus;
    }
}
