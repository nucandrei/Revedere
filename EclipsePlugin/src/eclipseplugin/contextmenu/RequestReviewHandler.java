package eclipseplugin.contextmenu;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.Workbench;
import org.nuc.revedere.client.RevedereSession;
import org.nuc.revedere.review.ReviewData;
import org.nuc.revedere.review.ReviewFile;
import eclipseplugin.Activator;
import eclipseplugin.dialogs.ReviewDocumentDialog;
import eclipseplugin.dialogs.ReviewFilesDialog;
import eclipseplugin.preferences.PreferenceConstants;
import eclipseplugin.revedere.RevedereManager;

public class RequestReviewHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

        final RevedereSession currentSession = RevedereManager.getInstance().getCurrentSession();
        if (currentSession == null) {
            MessageDialog.openInformation(window.getShell(), "Revederé", "The Revederé plugin is not connected.");
            return null;
        }
        final IProject selectedProject = getSelectedProject();
        final IPath projectPath = selectedProject.getLocation();
        final IPreferenceStore preferencePage = Activator.getDefault().getPreferenceStore();

        final boolean includeEmptyFolders = preferencePage.getBoolean(PreferenceConstants.EMPTY_FOLDERS_VISIBLE_IN_REVIEW_FILES_DIALOG);
        final boolean filterFiles = preferencePage.getBoolean(PreferenceConstants.FILTER_REVIEW_FILES);
        final String filesFilter = preferencePage.getString(PreferenceConstants.REVIEW_FILES_FILTER);

        final ReviewFilesDialog reviewFilesDialog = new ReviewFilesDialog(window.getShell(), getAllResources(projectPath, includeEmptyFolders, filterFiles, filesFilter));
        final int dialogResult = reviewFilesDialog.open();
        if (dialogResult == Window.CANCEL) {
            return null;
        }

        final ReviewData reviewData = createReviewData(reviewFilesDialog.getSelectedResources());
        final ReviewDocumentDialog dialog = new ReviewDocumentDialog(window.getShell(), reviewData, currentSession.getReviewDocumentSections(), selectedProject.getName());
        dialog.open();
        return null;
    }

    private ReviewData createReviewData(List<IResource> resources) {
        final List<String> folders = new ArrayList<>();
        final List<ReviewFile> reviewFiles = new ArrayList<>();
        for (IResource resource : resources) {
            if (resource.getType() == IResource.FOLDER) {
                folders.add(resource.getProjectRelativePath().toString());
            } else {
                final ReviewFile reviewFile = new ReviewFile(resource.getProjectRelativePath().toString(), getFileContent(resource));
                reviewFiles.add(reviewFile);
            }
        }

        return new ReviewData(reviewFiles, folders);
    }

    private String getFileContent(IResource resource) {
        try {
            return new Scanner(resource.getLocation().toFile()).useDelimiter("\\Z").next();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    private List<IResource> getAllResources(IPath path, boolean includeEmptyFolders, boolean filterFiles, String fileFilter) {
        final List<IResource> resources = new ArrayList<>();
        final IContainer container = ResourcesPlugin.getWorkspace().getRoot().getContainerForLocation(path);
        try {
            for (IResource resource : container.members()) {
                if (resource.getType() == IResource.FOLDER) {
                    final List<IResource> containedResources = getAllResources(resource.getLocation(), includeEmptyFolders, filterFiles, fileFilter);
                    if (includeEmptyFolders || !containedResources.isEmpty()) {
                        resources.addAll(containedResources);
                        resources.add(resource);
                    }
                }

                if (resource.getType() == IResource.FILE) {
                    if (!filterFiles || resource.getProjectRelativePath().toString().matches(fileFilter)) {
                        resources.add(resource);
                    }
                }
            }
        } catch (CoreException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
        return resources;

    }

    private IProject getSelectedProject() {
        final ISelectionService selectionService = Workbench.getInstance().getActiveWorkbenchWindow().getSelectionService();
        final ISelection selection = selectionService.getSelection();

        if (!(selection instanceof IStructuredSelection)) {
            return null;
        }

        final IStructuredSelection ss = (IStructuredSelection) selection;
        final Object element = ss.getFirstElement();
        if (element instanceof IResource) {
            return (IProject) element;
        }
        if (!(element instanceof IAdaptable)) {
            return null;
        }
        final IAdaptable adaptable = (IAdaptable) element;
        final Object adapter = adaptable.getAdapter(IResource.class);
        return (IProject) adapter;
    }
}
