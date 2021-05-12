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
	private int proposedTokenSelection = 0;
	private int tokenSelStart = 0;


	public CodeEditor() {
		super();

		// we want to definitely draw the background, so we do not draw opaque, such that
		// we can draw the background ourselves, including making lines gray that have been
		// changed since startup
		// (if setOpaque(true) is called, our background is just overwritten with transparency)
		setOpaque(false);
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
	protected void paintComponent(Graphics g) {

		paintBackground(g);

		super.paintComponent(g);

		paintForeground(g);
	}

	protected void paintBackground(Graphics g) {

		if (g instanceof Graphics2D) {
			Graphics2D graphics2d = (Graphics2D) g;
			graphics2d.setRenderingHint(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			int width = getWidth();
			int height = getHeight();
			graphics2d.setColor(getBackground());
			graphics2d.fillRect(0, 0, width, height);

			// TODO:
			// in here, access the latest diff between the original code and the current code,
			// and draw the changed lines in gray
		}
	}

	// we get deprecation warnings for modelToView, which we are not super interested in at this point...
	@SuppressWarnings( "deprecation" )
	protected void paintForeground(Graphics g) {

		if ((proposedTokens != null) && proposedTokens.size() > 0) {
			try {
				if (proposedTokenSelection >= proposedTokens.size()) {
					proposedTokenSelection = 0;
				}
				Rectangle r = modelToView(tokenSelStart);
				FontMetrics fm = g.getFontMetrics();
				int asc = fm.getAscent();
				int totAsc = asc;
				int i = 0;
				for (String token : proposedTokens) {
					// TODO :: adjust colors based on scheme!
					g.setColor(Color.BLACK);
					Rectangle2D r2 = fm.getStringBounds(token, g);
					g.fillRect(r.x, r.y + totAsc - asc, 2 + (int) r2.getWidth(), 4 + (int) r2.getHeight());
					if (i == proposedTokenSelection) {
						g.setColor(Color.YELLOW);
					} else {
						g.setColor(Color.LIGHT_GRAY);
					}
					g.drawString(token, r.x, r.y + totAsc);
					totAsc += asc + 2;
					i++;
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

			int x = getLineStartInPxFromCursorPosContextAware(pos, len, text) - 1;

			if (prevStartLinePos != x) {
				prevStartLinePos = x;
				doRepaint = true;
			}
		}

		if (showHorzLine) {

			g.setColor(startLineColor);
			try {
				// in the future, modelToView2D is used instead, but we want to be backwards compatible...
				int y = ((int) modelToView(pos).getY());

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

	public void setProposedTokens(List<String> proposedTokens) {
		this.proposedTokens = proposedTokens;
	}

	public List<String> getProposedTokens() {
		return proposedTokens;
	}

	public int getProposedTokenSelection() {
		return proposedTokenSelection;
	}

	public void setProposedTokenSelection(int proposedTokenSelection) {
		this.proposedTokenSelection = proposedTokenSelection;
	}

	public void setTokenSelStart(int tokenSelStart) {
		this.tokenSelStart = tokenSelStart;
	}

	public int getTokenSelStart() {
		return tokenSelStart;
	}

	/**
	 * Returns the indentation in px of a particular line, based on either its own indentation
	 * or, if it is empty, based on the indentation of the lines around it
	 */
	private int getLineStartInPxFromCursorPosContextAware(int pos, int len, String text) {

		// get the line start position at the current cursor pos
		Integer textPos = getLineStartInPxFromCursorPos(pos, len, text);

		// the line is completely empty?
		if (textPos == null) {

			// then let's go above and below the current line...
			// (getting 0 in case of above or below being out of text bounds)
			int posBefore = 1;
			int posAfter = 1;
			Integer textPosBefore = getLineStartInPxFromCursorPos(pos - posBefore, len, text);
			Integer textPosAfter = getLineStartInPxFromCursorPos(pos + posAfter, len, text);
			while (textPosBefore == null) {
				posBefore++;
				textPosBefore = getLineStartInPxFromCursorPos(pos - posBefore, len, text);
			}
			while (textPosAfter == null) {
				posAfter++;
				textPosAfter = getLineStartInPxFromCursorPos(pos + posAfter, len, text);
			}
			// ... and take the maximum indentation of the two!
			textPos = Math.max(textPosBefore, textPosAfter);
		}

		return textPos;
	}

	/**
	 * Returns the indentation in px of a particular line itself
	 */
	@SuppressWarnings("deprecation")
	private Integer getLineStartInPxFromCursorPos(int pos, int len, String text) {

		char chr = ' ';

		// if we are asked for a line out of the bounds of the text, report 0 as indentation in px
		if (pos < 0) {
			return 0;
		}
		if (pos > len) {
			return 0;
		}

		// start at the current cursor position, and go forward, until
		// anything else than space or tab is encountered
		while (pos < len) {
			chr = text.charAt(pos);
			if ((chr != ' ') && (chr != '\t')) {
				break;
			}
			pos++;
		}

		// now that we are at the right-most location in this line at which it makes sense that the
		// indentation could be, slowly go back again...
		int firstLetter = pos;

		// go one back if we are at the end of the line
		if (chr == '\n') {
			pos--;
		}

		// go back inside the text, if we have shot too far out
		while (pos >= len) {
			pos--;
		}

		// go back until we encounter the start of this line, keeping track of
		// the left-most non-space, non-tab character
		boolean lineIsEmpty = true;
		while (pos > -1) {
			chr = text.charAt(pos);
			if (chr == '\n') {
				if (lineIsEmpty) {
					return null;
				}
				break;
			}
			if ((chr != ' ') && (chr != '\t')) {
				firstLetter = pos;
			}
			pos--;
			lineIsEmpty = false;
		}

		try {
			// in the future, modelToView2D is used instead, but we want to be backwards compatible...
			return ((int) modelToView(firstLetter).getX());

		} catch (BadLocationException e) {
			// whoops!
			return 0;
		}
	}
}
