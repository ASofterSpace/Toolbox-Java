/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.gui;


public abstract class FileTreeNode {

	String name;

	FileTreeFolder parent;


	public FileTreeNode(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	public FileTreeFolder getParent() {
		// jump over squish nodes
		if (parent != null) {
			if (parent.parent != null) {
				if (parent.parent.getSquishNode() != null) {
					return parent.getParent();
				}
			}
		}
		return parent;
	}

	public void setParent(FileTreeFolder parent) {
		this.parent = parent;
	}

	abstract public String getDirectoryName();

}
