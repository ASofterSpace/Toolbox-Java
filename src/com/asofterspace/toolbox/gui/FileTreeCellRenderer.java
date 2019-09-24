/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;


public class FileTreeCellRenderer extends DefaultTreeCellRenderer {

	public static final long serialVersionUID = 235803948593477295l;


	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object node,
			boolean sel, boolean exp, boolean leaf, int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, node, sel, exp, leaf, row, hasFocus);

		FileTab tab = null;

		if (node instanceof FileTreeFile) {
			FileTreeFile fileNode = (FileTreeFile) node;
			tab = fileNode.getTab();
		}

		// if a file is actually missing, show it in red
		if ((tab != null) && tab.isMissing()) {
			setForeground(new Color(255, 0, 0));
		} else {
			setForeground(new Color(0, 0, 0));
		}

		// if a file has been changed recently, show it in bold
		if ((tab != null) && tab.hasBeenChanged()) {
			setFont(getFont().deriveFont(Font.BOLD));
		} else {
			setFont(getFont().deriveFont(0));
		}

		return this;
	}
}
