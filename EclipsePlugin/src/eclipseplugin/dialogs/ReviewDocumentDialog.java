package eclipseplugin.dialogs;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Combo;
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
    private final ReviewData currentReviewData;
    private final boolean sourceSide;
    private final Review currentReview;
    private final String projectName;
    private final Set<ReviewDocumentSection> reviewDocumentSections;
    private final Map<ReviewDocumentSection, Text> correspondingTexts = new HashMap<>();
    private Combo usersCombo;

    public ReviewDocumentDialog(Shell parentShell, ReviewData reviewData, Set<ReviewDocumentSection> reviewDocumentSections, String projectName) {
        super(parentShell);
        this.currentReviewData = reviewData;
        this.reviewDocumentSections = reviewDocumentSections;
        this.currentReview = null;
        this.sourceSide = true;
        this.projectName = projectName;
    }

    public ReviewDocumentDialog(Shell parentShell, Review review) {
        super(parentShell);
        this.currentReviewData = null;
        this.reviewDocumentSections = review.getReviewDocument().getSections();
        this.currentReview = review;
        this.sourceSide = false;
        this.projectName = "";
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        final Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new GridLayout(2, false));

        if (sourceSide) {
            final Label userLabel = new Label(container, SWT.NONE);
            userLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
            userLabel.setText("User*");

            usersCombo = new Combo(container, SWT.NONE);
            usersCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
            final String[] users = RevedereManager.getInstance().getCurrentSession().getUsers();
            usersCombo.setItems(users);
            if (users.length == 1) {
                usersCombo.select(0);
                usersCombo.setEnabled(false);
            }
        }

        for (ReviewDocumentSection reviewDocumentSection : reviewDocumentSections) {
            final Label label = new Label(container, SWT.NONE);
            label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
            final String isMandatoryMark = reviewDocumentSection.isMandatory() ? "*" : "";
            label.setText(reviewDocumentSection.getSectionName() + isMandatoryMark);

            final Text text = new Text(container, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
            if (sourceSide) {
                text.setText(reviewDocumentSection.getDefaultValue());

            } else {
                text.setText(currentReview.getReviewDocument().getSectionText(reviewDocumentSection));
            }
            final GridData sectionGridData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
            sectionGridData.heightHint = reviewDocumentSection.getNoLines() * text.getLineHeight();
            text.setLayoutData(sectionGridData);
            text.setEnabled(sourceSide);
            correspondingTexts.put(reviewDocumentSection, text);
        }
        return container;
    }

    @Override
    public void createButtonsForButtonBar(Composite composite) {
        super.createButtonsForButtonBar(composite);
        final Button requestNewReviewOrDownloadBtn = getButton(IDialogConstants.OK_ID);
        requestNewReviewOrDownloadBtn.setText(sourceSide ? "Request review" : "Download");
        requestNewReviewOrDownloadBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                if (sourceSide) {
                    try {
                        final ReviewDocument reviewDocument = getReviewDocument();
                        int selectedIndex = usersCombo.getSelectionIndex();
                        if (selectedIndex == -1) {
                            throw new Exception("No user is selected");
                        }
                        final User intendedUser = new User(usersCombo.getItem(selectedIndex));
                        RevedereManager.getInstance().getCurrentSession().requestReview(intendedUser, currentReviewData, reviewDocument);
                        ReviewDocumentDialog.this.close();

                    } catch (Exception ex) {
                        MessageDialog.openInformation(ReviewDocumentDialog.this.getShell(), "Reveder�", ex.getMessage());
                        return;
                    }

                } else {
                    downloadReview();
                    System.out.println("Downloaded changes");
                }
            }
        });
    }

    private void downloadReview() {
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        final String projectName = currentReview.getReviewDocument().getSectionText(ReviewDocument.PROJECT_NAME_SECTION);
        final IProject project = root.getProject(projectName);

        try {
            final NullProgressMonitor nullProgressMonitor = new NullProgressMonitor();
            if (!project.exists()) {
                project.create(nullProgressMonitor);
                project.open(nullProgressMonitor);
            }

            for (String folderName : currentReview.getData().getFolders()) {
                prepareFolder(project.getFolder(folderName));
            }

            for (ReviewFile reviewFile : currentReview.getData().getReviewFiles()) {
                final IFile file = project.getFile(reviewFile.getFileRelativePath());
                final InputStream stream = new ByteArrayInputStream(reviewFile.getFileContent().getBytes(StandardCharsets.UTF_8));
                if (!file.exists()) {
                    file.create(stream, false, nullProgressMonitor);

                } else {
                    file.setContents(stream, true, true, nullProgressMonitor);
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
        if (!folder.exists()) {
            final IContainer parent = folder.getParent();
            if (parent instanceof IFolder) {
                prepareFolder((IFolder) parent);
            }
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
        return new Point(400, 600);
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    private ReviewDocument getReviewDocument() throws Exception {
        final ReviewDocument reviewDocument = new ReviewDocument();
        for (ReviewDocumentSection reviewDocumentSection : reviewDocumentSections) {
            final Text correspondingText = correspondingTexts.get(reviewDocumentSection);
            if (reviewDocumentSection.isMandatory() && correspondingText.getText().isEmpty()) {
                throw new Exception(reviewDocumentSection.getSectionName() + " field is mandatory");
            }
            reviewDocument.addSection(reviewDocumentSection, correspondingTexts.get(reviewDocumentSection).getText());
        }

        reviewDocument.addSection(ReviewDocument.PROJECT_NAME_SECTION, projectName);
        return reviewDocument;
    }
}
