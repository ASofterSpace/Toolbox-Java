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

	public static final int PREV_SELECTED_TAB_AMOUNT = 12;


	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object node,
			boolean sel, boolean exp, boolean leaf, int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, node, sel, exp, leaf, row, hasFocus);

		int font = 0;

		FileTab tab = null;

		Color foregroundColor = new Color(0, 0, 0);
		Color missingColor = new Color(255, 0, 0);
		Color backgroundColor = new Color(255, 255, 255);
		Color focusColor = new Color(196, 128, 255);
		Color selectedColor = new Color(212, 196, 255);
		Color highlightedColor = new Color(64, 0, 128);

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
					highlightedColor = new Color(221, 196, 255);
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
			if ((tab != null) && tab.isHighlighted()) {
				setForeground(highlightedColor);
				font |= Font.ITALIC;
			} else {
				setForeground(foregroundColor);
			}
		}
		setOpaque(true);
		if (hasFocus) {
			setBackground(focusColor);
		} else if (sel) {
			setBackground(selectedColor);
		} else {
			setBackground(backgroundColor);
			if (node instanceof FileTreeNode) {
				int selOrder = ((FileTreeNode) node).getSelectionOrder();
				if (selOrder > 0) {
					Color selColor = GuiUtils.intermix(
						selectedColor, backgroundColor, selOrder / (double) PREV_SELECTED_TAB_AMOUNT);
					setBackground(selColor);
				}
			}
		}

		// if a file has been changed recently, show it in bold
		if ((tab != null) && tab.hasBeenChanged()) {
			font |= Font.BOLD;
		}
		setFont(getFont().deriveFont(font));

		return this;
	}
}
