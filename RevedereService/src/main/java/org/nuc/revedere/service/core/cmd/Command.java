package org.nuc.revedere.service.core.cmd;

import java.io.Serializable;

public class Command implements Serializable {
    private static final long serialVersionUID = 8951251921566552871L;
    private final String serviceName;

    public Command(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
}
