package org.nuc.revedere.shortmessage;

import java.util.List;

import org.nuc.revedere.shortmessage.ShortMessage;

public interface MessageBoxPersistence {

    public void save(ShortMessage message, String messageBoxName);

    public void update(ShortMessage message, String messageBoxName);
    
    public List<ShortMessage> getReadMessages(String messageBoxName);
    
    public List<ShortMessage> getUnreadMessages(String messageBoxName);
    
    public List<ShortMessage> getSentMessages(String messageBoxName);

    public void clear(String msgBoxName);
}
