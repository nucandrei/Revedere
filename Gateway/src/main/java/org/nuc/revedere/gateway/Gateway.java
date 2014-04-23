package org.nuc.revedere.gateway;

import org.nuc.revedere.service.core.SupervisedService;

public class Gateway extends SupervisedService {
    private final static String GATEWAY_SERVICE_NAME = "Gateway";

    public Gateway() throws Exception {
        super(GATEWAY_SERVICE_NAME);
    }

    public static void main(String[] args) throws Exception {
        new Gateway();
    }

}
