package eclipseplugin.views.composites;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Button;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.nuc.revedere.client.RevedereSession;
import org.nuc.revedere.core.User;
import org.nuc.revedere.core.messages.update.ReviewUpdate;
import org.nuc.revedere.review.Review;
import org.nuc.revedere.review.ReviewState;
import org.nuc.revedere.util.Collector;
import org.nuc.revedere.util.Collector.CollectorListener;

import eclipseplugin.revedere.RevedereManager;
import eclipseplugin.views.ViewStack;

public class SingleReviewComposite extends Composite {
    private final RevedereManager revedereManager = RevedereManager.getInstance();
    private CollectorListener<ReviewUpdate> collectorListener;
    private final Image backImage;
    private Review currentReview;
    private boolean hasFocus;
    private Label reviewNameLabel;
    private Button acceptOrRequestButton;
    private Button nextStateButton;

    private final MouseAdapter closeReviewMouseAdapter;
    private final MouseAdapter acceptReviewMouseAdapter;
    private final MouseAdapter requestReviewMouseAdapter;
    private final MouseAdapter denyReviewMouseAdapter;
    private final MouseAdapter doneReviewMouseAdapter;
    private final ViewStack viewStack;

    public SingleReviewComposite(Composite parent, ViewStack viewStack) {
        super(parent, SWT.NONE);
        setLayout(new GridLayout(4, false));
        this.viewStack = viewStack;

        reviewNameLabel = new Label(this, SWT.NONE);
        reviewNameLabel.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
        new Label(this, SWT.NONE);
        new Label(this, SWT.NONE);

        final Label backButton = new Label(this, SWT.NONE);
        backButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        backImage = AbstractUIPlugin.imageDescriptorFromPlugin("EclipsePlugin", "/icons/back.png").createImage();
        backButton.setImage(backImage);
        backButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                hasFocus = false;
                final User loggedUser = revedereManager.getCurrentSession().getCurrentUser();
                final User reviewUser = (currentReview.getSourceUser().equals(loggedUser)) ? currentReview.getDestinationUser() : currentReview.getSourceUser();
                viewStack.changeToReviewView(reviewUser);
            }
        });

        final Tree tree = new Tree(this, SWT.BORDER);
        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));

        Button openDocButton = new Button(this, SWT.NONE);
        openDocButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                // Do nothing
            }
        });
        openDocButton.setText("Open Document");
        new Label(this, SWT.NONE);

        acceptOrRequestButton = new Button(this, SWT.NONE);
        acceptOrRequestButton.setText("Accept");

        nextStateButton = new Button(this, SWT.NONE);
        GridData gd_nextStateButton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_nextStateButton.widthHint = 58;
        nextStateButton.setLayoutData(gd_nextStateButton);
        addListenerOnReviewUpdateIfMissing();

        closeReviewMouseAdapter = new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                revedereManager.getCurrentSession().updateReview(currentReview, ReviewState.CLOSED);
            }
        };

        acceptReviewMouseAdapter = new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                revedereManager.getCurrentSession().updateReview(currentReview, ReviewState.ACCEPT);
            }
        };

        requestReviewMouseAdapter = new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                revedereManager.getCurrentSession().updateReview(currentReview, ReviewState.REQUEST);
            }
        };

        denyReviewMouseAdapter = new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                revedereManager.getCurrentSession().updateReview(currentReview, ReviewState.DENY);
            }
        };

        doneReviewMouseAdapter = new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                revedereManager.getCurrentSession().updateReview(currentReview, ReviewState.DONE);
            }
        };
    }

    public void update(Review review) {
        if (this.currentReview != review) {
            Display.getDefault().syncExec(new Runnable() {
                @Override
                public void run() {
                    hasFocus = true;
                    addListenerOnReviewUpdateIfMissing();
                    currentReview = review;
                    reviewNameLabel.setText(review.getID());
                    drawContextualButtons(review.getState(), review.getSourceUser().equals(revedereManager.getCurrentSession().getCurrentUser()));
                    viewStack.layout();
                }
            });
        }
    }

    private void drawContextualButtons(ReviewState currentReviewState, boolean isSourceUser) {
        if (isSourceUser) {
            switch (currentReviewState) {
            case REQUEST:
                acceptOrRequestButton.setVisible(false);
                setNextStateAsClosed();
                break;
            case ACCEPT:
            case CLOSED:
                acceptOrRequestButton.setVisible(false);
                nextStateButton.setVisible(false);
                break;
            case DENY:
                acceptOrRequestButton.setVisible(true);
                acceptOrRequestButton.setText("Request");
                acceptOrRequestButton.addMouseListener(requestReviewMouseAdapter);
                setNextStateAsClosed();
                break;
            case DONE:
                acceptOrRequestButton.setVisible(false);
                setNextStateAsClosed();
                break;
            }

        } else {
            switch (currentReviewState) {
            case REQUEST:
                acceptOrRequestButton.setVisible(true);
                acceptOrRequestButton.setText("Accept");
                acceptOrRequestButton.addMouseListener(acceptReviewMouseAdapter);

                nextStateButton.setVisible(true);
                nextStateButton.setText("Deny");
                nextStateButton.addMouseListener(denyReviewMouseAdapter);
                break;
            case ACCEPT:
                acceptOrRequestButton.setVisible(false);

                nextStateButton.setVisible(true);
                nextStateButton.setText("Done");
                nextStateButton.addMouseListener(doneReviewMouseAdapter);
                break;
            case DENY:
            case DONE:
            case CLOSED:
                acceptOrRequestButton.setVisible(false);
                nextStateButton.setVisible(false);
                break;
            }
        }
    }

    private void setNextStateAsClosed() {
        nextStateButton.setVisible(true);
        nextStateButton.setText("Close");
        nextStateButton.addMouseListener(closeReviewMouseAdapter);
    }

    private void addListenerOnReviewUpdateIfMissing() {
        final RevedereSession currentRevedereSession = revedereManager.getCurrentSession();
        if (currentRevedereSession != null && collectorListener == null) {
            collectorListener = new CollectorListener<ReviewUpdate>() {
                @Override
                public void onUpdate(Collector<ReviewUpdate> collector, ReviewUpdate update) {
                    if (hasFocus && update != null && update.getReview().equals(currentReview)) {
                        currentReview = null;
                        update(update.getReview());
                    }
                }
            };
            currentRevedereSession.addListenerToReviewCollector(collectorListener);
        }
    }

}
