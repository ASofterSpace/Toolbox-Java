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

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
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
		return height;
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

	private int clipHorz(int x) {

		if (x < 0) {
			x = 0;
		}

		if (x >= width) {
			x = width - 1;
		}

		return x;
	}

	private int clipVert(int y) {

		if (y < 0) {
			y = 0;
		}

		if (y >= height) {
			y = height - 1;
		}

		return y;
	}

	/**
	 * Draw another image on top of this one, starting (top left) at
	 * coordinates x and y (respective to this image)
	 */
	public void draw(Image other, int drawAtX, int drawAtY) {

		for (int x = 0; (x < other.width) && (x + drawAtX < width); x++) {
			for (int y = 0; (y < other.height) && (y + drawAtY < height); y++) {
				if ((x + drawAtX >= 0) && (y + drawAtY >= 0)) {
					data[y + drawAtY][x + drawAtX] = other.data[y][x];
				}
			}
		}
	}

	public void drawLine(int startX, int startY, int endX, int endY, ColorRGB lineColor) {

		// ensure we stay in the drawable area
		startX = clipHorz(startX);
		endX = clipHorz(endX);
		startY = clipVert(startY);
		endY = clipVert(endY);

		// ensure we draw from top left to bottom right
		if (startX > endX) {
			int buf = endX;
			endX = startX;
			startX = buf;
		}

		if (startY > endY) {
			int buf = endY;
			endY = startY;
			startY = buf;
		}

		// figure out how far we draw horizontally and vertically
		int lineWidth = endX - startX;
		int lineHeight = endY - startY;

		// actually do the drawing - mainly horizontally or mainly vertically
		if (lineWidth > lineHeight) {
			for (int x = startX; x <= endX; x++) {
				int y = startY + (((x - startX) * lineHeight) / lineWidth);
				data[y][x] = lineColor;
			}
		} else {
			for (int y = startY; y <= endY; y++) {
				int x = startX + (((y - startY) * lineWidth) / lineHeight);
				data[y][x] = lineColor;
			}
		}
	}

	/**
	 * Resample the image to some new size,
	 * applying some nice filters to obtain a smooth result
	 */
	public void resampleTo(int newWidth, int newHeight) {

		// nothing needs to be changed... great, that makes it easier xD
		if ((newWidth == width) && (newHeight == height)) {
			return;
		}

		ColorRGB[][] horzData = new ColorRGB[height][newWidth];

		for (int x = 0; x < newWidth; x++) {
			for (int y = 0; y < height; y++) {
				double newX = (x * width) / ((double) newWidth);
				int loX = (int) Math.floor(newX);
				int hiX = (int) Math.ceil(newX);
				double loAmount = newX - loX;
				horzData[y][x] = ColorRGB.intermix(data[y][loX], data[y][hiX], loAmount);
			}
		}

		ColorRGB[][] fullData = new ColorRGB[newHeight][newWidth];

		for (int x = 0; x < newWidth; x++) {
			for (int y = 0; y < newHeight; y++) {
				double newY = (y * height) / ((double) newHeight);
				int loY = (int) Math.floor(newY);
				int hiY = (int) Math.ceil(newY);
				double loAmount = newY - loY;
				fullData[y][x] = ColorRGB.intermix(horzData[loY][x], horzData[hiY][x], loAmount);
			}
		}

		this.data = fullData;
		this.width = newWidth;
		this.height = newHeight;
	}

	public void resampleToWidth(int newWidth) {
		resampleTo(newWidth, (height * newWidth) / width);
	}

	public void resampleToHeight(int newHeight) {
		resampleTo((width * newHeight) / height, newHeight);
	}

	/**
	 * Resample the image by some amount horizontally and vertically,
	 * applying some nice filters to obtain a smooth result
	 */
	public void resampleBy(double horizontalStretch, double verticalStretch) {

		int newWidth = (int) (width * horizontalStretch);
		int newHeight = (int) (height * verticalStretch);

		resampleTo(newWidth, newHeight);
	}

	/**
	 * Plainly resize the image to some new size
	 */
	public void resizeTo(int newWidth, int newHeight) {

		// nothing needs to be changed... great, that makes it easier xD
		if ((newWidth == width) && (newHeight == height)) {
			return;
		}

		ColorRGB[][] horzData = new ColorRGB[height][newWidth];

		for (int x = 0; x < newWidth; x++) {
			for (int y = 0; y < height; y++) {
				horzData[y][x] = data[y][(x * width) / newWidth];
			}
		}

		ColorRGB[][] fullData = new ColorRGB[newHeight][newWidth];

		for (int x = 0; x < newWidth; x++) {
			for (int y = 0; y < newHeight; y++) {
				fullData[y][x] = horzData[(y * height) / newHeight][x];
			}
		}

		this.data = fullData;
		this.width = newWidth;
		this.height = newHeight;
	}

	public void resizeToWidth(int newWidth) {
		resizeTo(newWidth, (height * newWidth) / width);
	}

	public void resizeToHeight(int newHeight) {
		resizeTo((width * newHeight) / height, newHeight);
	}

	/**
	 * Plainly resize the image by some amount horizontally and vertically
	 */
	public void resizeBy(double horizontalStretch, double verticalStretch) {

		int newWidth = (int) (width * horizontalStretch);
		int newHeight = (int) (height * verticalStretch);

		resizeTo(newWidth, newHeight);
	}

	public void rotateLeft() {

		int newHeight = width;
		int newWidth = height;

		ColorRGB[][] rotatedData = new ColorRGB[newHeight][newWidth];

		for (int x = 0; x < newWidth; x++) {
			for (int y = 0; y < newHeight; y++) {
				rotatedData[y][x] = data[x][width - y - 1];
			}
		}

		this.data = rotatedData;
	}

	public void rotateRight() {

		int newHeight = width;
		int newWidth = height;

		ColorRGB[][] rotatedData = new ColorRGB[newHeight][newWidth];

		for (int x = 0; x < newWidth; x++) {
			for (int y = 0; y < newHeight; y++) {
				rotatedData[y][x] = data[height - x - 1][y];
			}
		}

		this.data = rotatedData;
	}

	/**
	 * Add the amount of pixels to the top, right, bottom and left of the image,
	 * filling the new space with the fillWith color
	 * (negative values are allowed, in that case the image will shrink)
	 */
	public void expand(int top, int right, int bottom, int left, ColorRGB fillWith) {

		int newwidth = width + right + left;

		int newheight = height + top + bottom;

		if (newheight < 0) {
			newheight = 0;
		}

		if (newwidth < 0) {
			newwidth = 0;
		}

		ColorRGB[][] newdata = new ColorRGB[newheight][newwidth];

		if ((newheight > 0) && (newwidth > 0)) {

			for (int y = 0; y < newheight; y++) {
				for (int x = 0; x < newwidth; x++) {
					newdata[y][x] = fillWith;
				}
			}

			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					if ((top+y >= 0) && (left+x >= 0) && (top+y < newheight) && (left+x < newwidth)) {
						newdata[top+y][left+x] = data[y][x];
					}
				}
			}
		}

		this.width = newwidth;

		this.height = newheight;

		this.data = newdata;
	}

	public void expandTop(int howMuch, ColorRGB fillWith) {
		expand(howMuch, 0, 0, 0, fillWith);
	}

	public void expandRight(int howMuch, ColorRGB fillWith) {
		expand(0, howMuch, 0, 0, fillWith);
	}

	public void expandBottom(int howMuch, ColorRGB fillWith) {
		expand(0, 0, howMuch, 0, fillWith);
	}

	public void expandLeft(int howMuch, ColorRGB fillWith) {
		expand(0, 0, 0, howMuch, fillWith);
	}

	@Override
	public int hashCode() {

		if (width < 1) {
			return 0;
		}

		if (height < 1) {
			return 0;
		}

		// this is not crypto, we just want a quick and easy way to group possibly-same images
		// into same buckets, while putting definitely-different images into different buckets...
		// and checking whether the top left pixel is the same works like a charm for that :)
		return data[0][0].hashCode();
	}

	public boolean equals(Object other) {

		if (other == null) {
			return false;
		}

		if (!(other instanceof Image)) {
			return false;
		}

		Image otherImage = (Image) other;

		if (getWidth() != otherImage.getWidth()) {
			return false;
		}

		if (getHeight() != otherImage.getHeight()) {
			return false;
		}

		ColorRGB[][] otherData = otherImage.getData();

		for (int y = 0; y < getHeight(); y++) {
			for (int x = 0; x < getWidth(); x++) {
				if (!this.data[y][x].fastEquals(otherData[y][x])) {
					return false;
				}
			}
		}

		return true;
	}

	public String toString() {
		return "Image(" + width + " x " + height + ")";
	}

}
