package org.nuc.revedere.service.core;

import java.io.Serializable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;

import org.apache.log4j.Logger;
import org.nuc.distry.service.DistryListener;
import org.nuc.distry.service.Service;
import org.nuc.revedere.core.messages.Response;
import org.nuc.revedere.core.messages.request.Request;
import org.nuc.revedere.util.Container;

public class JMSRequestor<T extends Request> {
    private static final Logger LOGGER = Logger.getLogger(JMSRequestor.class);
    private Service supportService;

    public JMSRequestor(Service supportService) {
        this.supportService = supportService;
    }

    public Response<T> request(String topic, final T request) {
        final String requestTopic = String.format("%s.Request", topic);
        final String responseTopic = String.format("%s.Response", topic);
        final Container<Response<T>> responseContainer = new Container<>();
        final CountDownLatch latch = new CountDownLatch(1);
        final DistryListener responseListener = new DistryListener() {
            @SuppressWarnings("unchecked")
            public void onMessage(Serializable message) {
                try {
                    final Response<T> possibleResponse = (Response<T>) message;
                    if (possibleResponse.getRequest().equals(request)) {
                        responseContainer.setContent(possibleResponse);
                        latch.countDown();
                    }
                } catch (ClassCastException e) {
                    // ignore this message.
                }
            }
        };
        try {
            supportService.addMessageListener(responseTopic, responseListener);
        } catch (Exception e) {
            LOGGER.error("Could not set listener for request on topic: " + responseTopic, e);
            try {
                supportService.removeMessageListener(responseTopic, responseListener);
            } catch (JMSException exception) {
                LOGGER.error("Failed to remove message listener");
            }
            return null;
        }

        try {
            supportService.sendMessage(requestTopic, request);
            latch.await(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOGGER.error("Could not send request on topic: " + requestTopic, e);
        }

        try {
            supportService.removeMessageListener(responseTopic, responseListener);
        } catch (JMSException e) {
            LOGGER.error("Failed to remove message listener");
        }
        return responseContainer.getContent();
    }
}
