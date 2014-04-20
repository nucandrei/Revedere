package org.nuc.revedere.service.core;

import java.io.Serializable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.nuc.revedere.service.core.messages.Request;
import org.nuc.revedere.service.core.messages.Response;
import org.nuc.revedere.util.Container;

public class Requestor {
    private Service supportService;

    public Requestor(Service supportService) {
        this.supportService = supportService;
    }

    public Response<Request> request(String topic, final Request request) {
        final String requestTopic = String.format("%s.Request", topic);
        final String responseTopic = String.format("%s.Response", topic);
        final Container<Response<Request>> responseContainer = new Container<>();
        final CountDownLatch latch = new CountDownLatch(1);

        try {
            supportService.setMessageListener(responseTopic, new MessageListener() {
                @SuppressWarnings("unchecked")
                public void onMessage(Message msg) {
                    final ObjectMessage objectMessage = (ObjectMessage) msg;
                    try {
                        Serializable message = objectMessage.getObject();
                        if (message instanceof Response) {
                            final Response<Request> possibleResponse = (Response<Request>) message;
                            if (possibleResponse.getRequest().equals(request)) {
                                responseContainer.setContent(possibleResponse);
                                latch.countDown();
                            }
                        }
                    } catch (JMSException e) {
                        supportService.LOGGER.error("Caught exception while processing received message ", e);
                    }
                }
            });
        } catch (Exception e) {
            supportService.LOGGER.error("Could not set listener for request on topic: " + responseTopic, e);
        }

        try {
            supportService.sendMessage(requestTopic, request);
            latch.await(10, TimeUnit.SECONDS);
            supportService.setMessageListener(responseTopic, null);
        } catch (Exception e) {
            supportService.LOGGER.error("Could not send request on topic: " + requestTopic, e);
        }

        return responseContainer.getContent();
    }
}
