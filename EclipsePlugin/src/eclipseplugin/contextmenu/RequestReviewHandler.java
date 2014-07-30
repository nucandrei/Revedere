package eclipseplugin.contextmenu;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.Workbench;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.nuc.revedere.review.ReviewData;
import org.nuc.revedere.review.ReviewDocumentSection;
import org.nuc.revedere.review.ReviewFile;
import org.osgi.framework.Bundle;

import eclipseplugin.dialogs.ReviewDocumentDialog;
import eclipseplugin.dialogs.ReviewFilesDialog;
import eclipseplugin.revedere.RevedereManager;

public class RequestReviewHandler extends AbstractHandler {
    static final Set<ReviewDocumentSection> sections = new HashSet<>();
    static {
        final Bundle bundle = Platform.getBundle("EclipsePlugin");
        final URL url = bundle.getEntry("res/reviewdocument.xml");
        try {
            String uri = FileLocator.resolve(url).getFile();
            File file = new File(uri);
            final SAXBuilder builder = new SAXBuilder();
            final Document document = builder.build(file);
            final Element fieldsElement = document.getRootElement().getChild("fields");
            for (Element fieldElement : fieldsElement.getChildren("field")) {
                final String name = fieldElement.getChildText("name");
                final boolean mandatory = Boolean.valueOf(fieldElement.getChildText("mandatory"));
                final String initialValue = fieldElement.getChildText("initialValue");
                sections.add(new ReviewDocumentSection(name, mandatory, initialValue));
            }

        } catch (IOException | JDOMException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);

        if (RevedereManager.getInstance().getCurrentSession() == null) {
            MessageDialog.openInformation(window.getShell(), "Revederé", "The Revederé plugin is not connected.");
            return null;
        }
        final IProject selectedProject = getSelectedProject();
        final IPath projectPath = selectedProject.getLocation();
        final ReviewFilesDialog reviewFilesDialog = new ReviewFilesDialog(window.getShell(), getAllResources(projectPath));
        final int dialogResult = reviewFilesDialog.open();
        if (dialogResult == Window.CANCEL) {
            return null;
        }

        final ReviewData reviewData = createReviewData(reviewFilesDialog.getSelectedResources());
        final ReviewDocumentDialog dialog = new ReviewDocumentDialog(window.getShell(), reviewData, sections, selectedProject.getName());
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

    private List<IResource> getAllResources(IPath path) {
        final List<IResource> resources = new ArrayList<>();
        final IContainer container = ResourcesPlugin.getWorkspace().getRoot().getContainerForLocation(path);
        try {
            for (IResource resource : container.members()) {
                if (resource.getType() == IResource.FOLDER) {
                    resources.addAll(getAllResources(resource.getLocation()));
                }
                resources.add(resource);
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
