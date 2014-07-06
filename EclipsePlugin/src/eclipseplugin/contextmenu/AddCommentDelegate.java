package eclipseplugin.contextmenu;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.nuc.revedere.review.Review;
import org.nuc.revedere.review.ReviewComment;
import org.nuc.revedere.review.ReviewFile;
import org.nuc.revedere.review.ReviewState;

import eclipseplugin.revedere.RevedereManager;

public class AddCommentDelegate implements IEditorActionDelegate {
    private IEditorPart editorPart;

    @Override
    public void run(IAction arg0) {
        final ITextEditor editor = (ITextEditor) editorPart;
        final TextSelection selection = (TextSelection) editor.getSelectionProvider().getSelection();
        final FileEditorInput fileInput = (FileEditorInput) editor.getEditorInput();
        final IProject project = fileInput.getFile().getProject();
        if (RevedereManager.getInstance().getReviewBox().isReviewProject(project)) {
            final Review correspondingReview = RevedereManager.getInstance().getReviewBox().getReview(project);
            if (!correspondingReview.getState().equals(ReviewState.ACCEPT)) {
                MessageDialog.openInformation(editorPart.getSite().getShell(), "Revederé", "The corresponding review was not acceped");
                return;
            }
            final String editorFilePath = fileInput.getFile().getProjectRelativePath().toString();
            for (ReviewFile reviewFile : correspondingReview.getData().getReviewFiles()) {
                if (reviewFile.getFileRelativePath().equals(editorFilePath)) {
                    reviewFile.addComment(new ReviewComment(selection.getStartLine(), selection.getEndLine(), selection.getText()));
                    return;
                }
            }

        } else {
            MessageDialog.openInformation(editorPart.getSite().getShell(), "Revederé", String.format("The project: %s is not in review", project.getName()));
            return;
        }

    }

    @Override
    public void selectionChanged(IAction arg0, ISelection selection) {
        System.out.println("selection changed");

    }

    @Override
    public void setActiveEditor(IAction arg0, IEditorPart editorPart) {
        this.editorPart = editorPart;
    }

}
