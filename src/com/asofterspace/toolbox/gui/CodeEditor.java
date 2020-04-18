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
	private boolean showHorzLine = false;

	private int prevStartLinePos = 0;
	private int prevHorzLinePos = 0;
	private int actualHorzLinePos = 0;

	private Color startLineColor = Color.DARK_GRAY;
	private Color horzLineColor = Color.DARK_GRAY;

	private boolean fancyBackground;


	public CodeEditor() {
		this(false);
	}

	public CodeEditor(boolean fancyBackground) {
		super();

		this.fancyBackground = fancyBackground;

		if (fancyBackground) {
			// we want to use a transparent background!
			setOpaque(false);
		}
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

			g.setColor(startLineColor);
			int y = 0;
			int height = getHeight();
			while (y < height) {
				g.drawLine(prevStartLinePos, y, prevStartLinePos, y+2);
				y += 10;
			}
			// g.drawLine(prevStartLinePos, 0, prevStartLinePos, getHeight());
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

			g.setColor(horzLineColor);
			int x = 0;
			int width = getWidth();
			while (x < width) {
				g.drawLine(x, actualHorzLinePos, x+2, actualHorzLinePos);
				x += 10;
			}
		}

		if (doRepaint) {
			repaint();
		}
	}

	@Override
	protected void paintComponent(Graphics graphics) {

		if (graphics instanceof Graphics2D) {
			Graphics2D graphics2d = (Graphics2D) graphics;
			graphics2d.setRenderingHint(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			if (fancyBackground) {

				Color bgCol = getBackground();
				boolean darkMode = bgCol.getGreen() < 128;

				/*
				// simpler but less fancy background
				int width = getWidth();
				int height = getHeight();
				int step = 0;
				int stepSize = height / 50;
				if (stepSize < 1) {
					stepSize = 1;
				}
				int prevY = 0;
				int y = height / 2;
				graphics2d.setColor(new Color(0, 0, 0));
				graphics2d.fillRect(0, prevY, width, y);
				for (; y < height;) {
					prevY = y;
					y += stepSize;
					if (y > height) {
						y = height;
					}
					g2.setColor(new Color(step / 2, 0, step));
					g2.fillRect(0, prevY, width, y);
					step++;
				}
				*/

				// very fancy background
				int width = getWidth();
				int height = getHeight();
				int stepV = 0;
				int stepH = 0;
				int stepSizeV = height / 100;
				if (stepSizeV < 1) {
					stepSizeV = 1;
				}
				int stepSizeH = width / 50;
				if (stepSizeH < 1) {
					stepSizeH = 1;
				}
				int prevY = 0;
				int y = 0;
				for (; y < height;) {
					prevY = y;
					y += stepSizeV;
					if (y > height) {
						y = height;
					}
					int prevX = width;
					int x = width;
					stepH = 0;
					for (; x > 0;) {
						prevX = x;
						x -= stepSizeH;
						if (x < 0) {
							x = 0;
						}
						int colorStep = stepV - stepH;
						int r = 0;
						int g = 0;
						int b = 0;
						if (darkMode) {
							r = (colorStep * 2) / 3;
							g = 0;
							b = colorStep;
						} else {
							if (colorStep < 0) {
								colorStep = 0;
							}
							r = 255 - colorStep;
							g = 255 - (colorStep * 2);
							b = 255 - ((colorStep * 2) / 3);
						}
						if (r < 0) {r = 0;}
						if (r > 255) {r = 255;}
						if (g < 0) {g = 0;}
						if (g > 255) {g = 255;}
						if (b < 0) {b = 0;}
						if (b > 255) {b = 255;}
						graphics2d.setColor(new Color(r, g, b));
						graphics2d.fillRect(x, prevY, prevX, y);
						stepH++;
					}
					stepV++;
				}
			}
		}

		super.paintComponent(graphics);
	}
}
