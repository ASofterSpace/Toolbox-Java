/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.List;

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
	private boolean showHorzLine = false;

	private int prevStartLinePos = 0;
	private int prevHorzLinePos = 0;
	private int actualHorzLinePos = 0;

	private Color startLineColor = Color.DARK_GRAY;
	private Color horzLineColor = Color.DARK_GRAY;

	private List<String> proposedTokens;


	public CodeEditor() {
		super();
	}

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

	/**
	 * Enable or disable the horz line, which is a horizontal dotted line that indicates
	 * the vertical location below the current line
	 */
	public void enableHorzLine(boolean doEnable) {
		this.showHorzLine = doEnable;
	}

	public void setHorzLineColor(Color horzLineColor) {
		this.horzLineColor = horzLineColor;
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
	@SuppressWarnings("deprecation")
	public void paint(Graphics g) {

		super.paint(g);

		if ((proposedTokens != null) && proposedTokens.size() > 0) {
			try {
				Rectangle r = modelToView(getSelectionStart());
				FontMetrics fm = g.getFontMetrics();
				int asc = fm.getAscent();
				int totAsc = asc;
				for (String token : proposedTokens) {
					// TODO :: adjust colors based on scheme!
					g.setColor(Color.BLACK);
					Rectangle2D r2 = fm.getStringBounds(token, g);
					g.fillRect(r.x, r.y + totAsc - asc, 2 + (int) r2.getWidth(), 4 + (int) r2.getHeight());
					g.setColor(Color.LIGHT_GRAY);
					g.drawString(token, r.x, r.y + totAsc);
					totAsc += asc + 2;
				}
			} catch (BadLocationException e) {
				// whoops!
				System.err.println("BadLocationException in CodeEditor while showing proposed tokens!");
			}
		}

		if ((!showStartLine) && (!showHorzLine)) {
			return;
		}

		// only get these once, not once per line
		String text = getText();
		int len = text.length();
		int pos = getSelectionStart();
		int firstLetter = pos;
		boolean doRepaint = false;

		if (showStartLine) {
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
					doRepaint = true;
				}

			} catch (BadLocationException e) {
				// whoops!
			}
		}

		if (showHorzLine) {

			g.setColor(startLineColor);
			try {
				// in the future, modelToView2D is used instead, but we want to be backwards compatible...
				int y = ((int) modelToView(firstLetter).getY());

				if (prevHorzLinePos != y) {
					prevHorzLinePos = y;
					actualHorzLinePos = prevHorzLinePos + getFontMetrics(getFont()).getHeight();
					doRepaint = true;
				}

			} catch (BadLocationException e) {
				// whoops!
			}
		}

		// if the line positions changed...
		if (doRepaint) {

			// ... call repaint! (and then we will end up here again, so no need to actually draw
			// anything right now...)
			repaint();

		} else {

			// if the line positions did not change, then actually paint:
			// first the vertical line...
			g.setColor(startLineColor);
			int y = 0;
			int height = getHeight();
			while (y < height) {
				g.drawLine(prevStartLinePos, y, prevStartLinePos, y+2);
				y += 10;
			}
			// we could also just draw a plain vertical line instead
			// g.drawLine(prevStartLinePos, 0, prevStartLinePos, getHeight());

			// ... and now the horizontal line
			g.setColor(horzLineColor);
			int x = 0;
			int width = getWidth();
			while (x < width) {
				g.drawLine(x, actualHorzLinePos, x+2, actualHorzLinePos);
				x += 10;
			}
		}
	}

	@Override
	protected void paintComponent(Graphics graphics) {

		if (graphics instanceof Graphics2D) {
			Graphics2D graphics2d = (Graphics2D) graphics;
			graphics2d.setRenderingHint(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		}

		super.paintComponent(graphics);
	}

	public void setProposedTokens(List<String> proposedTokens) {
		this.proposedTokens = proposedTokens;
	}

}
