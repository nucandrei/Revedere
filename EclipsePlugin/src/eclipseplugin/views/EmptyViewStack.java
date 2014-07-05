package eclipseplugin.views;

import org.nuc.revedere.core.User;
import org.nuc.revedere.review.Review;

public class EmptyViewStack implements ViewStack {
    private static final EmptyViewStack instance = new EmptyViewStack();

    public static EmptyViewStack getInstance() {
        return instance;
    }

    @Override
    public void changeToNoConnectionOrSession() {
        // Do nothing
    }

    @Override
    public void changeToUsersView() {
        // Do nothing
    }

    @Override
    public void changeToMessageView(User user) {
        // Do nothing
    }

    @Override
    public void changeToReviewView(User selectedUser) {
        // Do nothing
    }
    
    @Override
    public void changeToSingleReviewView(Review selectedReview) {
        // Do nothing  
    }

    @Override
    public void layout() {
        // Do nothing
    }
}
