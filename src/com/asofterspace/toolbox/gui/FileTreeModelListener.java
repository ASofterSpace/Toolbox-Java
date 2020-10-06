/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.gui;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;


public interface FileTreeModelListener extends TreeModelListener {

	void treeNodesRenamed(TreeModelEvent e);

	void treeNodesResized(TreeModelEvent e);

}
