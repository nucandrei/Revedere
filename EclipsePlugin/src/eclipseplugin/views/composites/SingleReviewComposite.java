package eclipseplugin.views.composites;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.nuc.revedere.client.RevedereSession;
import org.nuc.revedere.core.User;
import org.nuc.revedere.core.messages.update.ReviewUpdate;
import org.nuc.revedere.review.Review;
import org.nuc.revedere.review.ReviewComment;
import org.nuc.revedere.review.ReviewData;
import org.nuc.revedere.review.ReviewDocument;
import org.nuc.revedere.review.ReviewFile;
import org.nuc.revedere.review.ReviewState;
import org.nuc.revedere.util.Collector;
import org.nuc.revedere.util.Collector.CollectorListener;
import org.nuc.revedere.util.Tuple;

import eclipseplugin.dialogs.ReviewDocumentDialog;
import eclipseplugin.revedere.RevedereManager;
import eclipseplugin.util.TreeBuilder;
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
    private final Image folderImage;
    private final Image fileImage;
    private final Image commentImage;

    private final Map<TreeItem, Tuple<ReviewFile, ReviewComment>> comments = new HashMap<>();
    private final Tree tree;
    private final TreeBuilder treeBuilder;
    private final Map<String, TreeItem> treeItems;

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

        tree = new Tree(this, SWT.BORDER);
        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));

        treeBuilder = new TreeBuilder(tree, SWT.NONE);
        treeItems = treeBuilder.getTreeItems();

        tree.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {

                final TreeItem selected = tree.getSelection()[0];
                if (comments.containsKey(selected)) {
                    final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
                    final String projectName = currentReview.getReviewDocument().getSectionText(ReviewDocument.PROJECT_NAME_SECTION);
                    final IProject project = root.getProject(projectName);
                    if (!project.exists()) {
                        MessageDialog.openInformation(parent.getShell(), "Revederé", String.format("The project: %s does not exists", projectName));
                        return;
                    }
                    final ReviewFile reviewFile = comments.get(selected).getTItem();
                    final ReviewComment reviewComment = comments.get(selected).getUItem();
                    final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    final IFile reviewFileInEclipse = project.getFile(reviewFile.getFileRelativePath());
                    try {
                        final ITextEditor editor = (ITextEditor) IDE.openEditor(page, reviewFileInEclipse);
                        editor.selectAndReveal(reviewComment.getSelectionOffset(), reviewComment.getSelectionLength());
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        Button openDocButton = new Button(this, SWT.NONE);
        openDocButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                final ReviewDocumentDialog reviewDocumentDialog = new ReviewDocumentDialog(parent.getShell(), currentReview);
                reviewDocumentDialog.open();
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
                final IProject correspondingProject = revedereManager.getReviewBox().removeReview(currentReview);
                if (correspondingProject == null) {
                    return;
                }
                final MessageDialog messageDialog = new MessageDialog(SingleReviewComposite.this.getShell(), "Revedere", null, "Do you want to remove project?", MessageDialog.QUESTION, new String[] { "YES", "NO" }, 1);
                final int response = messageDialog.open();
                if (response == 0) {
                    deleteProject(correspondingProject);
                }
            }
        };

        final IWorkbench workbench = PlatformUI.getWorkbench();
        final ISharedImages images = workbench.getSharedImages();
        folderImage = images.getImage(ISharedImages.IMG_OBJ_FOLDER);
        fileImage = images.getImage(ISharedImages.IMG_OBJ_FILE);
        commentImage = images.getImage(ISharedImages.IMG_TOOL_PASTE);
    }

    private void deleteProject(IProject correspondingProject) {
        try {
            correspondingProject.delete(IResource.ALWAYS_DELETE_PROJECT_CONTENT, null);
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }

    public void update(Review review) {
        if (this.currentReview != review) {
            Display.getDefault().syncExec(new Runnable() {
                @Override
                public void run() {
                    hasFocus = true;
                    addListenerOnReviewUpdateIfMissing();
                    currentReview = review;
                    redrawTree(currentReview.getData(), currentReview.getState());
                    reviewNameLabel.setText(review.getReviewDocument().getSectionText(ReviewDocument.PROJECT_NAME_SECTION));
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

    private void redrawTree(ReviewData reviewData, ReviewState reviewState) {
        for (TreeItem existingTreeItem : treeItems.values()) {
            existingTreeItem.dispose();
        }
        treeItems.clear();
        comments.clear();

        for (String folder : reviewData.getFolders()) {
            treeBuilder.constructTree(folder, folderImage);
        }

        for (ReviewFile reviewFile : reviewData.getReviewFiles()) {
            final TreeItem treeItem = (TreeItem) treeBuilder.constructTree(reviewFile.getFileRelativePath(), fileImage);
            if (reviewState.equals(ReviewState.DONE) || reviewState.equals(ReviewState.CLOSED)) {
                int currentComment = 0;
                for (ReviewComment reviewComment : reviewFile.getComments()) {
                    TreeItem reviewCommentItem = new TreeItem(treeItem, SWT.NONE);
                    reviewCommentItem.setText(reviewComment.getComment());
                    reviewCommentItem.setImage(commentImage);
                    treeItems.put(reviewFile.getFileRelativePath() + currentComment, reviewCommentItem);
                    currentComment++;
                    comments.put(reviewCommentItem, new Tuple<>(reviewFile, reviewComment));
                }
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
