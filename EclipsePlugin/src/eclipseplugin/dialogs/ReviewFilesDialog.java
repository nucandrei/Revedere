package eclipseplugin.dialogs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import eclipseplugin.util.CheckBoxTreeBuilder;

import org.eclipse.swt.layout.GridData;

public class ReviewFilesDialog extends Dialog {
    private final List<IResource> resources;
    private List<IResource> selectedResources = new ArrayList<>();
    private final Image folderImage;
    private final Image fileImage;
    private CheckBoxTreeBuilder checkBoxTreeBuilder;
    private final Map<String, IResource> resourcesByName = new HashMap<>();

    public ReviewFilesDialog(Shell parentShell, List<IResource> resources) {
        super(parentShell);
        this.resources = resources;
        for (IResource resource : resources) {
            this.resourcesByName.put(resource.getProjectRelativePath().toString(), resource);
        }
        final IWorkbench workbench = PlatformUI.getWorkbench();
        final ISharedImages images = workbench.getSharedImages();
        folderImage = images.getImage(ISharedImages.IMG_OBJ_FOLDER);
        fileImage = images.getImage(ISharedImages.IMG_OBJ_FILE);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        final Composite container = (Composite) super.createDialogArea(parent);
        container.setLayout(new GridLayout(1, false));
        final Tree tree = new Tree(container, SWT.BORDER | SWT.CHECK);
        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        checkBoxTreeBuilder = new CheckBoxTreeBuilder(tree, SWT.CHECK);
        checkBoxTreeBuilder.attachCheckBoxes();
        final List<IResource> folders = new ArrayList<>();
        final List<IResource> files = new ArrayList<>();
        for (IResource resource : resources) {
            if (resource.getType() == IResource.FOLDER) {
                folders.add(resource);
            } else {
                files.add(resource);
            }
        }

        for (IResource folder : folders) {
            checkBoxTreeBuilder.constructTree(folder.getProjectRelativePath().toString(), folderImage);
        }

        for (IResource file : files) {
            checkBoxTreeBuilder.constructTree(file.getProjectRelativePath().toString(), fileImage);
        }

        return container;
    }

    @Override
    public void createButtonsForButtonBar(Composite composite) {

        addContributeButton(composite, "Check/uncheck all", SWT.CHECK).addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                checkBoxTreeBuilder.checkAll(((Button) event.getSource()).getSelection());
            }
        });

        addContributeButton(composite, "Create review document", SWT.NONE).addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                System.out.println(checkBoxTreeBuilder.getSelectedItems());
                for (String resourceName : checkBoxTreeBuilder.getSelectedItems()) {
                    selectedResources.add(resourcesByName.get(resourceName));
                }
                ReviewFilesDialog.this.close();
            }
        });

        addContributeButton(composite, "Cancel", SWT.NONE).addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                ReviewFilesDialog.this.cancelPressed();
            }
        });
    }

    private Button addContributeButton(Composite parent, String text, int style) {
        final Button button = new Button(parent, style);
        button.setText(text);
        final GridData data = new GridData(SWT.FILL, SWT.CENTER, false, false);
        data.widthHint = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x;
        button.setLayoutData(data);
        refreshLayout(parent);
        return button;
    }

    private void refreshLayout(Composite parent) {
        final GridLayout layout = (GridLayout) parent.getLayout();
        layout.numColumns += 1;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Select review files");
    }

    @Override
    protected Point getInitialSize() {
        return new Point(500, 500);
    }

    public List<IResource> getSelectedResources() {
        return selectedResources;
    }
}
