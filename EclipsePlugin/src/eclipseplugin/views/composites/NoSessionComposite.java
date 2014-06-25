package eclipseplugin.views.composites;

import java.util.Collections;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class NoSessionComposite extends Composite {
    private static final String preferenceReference = "eclipseplugin.preferences.RevederePreferences";
    private static final String[] PREFERENCE_IDS = new String[] { preferenceReference };
    private final Link link;

    public NoSessionComposite(Composite parent) {
        super(parent, SWT.WRAP);
        this.setLayout(new FillLayout());
        link = new Link(this, SWT.WRAP);
        link.setText("The plug-in is not connected to back-end server or is logged out. Go to \"Window->Preferences->Revedere\" or click <a>here</a> to connect, login or register.");
        link.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(null, preferenceReference, PREFERENCE_IDS, Collections.emptyMap());
                dialog.open();
            }
        });
        link.pack();
    }
}
