package org.nuc.revedere.integration;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.nuc.revedere.client.RevedereConnector;
import org.nuc.revedere.client.RevedereSession;
import org.nuc.revedere.core.User;
import org.nuc.revedere.core.messages.update.UserListUpdate;
import org.nuc.revedere.shortmessage.MessageBox;
import org.nuc.revedere.util.Collector;
import org.nuc.revedere.util.Collector.CollectorListener;
import org.nuc.revedere.util.Container;

import static org.junit.Assert.*;

public class DummyClientLoginTest {
    private static final long TIMEOUT = 1000;

    @Test
    public void testLogin() throws Exception {
        final RevedereConnector revedereConnector = new RevedereConnector("127.0.0.1:6045");
        final RevedereSession session = revedereConnector.login("user", "user");
        try {
            final Container<UserListUpdate> expectedUpdateContainer = new Container<UserListUpdate>();
            final CountDownLatch latch = new CountDownLatch(2);

            session.addListenerToUserCollector(new CollectorListener<UserListUpdate>() {
                public void onUpdate(Collector<UserListUpdate> source, UserListUpdate update) {
                    expectedUpdateContainer.setContent(update);
                    latch.countDown();
                }
            });

            latch.await(TIMEOUT, TimeUnit.MILLISECONDS);
            assertNotNull(expectedUpdateContainer.getContent());
            assertTrue(expectedUpdateContainer.getContent().getUsersWhoWentOnline().contains(new User("user", "user")));

            MessageBox myMessageBox = session.getMessageBox();
            Thread.sleep(1000);
            assertFalse(myMessageBox.getUnreadMessages().isEmpty());
        } finally {
            session.logout();
            session.close();
        }
    }
}
