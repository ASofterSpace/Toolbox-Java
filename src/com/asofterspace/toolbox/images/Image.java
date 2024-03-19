/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.images;

import com.asofterspace.toolbox.utils.MathUtils;
import com.asofterspace.toolbox.utils.Pair;
import com.asofterspace.toolbox.utils.SortUtils;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.Toolkit;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


/**
 * A simple image containing many, many RGB triplets
 *
 * @author Moya (a softer space), 2019
 */
public class Image {

	private ColorRGBA[][] data;

	private int width;

	private int height;

	private int lineWidth = 1;


	public Image(int width, int height) {
		init(width, height);
	}

	/**
	 * Creates an image of the given dimensions, pre-filled with the given background color
	 */
	public Image(int width, int height, ColorRGBA backgroundColor) {
		initWithoutClear(width, height);
		clear(backgroundColor);
	}

	public Image() {
		init(8, 8);
	}

	/**
	 * Internal constructor; only called by copy() and static factory methods,
	 * as we want to only allow the outside world to actually create cleared images
	 */
	private Image(int width, int height, boolean doClear) {
		if (doClear) {
			init(width, height);
		} else {
			initWithoutClear(width, height);
		}
	}

	public Image(ColorRGBA[][] data) {

		this.data = data;

		this.height = data.length;

		if (this.height < 1) {
			this.width = 0;
		} else {
			this.width = data[0].length;
		}
	}

	protected void init(int width, int height) {

		initWithoutClear(width, height);

		clear();
	}

	protected void initWithoutClear(int width, int height) {

		this.height = height;
		this.width = width;

		this.data = new ColorRGBA[height][width];
	}

	/**
	 * Copy this image, giving back a new image that contains the same pixel values
	 * but when modified does not modify this one
	 */
	public Image copy() {

		Image result = new Image(width, height, false);

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				result.data[y][x] = this.data[y][x];
			}
		}

		return result;
	}

	/**
	 * Copy this image, giving back a new image that contains the same pixel values
	 * but with a certain part cut out
	 */
	public Image copy(int top, int right, int bottom, int left) {

		int newWidth = 1 + right - left;
		int newHeight = 1 + bottom - top;

		if ((newWidth < 0) || (newHeight < 0)) {
			return new Image(0, 0, false);
		}

		Image result = new Image(newWidth, newHeight, false);

		for (int y = top; y <= bottom; y++) {
			for (int x = left; x <= right; x++) {
				result.data[y-top][x-left] = this.data[y][x];
			}
		}

		return result;
	}

	/**
	 * Copy to clipboard, for paste from clipboard see createFromClipboard()
	 */
	public void copyToClipboard() {
		ClipboardTransferImage clipContent = new ClipboardTransferImage(this);
		Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
		clip.setContents(clipContent, null);
	}

	/**
	 * Replaces some same pixel values with single object instances
	 * (replacing all would take a LONG time in a picture that has many different colors,
	 * so instead we sample 256 values, check if any occur more than once, and those we
	 * replace in the entire image)
	 */
	public void minify() {

		/*
		// simplest approach, but too slow:

		List<ColorRGBA> encountered = new ArrayList<>();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				boolean found = false;
				for (ColorRGBA col : encountered) {
					if (data[y][x].equals(col)) {
						data[y][x] = col;
						found = true;
						break;
					}
				}
				if (!found) {
					encountered.add(data[y][x]);
				}
			}
		}
		*/

		Map<ColorRGBA, Integer> sample = new HashMap<>();
		Random rand = new Random();
		for (int i = 0; i < 256; i++) {
			ColorRGBA sampleCol = data[rand.nextInt(height)][rand.nextInt(width)];
			Integer soFar = sample.get(sampleCol);
			if (soFar == null) {
				sample.put(sampleCol, 1);
			} else {
				sample.put(sampleCol, soFar + 1);
			}
		}

		List<ColorRGBA> encountered = new ArrayList<>();
		for (Map.Entry<ColorRGBA, Integer> sampleCol : sample.entrySet()) {
			if (sampleCol.getValue() > 1) {
				encountered.add(sampleCol.getKey());
			}
		}

		// if we will not be able to minimize anything then there is no need to loop over the entire image...
		if (encountered.size() < 1) {
			return;
		}

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				for (ColorRGBA col : encountered) {
					if (data[y][x].equals(col)) {
						data[y][x] = col;
						break;
					}
				}
			}
		}
	}

	public void clear() {

		clear(new ColorRGBA());
	}

	public void clear(ColorRGBA defaultCol) {

		/*
		// naive implementation:

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				this.data[y][x] = defaultCol;
			}
		}
		*/

		// quicker:

		if ((height < 1) || (width < 1)) {
			return;
		}

		int y = 0;
		for (int x = 0; x < width; x++) {
			this.data[y][x] = defaultCol;
		}

		for (y = 1; y < height; y++) {
			System.arraycopy(this.data[0], 0, this.data[y], 0, width);
		}
	}

	public void setWidthAndHeight(int newWidth, int newHeight) {
		init(newWidth, newHeight);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public ColorRGBA[][] getData() {
		return data;
	}

	public ColorRGBA[][] getDataSafely() {

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (data[y][x] == null) {
					System.err.println("Image.getDataSafely() called, but " + x + "x" + y + " is null! Will be set to black...");
					data[y][x] = ColorRGBA.BLACK;
				}
			}
		}

		return data;
	}

	public BufferedImage getAwtImage() {

		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		drawToAwtImage(result, 0, 0);

		return result;
	}

	public ColorRGBA getPixel(int x, int y) {
		return data[y][x];
	}

	public ColorRGBA getPixelSafely(int x, int y) {
		if ((x < 0) || (x >= width) || (y < 0) || (y >= height)) {
			return null;
		}
		return data[y][x];
	}

	public void setPixel(int x, int y, ColorRGBA pix) {
		data[y][x] = pix;
	}

	public void setPixelSafely(int x, int y, ColorRGBA pix) {
		if ((x < 0) || (x >= width) || (y < 0) || (y >= height)) {
			return;
		}
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

		/*
		// naive implementation:

		for (int x = 0; (x < other.width) && (x + drawAtX < width); x++) {
			for (int y = 0; (y < other.height) && (y + drawAtY < height); y++) {
				if ((x + drawAtX >= 0) && (y + drawAtY >= 0)) {
					data[y + drawAtY][x + drawAtX] = other.data[y][x];
				}
			}
		}
		*/

		// quicker:

		for (int y = 0; (y < other.height) && (y + drawAtY < height); y++) {
			if (y + drawAtY >= 0) {
				int drawWidth = other.width;
				if (drawAtX + drawWidth > width) {
					drawWidth = width - drawAtX;
				}
				System.arraycopy(other.data[y], 0, this.data[y + drawAtY], drawAtX, drawWidth);
			}
		}
	}

	/**
	 * Draw another image on top of this one, starting (top left) at
	 * coordinates x and y (respective to this image), but ignore the
	 * transparent color in the other image
	 */
	public void draw(Image other, int drawAtX, int drawAtY, ColorRGBA transparentColor) {

		for (int x = 0; (x < other.width) && (x + drawAtX < width); x++) {
			for (int y = 0; (y < other.height) && (y + drawAtY < height); y++) {
				if ((x + drawAtX >= 0) && (y + drawAtY >= 0)) {
					if (!transparentColor.fastEquals(other.data[y][x])) {
						data[y + drawAtY][x + drawAtX] = other.data[y][x];
					}
				}
			}
		}
	}

	public void drawLine(int startX, int startY, int endX, int endY, ColorRGBA lineColor) {
		drawLine(startX, startY, endX, endY, lineColor, false);
	}

	public void drawDottedLine(int startX, int startY, int endX, int endY, ColorRGBA lineColor) {
		drawLine(startX, startY, endX, endY, lineColor, true);
	}

	private void drawLine(int startX, int startY, int endX, int endY, ColorRGBA lineColor, boolean dotted) {

		boolean invertDirection = false;

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
			invertDirection = !invertDirection;
		}

		if (startY > endY) {
			int buf = endY;
			endY = startY;
			startY = buf;
			invertDirection = !invertDirection;
		}

		// figure out how far we draw horizontally and vertically
		int lineWidth = endX - startX;
		int lineHeight = endY - startY;

		// no line drawn at all (it is just one pixel?) - well okay!
		if ((lineWidth < 1) && (lineHeight < 1)) {
			drawLinePoint(startX, startY, lineColor);
			return;
		}

		// actually do the drawing - mainly horizontally or mainly vertically
		if (lineWidth > lineHeight) {
			for (int x = startX; x <= endX; x++) {
				if (dotted) {
					if ((x % 6 == 3) || (x % 6 == 4) || (x % 6 == 5)) {
						continue;
					}
				}
				int y;
				if (invertDirection) {
					y = startY + MathUtils.divideInts((endX - x) * lineHeight, lineWidth);
				} else {
					y = startY + MathUtils.divideInts((x - startX) * lineHeight, lineWidth);
				}
				drawLinePoint(x, y, lineColor);
			}
		} else {
			for (int y = startY; y <= endY; y++) {
				if (dotted) {
					if ((y % 6 == 3) || (y % 6 == 4) || (y % 6 == 5)) {
						continue;
					}
				}
				int x;
				if (invertDirection) {
					x = startX + MathUtils.divideInts((endY - y) * lineWidth, lineHeight);
				} else {
					x = startX + MathUtils.divideInts((y - startY) * lineWidth, lineHeight);
				}
				drawLinePoint(x, y, lineColor);
			}
		}
	}

	private void drawLinePoint(int x, int y, ColorRGBA color) {
		setPixelSafely(x, y, color);
		if (lineWidth > 1) {
			setPixelSafely(x+1, y, color);
			setPixelSafely(x+1, y+1, color);
			setPixelSafely(x, y+1, color);
		}
		if (lineWidth > 2) {
			setPixelSafely(x-1, y+1, color);
			setPixelSafely(x-1, y, color);
			setPixelSafely(x-1, y-1, color);
			setPixelSafely(x, y-1, color);
			setPixelSafely(x+1, y-1, color);
		}
	}

	public void drawRectangle(int startX, int startY, int endX, int endY, ColorRGBA rectColor) {
		if (startX < 0) {
			startX = 0;
		}
		if (startX >= width) {
			startX = width - 1;
		}
		if (endX < 0) {
			endX = 0;
		}
		if (endX >= width) {
			endX = width - 1;
		}
		if (startY < 0) {
			startY = 0;
		}
		if (startY >= height) {
			startY = height - 1;
		}
		if (endY < 0) {
			endY = 0;
		}
		if (endY >= height) {
			endY = height - 1;
		}

		for (int x = startX; x <= endX; x++) {
			for (int y = startY; y <= endY; y++) {
				data[y][x] = rectColor;
			}
		}
	}

	public void drawRectangleWithTransparency(int startX, int startY, int endX, int endY, ColorRGBA rectColor) {
		if (!rectColor.hasTransparency()) {
			drawRectangle(startX, startY, endX, endY, rectColor);
		}

		if (startX < 0) {
			startX = 0;
		}
		if (startX >= width) {
			startX = width - 1;
		}
		if (endX < 0) {
			endX = 0;
		}
		if (endX >= width) {
			endX = width - 1;
		}
		if (startY < 0) {
			startY = 0;
		}
		if (startY >= height) {
			startY = height - 1;
		}
		if (endY < 0) {
			endY = 0;
		}
		if (endY >= height) {
			endY = height - 1;
		}

		for (int x = startX; x <= endX; x++) {
			for (int y = startY; y <= endY; y++) {
				data[y][x] = rectColor.drawTransparentlyOnto(data[y][x]);
			}
		}
	}

	public void drawDiamond(int startX, int startY, int endX, int endY, ColorRGBA rectColor) {
		if (startX < 0) {
			startX = 0;
		}
		if (startX >= width) {
			startX = width - 1;
		}
		if (endX < 0) {
			endX = 0;
		}
		if (endX >= width) {
			endX = width - 1;
		}
		if (startY < 0) {
			startY = 0;
		}
		if (startY >= height) {
			startY = height - 1;
		}
		if (endY < 0) {
			endY = 0;
		}
		if (endY >= height) {
			endY = height - 1;
		}

		int midX = startX + ((endX - startX) / 2);
		int midY = startY + ((endY - startY) / 2);
		for (int x = startX; x <= endX; x++) {
			for (int y = startY; y <= endY; y++) {
				double xperc = 0;
				if (x < midX) {
					xperc = (x - startX) / (1.0 * (midX - startX));
				} else {
					xperc = (endX - x) / (1.0 * (endX - midX));
				}
				double yperc = 0;
				if (y < midY) {
					yperc = (y - startY) / (1.0 * (midY - startY));
				} else {
					yperc = (endY - y) / (1.0 * (endY - midY));
				}
				if (yperc + xperc > 1) {
					data[y][x] = rectColor;
				}
			}
		}
	}

	/**
	 * Specify one of left and right, and one of top and bottom - the other one will be chosen automatically
	 * (However, all four are absolute values starting from top left - so if you have a width of 100, and want
	 * a distance to the right side of 10, you have to put right 90, not right 10!)
	 */
	public void drawText(String text, Integer top, Integer right, Integer bottom, Integer left) {

		drawText(text, top, right, bottom, left, null, null, null);
	}

	public void drawText(String text, Integer top, Integer right, Integer bottom, Integer left, String fontName, Integer fontSize, Boolean useAntiAliasing) {

		drawText(text, top, right, bottom, left, fontName, fontSize, useAntiAliasing, null);
	}

	public void drawText(String text, Integer top, Integer right, Integer bottom, Integer left, String fontName, Integer fontSize, Boolean useAntiAliasing, ColorRGBA textColor) {

		drawText(text, top, right, bottom, left, fontName, fontSize, useAntiAliasing, textColor, null);
	}

	public void drawText(String text, Integer top, Integer right, Integer bottom, Integer left, String fontName, Integer fontSize, Boolean useAntiAliasing, ColorRGBA textColor, ColorRGBA backgroundColor) {

		drawTextOnto(text, top, right, bottom, left, fontName, fontSize, useAntiAliasing, textColor, null, backgroundColor);
	}

	public static int getTextHeight(String fontName, int fontSize) {

		Font font = new Font(fontName, Font.PLAIN, fontSize);

		Canvas c = new Canvas();
		FontMetrics metrics = c.getFontMetrics(font);

		return metrics.getHeight();
	}

	/**
	 * Draw text at top and right (bottom and left can be left out;
	 * if they are set to the same values as top/right, the text is centered)
	 */
	private void drawTextOnto(String text, Integer top, Integer right, Integer bottom, Integer left, String fontName, Integer fontSize, Boolean useAntiAliasing, ColorRGBA textColor, Image targetImage, ColorRGBA backgroundColor) {

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
		int textHeight = metrics.getHeight();

		if (targetImage != null) {
			if (useAntiAliasing) {
				targetImage.setWidthAndHeight(textWidth / 2, textHeight / 2);
			} else {
				targetImage.setWidthAndHeight(textWidth, textHeight);
			}
			targetImage.clear(backgroundColor);
		}

		BufferedImage bufImg = new BufferedImage(textWidth, textHeight, BufferedImage.TYPE_INT_ARGB);

		Graphics2D graphics = bufImg.createGraphics();
		graphics.setFont(font);
		if (backgroundColor != null) {
			graphics.setColor(backgroundColor.toColor());
			graphics.fillRect(0, 0, textWidth, textHeight);
		}
		if (textColor == null) {
			graphics.setColor(Color.black);
		} else {
			graphics.setColor(textColor.toColor());
		}
		graphics.drawString(text, 0, metrics.getMaxAscent());
		graphics.dispose();

		// copy image of drawn text from buffered image onto our image
		if (useAntiAliasing) {
			Image intermediate = new Image(textWidth, textHeight);
			intermediate.drawAwtImage(bufImg, 0, 0);
			intermediate.resampleTo((textWidth / 2) + (textWidth % 2), (textHeight / 2) + (textHeight % 2));
			if (left == null) {
				left = right - intermediate.getWidth();
			} else {
				if (left.equals(right)) {
					left -= intermediate.getWidth() / 2;
				}
			}
			if (top == null) {
				top = bottom - intermediate.getHeight();
			} else {
				if (top.equals(bottom)) {
					top -= intermediate.getHeight() / 2;
				}
			}

			if (targetImage != null) {
				targetImage.draw(intermediate, 0, 0);
			} else {
				draw(intermediate, left, top);
			}
		} else {

			if (left == null) {
				left = right - textWidth;
			} else {
				if (left.equals(right)) {
					left -= textWidth / 2;
				}
			}
			if (top == null) {
				top = bottom - textHeight;
			} else {
				if (top.equals(bottom)) {
					top -= textHeight / 2;
				}
			}

			if (targetImage != null) {
				targetImage.drawAwtImage(bufImg, 0, 0);
			} else {
				drawAwtImage(bufImg, left, top);
			}
		}
	}

	public static Image createTextImage(String text, String fontName, Integer fontSize, Boolean useAntiAliasing, ColorRGBA textColor, ColorRGBA backgroundColor) {

		Image result = new Image(1, 1);
		result.drawTextOnto(text, 0, null, null, 0, fontName, fontSize, useAntiAliasing, textColor, result, backgroundColor);
		return result;
	}

	public static Image createFromAwtImage(BufferedImage javaImg) {

		Image result = new Image(javaImg.getWidth(), javaImg.getHeight(), false);

		result.drawAwtImage(javaImg, 0, 0);

		return result;
	}

	/**
	 * Paste from clipboard, for copy to clipboard see copyToClipboard()
	 */
	public static Image createFromClipboard() {

		Transferable clipContent = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);

		// does the clipboard contain anything at all?
		if (clipContent == null) {
			return null;
		}

		// is the stuff in the clipboard an image?
		if (!clipContent.isDataFlavorSupported(DataFlavor.imageFlavor)) {
			return null;
		}

		try {
			// yes to both - let's do this!
			return createFromAwtImage((BufferedImage) clipContent.getTransferData(DataFlavor.imageFlavor));

		} catch (UnsupportedFlavorException | IOException e) {

			// whoops - this should not happen, as we already checked about this flavor being the right one...
			return null;
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
				data[y+top][x+left] = new ColorRGBA(r, g, b, a);
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
			expandBy(- (int) Math.floor(cutOffAmount), 0, - (int) Math.ceil(cutOffAmount), 0, ColorRGBA.WHITE);
		} else {
			// cut off left and right
			double cutOffWidth = (newWidth * height) / newHeight;
			double cutOffAmount = (width - cutOffWidth) / 2;
			// System.out.println("B width: " + width + ", height: " + height + ", newWidth: " + newWidth + ", newHeight: " + newHeight + ", cutOff: " + cutOffAmount);
			expandBy(0, - (int) Math.floor(cutOffAmount), 0, - (int) Math.ceil(cutOffAmount), ColorRGBA.WHITE);
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

		ColorRGBA[][] horzData = new ColorRGBA[height][newWidth];

		for (int x = 0; x < newWidth; x++) {
			for (int y = 0; y < height; y++) {
				double newX = (x * width) / ((double) newWidth);
				int loX = (int) Math.floor(newX);
				int hiX = (int) Math.ceil(newX);
				double hiAmount = newX - loX;
				if (hiX < width) {
					horzData[y][x] = ColorRGBA.intermix(data[y][loX], data[y][hiX], 1 - hiAmount);
				} else {
					horzData[y][x] = data[y][loX];
				}
			}
		}

		ColorRGBA[][] fullData = new ColorRGBA[newHeight][newWidth];

		for (int x = 0; x < newWidth; x++) {
			for (int y = 0; y < newHeight; y++) {
				double newY = (y * height) / ((double) newHeight);
				int loY = (int) Math.floor(newY);
				int hiY = (int) Math.ceil(newY);
				double hiAmount = newY - loY;
				if (hiY < height) {
					fullData[y][x] = ColorRGBA.intermix(horzData[loY][x], horzData[hiY][x], 1 - hiAmount);
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

		int origWidth = width;
		int origHeight = height;

		while (width > 3 * newWidth) {
			resampleTo(width / 2, (origHeight * width) / (2 * origWidth));
		}
		resampleTo(newWidth, (origHeight * newWidth) / origWidth);
	}

	public void resampleToHeight(int newHeight) {

		int origWidth = width;
		int origHeight = height;

		while (height > 3 * newHeight) {
			resampleTo((origWidth * height) / (2 * origHeight), height / 2);
		}
		resampleTo((origWidth * newHeight) / origHeight, newHeight);
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

		ColorRGBA[][] horzData = new ColorRGBA[height][newWidth];

		for (int x = 0; x < newWidth; x++) {
			for (int y = 0; y < height; y++) {
				horzData[y][x] = data[y][(x * width) / newWidth];
			}
		}

		ColorRGBA[][] fullData = new ColorRGBA[newHeight][newWidth];

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

	/**
	 * Add the amount of pixels to the top, right, bottom and left of the image,
	 * filling the new space with the fillWith color
	 * (negative values are allowed, in that case the image will shrink)
	 */
	public void expandBy(int top, int right, int bottom, int left, ColorRGBA fillWith) {

		int newwidth = width + right + left;

		int newheight = height + top + bottom;

		if (newheight < 0) {
			newheight = 0;
		}

		if (newwidth < 0) {
			newwidth = 0;
		}

		ColorRGBA[][] newdata = new ColorRGBA[newheight][newwidth];

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

	public void expandTopBy(int howMuch, ColorRGBA fillWith) {
		expandBy(howMuch, 0, 0, 0, fillWith);
	}

	public void expandRightBy(int howMuch, ColorRGBA fillWith) {
		expandBy(0, howMuch, 0, 0, fillWith);
	}

	public void expandBottomBy(int howMuch, ColorRGBA fillWith) {
		expandBy(0, 0, howMuch, 0, fillWith);
	}

	public void expandLeftBy(int howMuch, ColorRGBA fillWith) {
		expandBy(0, 0, 0, howMuch, fillWith);
	}

	public void rotateLeft() {

		int newHeight = width;
		int newWidth = height;

		ColorRGBA[][] rotatedData = new ColorRGBA[newHeight][newWidth];

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

		ColorRGBA[][] rotatedData = new ColorRGBA[newHeight][newWidth];

		for (int x = 0; x < newWidth; x++) {
			for (int y = 0; y < newHeight; y++) {
				rotatedData[y][x] = data[height - x - 1][y];
			}
		}

		this.data = rotatedData;
		this.width = newWidth;
		this.height = newHeight;
	}

	public void editChannels(String baseForR, double modifierForR,
							 String baseForG, double modifierForG,
							 String baseForB, double modifierForB) {

		editChannels(baseForR, modifierForR, baseForG, modifierForG, baseForB, modifierForB,
			ColorRGBA.DEFAULT_ALLOW_OVERFLOW);
	}

	public void editChannels(String baseForR, double modifierForR,
							 String baseForG, double modifierForG,
							 String baseForB, double modifierForB,
							 boolean allowOverflow) {

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				data[y][x] = data[y][x].getEditedChannels(
					baseForR, modifierForR,
					baseForG, modifierForG,
					baseForB, modifierForB,
					allowOverflow);
			}
		}
	}

	public void editChannelsAboveCutoff(String baseForR, double modifierForR,
							 String baseForG, double modifierForG,
							 String baseForB, double modifierForB,
							 boolean allowOverflow, int cutoff) {

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (data[y][x].getSum() > cutoff) {
					data[y][x] = data[y][x].getEditedChannels(
						baseForR, modifierForR,
						baseForG, modifierForG,
						baseForB, modifierForB,
						allowOverflow);
				}
			}
		}
	}

	public void editChannels(String baseForR, double modifierForR,
							 String baseForG, double modifierForG,
							 String baseForB, double modifierForB,
							 String baseForA, double modifierForA) {

		editChannels(baseForR, modifierForR, baseForG, modifierForG,
			baseForB, modifierForB, baseForA, modifierForA,
			ColorRGBA.DEFAULT_ALLOW_OVERFLOW);
	}

	public void editChannels(String baseForR, double modifierForR,
							 String baseForG, double modifierForG,
							 String baseForB, double modifierForB,
							 String baseForA, double modifierForA,
							 boolean allowOverflow) {

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				data[y][x] = data[y][x].getEditedChannels(
					baseForR, modifierForR,
					baseForG, modifierForG,
					baseForB, modifierForB,
					baseForA, modifierForA,
					allowOverflow);
			}
		}
	}

	public void dampen(float amount) {

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				data[y][x] = data[y][x].getDampened(amount);
			}
		}
	}

	public void intermix(ColorRGBA intermixWith, float amountOfPictureRemaining) {

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				data[y][x] = ColorRGBA.intermix(data[y][x], intermixWith, amountOfPictureRemaining);
			}
		}
	}

	public void intermixImage(Image other, float amountOfPictureRemaining) {

		int minWidth = Math.min(width, other.getWidth());
		int minHeight = Math.min(height, other.getHeight());

		for (int x = 0; x < minWidth; x++) {
			for (int y = 0; y < minHeight; y++) {
				data[y][x] = ColorRGBA.intermix(data[y][x],  other.data[y][x], amountOfPictureRemaining);
			}
		}
	}

	public void intermixImageMin(Image other) {

		int minWidth = Math.min(width, other.getWidth());
		int minHeight = Math.min(height, other.getHeight());

		for (int x = 0; x < minWidth; x++) {
			for (int y = 0; y < minHeight; y++) {
				data[y][x] = ColorRGBA.min(data[y][x],  other.data[y][x]);
			}
		}
	}

	public void intermixImageMax(Image other) {

		int minWidth = Math.min(width, other.getWidth());
		int minHeight = Math.min(height, other.getHeight());

		for (int x = 0; x < minWidth; x++) {
			for (int y = 0; y < minHeight; y++) {
				data[y][x] = ColorRGBA.max(data[y][x], other.data[y][x]);
			}
		}
	}

	public void intermixImageLeftToRight(Image other) {

		int minWidth = Math.min(width, other.getWidth());
		int minHeight = Math.min(height, other.getHeight());

		for (int x = 0; x < minWidth; x++) {
			for (int y = 0; y < minHeight; y++) {
				data[y][x] = ColorRGBA.intermix(data[y][x],  other.data[y][x], (1.0f * x) / minWidth);
			}
		}
	}

	public void intermixImageRightToLeft(Image other) {

		int minWidth = Math.min(width, other.getWidth());
		int minHeight = Math.min(height, other.getHeight());

		for (int x = 0; x < minWidth; x++) {
			for (int y = 0; y < minHeight; y++) {
				data[y][x] = ColorRGBA.intermix(data[y][x],  other.data[y][x], (1.0f * (minWidth - x)) / minWidth);
			}
		}
	}

	public void intermixImageTopToBottom(Image other) {

		int minWidth = Math.min(width, other.getWidth());
		int minHeight = Math.min(height, other.getHeight());

		for (int x = 0; x < minWidth; x++) {
			for (int y = 0; y < minHeight; y++) {
				data[y][x] = ColorRGBA.intermix(data[y][x],  other.data[y][x], (1.0f * y) / minHeight);
			}
		}
	}

	public void intermixImageBottomToTop(Image other) {

		int minWidth = Math.min(width, other.getWidth());
		int minHeight = Math.min(height, other.getHeight());

		for (int x = 0; x < minWidth; x++) {
			for (int y = 0; y < minHeight; y++) {
				data[y][x] = ColorRGBA.intermix(data[y][x],  other.data[y][x], (1.0f * (minHeight - y)) / minHeight);
			}
		}
	}

	public void interlaceImage(Image other) {

		int minWidth = Math.min(width, other.getWidth());
		int minHeight = Math.min(height, other.getHeight());

		for (int x = 0; x < minWidth; x++) {
			for (int y = 0; y < minHeight; y++) {
				if ((x + y) % 2 == 0) {
					data[y][x] = other.data[y][x];
				}
			}
		}
	}

	public void maskOutImage(Image other, ColorRGBA replaceWithCol) {

		int minWidth = Math.min(width, other.getWidth());
		int minHeight = Math.min(height, other.getHeight());

		for (int x = 0; x < minWidth; x++) {
			for (int y = 0; y < minHeight; y++) {
				if (data[y][x].equals(other.data[y][x])) {
					data[y][x] = replaceWithCol;
				}
			}
		}
	}
	public void multiply(ColorRGBA multiplyWith) {

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				data[y][x] = ColorRGBA.multiply(data[y][x], multiplyWith);
			}
		}
	}

	public void invert() {

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				data[y][x] = data[y][x].getInverted();
			}
		}
	}

	public void invertBrightness1() {

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				data[y][x] = data[y][x].getBrightnessInverted1();
			}
		}
	}

	public void invertBrightness2() {

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				data[y][x] = data[y][x].getBrightnessInverted2();
			}
		}
	}

	/*
	Farben intensivieren:
	p^[1] := max255((p^[1] * p^[1]) div 128);
	p^[2] := max255((p^[2] * p^[2]) div 128);
	p^[3] := max255((p^[3] * p^[3]) div 128);
	*/
	public void intensify() {

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int r = Math.min(255, (data[y][x].getR() * data[y][x].getR()) / 128);
				int g = Math.min(255, (data[y][x].getG() * data[y][x].getG()) / 128);
				int b = Math.min(255, (data[y][x].getB() * data[y][x].getB()) / 128);
				int a = data[y][x].getA();
				data[y][x] = new ColorRGBA(r, g, b, a);
			}
		}
	}

	/*
	Farben leicht intensivieren:
	p^[1] := (max255((p^[1] * p^[1]) div 128) + p^[1]) div 2;
	p^[2] := (max255((p^[2] * p^[2]) div 128) + p^[2]) div 2;
	p^[3] := (max255((p^[3] * p^[3]) div 128) + p^[3]) div 2;
	*/
	public void intensifySlightly() {

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int r = ((Math.min(255, (data[y][x].getR() * data[y][x].getR()) / 128)) + data[y][x].getR()) / 2;
				int g = ((Math.min(255, (data[y][x].getG() * data[y][x].getG()) / 128)) + data[y][x].getG()) / 2;
				int b = ((Math.min(255, (data[y][x].getB() * data[y][x].getB()) / 128)) + data[y][x].getB()) / 2;
				int a = data[y][x].getA();
				data[y][x] = new ColorRGBA(r, g, b, a);
			}
		}
	}

	/*
	intensifies colors, and the ones that achieve black or white are set to that,
	but all others are kept as before, so if it was somewhere in the middle before,
	it just stays exactly there
	*/
	public void intensifyExtremes() {

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				ColorRGBA cur = data[y][x];
				int rO = cur.getR();
				int gO = cur.getG();
				int bO = cur.getB();
				int aO = cur.getA();
				int r = Math.min(255, (rO * rO) / 128);
				int g = Math.min(255, (gO * gO) / 128);
				int b = Math.min(255, (bO * bO) / 128);
				if (rO+gO+bO > 637) {
					data[y][x] = new ColorRGBA(r, g, b, aO);
					continue;
				}
				if (rO+gO+bO < 128) {
					data[y][x] = new ColorRGBA(r, g, b, aO);
					continue;
				}
			}
		}
	}

	public void createMapOfExtremes(ColorRGBA extremeCol, ColorRGBA nonExtremeCol) {

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				ColorRGBA cur = data[y][x];
				int rO = cur.getR();
				int gO = cur.getG();
				int bO = cur.getB();
				int aO = cur.getA();
				if (rO+gO+bO > 637) {
					data[y][x] = extremeCol;
					continue;
				}
				if (rO+gO+bO < 128) {
					data[y][x] = extremeCol;
					continue;
				}
				data[y][x] = nonExtremeCol;
			}
		}
	}

	public void createMapOfDifferences() {

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				ColorRGBA cur = data[y][x];
				ColorRGBA nextR = cur;
				ColorRGBA nextD = cur;
				ColorRGBA nextRD = cur;
				if (x < width - 1) {
					nextR = data[y][x+1];
				}
				if (y < height - 1) {
					nextD = data[y+1][x];
					if (x < width - 1) {
						nextRD = data[y+1][x+1];
					}
				}

				data[y][x] = new ColorRGBA(
					Math.max(Math.abs(cur.getR() - nextR.getR()), Math.max(Math.abs(cur.getR() - nextD.getR()), Math.abs(cur.getR() - nextRD.getR()))),
					Math.max(Math.abs(cur.getG() - nextR.getG()), Math.max(Math.abs(cur.getG() - nextD.getG()), Math.abs(cur.getG() - nextRD.getG()))),
					Math.max(Math.abs(cur.getB() - nextR.getB()), Math.max(Math.abs(cur.getB() - nextD.getB()), Math.abs(cur.getB() - nextRD.getB())))
				);
			}
		}
	}

	public void createMapOfDifferencesBW() {

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				ColorRGBA cur = data[y][x];
				ColorRGBA nextR = cur;
				ColorRGBA nextD = cur;
				ColorRGBA nextRD = cur;
				if (x < width - 1) {
					nextR = data[y][x+1];
				}
				if (y < height - 1) {
					nextD = data[y+1][x];
					if (x < width - 1) {
						nextRD = data[y+1][x+1];
					}
				}

				int val = Math.max(
					Math.max(
						Math.max(Math.abs(cur.getR() - nextR.getR()), Math.max(Math.abs(cur.getR() - nextD.getR()), Math.abs(cur.getR() - nextRD.getR()))),
						Math.max(Math.abs(cur.getG() - nextR.getG()), Math.max(Math.abs(cur.getG() - nextD.getG()), Math.abs(cur.getG() - nextRD.getG())))
					),
					Math.max(
						Math.max(Math.abs(cur.getB() - nextR.getB()), Math.max(Math.abs(cur.getB() - nextD.getB()), Math.abs(cur.getB() - nextRD.getB()))),
						Math.max(Math.abs(cur.getA() - nextR.getA()), Math.max(Math.abs(cur.getA() - nextD.getA()), Math.abs(cur.getA() - nextRD.getA())))
					)
				);

				data[y][x] = new ColorRGBA(val, val, val);
			}
		}
	}

	/**
	 * Get the most common color in the image
	 */
	public ColorRGBA getMostCommonColor() {

		HashMap<ColorRGBA, Integer> colorCountingMap = new HashMap<>();

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				Integer amount = colorCountingMap.get(data[y][x]);
				if (amount == null) {
					amount = 0;
				}
				colorCountingMap.put(data[y][x], amount + 1);
			}
		}

		List<Pair<ColorRGBA, Integer>> sortedColorCounters = SortUtils.sortMapByValues(colorCountingMap);

		if (sortedColorCounters.size() > 0) {
			return sortedColorCounters.get(sortedColorCounters.size() - 1).getKey();
		}

		return null;
	}

	public void replaceStragglersWith(ColorRGBA bgColor, ColorRGBA toReplaceWith) {

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {

				if (bgColor.equals(data[y][x])) {
					continue;
				}

				if (y > 0) {
					if (!bgColor.equals(data[y-1][x])) {
						continue;
					}
				}

				if (y < height-1) {
					if (!bgColor.equals(data[y+1][x])) {
						continue;
					}
				}

				if (x > 0) {
					if (!bgColor.equals(data[y][x-1])) {
						continue;
					}
				}

				if (x < width-1) {
					if (!bgColor.equals(data[y][x+1])) {
						continue;
					}
				}

				if (x > 0) {
					if (y > 0) {
						if (!bgColor.equals(data[y-1][x-1])) {
							continue;
						}
					}

					if (y < height-1) {
						if (!bgColor.equals(data[y+1][x-1])) {
							continue;
						}
					}
				}

				if (x < width-1) {
					if (y > 0) {
						if (!bgColor.equals(data[y-1][x+1])) {
							continue;
						}
					}

					if (y < height-1) {
						if (!bgColor.equals(data[y+1][x+1])) {
							continue;
						}
					}
				}

				data[y][x] = toReplaceWith;
			}
		}
	}

	public void replaceStragglersIshWith(ColorRGBA bgColor, ColorRGBA toReplaceWith) {

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {

				if (bgColor.equals(data[y][x])) {
					continue;
				}

				if (y > 0) {
					if (!bgColor.equals(data[y-1][x])) {
						continue;
					}
				}

				if (y < height-1) {
					if (!bgColor.equals(data[y+1][x])) {
						continue;
					}
				}

				if (x > 0) {
					if (!bgColor.equals(data[y][x-1])) {
						continue;
					}
				}

				if (x < width-1) {
					if (!bgColor.equals(data[y][x+1])) {
						continue;
					}
				}

				data[y][x] = toReplaceWith;
			}
		}
	}

	/**
	 * Get the most common color in the rows and columns at the edge of the image
	 */
	public ColorRGBA getMostCommonSurroundingColor() {

		HashMap<ColorRGBA, Integer> colorCountingMap = new HashMap<>();

		for (int x = 0; x < width; x++) {
			Integer amount = colorCountingMap.get(data[0][x]);
			if (amount == null) {
				amount = 0;
			}
			colorCountingMap.put(data[0][x], amount + 1);

			amount = colorCountingMap.get(data[height-1][x]);
			if (amount == null) {
				amount = 0;
			}
			colorCountingMap.put(data[height-1][x], amount + 1);
		}

		for (int y = 1; y < height - 1; y++) {
			Integer amount = colorCountingMap.get(data[y][0]);
			if (amount == null) {
				amount = 0;
			}
			colorCountingMap.put(data[y][0], amount + 1);

			amount = colorCountingMap.get(data[y][width-1]);
			if (amount == null) {
				amount = 0;
			}
			colorCountingMap.put(data[y][width-1], amount + 1);
		}

		List<Pair<ColorRGBA, Integer>> sortedColorCounters = SortUtils.sortMapByValues(colorCountingMap);

		if (sortedColorCounters.size() > 0) {
			return sortedColorCounters.get(sortedColorCounters.size() - 1).getKey();
		}

		return null;
	}

	public void replaceColors(ColorRGBA oldCol, ColorRGBA newCol) {

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (data[y][x].equals(oldCol)) {
					data[y][x] = newCol;
				}
			}
		}
	}

	public void replaceColors(ColorRGBA oldCol, ColorRGBA newCol, int leniency) {

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (data[y][x].fastDiff(oldCol) < leniency) {
					data[y][x] = newCol;
				}
			}
		}
	}

	public void replaceColorsExcept(ColorRGBA oldCol, ColorRGBA newCol) {

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (!data[y][x].equals(oldCol)) {
					data[y][x] = newCol;
				}
			}
		}
	}

	public void makeBlackAndWhite() {

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (data[y][x].getSum() > 256 * 3 / 2) {
					data[y][x] = ColorRGBA.WHITE;
				} else {
					data[y][x] = ColorRGBA.BLACK;
				}
			}
		}
	}

	public void removeColors() {

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				data[y][x] = data[y][x].getRemovedColors();
			}
		}
	}

	public void removePerceivedColors() {

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				data[y][x] = data[y][x].getRemovedPerceivedColors();
			}
		}
	}

	/**
	 * Just remove the alpha transparency completely
	 */
	public void removeAlpha() {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				ColorRGBA col = data[y][x];
				int a = col.getA();
				if (a != 255) {
					data[y][x] = new ColorRGBA(col.getR(), col.getG(), col.getB(), 255);
				}
			}
		}
	}

	/**
	 * Take the RGBA image and set all alpha to 255, by displaying it on a continuous,
	 * bgColor-ed background
	 */
	public void bakeAlpha(ColorRGBA bgColor) {

		int bgR = bgColor.getR();
		int bgG = bgColor.getG();
		int bgB = bgColor.getB();

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				ColorRGBA col = data[y][x];
				int a = col.getA();
				if (a != 255) {
					int r = col.getR();
					int g = col.getG();
					int b = col.getB();
					if (a == 0) {
						r = bgR;
						g = bgG;
						b = bgB;
					} else {
						r = ((a * r) / 255) + (((255 - a) * bgR) / 255);
						g = ((a * g) / 255) + (((255 - a) * bgG) / 255);
						b = ((a * b) / 255) + (((255 - a) * bgB) / 255);
					}
					a = 255;
					data[y][x] = new ColorRGBA(r, g, b, a);
				}
			}
		}
	}

	public void extractBlackToAlpha() {
		if ((width < 1) || (height < 1)) {
			return;
		}

		// replace black color with full alpha, and any other colors with alpha-ified versions...
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				ColorRGBA col = data[y][x];
				int r = col.getR();
				int g = col.getG();
				int b = col.getB();
				int a = col.getA();

				int max = r;
				if (g > max) {
					max = g;
				}
				if (b > max) {
					max = b;
				}

				if (max < 1) {
					r = 0;
					b = 0;
					g = 0;
					a = 0;
				} else {
					r = (255 * r) / max;
					g = (255 * g) / max;
					b = (255 * b) / max;
					a = (a * max) / 255;
				}

				data[y][x] = new ColorRGBA(r, g, b, a);
			}
		}
	}

	public void extractBackgroundColorToAlpha() {
		if ((width < 1) || (height < 1)) {
			return;
		}

		// replace the background color with full alpha
		ColorRGBA bgColor = data[0][0];
		ColorRGBA fullAlpha = new ColorRGBA(bgColor, 0);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (bgColor.fastEquals(data[y][x])) {
					data[y][x] = fullAlpha;
				}
			}
		}
	}

	public int getLineWidth() {
		return lineWidth;
	}

	public void setLineWidth(int lineWidth) {
		this.lineWidth = lineWidth;
	}

	/**
	 * Instructs this image to redraw itself, e.g. as its resolution has changed
	 * For the base class, nothing happens; an extending class which always draws a certain image
	 * can override this
	 */
	public void redraw() {
		// do nothing in the base implementation
	}

	public void drawArea(List<Pair<Integer, Integer>> areaCornerPoints, ColorRGBA areaColor) {
		List<Pair<Integer, Integer>> points = getPointsInArea(areaCornerPoints);
		for (Pair<Integer, Integer> point : points) {
			setPixelSafely(point.getX(), point.getY(), areaColor);
		}
	}

	/**
	 * Takes in a list of points / (x, y) coordinates and returns a list of all points / (x, y) coordinates
	 * within an area spanned by these points
	 */
	public List<Pair<Integer, Integer>> getPointsInArea(List<Pair<Integer, Integer>> areaCornerPoints) {
		List<Pair<Integer, Integer>> result = new ArrayList<>();
		for (Pair<Integer, Integer> p : areaCornerPoints) {
			if (!result.contains(p)) {
				result.add(p);
			}
		}

		// very VERY ooompfh algorithm that has O^3 complexity and EACH O is huge, but at least this way
		// we get full area coverage...
		// which is not even how the usual area filling algorithm behaves, but oh well... xD
		for (int i = 0; i < areaCornerPoints.size(); i++) {
			for (int j = i+1; j < areaCornerPoints.size(); j++) {
				for (int k = j+1; k < areaCornerPoints.size(); k++) {
					List<Pair<Integer, Integer>> cur = getPointsInTriangle(
						areaCornerPoints.get(i), areaCornerPoints.get(j), areaCornerPoints.get(k));
					for (Pair<Integer, Integer> p : cur) {
						if (!result.contains(p)) {
							result.add(p);
						}
					}
				}
			}
		}

		return result;
	}

	public List<Pair<Integer, Integer>> getPointsInTriangle(
		Pair<Integer, Integer> corner1, Pair<Integer, Integer> corner2, Pair<Integer, Integer> corner3) {

		List<Pair<Integer, Integer>> result = new ArrayList<>();

		// start at random
		Pair<Integer, Integer> leftMost = corner1;
		Pair<Integer, Integer> rightMost = corner2;

		// ensure leftMost and rightMost are sorted
		if (corner2.getX() < leftMost.getX()) {
			rightMost = leftMost;
			leftMost = corner2;
		}

		// check where corner3 goes - by default, in the middle, but maybe also left or right
		Pair<Integer, Integer> middle = corner3;
		if (corner3.getX() < leftMost.getX()) {
			middle = leftMost;
			leftMost = corner3;
		} else if (corner3.getX() > rightMost.getX()) {
			middle = rightMost;
			rightMost = corner3;
		}

		// now that we are sorted, let's iterate from left to right...
		int leftestX = leftMost.getX();
		int rightestX = rightMost.getX();
		for (int x = leftestX; x < rightestX; x++) {
			double progressLeftRight = (0.0 + x - leftestX) / (rightestX - leftestX);
			int yLineLeftToRight = (int) (leftMost.getY() + (progressLeftRight * (rightMost.getY() - leftMost.getY())));
			int yLineToFromMid;
			if (x < middle.getX()) {
				double beforeMidProgress = (0.0 + x - leftestX) / (middle.getX() - leftestX);
				yLineToFromMid = (int) (leftMost.getY() + (beforeMidProgress * (middle.getY() - leftMost.getY())));
			} else {
				double afterMidProgress = (0.0 + x - middle.getX()) / (rightestX - middle.getX());
				yLineToFromMid = (int) (middle.getY() + (afterMidProgress * (rightMost.getY() - middle.getY())));
			}
			int y1 = yLineLeftToRight;
			int y2 = yLineToFromMid;
			if (y2 < y1) {
				y2 = yLineLeftToRight;
				y1 = yLineToFromMid;
			}
			for (int y = y1; y <= y2; y++) {
				result.add(new Pair<>(x, y));
			}
		}

		return result;
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

		ColorRGBA[][] otherData = otherImage.getData();

		for (int y = 0; y < getHeight(); y++) {
			for (int x = 0; x < getWidth(); x++) {
				if (!this.data[y][x].fastEquals(otherData[y][x])) {
					// System.out.println("Equals fails at x: " + x + ", y: " + y +
					//	", this: " + this.data[y][x] + ", other: " + otherData[y][x]);
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
