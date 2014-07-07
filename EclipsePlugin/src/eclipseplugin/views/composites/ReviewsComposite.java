package eclipseplugin.views.composites;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import eclipseplugin.revedere.RevedereManager;
import eclipseplugin.views.ViewStack;

import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.nuc.revedere.client.RevedereSession;
import org.nuc.revedere.core.User;
import org.nuc.revedere.core.messages.update.ReviewUpdate;
import org.nuc.revedere.review.Review;
import org.nuc.revedere.review.ReviewDocumentSection;
import org.nuc.revedere.util.BidirectionMap;
import org.nuc.revedere.util.Collector;
import org.nuc.revedere.util.Collector.CollectorListener;

public class ReviewsComposite extends Composite {
    private final RevedereManager revedereManager = RevedereManager.getInstance();
    private final ViewStack viewStack;
    private final Image backImage;
    private Table table;
    protected boolean hasFocus = false;
    private User currentUser;
    private CollectorListener<ReviewUpdate> collectorListener;
    private final Label usernameLabel;
    private BidirectionMap<TableItem, Review> reviewItems = new BidirectionMap<>();

    public ReviewsComposite(Composite parent, ViewStack viewStack) {
        super(parent, 0);
        this.viewStack = viewStack;
        setLayout(new GridLayout(2, false));

        usernameLabel = new Label(this, SWT.NONE);
        usernameLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label backButton = new Label(this, SWT.NONE);
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

        table = new Table(this, SWT.FULL_SELECTION);
        table.setLinesVisible(true);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

        final TableColumn reviewStateColumn = new TableColumn(table, SWT.NONE);
        reviewStateColumn.setWidth(100);

        final TableColumn reviewNameColumn = new TableColumn(table, SWT.FILL);

        table.addControlListener(new ControlListener() {
            @Override
            public void controlResized(ControlEvent e) {
                reviewNameColumn.setWidth(table.getClientArea().width - 100);
            }

            @Override
            public void controlMoved(ControlEvent e) {
                // Do nothing
            }
        });

        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                table.pack();
                table.getParent().layout(false);

            }
        });

        table.addListener(SWT.MouseDown, new Listener() {
            @Override
            public void handleEvent(Event event) {
                final Point clickPoint = new Point(event.x, event.y);
                TableItem selectedItem = table.getItem(clickPoint);
                if (selectedItem != null) {
                    final Review selectedReview = reviewItems.getValue(selectedItem);
                    viewStack.changeToSingleReviewView(selectedReview);
                }
            }
        });
        this.layout();
    }

    public void update(User user) {
        this.hasFocus = true;
        if (user != currentUser) {
            this.currentUser = user;
            updateReviews();
        }
        this.usernameLabel.setText(user.getUsername());
        addListenerOnReviewUpdateIfMissing();
    }

    private void addListenerOnReviewUpdateIfMissing() {
        final RevedereSession currentRevedereSession = revedereManager.getCurrentSession();
        if (currentRevedereSession != null && collectorListener == null) {
            collectorListener = new CollectorListener<ReviewUpdate>() {
                @Override
                public void onUpdate(Collector<ReviewUpdate> collector, ReviewUpdate update) {
                    if (hasFocus) {
                        updateReviews();
                    }
                }
            };
            currentRevedereSession.addListenerToReviewCollector(collectorListener);
        }
    }

    private void updateReviews() {
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                for (TableItem tableItem : reviewItems.keys()) {
                    tableItem.dispose();
                }
                reviewItems.clear();

                for (Review review : revedereManager.getCurrentSession().getReviewCollector().getReviews(currentUser)) {
                    reviewItems.put(createTableItem(review.getState().toString(), review.getReviewDocument().getSectionText(ReviewDocumentSection.NAME)), review);
                }
                viewStack.layout();
            }
        });
    }

    private TableItem createTableItem(String reviewState, String reviewName) {
        final TableItem tableItem = new TableItem(table, SWT.NONE);
        tableItem.setText(0, reviewState);
        tableItem.setText(1, reviewName);
        return tableItem;
    }

}
