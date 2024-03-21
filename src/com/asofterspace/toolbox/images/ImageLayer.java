/**
 * Unlicensed code created by A Softer Space, 2024
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.images;


/**
 * A single layer in a multi-layered image
 *
 * @author Moya, 2024
 */
public abstract class ImageLayer  {

	int offsetX;

	int offsetY;


	public ImageLayer(int offsetX, int offsetY) {
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}

	public abstract void drawOnto(Image img);

	public abstract ImageLayer copy();

	public int getOffsetX() {
		return offsetX;
	}

	public void setOffsetX(int offsetX) {
		this.offsetX = offsetX;
	}

	public int getOffsetY() {
		return offsetY;
	}

	public void setOffsetY(int offsetY) {
		this.offsetY = offsetY;
	}

	public abstract int getWidth();
	public abstract int getHeight();

	public void move(int x, int y) {
		offsetX += x;
		offsetY += y;
	}

	public void moveTo(int x, int y) {
		offsetX = x;
		offsetY = y;
	}

}
