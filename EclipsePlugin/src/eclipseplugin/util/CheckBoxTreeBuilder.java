package eclipseplugin.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

public class CheckBoxTreeBuilder extends TreeBuilder {

    public CheckBoxTreeBuilder(Tree originalTree, int treeItemStyle) {
        super(originalTree, treeItemStyle);
    }

    public void attachCheckBoxes() {
        final Tree rootTree = getRootTree();
        rootTree.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (event.detail == SWT.CHECK) {
                    TreeItem item = (TreeItem) event.item;
                    boolean checkValue = item.getChecked();
                    checkItemAndDescendents(item, checkValue);
                    checkItemAndAscendents(item.getParentItem(), checkValue, false);
                }
            }
        });
    }

    public Set<String> getSelectedItems() {
        final Set<String> selectedItems = new HashSet<>();
        for (Entry<String, TreeItem> entry : getTreeItems().entrySet()) {
            if (entry.getValue().getChecked()) {
                selectedItems.add(entry.getKey());
            }
        }
        return selectedItems;
    }

    public void checkAll(boolean checkValue) {
        for (TreeItem childTreeItem : getRootTree().getItems()) {
            checkItemAndDescendents(childTreeItem, checkValue);
        }
    }

    private void checkItemAndDescendents(TreeItem treeItem, boolean checkValue) {
        treeItem.setGrayed(false);
        treeItem.setChecked(checkValue);
        for (TreeItem childTreeItem : treeItem.getItems()) {
            checkItemAndDescendents(childTreeItem, checkValue);
        }
    }

    private void checkItemAndAscendents(TreeItem treeItem, boolean checkValue, boolean grayValue) {
        if (treeItem == null) {
            return;
        }

        if (!grayValue) {
            for (TreeItem childTreeItem : treeItem.getItems()) {
                if (childTreeItem.getGrayed() || checkValue != childTreeItem.getChecked()) {
                    checkValue = true;
                    grayValue = true;
                    break;
                }
            }

        } else {
            checkValue = true;
        }
        treeItem.setChecked(checkValue);
        treeItem.setGrayed(grayValue);
        checkItemAndAscendents(treeItem.getParentItem(), checkValue, grayValue);
    }
}
