package eclipseplugin.revedere;

import org.eclipse.core.resources.IProject;
import org.nuc.revedere.review.Review;
import org.nuc.revedere.util.BidirectionMap;

public class ReviewBox {
    private final BidirectionMap<Review, IProject> projects;

    public ReviewBox() {
        projects = new BidirectionMap<>();
    }

    public void add(IProject project, Review review) {
        this.projects.put(review, project);
    }

    public boolean isReviewProject(IProject project) {
        return this.projects.containsValue(project);
    }

    public boolean isAlreadyInReview(Review review) {
        return this.projects.containsKey(review);
    }

    public IProject removeReview(Review review) {
        return this.projects.removeKey(review);
    }

    public Review getReview(IProject project) {
        return this.projects.getKey(project);
    }

    public void refreshReview(Review review) {
        if (this.projects.containsKey(review)) {
            final IProject project = this.projects.removeKey(review);
            this.projects.put(review, project);
        }
    }
}
