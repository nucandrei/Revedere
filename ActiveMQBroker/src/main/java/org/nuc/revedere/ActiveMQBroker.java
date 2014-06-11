package org.nuc.revedere;

import org.apache.activemq.broker.BrokerService;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class ActiveMQBroker {
    private final static Logger LOGGER = Logger.getLogger(ActiveMQBroker.class);

    public static void main(String[] args) {

        if (args.length != 1) {
            LOGGER.fatal("Could not extract broker address");
            showExample();
            LogManager.shutdown();
            return;
        }

        final BrokerService brokerService = new BrokerService();
        try {
            final String brokerAddress = args[0];
            brokerService.addConnector(brokerAddress);
            brokerService.start();
            LOGGER.info("Started broker");
        } catch (Exception e) {
            LOGGER.fatal("Could not start broker", e);
            showExample();
            return;
        }
    }

    private static void showExample() {
        LOGGER.info("Example of broker address: tcp://localhost:61616");
    }

}
