/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;


public class FileTreeModel implements TreeModel {

	private FileTreeFolder root;

	private List<TreeModelListener> listeners;


	public FileTreeModel() {
		root = null;
		listeners = new ArrayList<>();
	}

	public void regenerate(List<? extends FileTab> tabs) {

		// find shortest common ancestor, make this the root node, and all others underneath it...
		root = null;

		String splitsep = File.separator;
		if ("\\".equals(splitsep)) {
			splitsep = "\\\\";
		}

		for (FileTab tab : tabs) {

			String path = tab.getFilePath();
			String[] pathArr = path.split(splitsep);
			String[] folderPathArr = Arrays.copyOf(pathArr, pathArr.length - 1);

			if (root == null) {
				root = new FileTreeFolder(folderPathArr);
			} else {
				root = root.rebaseRoot(folderPathArr);
			}

			root.addFile(pathArr, tab);
		}

		// if no root has been found at all, create a fake one such that there are no exceptions
		if (root == null) {
			String[] emptyRootPath = {"(none loaded)"};
			root = new FileTreeFolder(emptyRootPath);
		}

		// tell the listener!
		for (TreeModelListener listener : listeners) {
			Object[] rootPath = {root};
			TreeModelEvent ev = new TreeModelEvent(this, rootPath);
			listener.treeStructureChanged(ev);
		}
	}

	@Override
	public void addTreeModelListener(TreeModelListener listener) {
		listeners.add(listener);
	}

	public FileTreeNode getChild(TreePath path) {

		if (path == null) {
			return null;
		}

		Object[] children = path.getPath();

		Object child = children[children.length - 1];

		if (child instanceof FileTreeNode) {
			return (FileTreeNode) child;
		}

		return null;
	}

	@Override
	public FileTreeNode getChild(Object parent, int index) {

		if (parent instanceof FileTreeFolder) {
			FileTreeFolder parentFolder = (FileTreeFolder) parent;

			return parentFolder.getChild(index);
		}

		return null;
	}

	@Override
	public int getChildCount(Object parent) {

		if (parent instanceof FileTreeFolder) {
			FileTreeFolder parentFolder = (FileTreeFolder) parent;

			return parentFolder.getChildren().size();
		}

		return 0;
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {

		if (parent instanceof FileTreeFolder) {
			FileTreeFolder parentFolder = (FileTreeFolder) parent;

			List<FileTreeNode> children = parentFolder.getChildren();

			for (int i = 0; i < children.size(); i++) {
				if (children.get(i).equals(child)) {
					return i;
				}
			}
		}

		return -1;
	}

	@Override
	public FileTreeFolder getRoot() {
		return root;
	}

	@Override
	public boolean isLeaf(Object node) {
		return (node instanceof FileTreeFile);
	}

	@Override
	public void removeTreeModelListener(TreeModelListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Call this to indicate that the node at the given path is being renamed
	 */
	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		for (TreeModelListener listener : listeners) {
			TreeModelEvent ev = new TreeModelEvent(this, path);
			if (listener instanceof FileTreeModelListener) {
				FileTreeModelListener fileListener = (FileTreeModelListener) listener;
				fileListener.treeNodesRenamed(ev);
			}
			listener.treeNodesChanged(ev);
		}
	}

	/**
	 * Call this to indicate that the node at the given path is being resized
	 */
	public void resizeNode(TreePath path) {
		for (TreeModelListener listener : listeners) {
			TreeModelEvent ev = new TreeModelEvent(this, path);
			if (listener instanceof FileTreeModelListener) {
				FileTreeModelListener fileListener = (FileTreeModelListener) listener;
				fileListener.treeNodesResized(ev);
			}
			listener.treeNodesChanged(ev);
		}
	}

	public FileTreeFile getNode(FileTab tab) {
		return getNodeFromParent(tab, root);
	}

	private FileTreeFile getNodeFromParent(FileTab tab, FileTreeFolder parent) {
		List<FileTreeNode> children = parent.getChildren();

		for (FileTreeNode child : children) {
			if (child instanceof FileTreeFile) {
				FileTreeFile childFile = (FileTreeFile) child;
				if (tab.equals(childFile.getTab())) {
					return childFile;
				}
			} else {
				if (child instanceof FileTreeFolder) {
					FileTreeFile result = getNodeFromParent(tab, (FileTreeFolder) child);
					if (result != null) {
						return result;
					}
				}
			}
		}
		return null;
	}

	public FileTreeNode[] getPathToRoot(FileTab tab) {
		FileTreeNode node = getNode(tab);
		ArrayList<FileTreeNode> resultList = new ArrayList<>();
		while (node != null) {
			resultList.add(node);
			node = node.getParent();
		}
		FileTreeNode[] result = new FileTreeNode[resultList.size()];
		for (int i = 0; i < resultList.size(); i++) {
			result[i] = resultList.get(resultList.size() - (i + 1));
		}
		return result;
	}

}
