package org.nuc.revedere.service.core;

import java.io.Serializable;

import javax.jms.JMSException;

public interface BrokerAdapter {
    public boolean isConnected();

    public void addMessageListener(String topicString, BrokerMessageListener messageListener) throws JMSException;

    public void removeMessageListener(String topic, BrokerMessageListener brokerMessageListener);

    public void sendMessage(String topicString, Serializable message) throws JMSException;

    public void closeConnection() throws JMSException;
}
