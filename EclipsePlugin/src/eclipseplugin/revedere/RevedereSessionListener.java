package eclipseplugin.revedere;

import org.nuc.revedere.client.RevedereSession;

public interface RevedereSessionListener {
    public void onNewRevedereSession(RevedereSession newSession);
}
