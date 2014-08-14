package org.nuc.revedere.client.persistence;

import java.util.List;
import java.util.Set;

import org.nuc.revedere.core.User;
import org.nuc.revedere.core.messages.Response;
import org.nuc.revedere.core.messages.request.ShortMessageHistoricalRequest;
import org.nuc.revedere.core.messages.update.ShortMessageUpdate;
import org.nuc.revedere.shortmessage.MessageBox;
import org.nuc.revedere.shortmessage.MessageBoxXMLPersistence;
import org.nuc.revedere.shortmessage.ShortMessage;
import org.nuc.revedere.shortmessage.ShortMessageCollector;
import org.nuc.revedere.util.Collector.CollectorListener;

public class ShortMessagePersistence {
    private final User currentUser;
    private final ShortMessageCollector shortMessageCollector;
    private final MessageBox messageBox;

    public ShortMessagePersistence(User currentUser) {
        this.currentUser = currentUser;
        this.shortMessageCollector = new ShortMessageCollector();
        this.messageBox = new MessageBox(currentUser.getUsername(), new MessageBoxXMLPersistence("messages" + currentUser.getUsername() + ".xml"));
    }

    public void addListenerToChange(CollectorListener<ShortMessageUpdate> listener) {
        this.shortMessageCollector.addListener(listener);
    }

    public void removeListenerToChange(CollectorListener<ShortMessageUpdate> listener) {
        this.shortMessageCollector.addListener(listener);
    }

    public void onUpdate(ShortMessageUpdate update) {
        this.messageBox.addAll(update.getUpdate());
        this.shortMessageCollector.agregate(update);

    }

    @SuppressWarnings("unchecked")
    public void init(Response<ShortMessageHistoricalRequest> response) {
        if (response != null && response.isSuccessfull()) {
            final List<ShortMessage> shortMessages = (List<ShortMessage>) response.getAttachment();
            this.shortMessageCollector.agregate(new ShortMessageUpdate(shortMessages, currentUser));
            this.messageBox.addAll(shortMessages);
        }
    }

    public ShortMessageHistoricalRequest getInitialRequest() {
        return new ShortMessageHistoricalRequest(currentUser, true, true, messageBox.getLastTimestamp());

    }

    public Set<ShortMessage> getMessagesForUser(User user) {
        return messageBox.getMessages(user);
    }
}
