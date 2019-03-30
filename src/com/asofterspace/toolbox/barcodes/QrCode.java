/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.barcodes;

import com.asofterspace.toolbox.utils.ColorRGB;
import com.asofterspace.toolbox.utils.Image;


/**
 * A QR Code based either on some image data,
 * or on some bits that are set as datapoints directly
 *
 * @author Moya (a softer space), 2019
 */
public class QrCode {

	private int version;

	private int width;

	private int height;

	// rotate the input?
	private int rotate;

	// some people seem to define an additional format mask,
	// meaning that the edcLevel and maskPattern that are read
	// out change, and that the constants change, but others
	// do neither - we opt for neither, which also works fine :)
	private int edcLevel;

	private int maskPattern;

	private boolean[][] data;

	// a map of the fields which are inaccessible for D and E
	private boolean[][] inaccessibleFields;

	// the current x and y coordinates at which we are trying to read data
	private int currentX = 0;
	private int currentY = 0;
	private boolean goingUp = true;
	private boolean readingRight = true;


	// attempts to read from the image with one image pixel being one qr code pixel
	public QrCode(Image readFromImage) {

		this(widthToVersion(readFromImage.getWidth()));

		setDatapoints(readFromImage);
	}

	// attempts to read from the image a certain qr code version, resizing the image
	public QrCode(Image readFromImage, int version) {

		this(version);

		setDatapoints(readFromImage);
	}

	// creates a new empty qr code of a certain version
	public QrCode(int version) {

		this.version = version;

		width = versionToWidth(version);

		height = width;

		data = new boolean[width][height];
		inaccessibleFields = new boolean[width][height];

		// TODO :: add info about inaccessible fields for other versions too!
		// (this here should work for all small QR codes that have only one small alignment block...
		// once you have more alignment blocks - and especially once you have the version bits
		// from version 7 onwards - this needs to be adjusted!)

		// top left block
		for (int x = 0; x < 9; x++) {
			for (int y = 0; y < 9; y++) {
				inaccessibleFields[x][y] = true;
			}
		}
		// top right block
		for (int x = width - 8; x < width; x++) {
			for (int y = 0; y < 9; y++) {
				inaccessibleFields[x][y] = true;
			}
		}
		// bottom left block
		for (int x = 0; x < 9; x++) {
			for (int y = height - 8; y < height; y++) {
				inaccessibleFields[x][y] = true;
			}
		}
		// small alignment block
		for (int x = width - 9; x < width - 4; x++) {
			for (int y = height - 9; y < height - 4; y++) {
				inaccessibleFields[x][y] = true;
			}
		}
		// horizontal alignment bar
		for (int x = 0; x < width; x++) {
			inaccessibleFields[x][6] = true;
		}
		// vertical alignment bar
		for (int y = 0; y < height; y++) {
			inaccessibleFields[6][y] = true;
		}
	}

	private static int widthToVersion(int width) {

		// version 1 is 21x21, version 2 is 25x25, version 3 is 29x29, ...
		return (width - 17) / 4;
	}

	private static int versionToWidth(int version) {

		// version 1 is 21x21, version 2 is 25x25, version 3 is 29x29, ...
		return 17 + (4 * version);
	}

	public Image getDatapointsAsImage() {

		Image result = new Image(width, height);

		ColorRGB black = new ColorRGB(0, 0, 0);
		ColorRGB white = new ColorRGB(255, 255, 255);

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (data[x][y]) {
					result.setPixel(x, y, black);
				} else {
					result.setPixel(x, y, white);
				}
			}
		}

		return result;
	}

	public void setDatapoints(Image img) {

		int imgWidth = img.getWidth();
		int imgHeight = img.getHeight();

		int halfPixW = imgWidth / (2 * width);
		int halfPixH = imgHeight / (2 * height);

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {

				int ix = ((x * imgWidth) / width) + halfPixW;
				int iy = ((y * imgHeight) / height) + halfPixH;

				if (ix > imgWidth - 1) {
					ix = imgWidth - 1;
				}

				if (iy > imgHeight - 1) {
					iy = imgHeight - 1;
				}

				data[x][y] = img.getPixel(ix, iy).isDark();
			}
		}
	}

	public void setDatapoint(int x, int y, boolean value) {
		data[x][y] = value;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	// get the bit at this position, but keep track of rotations!
	private boolean rotatedBit(int x, int y) {

		// do the rotation
		switch (rotate) {
			case 0:
				return data[x][y];
			case 1:
				// top right requested - return top left
				// bottom right requested - return top right
				// ...
				return data[y][height - x - 1];
			case 2:
				return data[width - x - 1][height - y - 1];
			case 3:
				return data[width - y - 1][x];
		}

		return false;
	}

	// get the bit at this position, but keep track of rotations and keep track of inversions too!
	private boolean bit(int x, int y) {

		boolean result = rotatedBit(x, y);

		// do the inversion
		switch (maskPattern) {
			case 0:
				if (((y*x) % 2) + ((y*x) % 3) == 0) {
					return !result;
				}
				return result;
			case 1:
				if (((y/2) + (x/3)) % 2 == 0) {
					return !result;
				}
				return result;
			case 2:
				if (((y*x) % 3 + y + x) % 2 == 0) {
					return !result;
				}
				return result;
			case 3:
				if (((y*x) % 3 + (y*x)) % 2 == 0) {
					return !result;
				}
				return result;
			case 4:
				if (y % 2 == 0) {
					return !result;
				}
				return result;
			case 5:
				if ((y + x) % 2 == 0) {
					return !result;
				}
				return result;
			case 6:
				if ((y + x) % 3 == 0) {
					return !result;
				}
				return result;
			case 7:
				if (x % 3 == 0) {
					return !result;
				}
				return result;
		}

		return result;
	}

	private byte bitsToNibble(boolean b1, boolean b2, boolean b3, boolean b4) {

		return (byte) ((b1?8:0) + (b2?4:0) + (b3?2:0) + (b4?1:0));
	}

	private int bitsToInt(boolean[] datastream, int offset, int length) {

		int result = 0;
		int twoThePower = 1;

		for (int i = length - 1; i >= 0; i--) {
			if (datastream[offset + i]) {
				result += twoThePower;
			}
			twoThePower *= 2;
		}

		return result;
	}

	private void writeBitsIntoStream(boolean[] datastream, int offset, boolean b1, boolean b2, boolean b3, boolean b4, boolean b5, boolean b6, boolean b7, boolean b8) {

		datastream[offset+0] = b8;
		datastream[offset+1] = b7;
		datastream[offset+2] = b6;
		datastream[offset+3] = b5;
		datastream[offset+4] = b4;
		datastream[offset+5] = b3;
		datastream[offset+6] = b2;
		datastream[offset+7] = b1;
	}

	private void constructInversionField() {

		boolean bit1 = rotatedBit(0, 8);
		boolean bit2 = rotatedBit(1, 8);

		edcLevel = (int) bitsToNibble(false, false, bit1, bit2) & 0xFF;

		bit1 = rotatedBit(2, 8);
		bit2 = rotatedBit(3, 8);
		boolean bit3 = rotatedBit(4, 8);

		maskPattern = (int) bitsToNibble(false, bit1, bit2, bit3) & 0xFF;
	}

	private boolean hasPositionBlockAt(int x, int y) {

		// TODO :: check better, also a bit error-aware etc.
		// (right now we are just checking the diagonal of the block ^^)
		if (data[x][y] && !data[x+1][y+1] && data[x+2][y+2] && data[x+3][y+3] && data[x+4][y+4] && !data[x+5][y+5] && data[x+6][y+6]) {
			return true;
		}

		return false;
	}

	private void stepOneFieldAhead() {
		if (readingRight) {
			readingRight = false;
			currentX--;
		} else {
			readingRight = true;
			currentX++;
			if (goingUp) {
				currentY--;
				if (currentY < 0) {
					goingUp = false;
					currentY = 0;
					currentX -= 2;
				}
			} else {
				currentY++;
				if (currentY >= height) {
					goingUp = true;
					currentY = height - 1;
					currentX -= 2;
				}
			}

			// if we crossed the x = 6 vertical line,
			// left and right are shifted by one...
			// because why not .-.
			if (currentX == 6) {
				currentX--;
			}
		}
	}

	// returns true if something could be written,
	// false otherwise (as no more data is left)
	private void writeBytesIntoStream(boolean[] datastream) {

		// first of all, we want to get eight fields that are hanging together...
		int offset = 0;
		while (true) {
			while (inaccessibleFields[currentX][currentY]) {
				stepOneFieldAhead();
				if (currentX < 0) {
					return;
				}
			}
			datastream[offset] = bit(currentX, currentY);
			offset++;
			stepOneFieldAhead();
			if (currentX < 0) {
				return;
			}
		}
	}

	public String getContent() {

		// first of all, figure out the orientation by getting the large blocks on the sides...
		// ... setting the orientation to 0 as default for reading out the blocks...
		rotate = -1;

		boolean hasLeftTopBlock = hasPositionBlockAt(0, 0);
		boolean hasRightTopBlock = hasPositionBlockAt(width - 7, 0);
		boolean hasLeftBottomBlock = hasPositionBlockAt(0, height - 7);
		boolean hasRightBottomBlock = hasPositionBlockAt(width - 7, height - 7);

		if (hasLeftTopBlock && hasRightTopBlock && hasLeftBottomBlock) {
			// all is awesome!
			rotate = 0;
		} else if (hasLeftTopBlock && hasLeftBottomBlock && hasRightBottomBlock) {
			// the QR code has been rotated right by 90°
			rotate = 1;
		} else if (hasRightTopBlock && hasLeftBottomBlock && hasRightBottomBlock) {
			// the QR code has been rotated (right) by 180°
			rotate = 2;
		} else if (hasLeftTopBlock && hasRightTopBlock && hasRightBottomBlock) {
			// the QR code has been rotated left by 90° (or right by 270°)
			rotate = 3;
		}

		if (rotate < 0) {
			return null;
		}

		constructInversionField();

		// TODO :: also enable other versions!
		if ((version == 2) || (version == 3) || (version == 4)) {

			// message data can never be larger than actual QR code size
			// (for small QR codes this is a waste of space, but they are small;
			// for large QR codes it is nearly optimal anyway, so whatever)
			boolean[] ds = new boolean[width*height];

			// read out the message data bits
			switch (edcLevel) {

				// H: high quality
				case 0:
					// TODO :: use writeBytesIntoStream here too!
					writeBitsIntoStream(ds,  0, bit(27, 25), bit(28, 25), bit(27, 26), bit(28, 26), bit(27, 27), bit(28, 27), bit(27, 28), bit(28, 28));
					writeBitsIntoStream(ds,  8, bit(27, 17), bit(28, 17), bit(27, 18), bit(28, 18), bit(27, 19), bit(28, 19), bit(27, 20), bit(28, 20));
					writeBitsIntoStream(ds, 16, bit(27,  9), bit(28,  9), bit(27, 10), bit(28, 10), bit(27, 11), bit(28, 11), bit(27, 12), bit(28, 12));
					writeBitsIntoStream(ds, 24, bit(25, 16), bit(26, 16), bit(25, 15), bit(26, 15), bit(25, 14), bit(26, 14), bit(25, 13), bit(26, 13));
					writeBitsIntoStream(ds, 32, bit(25, 16+8), bit(26, 16+8), bit(25, 15+8), bit(26, 15+8), bit(25, 14+8), bit(26, 14+8), bit(25, 13+8), bit(26, 13+8));
					writeBitsIntoStream(ds, 40, bit(23, 25), bit(24, 25), bit(23, 26), bit(24, 26), bit(23, 27), bit(24, 27), bit(23, 28), bit(24, 28));
					writeBitsIntoStream(ds, 48, bit(23, 12), bit(24, 12), bit(23, 13), bit(24, 13), bit(23, 14), bit(24, 14), bit(23, 15), bit(24, 15));
					break;

				// Q: good quality
				case 1:
					// TODO :: implement
					break;

				// M: medium quality
				case 2:
					// TODO :: implement
					break;

				// L: low quality
				case 3:
					currentX = width - 1;
					currentY = height - 1;
					goingUp = true;
					readingRight = true;
					writeBytesIntoStream(ds);
					break;
			}

			boolean debug = false;

			if (debug) {
				StringBuilder debugInfo = new StringBuilder();
				for (int i = 0; i < ds.length; i++) {
					if (ds[i]) {
						debugInfo.append("1");
					} else {
						debugInfo.append("0");
					}
				}
				System.out.println(debugInfo);
			}

			StringBuilder result = new StringBuilder();

			// now read the encoded data blocks...
			int cur = 0;

			// by default (if no ECI is given) use ASCII...
			int eciNumber = 27;

			while (cur < ds.length) {
				// ... with each data block starting with a 4 bit mode indicator
				byte modeIndicator = bitsToNibble(ds[cur], ds[cur+1], ds[cur+2], ds[cur+3]);
				cur += 4;

				switch (modeIndicator) {

					// end of message
					case 0:
						return result.toString();

					// numeric
					case 1:
						// TODO
						break;

					// alphanumeric
					case 2:
						// TODO
						break;

					// ascii bytes
					case 4:

						// the length of the field length depends on the version, of course .-.
						int blockLength;

						if (version > 9) {
							blockLength = bitsToInt(ds, cur, 16);
							cur += 16;
						} else {
							blockLength = bitsToInt(ds, cur, 8);
							cur += 8;
						}

						for (int n = 0; n < blockLength; n++) {
							// TODO :: if eciNumber is not 27, then do... uhm... something to get the correct conversion xD
							result.append((char) bitsToInt(ds, cur, 8));
							cur += 8;
						}

						break;

					// ECI - extended channel interpretation
					case 7:

						if (ds[cur] == false) {
							eciNumber = bitsToInt(ds, cur, 8);
							cur += 8;
						} else {
							if (ds[cur+1] == false) {
								eciNumber = bitsToInt(ds, cur, 16);
								cur += 16;
							} else {
								eciNumber = bitsToInt(ds, cur, 24);
								cur += 24;
							}
						}

						break;

					// kanji
					case 8:
						// TODO
						break;
				}
			}

			return result.toString();
		}

		return null;
	}

	public String toString() {

		String qualityStr = "unknown";

		switch (edcLevel) {

			case 0:
				qualityStr = "H / high quality";
				break;

			case 1:
				qualityStr = "Q / quality";
				break;

			case 2:
				qualityStr = "M / medium";
				break;

			case 3:
				qualityStr = "L / low";
				break;
		}

		return "QR Code (version " + version + ", " + qualityStr + " error correction level) containing: " + getContent();
	}

}
