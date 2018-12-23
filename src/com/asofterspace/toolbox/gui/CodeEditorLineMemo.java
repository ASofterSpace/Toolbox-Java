/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.gui;

import java.awt.Dimension;

import javax.swing.JTextPane;


/**
 * This is a JTextPane styled such that it behaves nicely as a memo of line numbers for a code editor
 * (you might want to style it with the com.asofterspace.toolbox.codeeditor.LineNumbering style to
 * actually get it to behave properly, and register it with the codeeditor calling setCodeEditorLineMemo
 * on its highlighter.)
 *
 * @author Moya (a softer space, 2018)
 */
public class CodeEditorLineMemo extends JTextPane {

	private final static long serialVersionUID = 1L;


	public boolean getScrollableTracksViewportWidth() {

		// do NOT report something fancy...
		// return getUI().getPreferredSize(this).width <= getParent().getSize().width;

		// ... instead, ALWAYS ask the surrounding container to make as much space as
		// required (this prevents word-wrap, as I really don't like word wrapping in
		// a line memo containing the line numbers!)
		return false;
	}

	public Dimension getPreferredSize() {

		// get the size that the text takes up...
		Dimension result = getUI().getPreferredSize(this);

		// ... and return it as is ^^
		return result;
	}
}
