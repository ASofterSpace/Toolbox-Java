/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.gui;

import java.awt.Dimension;

import javax.swing.JTextPane;


/**
 * This is a JTextPane styled such that it behaves nicely as a generic code editor
 * (you might want to style it with one of the code editor styles from
 * com.asofterspace.toolbox.codeeditor.* to get a code editor for a specific language)
 *
 * @author Moya (a softer space, 2018)
 */
public class CodeEditor extends JTextPane {

	private final static long serialVersionUID = 1L;


	public boolean getScrollableTracksViewportWidth() {

		// do NOT report something fancy...
		// return getUI().getPreferredSize(this).width <= getParent().getSize().width;

		// ... instead, ALWAYS ask the surrounding container to make as much space as
		// required (this prevents word-wrap, as I really don't like word wrapping in
		// a code editor!)
		return false;
	}

	public Dimension getPreferredSize() {

		// get the size that the text takes up...
		Dimension result = getUI().getPreferredSize(this);

		// ... and add a small margin to the right and at the bottom
		result.width = result.width + 25;
		result.height = result.height + 25;

		return result;
	}
}
