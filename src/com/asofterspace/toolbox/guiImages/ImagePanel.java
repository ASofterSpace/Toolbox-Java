/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.guiImages;

import com.asofterspace.toolbox.images.Image;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;


/**
 * This is a JPanel that, instead of just showing a boring background color,
 * actually shows a fancy image! :D
 */
public class ImagePanel extends JPanel {

	public static final long serialVersionUID = 3458397457249723l;

	private Image image;

	private BufferedImage imageToDraw;

	private Integer ourMinimumHeight = null;

	private int lastWidth = 0;

	private int lastHeight = 0;


	public ImagePanel() {
		super();
	}

	public void setImage(Image img) {
		this.image = img;
		redrawImage();
	}

	private void redrawImage() {
		this.image.redraw();
		this.imageToDraw = image.getAwtImage();
	}

	public void setMinimumHeight(int height) {
		this.ourMinimumHeight = height;
	}

	@Override
	public Dimension getMinimumSize() {
		Dimension result = super.getMinimumSize();
		if (ourMinimumHeight == null) {
			return result;
		}
		result.height = ourMinimumHeight;
		return result;
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension result = super.getPreferredSize();
		if (ourMinimumHeight == null) {
			return result;
		}
		result.height = ourMinimumHeight;
		return result;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		int curWidth = getWidth();
		int curHeight = getHeight();
		if (!((curWidth == lastWidth) && (curHeight == lastHeight))) {
			image.setWidthAndHeight(curWidth, curHeight);
			lastWidth = curWidth;
			lastHeight = curHeight;
			redrawImage();
		}

		g.drawImage(
			imageToDraw,
			0,
			0,
			null
		);
	}

}
