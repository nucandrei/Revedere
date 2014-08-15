package eclipseplugin.views.composites;

import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.nuc.revedere.client.RevedereSession;
import org.nuc.revedere.core.User;
import org.nuc.revedere.core.messages.update.ShortMessageUpdate;
import org.nuc.revedere.shortmessage.ShortMessage;
import org.nuc.revedere.util.Collector;
import org.nuc.revedere.util.Collector.CollectorListener;

import eclipseplugin.revedere.RevedereManager;
import eclipseplugin.views.ViewStack;

import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

public class MessageComposite extends Composite {
    private final RevedereManager revedereManager = RevedereManager.getInstance();
    private final Composite parent;
    private final Label userNameLabel;
    private final Label backButton;
    private final Image backImage;

    private final Button sendNewMessageButton;
    private final Text newMessageText;
    private final Text historicMessages;
    private final ViewStack viewStack;

    private boolean hasFocus = false;
    private User currentUser;
    private CollectorListener<ShortMessageUpdate> collectorListener;

    public MessageComposite(Composite parent, ViewStack viewStack) {
        super(parent, SWT.WRAP);
        this.viewStack = viewStack;
        this.parent = parent;
        setLayout(new GridLayout(2, false));

        userNameLabel = new Label(this, SWT.NONE);
        userNameLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        backButton = new Label(this, SWT.NONE);
        backButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        backImage = AbstractUIPlugin.imageDescriptorFromPlugin("EclipsePlugin", "/icons/back.png").createImage();
        backButton.setImage(backImage);
        backButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                hasFocus = false;
                viewStack.changeToUsersView();
            }
        });

        historicMessages = new Text(this, SWT.BORDER | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL);
        historicMessages.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

        newMessageText = new Text(this, SWT.BORDER);
        newMessageText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        sendNewMessageButton = new Button(this, SWT.NONE);
        sendNewMessageButton.setText("Send");
        sendNewMessageButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                if (!newMessageText.getText().isEmpty()) {
                    revedereManager.getCurrentSession().sendMessage(currentUser, newMessageText.getText());
                    newMessageText.setText("");
                }
            }
        });
        this.parent.layout();
    }

    public void update(User user) {
        this.hasFocus = true;
        if (user != currentUser) {
            updateHistoricMessages(revedereManager.getCurrentSession().getMessagesForUser(user));
        }

        this.currentUser = user;
        this.userNameLabel.setText(user.getName());
        addListenerOnShortMessageUpdateIfMissing();
    }

    private void addListenerOnShortMessageUpdateIfMissing() {
        final RevedereSession currentRevedereSession = revedereManager.getCurrentSession();
        if (currentRevedereSession != null && collectorListener == null) {
            collectorListener = new CollectorListener<ShortMessageUpdate>() {
                @Override
                public void onUpdate(Collector<ShortMessageUpdate> collector, ShortMessageUpdate update) {
                    if (hasFocus) {
                        updateHistoricMessages(currentRevedereSession.getMessagesForUser(currentUser));
                    }
                }
            };
            currentRevedereSession.addListenerToShortMessageCollector(collectorListener);
        }
    }

    private void updateHistoricMessages(Set<ShortMessage> messages) {
        final StringBuilder stringBuilder = new StringBuilder();
        for (ShortMessage message : messages) {
            stringBuilder.append(message);
            stringBuilder.append("\n");
        }

        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                historicMessages.setText(stringBuilder.toString());
                viewStack.layout();
            }
        });
    }
}
