package eclipseplugin.preferences;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;

import eclipseplugin.Activator;
import eclipseplugin.revedere.RevedereManager;

public class RevederePreferences extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    private static final char BULLET_UNICODE_CHARACTER = '\u2022';
    private static final String PASS_FIELD = "pass";
    private static final String GATEWAY_FIELD = "gateway";
    private static final String USER_FIELD = "user";

    private Set<String> errorGenerators = new HashSet<>();
    private StringFieldEditor usernameFieldEditor;
    private StringFieldEditor passwordFieldEditor;
    private StringFieldEditor gatewayFieldEditor;

    private Button registerButton;
    private Button login_logoutButton;

    private RevedereManager revedereManager = RevedereManager.getInstance();

    public RevederePreferences() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        noDefaultAndApplyButton();
    }

    public void init(IWorkbench workbench) {
        // Do nothing
    }

    public void createFieldEditors() {
        final BooleanFieldEditor emptyFoldersVisibleInReviewFilesDialog = new BooleanFieldEditor(PreferenceConstants.EMPTY_FOLDERS_VISIBLE_IN_REVIEW_FILES_DIALOG, "Empty folders are visible in review files dialog", getFieldEditorParent());
        addField(emptyFoldersVisibleInReviewFilesDialog);
        
        final BooleanFieldEditor applyReviewFilesFilter = new BooleanFieldEditor(PreferenceConstants.FILTER_REVIEW_FILES, "Review files are filtered", getFieldEditorParent());
        addField(applyReviewFilesFilter);
        
        final StringFieldEditor reviewFilesFilter = new StringFieldEditor(PreferenceConstants.REVIEW_FILES_FILTER, "Filter value(regex)", getFieldEditorParent());
        reviewFilesFilter.setTextLimit(30);
        addField(reviewFilesFilter);

        final Label separator = new Label(getFieldEditorParent(), SWT.SEPARATOR | SWT.HORIZONTAL);
        GridDataFactory.fillDefaults().span(3, 1).grab(true, false).applyTo(separator);

        usernameFieldEditor = new StringFieldEditor(PreferenceConstants.USER_STRING, "Username:", getFieldEditorParent()) {
            @Override
            protected boolean checkState() {
                if (this.getStringValue().trim().isEmpty()) {
                    setError("Username field cannot be empty");
                    errorGenerators.add(USER_FIELD);
                    return false;
                }
                clearError(USER_FIELD);
                return true;
            }
        };
        usernameFieldEditor.setTextLimit(30);
        addField(usernameFieldEditor);

        passwordFieldEditor = new StringFieldEditor(PreferenceConstants.PASSWORD_STRING, "Password:", getFieldEditorParent()) {
            @Override
            protected void doFillIntoGrid(Composite parent, int numColumns) {
                super.doFillIntoGrid(parent, numColumns);
                getTextControl().setEchoChar(BULLET_UNICODE_CHARACTER);
            }

            @Override
            protected boolean checkState() {
                if (this.getStringValue().trim().isEmpty()) {
                    setError("Password field cannot be empty");
                    errorGenerators.add(PASS_FIELD);
                    return false;
                }
                clearError(PASS_FIELD);
                return true;
            }
        };
        passwordFieldEditor.setTextLimit(30);
        addField(passwordFieldEditor);

        gatewayFieldEditor = new StringFieldEditor(PreferenceConstants.GATEWAY_STRING, "Gateway address:", getFieldEditorParent()) {
            @Override
            protected boolean checkState() {
                if (this.getStringValue().trim().isEmpty()) {
                    setError("Gateway address field cannot be empty");
                    errorGenerators.add(GATEWAY_FIELD);
                    return false;
                }
                clearError(GATEWAY_FIELD);
                return true;
            }
        };
        addField(gatewayFieldEditor);
    }

    @Override
    protected void contributeButtons(final Composite parent) {
        final boolean activeSession = revedereManager.getCurrentSession() != null;
        registerButton = addContributeButton(parent, PreferenceConstants.REGISTER_BUTTON_TEXT, new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                final String username = usernameFieldEditor.getStringValue();
                final String password = passwordFieldEditor.getStringValue();
                final String gateway = gatewayFieldEditor.getStringValue();
                try {
                    establishConnectionIfMissing(gateway);
                    final String response = register(username, password);
                    setInfoMessage(response);
                } catch (Exception exception) {
                    setError(exception.getMessage());
                }
            }
        });
        registerButton.setVisible(!activeSession);

        final String login_logoutInitialText = (activeSession) ? PreferenceConstants.LOGOUT_BUTTON_TEXT : PreferenceConstants.LOGIN_BUTTON_TEXT;
        login_logoutButton = addContributeButton(parent, login_logoutInitialText, new SelectionAdapter() {
            private boolean nextIsLogin = !activeSession;

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (nextIsLogin) {
                    final String username = usernameFieldEditor.getStringValue();
                    final String password = passwordFieldEditor.getStringValue();
                    final String gateway = gatewayFieldEditor.getStringValue();
                    try {
                        establishConnectionIfMissing(gateway);
                        login(username, password);
                        setInfoMessage("Logged in");
                        nextIsLogin = false;
                        changeText(PreferenceConstants.LOGOUT_BUTTON_TEXT);
                        registerButton.setVisible(false);
                    } catch (Exception exception) {
                        setError(exception.getMessage());
                    }
                } else {
                    try {
                        revedereManager.logout();
                        setInfoMessage("Logged out");
                        nextIsLogin = true;
                        registerButton.setVisible(true);
                        changeText(PreferenceConstants.LOGIN_BUTTON_TEXT);
                    } catch (Exception exception) {
                        setError(exception.getMessage());
                    }
                }
            }

            private void changeText(String text) {
                login_logoutButton.setText(text);
            }
        });
        setActionsEnabled(errorGenerators.isEmpty());

        final GridLayout layout = (GridLayout) parent.getLayout();
        layout.numColumns += 2;
    }

    private Button addContributeButton(Composite parent, String text, SelectionAdapter adapter) {
        final Button button = new Button(parent, SWT.PUSH);
        button.setText(text);
        final int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
        final GridData data = new GridData(SWT.FILL, SWT.CENTER, false, false);
        final Point minButtonSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        data.widthHint = Math.max(widthHint, minButtonSize.x);
        button.setLayoutData(data);
        final GridLayout layout = (GridLayout) parent.getLayout();
        layout.numColumns += 1;
        button.addSelectionListener(adapter);
        return button;
    }

    private void setError(String message) {
        setActionsEnabled(false);
        setErrorMessage(message);
    }

    private void setInfoMessage(String message) {
        setMessage(message, INFORMATION);
    }

    private void clearError(String source) {
        errorGenerators.remove(source);
        if (errorGenerators.isEmpty()) {
            setActionsEnabled(true);
            setErrorMessage(null);
        }
    }

    private void setActionsEnabled(boolean enabled) {
        if (login_logoutButton != null) {
            login_logoutButton.setEnabled(enabled);
        }
        if (registerButton != null) {
            registerButton.setEnabled(enabled);
        }
    }

    private void establishConnectionIfMissing(String address) throws Exception {
        if (!revedereManager.hasConnector(address)) {
            revedereManager.createConnector(address);
        }
    }

    private void login(String username, String password) throws Exception {
        revedereManager.login(username, password);
    }

    protected String register(String username, String password) throws Exception {
        return revedereManager.register(username, password);
    }
}