package eclipseplugin.views;

import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.*;
import org.nuc.revedere.core.User;
import org.nuc.revedere.review.Review;

import eclipseplugin.revedere.RevedereManager;
import eclipseplugin.views.composites.MessageComposite;
import eclipseplugin.views.composites.NoSessionComposite;
import eclipseplugin.views.composites.ReviewsComposite;
import eclipseplugin.views.composites.SingleReviewComposite;
import eclipseplugin.views.composites.UsersComposite;

public class RevedereView extends ViewPart implements ViewStack {
    public static final String ID = "eclipseplugin.views.RevedereView";
    private RevedereManager revedereManager = RevedereManager.getInstance();
    private final StackLayout stackLayout = new StackLayout();
    private Composite noSessionComposite;
    private UsersComposite usersComposite;
    private MessageComposite messageComposite;
    private ReviewsComposite reviewsComposite;
    private Composite parent;
    private SingleReviewComposite singleReviewComposite;

    public void createPartControl(Composite parent) {
        this.parent = parent;
        this.parent.setLayout(stackLayout);
        noSessionComposite = new NoSessionComposite(parent);
        usersComposite = new UsersComposite(parent, this);
        messageComposite = new MessageComposite(parent, this);
        reviewsComposite = new ReviewsComposite(parent, this);
        singleReviewComposite = new SingleReviewComposite(parent, this);
        revedereManager.setViewStack(this);
        changeToNoConnectionOrSession();
    }

    @Override
    public void setFocus() {

    }

    @Override
    public void changeToNoConnectionOrSession() {
        stackLayout.topControl = noSessionComposite;
        layout();
    }

    @Override
    public void changeToUsersView() {
        usersComposite.setSession(revedereManager.getCurrentSession());
        stackLayout.topControl = usersComposite;
        layout();
    }

    @Override
    public void changeToMessageView(User user) {
        messageComposite.update(user);
        stackLayout.topControl = messageComposite;
        layout();
    }

    @Override
    public void changeToReviewView(User user) {
        reviewsComposite.update(user);
        stackLayout.topControl = reviewsComposite;
        layout();
    }

    @Override
    public void changeToSingleReviewView(Review selectedReview) {
        singleReviewComposite.update(selectedReview);
        stackLayout.topControl = singleReviewComposite;
        layout();
    }

    @Override
    public void layout() {
        ((Composite) stackLayout.topControl).layout();
        this.parent.layout();
    }
}