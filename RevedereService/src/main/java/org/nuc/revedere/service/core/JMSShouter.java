package org.nuc.revedere.service.core;

import java.io.Serializable;

import org.apache.log4j.Logger;

public class JMSShouter<T extends Serializable> {
    private static final Logger LOGGER = Logger.getLogger(JMSShouter.class);
    private Service supportService;

    public JMSShouter(Service supportService) {
        this.supportService = supportService;
    }

    public void shout(String topic, final T message) {
        final String shoutTopic = String.format("%s.Request", topic);
        try {
            supportService.sendMessage(shoutTopic, message);
        } catch (Exception e) {
            LOGGER.error("Could not shout on topic: " + shoutTopic, e);
        }
    }
}
