package org.nuc.revedere.shortmessage;

import java.util.Collections;
import java.util.List;

public class DoNothingMessageBoxPersistence implements MessageBoxPersistence {

    @Override
    public void save(ShortMessage message, String messageBoxName) {
        // Do nothing
    }

    @Override
    public void update(ShortMessage message, String messageBoxName) {
        // Do nothing
    }

    @Override
    public List<ShortMessage> getReadMessages(String messageBoxName) {
        return Collections.emptyList();
    }

    @Override
    public List<ShortMessage> getUnreadMessages(String messageBoxName) {
        return Collections.emptyList();
    }

    @Override
    public List<ShortMessage> getSentMessages(String messageBoxName) {
        return Collections.emptyList();
    }

    @Override
    public void clear(String msgBoxName) {
        // Do nothing
    }

}
