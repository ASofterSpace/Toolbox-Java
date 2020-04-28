/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.guiImages;

import com.asofterspace.toolbox.gui.CodeEditor;
import com.asofterspace.toolbox.images.Image;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.RenderingHints;


/**
 * This is an even fancier version of our regular code editor, which can display
 * and image as watermark inside of it
 *
 * @author Tom Moya Schiller, moya@asofterspace.com
 */
public class FancyCodeEditor extends CodeEditor {

	private final static long serialVersionUID = 1L;

	private final static int BG_OFFSET = 4;

	private boolean gradientBackground;

	private Image backgroundImage;

	private Image resampledImg;

	private BufferedImage imageToDraw;

	private int lastResampleWidth;


	public FancyCodeEditor() {
		super();
		adjustOpacity();
	}

	public void setGradientBackground(boolean gradientBackground) {
		this.gradientBackground = gradientBackground;
		adjustOpacity();
	}

	public void setBackgroundImage(Image backgroundImage) {
		this.backgroundImage = backgroundImage;
		this.lastResampleWidth = -1;
		adjustOpacity();
	}

	private void adjustOpacity() {

		boolean opaqueWanted = true;

		if (gradientBackground) {
			opaqueWanted = false;
		}

		if (backgroundImage != null) {
			opaqueWanted = false;
		}

		setOpaque(opaqueWanted);
	}

	@Override
	public Dimension getMinimumSize() {
		Dimension result = super.getMinimumSize();
		if (backgroundImage != null) {
			resampleBackgroundIfNeeded();
			result.height += imageToDraw.getHeight() + 8;
		}
		return result;
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension result = super.getPreferredSize();
		if (backgroundImage != null) {
			resampleBackgroundIfNeeded();
			result.height += imageToDraw.getHeight() + 8;
		}
		return result;
	}

	/**
	 * This checks if the background needs to be resampled, assuming that the background
	 * is actually shown... don't call this is backgroundImage is actually null ;)
	 */
	private void resampleBackgroundIfNeeded() {
		int curResampleWidth = getParent().getParent().getWidth() - (2*BG_OFFSET);
		if (curResampleWidth < 0) {
			curResampleWidth = 2*BG_OFFSET;
		}
		if (curResampleWidth != lastResampleWidth) {
			resampledImg = backgroundImage.copy();
			resampledImg.resampleToWidth(curResampleWidth);
			imageToDraw = resampledImg.getAwtImage();
			lastResampleWidth = curResampleWidth;
		}
	}

	@Override
	protected void paintComponent(Graphics graphics) {

		if (graphics instanceof Graphics2D) {
			Graphics2D graphics2d = (Graphics2D) graphics;
			graphics2d.setRenderingHint(
				RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			if (gradientBackground) {

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

			} else {

				// paint flat background
				int width = getWidth();
				int height = getHeight();
				graphics2d.setColor(getBackground());
				graphics2d.fillRect(0, 0, width, height);
			}

			if (backgroundImage != null) {
				resampleBackgroundIfNeeded();
				graphics2d.drawImage(
					imageToDraw,
					BG_OFFSET,
					getHeight() - (imageToDraw.getHeight() + BG_OFFSET),
					null
				);
			}
		}

		super.paintComponent(graphics);
	}
}
