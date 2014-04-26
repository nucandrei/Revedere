package org.nuc.revedere.service.core;

import java.io.Serializable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.nuc.revedere.core.messages.Request;
import org.nuc.revedere.core.messages.Response;
import org.nuc.revedere.util.Container;

public class Requestor<T extends Request> {
    private Service supportService;

    public Requestor(Service supportService) {
        this.supportService = supportService;
    }

    public Response<T> request(String topic, final T request) {
        final String requestTopic = String.format("%s.Request", topic);
        final String responseTopic = String.format("%s.Response", topic);
        final Container<Response<T>> responseContainer = new Container<>();
        final CountDownLatch latch = new CountDownLatch(1);

        try {
            supportService.setMessageListener(responseTopic, new MessageListener() {
                @SuppressWarnings("unchecked")
                public void onMessage(Message msg) {
                    final ObjectMessage objectMessage = (ObjectMessage) msg;
                    try {
                        Serializable message = objectMessage.getObject();
                        try {
                            final Response<T> possibleResponse = (Response<T>) message;
                            if (possibleResponse.getRequest().equals(request)) {
                                responseContainer.setContent(possibleResponse);
                                latch.countDown();
                            }
                        } catch (ClassCastException e) {
                            // ignore this message.
                        }
                    } catch (JMSException e) {
                        Service.LOGGER.error("Caught exception while processing received message ", e);
                    }
                }
            });
        } catch (Exception e) {
            Service.LOGGER.error("Could not set listener for request on topic: " + responseTopic, e);
            return null;
        }

        try {
            supportService.sendMessage(requestTopic, request);
            latch.await(10, TimeUnit.SECONDS);
            supportService.setMessageListener(responseTopic, null);
        } catch (Exception e) {
            Service.LOGGER.error("Could not send request on topic: " + requestTopic, e);
        }

        return responseContainer.getContent();
    }

    public void inform(String topic, final T request) {
        final String requestTopic = String.format("%s.Request", topic);
        try {
            supportService.sendMessage(requestTopic, request);
        } catch (Exception e) {
            Service.LOGGER.error("Could not send inform on topic: " + requestTopic, e);
        }
    }
}
