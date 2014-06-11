package org.nuc.revedere.service.core;

import java.io.Serializable;

public interface BrokerMessageListener {
    public void onMessage(Serializable serializable);
}
