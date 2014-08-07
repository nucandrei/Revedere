package eclipseplugin;

import org.apache.log4j.BasicConfigurator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IStartup;

import eclipseplugin.preferences.PreferenceConstants;
import eclipseplugin.revedere.RevedereManager;

public class StartUp implements IStartup {

    @Override
    public void earlyStartup() {
        BasicConfigurator.configure();
        final IPreferenceStore preferencePage = Activator.getDefault().getPreferenceStore();
        final boolean connectAtStartup = preferencePage.getBoolean(PreferenceConstants.CONNECT_AT_STARTUP_STRING);
        if (!connectAtStartup) {
            return;
        }

        final String address = preferencePage.getString(PreferenceConstants.GATEWAY_STRING);
        final String username = preferencePage.getString(PreferenceConstants.USER_STRING);
        final String password = preferencePage.getString(PreferenceConstants.PASSWORD_STRING);

        if (address == null || username == null || password == null) {
            return;
        }
        
        try {
            RevedereManager.getInstance().createConnector(address);
            RevedereManager.getInstance().login(username, password);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
