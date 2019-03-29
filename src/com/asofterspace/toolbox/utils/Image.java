/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.utils;


/**
 * A simple image containing many, many RGB triplets
 *
 * @author Moya (a softer space), 2019
 */
public class Image {

	private ColorRGB[][] data;

	private int width;

	private int height;


	public Image(int width, int height) {

		this.width = width;

		this.height = height;

		this.data = new ColorRGB[height][width];

		ColorRGB defaultCol = new ColorRGB();

		for (int y = 0; y < getHeight(); y++) {
			for (int x = 0; x < getWidth(); x++) {
				this.data[y][x] = defaultCol;
			}
		}
	}

	public Image(ColorRGB[][] data) {

		this.data = data;

		this.height = data.length;

		if (this.height < 1) {
			this.width = 0;
		} else {
			this.width = data[0].length;
		}
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return width;
	}

	public ColorRGB[][] getData() {
		return data;
	}

	public ColorRGB getPixel(int x, int y) {
		return data[y][x];
	}

	public void setPixel(int x, int y, ColorRGB pix) {
		data[y][x] = pix;
	}

	public String toString() {
		return "Image(" + width + " x " + height + ")";
	}

}
