package eclipseplugin.revedere;

import org.nuc.revedere.client.RevedereConnector;
import org.nuc.revedere.client.RevedereSession;

public class RevedereManager {
    private static final RevedereManager instance = new RevedereManager();
    private RevedereConnector revedereConnector;
    private String connectorAddress;
    private RevedereSession revedereSession;

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
        this.revedereSession = revedereConnector.login(username, password);
    }

    public String register(String username, String password) throws Exception {
        verifyAPrioriConnector("No connection was established before register");
        return revedereConnector.register(username, password);
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
        }
    }

    public void verifyAPrioriConnector(String exceptionMessage) throws Exception {
        if (revedereConnector == null) {
            throw new Exception(exceptionMessage);
        }
    }
}
