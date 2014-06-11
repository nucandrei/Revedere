package org.nuc.revedere.service.core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;

public class ActiveMQBrokerAdapter implements BrokerAdapter {
    private static final Logger LOGGER = Logger.getLogger(BrokerAdapter.class);
    private ConnectionFactory connectionFactory;
    private Connection connection;
    private final Map<String, MessageConsumer> consumerMap;
    private final Map<String, MessageProducer> producerMap;
    private final String address;
    private Session session;

    public ActiveMQBrokerAdapter(String brokerAddress) throws JMSException {
        consumerMap = new HashMap<String, MessageConsumer>();
        producerMap = new HashMap<String, MessageProducer>();
        this.address = brokerAddress;
        connectToBroker();
    }

    private void connectToBroker() throws JMSException {
        LOGGER.info(String.format("Trying to connect to : %s", address));
        connectionFactory = new ActiveMQConnectionFactory(address);
        connection = connectionFactory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        LOGGER.info(String.format("Connected to : %s", address));
    }

    public boolean isConnected() {
        return this.session != null;
    }

    public void setMessageListener(String topicString, final BrokerMessageListener messageListener) throws JMSException {
        LOGGER.info("Linking message listener to topic: " + topicString);
        MessageConsumer consumer = consumerMap.get(topicString);
        if (consumer == null) {
            Topic topic = session.createTopic(topicString);
            consumer = session.createConsumer(topic);
            consumerMap.put(topicString, consumer);
        }

        consumer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message msg) {
                final ObjectMessage objectMessage = (ObjectMessage) msg;
                try {
                    messageListener.onMessage(objectMessage.getObject());
                } catch (Exception e) {
                    LOGGER.error("Caught exception while processing received message ", e);
                }
            }
        });
    }

    public void sendMessage(String topicString, Serializable message) throws JMSException {
        final ObjectMessage objectMessage = session.createObjectMessage();
        objectMessage.setObject(message);
        MessageProducer intendedProducer = producerMap.get(topicString);
        if (intendedProducer == null) {
            Topic topic = session.createTopic(topicString);
            intendedProducer = session.createProducer(topic);
            producerMap.put(topicString, intendedProducer);
        }
        intendedProducer.send(objectMessage);
        LOGGER.debug("Sent message: " + message + " on topic: " + topicString);
    }

    public void closeConnection() throws JMSException {
        session.close();
        connection.close();
    }
}
