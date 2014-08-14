package org.nuc.revedere.heartmonitor.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.jms.JMSException;

import org.apache.log4j.Logger;
import org.nuc.distry.monitor.ServiceHeartInfo;
import org.nuc.distry.monitor.ServiceStatus;
import org.nuc.distry.service.cmd.ResetHeartbeatCommand;
import org.nuc.distry.service.cmd.StopCommand;
import org.nuc.distry.service.hb.ServiceState;
import org.nuc.revedere.heartmonitor.HeartMonitor;
import org.nuc.revedere.heartmonitor.ServiceHeartInfoListener;

@ManagedBean(name = "page")
@SessionScoped
public class ServersPageBean implements Serializable, ServiceHeartInfoListener {
    private static final Logger LOGGER = Logger.getLogger(ServersPageBean.class);
    private static final String CSS_CLASS_WARNING_SERVICE = "warningservice";
    private static final String CSS_CLASS_ERROR_SERVICE = "errorservice";
    private static final String CSS_CLASS_OK_SERVICE = "okservice";
    private static final long serialVersionUID = 8388751329694282175L;
    private final HeartMonitor heartMonitor;
    private Map<String, ServiceHeartInfo> persistence = new HashMap<>();

    public ServersPageBean() {
        heartMonitor = HeartMonitor.getInstance();
        heartMonitor.addServiceHeartInfoListener(this);
    }

    public ServiceHeartInfo[] getServiceList(boolean configured) {
        final List<ServiceHeartInfo> serviceList = new ArrayList<>();
        for (ServiceHeartInfo info : persistence.values()) {
            if (info.isConfiguredService() == configured) {
                serviceList.add(info);
            }
        }
        Collections.sort(serviceList);
        return serviceList.toArray(new ServiceHeartInfo[serviceList.size()]);
    }

    public boolean hasServices(boolean configured) {
        return getServiceList(configured).length != 0;
    }

    public String getRowClasses(boolean configured) {
        StringBuilder sBuilder = new StringBuilder();
        for (ServiceHeartInfo collector : getServiceList(configured)) {
            sBuilder.append(getRowClass(collector));
            sBuilder.append(",");
        }
        final String rowClasses = sBuilder.toString();
        if (rowClasses.length() == 0) {
            return "";
        }
        return rowClasses.substring(0, rowClasses.length() - 1);
    }

    private String getRowClass(ServiceHeartInfo serviceInfo) {
        if (serviceInfo.getServiceStatus().equals(ServiceStatus.UNKNOWN)) {
            return CSS_CLASS_OK_SERVICE;
        }

        if (serviceInfo.getServiceStatus().equals(ServiceStatus.LOST)) {
            return CSS_CLASS_ERROR_SERVICE;
        }

        if (serviceInfo.getHeartbeat().equals(ServiceState.ERROR) || serviceInfo.getHeartbeat().equals(ServiceState.FATAL)) {
            return CSS_CLASS_ERROR_SERVICE;
        }

        if (serviceInfo.getServiceStatus().equals(ServiceStatus.LATE) || serviceInfo.getHeartbeat().equals(ServiceState.WARNING)) {
            return CSS_CLASS_WARNING_SERVICE;
        }

        return CSS_CLASS_OK_SERVICE;
    }

    public void killServer(String serviceName) {
        try {
            this.heartMonitor.sendCommand(new StopCommand(serviceName));
            LOGGER.info("Sent kill message for service " + serviceName);
            
        } catch (JMSException e) {
            LOGGER.error("Could not send kill message", e);
        }
    }

    public void resetHeartbeat(String serviceName) {
        try {
            this.heartMonitor.sendCommand(new ResetHeartbeatCommand(serviceName));
            LOGGER.info("Sent reset heartbeat message for service " + serviceName);
            
        } catch (JMSException e) {
            LOGGER.error("Could not send reset heartbeat message", e);
        }
    }

    @Override
    public void onUpdate(Map<String, ServiceHeartInfo> update) {
        persistence = update;
    }
}
