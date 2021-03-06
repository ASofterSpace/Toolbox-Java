/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.gui;

import java.awt.Color;

import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;


public class FileTree extends JTree {

	public static final long serialVersionUID = 235803948593477294l;

	private String scheme = GuiUtils.LIGHT_SCHEME;


	public FileTree(TreeModel model) {
		super(model);

		this.setCellRenderer(new FileTreeCellRenderer());

		/*
		// we could use this to prevent the tree from being collapsed by user input
		TreeWillExpandListener willExpand = new TreeWillExpandListener() {
			@Override
			public void treeWillExpand(TreeExpansionEvent event) {
				// sure!
			}

			@Override
			public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
				// nope!
				throw new ExpandVetoException(event, "I don't want to!");
			}
		};
		this.addTreeWillExpandListener(willExpand);
		*/
	}

	public void setScheme(String scheme) {

		this.scheme = scheme;

		switch (scheme) {
			case GuiUtils.LIGHT_SCHEME:
				setForeground(Color.black);
				setBackground(Color.white);
				break;
			case GuiUtils.DARK_SCHEME:
				setForeground(Color.white);
				setBackground(Color.black);
				break;
		}
	}

	public String getScheme() {
		return scheme;
	}

	@Override
	public boolean isPathEditable(TreePath path) {
		return path.getLastPathComponent() instanceof FileTreeFile;
	}

	public void fullyExpand() {

		// expand every row, from top to bottom, which conveniently is actually recursive,
		// as expanded folders immediately add new rows underneath which we also iterate
		// over
		for (int i = 0; i < getRowCount(); i++) {
			expandRow(i);
		}
	}

}
