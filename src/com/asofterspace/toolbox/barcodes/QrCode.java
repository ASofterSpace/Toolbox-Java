/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.barcodes;


/**
 * A simple RGB triplet
 *
 * @author Moya (a softer space), 2019
 */
public class QrCode {

	private int version;

	private int width;

	private int height;

	// rotate the input?
	private int rotate;

	private int maskPattern;

	private boolean[][] data;


	public QrCode(int version) {

		this.version = version;

		switch (version) {
			case 1:
				width = 21;
				break;
			case 2:
				width = 25;
				break;
			case 3:
				width = 29;
				break;
			case 4:
				width = 33;
				break;
			case 10:
				width = 57;
				break;
			case 25:
				width = 117;
				break;
			case 40:
				width = 177;
				break;
		}

		height = width;

		data = new boolean[width][height];
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
				if ((x*y)%2 + (x*y)%3 == 0) {
					return !result;
				}
				return result;
			case 1:
				if (((x/2) + (y/3)) % 2 == 0) {
					return !result;
				}
				return result;
			case 2:
				if (((x*y)%3+x+y)%2==0) {
					return !result;
				}
				return result;
			// TODO NOW :: add the others, see: https://en.wikipedia.org/wiki/QR_code#/media/File:QR_Format_Information.svg
		}

		return result;
	}

	private byte bitsToByte(boolean b1, boolean b2, boolean b3, boolean b4, boolean b5, boolean b6, boolean b7, boolean b8) {

		System.out.println("" + (b1?1:0) + (b2?1:0) + (b3?1:0) + (b4?1:0) + (b5?1:0) + (b6?1:0) + (b7?1:0) + (b8?1:0));

		return (byte) ((b1?1:0) + (b2?2:0) + (b3?4:0) + (b4?8:0) + (b5?16:0) + (b6?32:0) + (b7?64:0) + (b8?128:0));
	}

	private void constructInversionField() {

		boolean bit1 = rotatedBit(2, 8);
		boolean bit2 = rotatedBit(3, 8);
		boolean bit3 = rotatedBit(4, 8);

		maskPattern = (int) bitsToByte(bit3, bit2, bit1, false, false, false, false, false) & 0xFF;
	}

	private boolean hasPositionBlockAt(int x, int y) {

		// TODO :: check better, also a bit error-aware etc.
		// (right now we are just checking the diagonal of the block ^^)
		// TODO :: use data[][] directly everywhere, as rotation is 0 anyway .-.
		if (data[x][y] && !rotatedBit(x+1, y+1) && rotatedBit(x+2, y+2) && rotatedBit(x+3, y+3) && rotatedBit(x+4, y+4) && !rotatedBit(x+5, y+5) && rotatedBit(x+6, y+6)) {
			return true;
		}

		return false;
	}

	public String getContent() {

		// first of all, figure out the orientation by getting the large blocks on the sides...
		// ... setting the orientation to 0 as default for reading out the blocks...
		rotate = 0;

		boolean hasLeftTopBlock = hasPositionBlockAt(0, 0);
		boolean hasRightTopBlock = hasPositionBlockAt(width - 7, 0);
		boolean hasLeftBottomBlock = hasPositionBlockAt(0, height - 7);
		boolean hasRightBottomBlock = hasPositionBlockAt(width - 7, height - 7);

		System.out.println("hasLeftTopBlock: " + hasLeftTopBlock + ", hasRightTopBlock: " + hasRightTopBlock + ", hasLeftBottomBlock: " + hasLeftBottomBlock + ", hasRightBottomBlock: " + hasRightBottomBlock);

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

		System.out.println("Rotation detected: " + rotate);

		constructInversionField();

		// TODO :: also enable other versions!
		if (version == 3) {
			// message data for version 3 QR codes has 26 bytes
			byte[] result = new byte[26];

			result[0] = bitsToByte(bit(27, 25), bit(28, 25), bit(27, 26), bit(28, 26), bit(27, 27), bit(28, 27), bit(27, 28), bit(28, 28));
			result[1] = bitsToByte(bit(27, 17), bit(28, 17), bit(27, 18), bit(28, 18), bit(27, 19), bit(28, 19), bit(27, 20), bit(28, 20));

			return new String(result);
		}

		return null;
	}

	public String toString() {
		StringBuilder str = new StringBuilder();
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				str.append(data[x][y] ? "1" : "0");
			}
			str.append("\n");
		}
		return "QR Code (version " + version + ") containing: " + getContent() + "\n" + str.toString();
	}

}
