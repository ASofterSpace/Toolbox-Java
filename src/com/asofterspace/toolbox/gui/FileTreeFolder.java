/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class FileTreeFolder extends FileTreeNode {

	private String[] originPath;

	private List<FileTreeNode> children;


	/**
	 * Create this as root folder, without a parent
	 */
	public FileTreeFolder(String[] path) {

		super(getFirstPathElement(path));

		originPath = path;

		children = new ArrayList<>();
	}

	/**
	 * Create this as subfolder of another one
	 */
	public FileTreeFolder(String name, FileTreeFolder parent) {

		super(name);

		originPath = new String[parent.originPath.length + 1];
		for (int i = 0; i < parent.originPath.length; i++) {
			originPath[i] = parent.originPath[i];
		}
		originPath[originPath.length - 1] = name;

		children = new ArrayList<>();

		this.parent = parent;
	}

	private static String getFirstPathElement(String[] path) {

		if (path.length == 0) {
			return File.separator;
		}

		return path[path.length - 1];
	}

	public FileTreeNode getChild(int index) {

		FileTreeFolder squishNode = getSquishNode();

		if (squishNode == null) {
			return children.get(index);
		}

		return squishNode.getChild(index);
	}

	public FileTreeNode getChild(String name) {

		for (FileTreeNode child : children) {
			if (child.name.equals(name)) {
				return child;
			}
		}

		return null;
	}

	public List<FileTreeNode> getChildren() {

		FileTreeFolder squishNode = getSquishNode();

		if (squishNode == null) {
			return children;
		}

		return squishNode.getChildren();
	}

	public FileTreeFolder getOrCreateFolder(String name) {

		for (FileTreeNode child : children) {
			if (child.name.equals(name) && (child instanceof FileTreeFolder)) {
				return (FileTreeFolder) child;
			}
		}

		FileTreeFolder newChild = new FileTreeFolder(name, this);
		this.appendChild(newChild);

		return newChild;
	}

	public void appendChild(FileTreeNode child) {
		children.add(child);
		child.setParent(this);

		// sort children of folders alphabetically...
		Collections.sort(children, new Comparator<FileTreeNode>() {
			public int compare(FileTreeNode a, FileTreeNode b) {
				// ... but keep folders and files separate from each other!
				if ((a instanceof FileTreeFolder) && (b instanceof FileTreeFile)) {
					return -1;
				}
				if ((a instanceof FileTreeFile) && (b instanceof FileTreeFolder)) {
					return 1;
				}
				return a.toString().toLowerCase().compareTo(b.toString().toLowerCase());
			}
		});
	}

	public FileTreeFolder rebaseRoot(String[] path) {

		int i = 0;

		while (true) {
			if (i == originPath.length) {
				// no need to rebase, as we are already the root - also for the new path:
				// this path: /foo/bar/dragon/fire    <- new root (same as previous root)
				//  new path: /foo/bar/dragon/fire/is/hot
				return this;
			}

			if (i == path.length) {
				// the new path is shorter than the current origin, but the same until there:
				// this path: /foo/bar/dragon/fire
				//  new path: /foo/bar    <- new root
				FileTreeFolder newRoot = new FileTreeFolder(path);
				FileTreeFolder curRoot = newRoot;
				for (; i < originPath.length - 1; i++) {
					FileTreeFolder newFolder = new FileTreeFolder(originPath[i], curRoot);
					curRoot.appendChild(newFolder);
					curRoot = newFolder;
				}
				curRoot.appendChild(this);
				this.parent = curRoot;
				return newRoot;
			}

			if (path[i].equals(originPath[i])) {
				// all is good - we are in agreement, carry on!
				i++;
			} else {
				// we are not in agreement about the start of the path, so we need a new common root:
				// this path: /foo/bar/dragon/fire
				//  new path: /foo/bar/cat/ears
				//  generate: /foo/bar    <- new root
				String[] commonPath = Arrays.copyOf(path, i);
				FileTreeFolder newRoot = new FileTreeFolder(commonPath);
				FileTreeFolder curRoot = newRoot;
				for (; i < originPath.length - 1; i++) {
					FileTreeFolder newFolder = new FileTreeFolder(originPath[i], curRoot);
					curRoot.appendChild(newFolder);
					curRoot = newFolder;
				}
				curRoot.appendChild(this);
				this.parent = curRoot;
				return newRoot;
			}
		}
	}

	public FileTreeFolder findFolder(String[] path) {

		int i = 0;

		if (path.length < originPath.length) {
			return null;
		}

		for (; i < originPath.length; i++) {
			if (!originPath[i].equals(path[i])) {
				return null;
			}
		}

		FileTreeFolder cur = this;

		for (; i < path.length; i++) {
			cur = cur.getOrCreateFolder(path[i]);
		}

		return cur;
	}

	public void addFile(String[] path, FileTab tab) {

		// find the parent folder
		// (not just the parent of this FileTreeFolder, but the one from the path!)
		String[] folderPath = Arrays.copyOf(path, path.length - 1);
		FileTreeFolder parentFolder = findFolder(folderPath);

		// create the file node
		FileTreeFile curFile = new FileTreeFile(path[path.length - 1], tab);

		// add the file to its parent
		parentFolder.appendChild(curFile);
	}

	/**
	 * If this folder is squished together with the ones underneath it,
	 * then get the next one down the line
	 */
	public FileTreeFolder getSquishNode() {

		if (children.size() != 1) {
			return null;
		}

		FileTreeNode possibleSquishNode = children.get(0);

		if (possibleSquishNode instanceof FileTreeFolder) {
			return (FileTreeFolder) possibleSquishNode;
		}

		return null;
	}

	@Override
	public String getDirectoryName() {
		return getDirectoryName(true);
	}

	/**
	 * If absolute is true, start from the root of the file system,
	 * otherwise start from here
	 */
	private String getDirectoryName(boolean absolute) {

		StringBuilder result = new StringBuilder();

		if (absolute) {
			String sep = "";
			for (String pathElement : originPath) {
				result.append(sep);
				result.append(pathElement);
				sep = File.separator;
			}
		} else {
			result.append(name);
		}

		FileTreeFolder squishNode = getSquishNode();

		if (squishNode == null) {
			return result.toString();
		}

		result.append(File.separator);
		result.append(squishNode.toString());

		return result.toString();
	}

	@Override
	public String toString() {
		return getDirectoryName(parent == null);
	}

}
