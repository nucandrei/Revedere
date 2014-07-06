package eclipseplugin.dialogs;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.nuc.revedere.review.Review;
import org.nuc.revedere.review.ReviewData;
import org.nuc.revedere.review.ReviewDocument;
import org.nuc.revedere.review.ReviewDocumentSection;
import org.nuc.revedere.review.ReviewFile;

import eclipseplugin.revedere.RevedereManager;

public class ReviewDocumentDialog extends Dialog {
    private Text reviewNameText;
    private Text authorText;
    private Text taskText;
    private Text changesText;
    private Text motivationText;
    private final ReviewData currentReviewData;
    private final boolean sourceSide;
    private final Review currentReview;
    private final String projectName;

    public ReviewDocumentDialog(Shell parentShell, ReviewData reviewData, String projectName) {
        super(parentShell);
        this.currentReviewData = reviewData;
        this.currentReview = null;
        this.sourceSide = true;
        this.projectName = projectName;
    }

    public ReviewDocumentDialog(Shell parentShell, Review review) {
        super(parentShell);
        this.currentReviewData = null;
        this.currentReview = review;
        this.sourceSide = false;
        this.projectName = "";
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        final Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new GridLayout(2, false));

        final Label lblReviewName = new Label(container, SWT.NONE);
        lblReviewName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblReviewName.setText("Review name");

        reviewNameText = new Text(container, SWT.BORDER);
        if (sourceSide) {
            reviewNameText.setText("");
        } else {
            reviewNameText.setText(currentReview.getReviewDocument().getSectionText(ReviewDocumentSection.NAME));
        }
        reviewNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        reviewNameText.setEnabled(sourceSide);

        final Label lblAuthor = new Label(container, SWT.NONE);
        lblAuthor.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        if (sourceSide) {
            lblAuthor.setText("Destination");
        } else {
            lblAuthor.setText("Source");
        }

        authorText = new Text(container, SWT.BORDER);
        if (sourceSide) {
            authorText.setText("");
        } else {
            authorText.setText(currentReview.getSourceUser().getUsername());
        }
        authorText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        authorText.setEnabled(sourceSide);

        final Label lblTask = new Label(container, SWT.NONE);
        lblTask.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblTask.setText("Task");

        taskText = new Text(container, SWT.BORDER);
        if (sourceSide) {
            taskText.setText("");
        } else {
            taskText.setText(currentReview.getReviewDocument().getSectionText(ReviewDocumentSection.TASK));
        }
        taskText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        taskText.setEnabled(sourceSide);

        final Label lblChanges = new Label(container, SWT.NONE);
        lblChanges.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblChanges.setText("Changes");

        changesText = new Text(container, SWT.BORDER);
        if (sourceSide) {
            changesText.setText("");
        } else {
            changesText.setText(currentReview.getReviewDocument().getSectionText(ReviewDocumentSection.CHANGES));
        }
        changesText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        changesText.setEnabled(sourceSide);

        final Label lblMotivation = new Label(container, SWT.NONE);
        lblMotivation.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblMotivation.setText("Motivation");

        motivationText = new Text(container, SWT.BORDER);
        if (sourceSide) {
            motivationText.setText("");
        } else {
            motivationText.setText(currentReview.getReviewDocument().getSectionText(ReviewDocumentSection.MOTIVATION));
        }
        motivationText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        motivationText.setEnabled(sourceSide);

        final Button requestNewReviewOrDownloadBtn = new Button(container, SWT.NONE);
        requestNewReviewOrDownloadBtn.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
        requestNewReviewOrDownloadBtn.setText(sourceSide ? "Request review" : "Download");
        requestNewReviewOrDownloadBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                if (sourceSide) {
                    final ReviewDocument reviewDocument = getReviewDocument();
                    final User intendedUser = new User(authorText.getText());
                    RevedereManager.getInstance().getCurrentSession().requestReview(intendedUser, currentReviewData, reviewDocument);
                    ReviewDocumentDialog.this.close();
                } else {
                    downloadReview();
                    System.out.println("Downloaded changes");
                }
            }
        });
        new Label(container, SWT.NONE);
        return container;
    }

    private void downloadReview() {
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        final String projectName = currentReview.getReviewDocument().getSectionText(ReviewDocumentSection.PROJECT_NAME);
        final IProject project = root.getProject(projectName);
        if (project.exists()) {
            MessageDialog.openInformation(super.getShell(), "Revederé", String.format("The project: %s already exists", projectName));
            return;
        }
        try {
            final NullProgressMonitor nullProgressMonitor = new NullProgressMonitor();
            project.create(nullProgressMonitor);
            project.open(nullProgressMonitor);
            for (ReviewFile reviewFile : currentReview.getData().getReviewFiles()) {
                final IFile file = project.getFile(reviewFile.getFileRelativePath());
                if (!file.exists()) {
                    final InputStream stream = new ByteArrayInputStream(reviewFile.getFileContent().getBytes(StandardCharsets.UTF_8));
                    if (file.getParent() instanceof IFolder) {
                        prepareFolder((IFolder) file.getParent());
                    }
                    file.create(stream, false, nullProgressMonitor);
                }
            }
            RevedereManager.getInstance().getReviewBox().add(project, currentReview);
        } catch (CoreException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void prepareFolder(IFolder folder) throws CoreException {
        final IContainer parent = folder.getParent();
        if (parent instanceof IFolder) {
            prepareFolder((IFolder) parent);
        }
        if (!folder.exists()) {
            folder.create(false, false, null);
        }
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
        reviewDocument.addSection(ReviewDocumentSection.PROJECT_NAME, projectName);
        return reviewDocument;
    }
}
