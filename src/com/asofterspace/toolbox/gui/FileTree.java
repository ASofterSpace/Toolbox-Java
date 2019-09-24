/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.gui;

import javax.swing.JTree;
import javax.swing.tree.TreeModel;


public class FileTree extends JTree {

	public static final long serialVersionUID = 235803948593477294l;


	public FileTree(TreeModel model) {
		super(model);
		this.setCellRenderer(new FileTreeCellRenderer());
	}

}
