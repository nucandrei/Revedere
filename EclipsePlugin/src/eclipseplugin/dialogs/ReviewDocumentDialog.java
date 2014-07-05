package eclipseplugin.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.nuc.revedere.core.User;
import org.nuc.revedere.review.ReviewData;
import org.nuc.revedere.review.ReviewDocument;
import org.nuc.revedere.review.ReviewDocumentSection;

import eclipseplugin.revedere.RevedereManager;

public class ReviewDocumentDialog extends Dialog {
    private Text reviewNameText;
    private Text authorText;
    private Text taskText;
    private Text changesText;
    private Text motivationText;
    private final ReviewData currentReviewData;

    public ReviewDocumentDialog(Shell parentShell, ReviewData reviewData) {
        super(parentShell);
        this.currentReviewData = reviewData;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        final Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new GridLayout(2, false));

        final Label lblReviewName = new Label(container, SWT.NONE);
        lblReviewName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblReviewName.setText("Review name");

        reviewNameText = new Text(container, SWT.BORDER);
        reviewNameText.setText("");
        reviewNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        final Label lblAuthor = new Label(container, SWT.NONE);
        lblAuthor.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblAuthor.setText("Author");

        authorText = new Text(container, SWT.BORDER);
        authorText.setText("");
        authorText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        final Label lblTask = new Label(container, SWT.NONE);
        lblTask.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblTask.setText("Task");

        taskText = new Text(container, SWT.BORDER);
        taskText.setText("");
        taskText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        final Label lblChanges = new Label(container, SWT.NONE);
        lblChanges.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblChanges.setText("Changes");

        changesText = new Text(container, SWT.BORDER);
        changesText.setText("");
        changesText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        final Label lblMotivation = new Label(container, SWT.NONE);
        lblMotivation.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblMotivation.setText("Motivation");

        motivationText = new Text(container, SWT.BORDER);
        motivationText.setText("");
        motivationText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        final Button requestNewReviewBtn = new Button(container, SWT.NONE);
        requestNewReviewBtn.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
        requestNewReviewBtn.setText("Request review");
        requestNewReviewBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                System.out.println("Requesting review");
                final ReviewDocument reviewDocument = getReviewDocument();
                final User intendedUser = new User(authorText.getText());
                RevedereManager.getInstance().getCurrentSession().requestReview(intendedUser, currentReviewData, reviewDocument);
                ReviewDocumentDialog.this.close();
            }
        });
        new Label(container, SWT.NONE);
        return container;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Review document");
    }

    @Override
    protected Point getInitialSize() {
        return new Point(500, 300);
    }

    private ReviewDocument getReviewDocument() {
        final ReviewDocument reviewDocument = new ReviewDocument();
        reviewDocument.addSection(ReviewDocumentSection.NAME, reviewNameText.getText());
        reviewDocument.addSection(ReviewDocumentSection.TASK, taskText.getText());
        reviewDocument.addSection(ReviewDocumentSection.CHANGES, changesText.getText());
        reviewDocument.addSection(ReviewDocumentSection.MOTIVATION, motivationText.getText());
        return reviewDocument;
    }
}
