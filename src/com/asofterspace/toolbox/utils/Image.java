/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.utils;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;


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

	public BufferedImage getAwtImage() {

		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		drawToAwtImage(result, 0, 0);

		return result;
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
	 * Specify one of left and right, and one of top and bottom - the other one will be chosen automatically
	 */
	public void drawText(String text, Integer top, Integer right, Integer bottom, Integer left) {

		drawText(text, top, right, bottom, left, null, null, null);
	}

	/**
	 * Specify one of left and right, and one of top and bottom - the other one will be chosen automatically
	 */
	public void drawText(String text, Integer top, Integer right, Integer bottom, Integer left, String fontName, Integer fontSize, Boolean useAntiAliasing) {

		// prepare font settings for drawing the text
		if (fontName == null) {
			fontName = "Arial";
		}

		if (fontSize == null) {
			fontSize = 15;
		}

		if (useAntiAliasing == null) {
			useAntiAliasing = true;
		}

		if (useAntiAliasing) {
			fontSize *= 2;
		}

		Font font = new Font(fontName, Font.PLAIN, fontSize);

		Canvas c = new Canvas();
		FontMetrics metrics = c.getFontMetrics(font);

		// draw text onto buffered image
		int textWidth = metrics.stringWidth(text);
		int textHeight = metrics.getMaxAscent();

		BufferedImage bufImg = new BufferedImage(textWidth, textHeight, BufferedImage.TYPE_INT_ARGB);

		Graphics2D graphics = bufImg.createGraphics();
		graphics.setFont(font);
		graphics.setColor(Color.black);
		graphics.drawString(text, 0, textHeight);
		graphics.dispose();

		// copy image of drawn text from buffered image onto our image
		if (useAntiAliasing) {
			Image intermediate = new Image(textWidth, textHeight);
			intermediate.drawAwtImage(bufImg, 0, 0);
			intermediate.resampleTo((textWidth / 2) + (textWidth % 2), (textHeight / 2) + (textHeight % 2));
			if (left == null) {
				left = right - intermediate.getWidth();
			}
			if (top == null) {
				top = bottom - intermediate.getHeight();
			}
			draw(intermediate, left, top);
		} else {
			if (left == null) {
				left = right - textWidth;
			}
			if (top == null) {
				top = bottom - textHeight;
			}
			drawAwtImage(bufImg, left, top);
		}
	}

	/**
	 * Draws an AWT image on our image
	 */
	public void drawAwtImage(BufferedImage javaImg, int left, int top) {

		int bufWidth = javaImg.getWidth();
		int bufHeight = javaImg.getHeight();

		for (int y = 0; (y < bufHeight) && (y + top < height); y++) {
			for (int x = 0; (x < bufWidth) && (x + left < width); x++) {
				int rgb = javaImg.getRGB(x, y);
				int a = (rgb >> 24) & 0xFF;
				int r = (rgb >> 16) & 0xFF;
				int g = (rgb >> 8) & 0xFF;
				int b = (rgb) & 0xFF;
				data[y+top][x+left] = new ColorRGB(r, g, b, a);
			}
		}
	}

	/**
	 * Draws our image on an AWT image
	 */
	public void drawToAwtImage(BufferedImage javaImg, int left, int top) {

		int bufWidth = javaImg.getWidth();
		int bufHeight = javaImg.getHeight();

		for (int y = 0; (y < bufHeight) && (y + top < height); y++) {
			for (int x = 0; (x < bufWidth) && (x + left < width); x++) {
				javaImg.setRGB(x+left, y+top, data[y][x].getRGB());
			}
		}
	}

	/**
	 * We pre-cut (via the expand function) the image such that
	 * width / height = newWidth / newHeight,
	 * meaning the old aspect ratio is the same as the new one
	 * (and we cannot change the new one, so we crop the old one)
	 */
	private void preExpandAspectRatioKeepingly(int newWidth, int newHeight) {

		if (height < 1) {
			return;
		}
		if (width < 1) {
			return;
		}

		// we want to achieve widthRatio == heightRatio
		int widthRatio = newWidth * height;
		int heightRatio = newHeight * width;

		if (widthRatio == heightRatio) {
			// perfect, we need to do nothing!
			return;
		}

		if (widthRatio > heightRatio) {

			// widthRatio is above heightRatio; inside widthRatio, we can only change height,
			// not newWidth, and under the assumption that we change things so that
			// widthRatio == heightRatio
			// we get
			// newWidth * height = newHeight * width
			// so
			// height = (newHeight * width) / newWidth
			// where the height is the height that we want to produce now...
			double cutOffHeight = (newHeight * width) / newWidth;
			// ... by cutting something off :)
			double cutOffAmount = (height - cutOffHeight) / 2;
			// System.out.println("A width: " + width + ", height: " + height + ", newWidth: " + newWidth + ", newHeight: " + newHeight + ", cutOff: " + cutOffAmount);
			expandBy(- (int) Math.floor(cutOffAmount), 0, - (int) Math.ceil(cutOffAmount), 0, ColorRGB.WHITE);
		} else {
			// cut off left and right
			double cutOffWidth = (newWidth * height) / newHeight;
			double cutOffAmount = (width - cutOffWidth) / 2;
			// System.out.println("B width: " + width + ", height: " + height + ", newWidth: " + newWidth + ", newHeight: " + newHeight + ", cutOff: " + cutOffAmount);
			expandBy(0, - (int) Math.floor(cutOffAmount), 0, - (int) Math.ceil(cutOffAmount), ColorRGB.WHITE);
		}
	}

	/**
	 * Resample the image to some new size,
	 * applying some nice filters to obtain a smooth result,
	 * optionally keeping the current aspect ratio
	 * (so discarding what is left over)
	 */
	public void resampleTo(int newWidth, int newHeight, Boolean keepAspectRatio) {

		if (keepAspectRatio) {
			preExpandAspectRatioKeepingly(newWidth, newHeight);
		}

		resampleTo(newWidth, newHeight);
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
				double hiAmount = newX - loX;
				if (hiX < width) {
					horzData[y][x] = ColorRGB.intermix(data[y][loX], data[y][hiX], 1 - hiAmount);
				} else {
					horzData[y][x] = data[y][loX];
				}
			}
		}

		ColorRGB[][] fullData = new ColorRGB[newHeight][newWidth];

		for (int x = 0; x < newWidth; x++) {
			for (int y = 0; y < newHeight; y++) {
				double newY = (y * height) / ((double) newHeight);
				int loY = (int) Math.floor(newY);
				int hiY = (int) Math.ceil(newY);
				double hiAmount = newY - loY;
				if (hiY < height) {
					fullData[y][x] = ColorRGB.intermix(horzData[loY][x], horzData[hiY][x], 1 - hiAmount);
				} else {
					fullData[y][x] = horzData[loY][x];
				}
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
		this.width = newWidth;
		this.height = newHeight;
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
		this.width = newWidth;
		this.height = newHeight;
	}

	/**
	 * Add the amount of pixels to the top, right, bottom and left of the image,
	 * filling the new space with the fillWith color
	 * (negative values are allowed, in that case the image will shrink)
	 */
	public void expandBy(int top, int right, int bottom, int left, ColorRGB fillWith) {

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

	public void expandTopBy(int howMuch, ColorRGB fillWith) {
		expandBy(howMuch, 0, 0, 0, fillWith);
	}

	public void expandRightBy(int howMuch, ColorRGB fillWith) {
		expandBy(0, howMuch, 0, 0, fillWith);
	}

	public void expandBottomBy(int howMuch, ColorRGB fillWith) {
		expandBy(0, 0, howMuch, 0, fillWith);
	}

	public void expandLeftBy(int howMuch, ColorRGB fillWith) {
		expandBy(0, 0, 0, howMuch, fillWith);
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
