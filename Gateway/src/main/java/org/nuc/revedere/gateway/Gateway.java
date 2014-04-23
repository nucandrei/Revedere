package org.nuc.revedere.gateway;

import java.io.IOException;
import javax.jms.JMSException;

import org.jdom2.JDOMException;
import org.nuc.revedere.service.core.Service;
import org.nuc.revedere.service.core.SupervisedService;

public class Gateway extends SupervisedService {
    private final static String GATEWAY_SERVICE_NAME = "Gateway";

    public Gateway() throws JDOMException, IOException, JMSException {
        super(GATEWAY_SERVICE_NAME);
    }

    public static void main(String[] args) {
        try {
            new Gateway();
        } catch (JDOMException | IOException | JMSException e) {
            Service.BACKUP_LOGGER.error("Could not start gateway", e);
        }
    }
}
