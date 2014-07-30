package eclipseplugin.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class TreeBuilder {
    private final Map<String, TreeItem> treeItems = new HashMap<>();
    private final Tree rootTree;
    private final int treeItemStyle;

    public TreeBuilder(Tree originalTree, int treeItemStyle) {
        this.rootTree = originalTree;
        this.treeItemStyle = treeItemStyle;
    }

    public Map<String, TreeItem> getTreeItems() {
        return treeItems;
    }

    public Object constructTree(String folder, Image image) {
        final TreeItem newTreeItem;
        if (treeItems.containsKey(folder)) {
            return treeItems.get(folder);
        }

        if (isRootFolder(folder)) {
            newTreeItem = new TreeItem(rootTree, treeItemStyle);

        } else {
            final Object parent = constructTree(getParentFolder(folder), image);
            if (parent instanceof Tree) {
                newTreeItem = new TreeItem((Tree) parent, treeItemStyle);
            } else {
                newTreeItem = new TreeItem((TreeItem) parent, treeItemStyle);
            }
        }

        newTreeItem.setText(getCurrentFolderShortName(folder));
        newTreeItem.setImage(image);
        treeItems.put(folder, newTreeItem);
        return newTreeItem;
    }

    protected Tree getRootTree() {
        return rootTree;
    }

    private boolean isRootFolder(String folder) {
        return !folder.contains("/");
    }

    private String getParentFolder(String folder) {
        return folder.substring(0, folder.lastIndexOf('/'));
    }

    private String getCurrentFolderShortName(String folder) {
        if (isRootFolder(folder)) {
            return folder;
        }
        return folder.substring(folder.lastIndexOf('/') + 1);
    }
}
