package eclipseplugin.views.composites;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.nuc.revedere.client.RevedereSession;
import org.nuc.revedere.core.User;
import org.nuc.revedere.core.messages.update.ShortMessageUpdate;
import org.nuc.revedere.core.messages.update.UserListUpdate;
import org.nuc.revedere.shortmessage.ShortMessage;
import org.nuc.revedere.util.BidirectionMap;
import org.nuc.revedere.util.Collector;
import org.nuc.revedere.util.Collector.CollectorListener;

import eclipseplugin.views.ViewStack;

public class UsersComposite extends Composite {
    protected static final int R_TABLE_ROW = 0;
    private final BidirectionMap<User, TableItem> itemsByUser = new BidirectionMap<>();
    private final int MESSAGE_TABLE_ROW = 2;
    private final int REVIEW_TABLE_ROW = 3;
    private RevedereSession revedereSession;
    private final Image onlineImage;
    private final Image offlineImage;
    private final Image messages;
    private final Image newMessages;
    private final Image reviews;
    private final Table table;
    private final ViewStack viewStack;

    public UsersComposite(Composite parent, ViewStack viewStack) {
        super(parent, 0);
        this.viewStack = viewStack;
        this.onlineImage = AbstractUIPlugin.imageDescriptorFromPlugin("EclipsePlugin", "/icons/online.png").createImage();
        this.offlineImage = AbstractUIPlugin.imageDescriptorFromPlugin("EclipsePlugin", "/icons/offline.png").createImage();

        this.messages = AbstractUIPlugin.imageDescriptorFromPlugin("EclipsePlugin", "/icons/messages.png").createImage();
        this.newMessages = AbstractUIPlugin.imageDescriptorFromPlugin("EclipsePlugin", "/icons/messages-new.png").createImage();

        this.reviews = AbstractUIPlugin.imageDescriptorFromPlugin("EclipsePlugin", "/icons/reviews.png").createImage();
        AbstractUIPlugin.imageDescriptorFromPlugin("EclipsePlugin", "/icons/reviews-new.png").createImage();

        this.table = new Table(this, SWT.FULL_SELECTION);
        initialize();
    }

    public void setSession(RevedereSession currentSession) {
        this.revedereSession = currentSession;
        if (revedereSession != null) {
            revedereSession.addListenerToUserCollector(new CollectorListener<UserListUpdate>() {

                @Override
                public void onUpdate(Collector<UserListUpdate> collector, UserListUpdate update) {
                    Display.getDefault().syncExec(new Runnable() {
                        @Override
                        public void run() {
                            for (User onlineUser : collector.getCurrentState().getUsersWhoWentOnline()) {
                                TableItem tableItem = itemsByUser.getValue(onlineUser);
                                if (tableItem == null) {
                                    tableItem = createTableItem(table, onlineUser.getUsername());
                                    itemsByUser.put(onlineUser, tableItem);
                                }
                                tableItem.setImage(0, onlineImage);
                            }

                            for (User offlineUser : collector.getCurrentState().getUsersWhoWentOffline()) {
                                TableItem tableItem = itemsByUser.getValue(offlineUser);
                                if (tableItem == null) {
                                    tableItem = createTableItem(table, offlineUser.getUsername());
                                    itemsByUser.put(offlineUser, tableItem);
                                }
                                tableItem.setImage(0, offlineImage);
                            }
                        }
                    });
                }
            });

            revedereSession.addListenerToShortMessageCollector(new CollectorListener<ShortMessageUpdate>() {

                @Override
                public void onUpdate(Collector<ShortMessageUpdate> source, ShortMessageUpdate update) {
                    Display.getDefault().syncExec(new Runnable() {
                        @Override
                        public void run() {
                            resetAllIconsToDefault(MESSAGE_TABLE_ROW, messages);
                            for (ShortMessage shortMessage : update.getUpdate()) {
                                if (!shortMessage.isRead()) {
                                    itemsByUser.getValue(shortMessage.getSender()).setImage(MESSAGE_TABLE_ROW, newMessages);
                                }
                            }
                        }
                    });
                }
            });
        }
    }

    private void initialize() {
        setLayout(new FillLayout(SWT.HORIZONTAL));

        final TableColumn userStateColumn = new TableColumn(table, SWT.NONE);
        userStateColumn.setWidth(20);

        final TableColumn userNameColumn = new TableColumn(table, SWT.NONE);
        userNameColumn.setWidth(table.getClientArea().width - 95);
        userNameColumn.setText("Name");

        final TableColumn messagesColumn = new TableColumn(table, SWT.NONE);
        messagesColumn.setWidth(20);

        final TableColumn reviewsColumn = new TableColumn(table, SWT.NONE);
        reviewsColumn.setWidth(20);

        table.addControlListener(new ControlListener() {
            @Override
            public void controlResized(ControlEvent e) {
                userNameColumn.setWidth(table.getClientArea().width - 60);
            }

            @Override
            public void controlMoved(ControlEvent e) {

            }
        });
        table.setVisible(true);
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                userNameColumn.setWidth(table.getClientArea().width - 60);
                table.pack();
                table.getParent().layout(false);
            }
        });
        table.setLinesVisible(false);
        table.setHeaderVisible(false);

        table.addListener(SWT.EraseItem, new Listener() {
            @Override
            public void handleEvent(Event event) {
                event.gc.setBackground(event.gc.getBackground());
                event.gc.fillRectangle(table.getClientArea());
            }
        });

        table.addListener(SWT.MouseDown, new Listener() {
            @Override
            public void handleEvent(Event event) {
                final Point clickPoint = new Point(event.x, event.y);
                TableItem selectedItem = table.getItem(clickPoint);
                if (selectedItem != null) {
                    final User selectedUser = itemsByUser.getKey(selectedItem);
                    final Rectangle messageRectangle = selectedItem.getBounds(MESSAGE_TABLE_ROW);
                    if (messageRectangle.contains(clickPoint)) {
                        // message button was pressed
                        viewStack.changeToMessageView(selectedUser);
                        return;
                    }
                    
                    final Rectangle reviewRectangle = selectedItem.getBounds(REVIEW_TABLE_ROW);
                    if (reviewRectangle.contains(clickPoint)) {
                        // message button was pressed
                        viewStack.changeToReviewView(selectedUser);
                        return;
                    }
                }
            }
        });
    }

    private void resetAllIconsToDefault(int index, Image defaultImage) {
        for (TableItem item : itemsByUser.values()) {
            item.setImage(index, defaultImage);
        }
    }

    private TableItem createTableItem(Table parentTable, String username) {
        final TableItem tableItem = new TableItem(table, SWT.NONE);
        tableItem.setText(1, username);
        tableItem.setImage(2, messages);
        tableItem.setImage(3, reviews);
        return tableItem;
    }
}
