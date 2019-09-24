/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.gui;


public class FileTreeFile extends FileTreeNode {

	private FileTab tab;


	public FileTreeFile(String name, FileTab tab) {
		super(name);

		this.tab = tab;
	}

	public FileTab getTab() {
		return tab;
	}

	@Override
	public String getDirectoryName() {
		return tab.getDirectoryName();
	}

	@Override
	public String toString() {

		String result = super.toString();

		if (tab.hasBeenChanged()) {
			result = result + GuiUtils.CHANGE_INDICATOR;
		}

		return result;
	}

}
