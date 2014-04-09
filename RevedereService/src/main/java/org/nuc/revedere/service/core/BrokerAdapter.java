package org.nuc.revedere.service.core;

import javax.jms.JMSException;
import javax.jms.MessageListener;

public interface BrokerAdapter {
    public boolean isConnected();

    public void setMessageListener(String topicString, MessageListener messageListener) throws JMSException;

    public void sendMessage(String topicString, String message) throws JMSException;

    public void closeConnection() throws JMSException;
}
