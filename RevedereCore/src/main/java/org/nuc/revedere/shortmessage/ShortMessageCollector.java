package org.nuc.revedere.shortmessage;

import java.util.ArrayList;
import java.util.List;

import org.nuc.revedere.core.messages.update.ShortMessageUpdate;
import org.nuc.revedere.util.Collector;

public class ShortMessageCollector extends Collector<ShortMessageUpdate> {
    private final List<ShortMessage> messagesList = new ArrayList<ShortMessage>();

    @Override
    public void agregate(ShortMessageUpdate update) {
        messagesList.addAll(update.getUpdate());
        super.agregate(update);
    }

    @Override
    public ShortMessageUpdate getCurrentState() {
        return new ShortMessageUpdate(messagesList, null);
    }

}
