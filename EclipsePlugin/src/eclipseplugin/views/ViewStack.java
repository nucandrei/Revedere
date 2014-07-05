package eclipseplugin.views;

import org.nuc.revedere.core.User;
import org.nuc.revedere.review.Review;

public interface ViewStack {
    public void changeToNoConnectionOrSession();

    public void changeToUsersView();

    public void changeToMessageView(User user);
    
    public void changeToReviewView(User selectedUser);

    public void layout();

    public void changeToSingleReviewView(Review selectedReview);


}
