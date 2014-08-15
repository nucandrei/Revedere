package eclipseplugin.revedere;

import org.nuc.revedere.client.RevedereConnector;
import org.nuc.revedere.client.RevedereSession;
import org.nuc.revedere.core.messages.update.ReviewUpdate;
import org.nuc.revedere.util.Collector;
import org.nuc.revedere.util.Collector.CollectorListener;

import eclipseplugin.views.EmptyViewStack;
import eclipseplugin.views.ViewStack;

public class RevedereManager {
    private static final RevedereManager instance = new RevedereManager();
    private RevedereConnector revedereConnector;
    private String connectorAddress;
    private RevedereSession revedereSession;
    private ViewStack viewStack = EmptyViewStack.getInstance();
    private final ReviewBox reviewBox = new ReviewBox();

    public static RevedereManager getInstance() {
        return instance;
    }

    public void createConnector(String address) throws Exception {
        this.revedereConnector = new RevedereConnector(address);
        this.connectorAddress = address;
    }

    public void injectRevedereConnector(RevedereConnector revedereConnector) {
        this.revedereConnector = revedereConnector;
    }

    public boolean hasConnector(String address) {
        return revedereConnector != null && address.equals(connectorAddress);
    }

    public void login(String username, String password) throws Exception {
        verifyAPrioriConnector("No connection was established before login");
        this.revedereSession = revedereConnector.login(username, password, "Eclipse");
        this.revedereSession.addListenerToReviewCollector(new CollectorListener<ReviewUpdate>() {

            @Override
            public void onUpdate(Collector<ReviewUpdate> collector, ReviewUpdate reviewUpdate) {
                if (reviewUpdate != null) {
                    reviewBox.refreshReview(reviewUpdate.getReview());
                }
            }
        });
        viewStack.changeToUsersView();
    }

    public void logout() throws Exception {
        verifyAPrioriConnector("No connection was established before logout");
        if (revedereSession != null) {
            revedereSession.logout();
            revedereSession = null;
            viewStack.changeToNoConnectionOrSession();
        }
    }

    public String register(String username, String password, String realName, boolean publishRealName, String emailAddress, boolean allowEmails) throws Exception {
        verifyAPrioriConnector("No connection was established before register");
        return revedereConnector.register(username, password, realName, publishRealName, emailAddress, allowEmails);
    }

    public String unregister(String username, String password) throws Exception {
        verifyAPrioriConnector("No connection was established before unregister");
        return revedereConnector.unregister(username, password);
    }

    public RevedereSession getCurrentSession() {
        return revedereSession;
    }

    public void closeLastCreatedConnector() {
        if (revedereConnector != null) {
            revedereConnector.disconnect();
            viewStack.changeToNoConnectionOrSession();
        }
    }

    public void verifyAPrioriConnector(String exceptionMessage) throws Exception {
        if (revedereConnector == null) {
            throw new Exception(exceptionMessage);
        }
    }

    public void setViewStack(ViewStack viewStack) {
        this.viewStack = viewStack;
    }

    public ReviewBox getReviewBox() {
        return this.reviewBox;
    }
}
