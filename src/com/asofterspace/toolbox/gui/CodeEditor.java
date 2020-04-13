/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.RenderingHints;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;


/**
 * This is a JTextPane styled such that it behaves nicely as a generic code editor
 * (you might want to style it with one of the code editor styles from
 * com.asofterspace.toolbox.codeeditor.* to get a code editor for a specific language)
 *
 * @author Moya (a softer space, 2018)
 */
public class CodeEditor extends JTextPane {

	private final static long serialVersionUID = 1L;

	private boolean showStartLine = false;

	private int prevStartLinePos = 0;


	public void enableStartLine(boolean doEnable) {
		this.showStartLine = doEnable;
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {

		// do NOT report something fancy...
		// return getUI().getPreferredSize(this).width <= getParent().getSize().width;

		// ... instead, ALWAYS ask the surrounding container to make as much space as
		// required (this prevents word-wrap, as I really don't like word wrapping in
		// a code editor!)
		return false;
	}

	@Override
	public Dimension getPreferredSize() {

		// get the size that the text takes up...
		Dimension result = new Dimension(100, 100);
		try {
			result = getUI().getPreferredSize(this);
		} catch (NullPointerException e) {
			// occasionally, we get an NPE here that is directly thrown by swing - oops!
		}

		// ... and add a small margin to the right and at the bottom
		result.width = result.width + 25;
		result.height = result.height + 25;

		return result;
	}

	@Override
	public void paint(Graphics g) {

		super.paint(g);

		if (!showStartLine) {
			return;
		}

		String text = getText();
		int pos = getSelectionStart();
		int firstLetter = pos;
		char chr = ' ';
		while (pos < text.length()) {
			chr = text.charAt(pos);
			if ((chr != ' ') && (chr != '\t')) {
				break;
			}
			pos++;
		}
		if (chr == '\n') {
			pos--;
		}
		while (pos > 0) {
			try {
				chr = text.charAt(pos);
			} catch (StringIndexOutOfBoundsException e) {
				// whoops!
			}
			if (chr == '\n') {
				break;
			}
			if ((chr != ' ') && (chr != '\t')) {
				firstLetter = pos;
			}
			pos--;
		}

		try {
			int x = ((int) modelToView2D(firstLetter).getX()) - 1;

			if (prevStartLinePos != x) {
				prevStartLinePos = x;
				repaint();
			}

		} catch (BadLocationException e) {
			// whoops!
		}

		g.setColor(Color.DARK_GRAY);
		g.drawLine(prevStartLinePos, 0, prevStartLinePos, getHeight());
	}

	@Override
	protected void paintComponent(Graphics g) {

		if (g instanceof Graphics2D) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		}

		super.paintComponent(g);
	}
}
