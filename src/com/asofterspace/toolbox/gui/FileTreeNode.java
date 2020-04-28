/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.gui;


public abstract class FileTreeNode {

	String name;

	FileTreeFolder parent;

	// 9: this is the absolutely latest entry!
	// 8: this is one before 9
	// 7: this is two before 9
	// ...
	// 0 and below: no special selection
	private int selectionOrder;


	public FileTreeNode(String name) {
		this.name = name;
		this.selectionOrder = 0;
	}

	public int getSelectionOrder() {
		return selectionOrder;
	}

	public void setSelectionOrder(int selectionOrder) {
		this.selectionOrder = selectionOrder;
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
