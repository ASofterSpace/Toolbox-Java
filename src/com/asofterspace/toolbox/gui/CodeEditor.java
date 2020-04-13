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

	private Color startLineColor = Color.DARK_GRAY;


	/**
	 * Enable or disable the start line, which is a vertical dotted line that indicates
	 * the horizontal location at which the current line starts
	 */
	public void enableStartLine(boolean doEnable) {
		this.showStartLine = doEnable;
	}

	public void setStartLineColor(Color startLineColor) {
		this.startLineColor = startLineColor;
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
		int len = text.length();
		int pos = getSelectionStart();
		int firstLetter = pos;
		char chr = ' ';
		while (pos < len) {
			chr = text.charAt(pos);
			if ((chr != ' ') && (chr != '\t')) {
				break;
			}
			pos++;
		}
		if (chr == '\n') {
			pos--;
		}
		while (pos >= len) {
			pos--;
		}
		while (pos > 0) {
			chr = text.charAt(pos);
			if (chr == '\n') {
				break;
			}
			if ((chr != ' ') && (chr != '\t')) {
				firstLetter = pos;
			}
			pos--;
		}

		try {
			// in the future, modelToView2D is used instead, but we want to be backwards compatible...
			int x = ((int) modelToView(firstLetter).getX()) - 1;

			if (prevStartLinePos != x) {
				prevStartLinePos = x;
				repaint();
			}

		} catch (BadLocationException e) {
			// whoops!
		}

		g.setColor(startLineColor);
		int y = 0;
		int height = getHeight();
		while (y < height) {
			g.drawLine(prevStartLinePos, y, prevStartLinePos, y+2);
			y += 10;
		}
		// g.drawLine(prevStartLinePos, 0, prevStartLinePos, getHeight());
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
