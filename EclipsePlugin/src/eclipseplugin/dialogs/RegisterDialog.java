package eclipseplugin.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Button;
import org.nuc.revedere.util.NetworkUtils;

public class RegisterDialog extends Dialog {

    private static final char BULLET_UNICODE_CHARACTER = '\u2022';
    private Text passwordField;
    private Text realNameField;
    private Text emailField;
    private Button publishRealNameCheck;
    private Button allowEmailCheck;

    private final String password;

    private String realName;
    private boolean publishRealName;
    private String emailAddress;
    private boolean allowEmail;

    public RegisterDialog(Shell parentShell, String password) {
        super(parentShell);
        this.password = password;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        final Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new GridLayout(2, false));

        final Label repeatPasswordLabel = new Label(container, SWT.NONE);
        repeatPasswordLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        repeatPasswordLabel.setText("  Repeat password  ");

        passwordField = new Text(container, SWT.BORDER);
        passwordField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        passwordField.setEchoChar(BULLET_UNICODE_CHARACTER);

        final Label realNameLabel = new Label(container, SWT.NONE);
        realNameLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        realNameLabel.setText("  Real name  ");

        realNameField = new Text(container, SWT.BORDER);
        realNameField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        new Label(container, SWT.NONE);

        publishRealNameCheck = new Button(container, SWT.CHECK);
        publishRealNameCheck.setText("Publish real name");
        new Label(container, SWT.NONE);

        final Label realNameWarning = new Label(container, SWT.NONE);
        realNameWarning.setText("Real name will be visible in HeartMonitor regardless this option");
        new Label(container, SWT.NONE);
        new Label(container, SWT.NONE);

        final Label emailLabel = new Label(container, SWT.NONE);
        emailLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        emailLabel.setText("E-mail address");

        emailField = new Text(container, SWT.BORDER);
        emailField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        new Label(container, SWT.NONE);

        allowEmailCheck = new Button(container, SWT.CHECK);
        allowEmailCheck.setText("Allow emails");
        new Label(container, SWT.NONE);

        final Label emailWarning = new Label(container, SWT.NONE);
        emailWarning.setText("Password recovery e-mails will be sent regardless this option");

        return container;
    }

    @Override
    public void createButtonsForButtonBar(Composite composite) {
        super.createButtonsForButtonBar(composite);

        final Button okButton = getButton(IDialogConstants.OK_ID);
        okButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                if (!passwordField.getText().equals(password)) {
                    MessageDialog.openInformation(RegisterDialog.this.getShell(), "Revederé", "Password was not correctly retyped");
                    return;
                }

                if (realNameField.getText().trim().isEmpty()) {
                    MessageDialog.openInformation(RegisterDialog.this.getShell(), "Revederé", "Invalid real name");
                    return;
                }

                if (emailField.getText().trim().isEmpty() || !NetworkUtils.isValidEmailAddress(emailField.getText())) {
                    MessageDialog.openInformation(RegisterDialog.this.getShell(), "Revederé", "Invalid e-mail address");
                    return;
                }
                
                realName = realNameField.getText();
                publishRealName = publishRealNameCheck.getSelection();
                emailAddress = emailField.getText();
                allowEmail = allowEmailCheck.getSelection();
                okPressed();
            }
        });
    }

    public String getRealName() {
        return realName;
    }

    public boolean publishRealName() {
        return publishRealName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public boolean allowEmails() {
        return allowEmail;
    }

}
