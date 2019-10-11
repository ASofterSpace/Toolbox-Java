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

		Color foregroundColor = new Color(0, 0, 0);
		Color missingColor = new Color(255, 0, 0);
		Color backgroundColor = new Color(255, 255, 255);
		Color focusColor = new Color(196, 128, 255);
		Color selectedColor = new Color(212, 196, 255);

		if (tree instanceof FileTree) {
			FileTree fileTree = (FileTree) tree;
			String scheme = fileTree.getScheme();
			switch (scheme) {
				case GuiUtils.DARK_SCHEME:
					foregroundColor = new Color(255, 255, 255);
					missingColor = new Color(255, 128, 112);
					backgroundColor = new Color(0, 0, 0);
					focusColor = new Color(178, 0, 242);
					selectedColor = new Color(132, 0, 172);
					break;
			}
		}

		if (node instanceof FileTreeFile) {
			FileTreeFile fileNode = (FileTreeFile) node;
			tab = fileNode.getTab();
		}

		// if a file is actually missing, show it in red
		if ((tab != null) && tab.isMissing()) {
			setForeground(missingColor);
		} else {
			setForeground(foregroundColor);
		}
		setOpaque(true);
		if (hasFocus) {
			setBackground(focusColor);
		} else if (sel) {
			setBackground(selectedColor);
		} else {
			setBackground(backgroundColor);
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
