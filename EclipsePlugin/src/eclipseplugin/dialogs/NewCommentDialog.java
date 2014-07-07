package eclipseplugin.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridData;
import org.nuc.revedere.util.Container;

public class NewCommentDialog extends Dialog {
    private Text txtDemoMotivation;
    private final Container<String> commentContainer;

    public NewCommentDialog(Shell parentShell, Container<String> commentContainer) {
        super(parentShell);
        this.commentContainer = commentContainer;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        final Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new GridLayout(1, false));

        txtDemoMotivation = new Text(container, SWT.BORDER);
        txtDemoMotivation.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        ModifyListener listener = new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                commentContainer.setContent(txtDemoMotivation.getText());
            }
        };
        txtDemoMotivation.addModifyListener(listener);
        return container;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Add comment");
    }

    @Override
    protected Point getInitialSize() {
        return new Point(500, 300);
    }

}
