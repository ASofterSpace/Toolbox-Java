/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.barcodes;

import com.asofterspace.toolbox.utils.ColorRGB;
import com.asofterspace.toolbox.utils.Image;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;


/**
 * A QR Code based either on some image data,
 * or on some bits that are set as datapoints directly,
 * or on some text that you want to encode
 * (so yes, this class can be used for encoding and
 * for decoding QR codes - whoop whoop!)
 *
 * @author Moya (a softer space), 2019
 */
public class QrCode {

	private int version;

	private int width;

	private int height;

	// rotate the input?
	private int rotate;

	private QrCodeQualityLevel edcLevel;

	private QrCodeMaskPattern maskPattern;

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

		int version = widthToVersion(readFromImage.getWidth());

		initBasedOnVersion(version);

		setDatapoints(readFromImage);
	}

	// attempts to read from the image a certain qr code version, resizing the image
	public QrCode(Image readFromImage, int version) {

		initBasedOnVersion(version);

		setDatapoints(readFromImage);
	}

	// writes a QR Code based on a text that you want to encode
	public QrCode(String data) {

		this.rotate = 0;

		// the desired edc level has to be set here, as input - as it will be needed immediately
		// to determine the needed stream length!
		this.edcLevel = QrCodeQualityLevel.LOW_QUALITY;

		boolean[] datastream = encodeData(data);

		int version = setVersionBasedOnDataStreamLength(datastream);

		initBasedOnVersion(version);

		writeInaccessibleFields();

		writeEdcLevel();

		// TODO IMPORTANT :: actually decide upon the pattern based on what we want to encode!
		this.maskPattern = QrCodeMaskPattern.PATTERN_0;

		writeMaskPattern();

		// expand the datastream to also have space for the padding and the error correction codes
		boolean[] fullDatastream = new boolean[width*height];
		System.arraycopy(datastream, 0, fullDatastream, 0, datastream.length);

		writePaddingData(fullDatastream, datastream.length);

		writeErrorCorrectionCode(fullDatastream);

		writeData(fullDatastream);

		// TODO IMPORTANT :: add the error correction code for the edc level and mask pattern
		// into the inaccessible area!
	}

	// creates a new empty qr code of a certain version
	public QrCode(int version) {

		initBasedOnVersion(version);
	}

	private void initBasedOnVersion(int version) {

		this.version = version;

		width = versionToWidth(version);

		height = width;

		data = new boolean[width][height];
		inaccessibleFields = new boolean[width][height];

		// TODO :: add info about inaccessible fields for other versions too!
		// (this here should work for all small QR codes that have only one small alignment block...
		// once you have more alignment blocks - and especially once you have the version bits
		// from version 7 onwards - this needs to be adjusted!)

		// horizontal alignment bar
		for (int x = 0; x < width; x++) {
			inaccessibleFields[x][6] = true;
		}
		// vertical alignment bar
		for (int y = 0; y < height; y++) {
			inaccessibleFields[6][y] = true;
		}

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
	}

	private void writeInaccessibleFields() {

		// TODO :: add info about inaccessible fields for other versions too!
		// (this here should work for all small QR codes that have only one small alignment block...
		// once you have more alignment blocks - and especially once you have the version bits
		// from version 7 onwards - this needs to be adjusted!)

		// horizontal alignment bar
		for (int x = 0; x < width; x++) {
			setRotatedBit(x, 6, (x % 2) == 0);
		}
		// vertical alignment bar
		for (int y = 0; y < height; y++) {
			setRotatedBit(6, y, (y % 2) == 0);
		}

		// top left block
		writePositionBlockAt(0, 0);
		// top right block
		writePositionBlockAt(width - 7, 0);
		// bottom left block
		writePositionBlockAt(0, height - 7);

		// small alignment block
		writeSmallPositionBlockAt(width - 9, height - 9);

		// add dark module
		setRotatedBit(8, height - 8, true);
	}

	private void writePositionBlockAt(int offsetX, int offsetY) {

		// write all black...
		for (int x = offsetX; x < offsetX + 7; x++) {
			for (int y = offsetY; y < offsetY + 7; y++) {
				setRotatedBit(x, y, true);
			}
		}

		// add the white details!
		for (int x = offsetX + 1; x < offsetX + 6; x++) {
			setRotatedBit(x, offsetY + 1, false);
		}
		for (int x = offsetX + 1; x < offsetX + 6; x++) {
			setRotatedBit(x, offsetY + 5, false);
		}
		for (int y = offsetY + 1; y < offsetY + 6; y++) {
			setRotatedBit(offsetX + 1, y, false);
		}
		for (int y = offsetY + 1; y < offsetY + 6; y++) {
			setRotatedBit(offsetX + 5, y, false);
		}
	}

	private void writeSmallPositionBlockAt(int offsetX, int offsetY) {

		// write all black...
		for (int x = offsetX; x < offsetX + 5; x++) {
			for (int y = offsetY; y < offsetY + 5; y++) {
				setRotatedBit(x, y, true);
			}
		}

		// add the white details!
		for (int x = offsetX + 1; x < offsetX + 4; x++) {
			setRotatedBit(x, offsetY + 1, false);
		}
		for (int x = offsetX + 1; x < offsetX + 4; x++) {
			setRotatedBit(x, offsetY + 3, false);
		}
		for (int y = offsetY + 1; y < offsetY + 4; y++) {
			setRotatedBit(offsetX + 1, y, false);
		}
		for (int y = offsetY + 1; y < offsetY + 4; y++) {
			setRotatedBit(offsetX + 3, y, false);
		}
	}

	private int setVersionBasedOnDataStreamLength(boolean[] datastream) {

		int length = datastream.length;

		version = 1;

		while (getDataLength() < length) {
			version++;

			if (version > 40) {
				version = 40;
				break;
			}
		}

		return version;
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

	// set the bit at this position, but keep track of rotations!
	private void setRotatedBit(int x, int y, boolean bit) {

		// do the rotation
		switch (rotate) {
			case 0:
				data[x][y] = bit;
				break;
			case 1:
				// top right requested - return top left
				// bottom right requested - return top right
				// ...
				data[y][height - x - 1] = bit;
				break;
			case 2:
				data[width - x - 1][height - y - 1] = bit;
				break;
			case 3:
				data[width - y - 1][x] = bit;
				break;
		}
	}

	// get the bit at this position, but keep track of rotations and keep track of inversions too!
	private boolean bit(int x, int y) {

		boolean result = rotatedBit(x, y);

		if (maskPattern != null) {
			result = maskPattern.applyMaskToBit(x, y, result);
		}

		return result;
	}

	// set the bit at this position, but keep track of rotations and keep track of inversions too!
	private void setBit(int x, int y, boolean bit) {

		if (maskPattern != null) {
			bit = maskPattern.applyMaskToBit(x, y, bit);
		}

		setRotatedBit(x, y, bit);
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

	private boolean[] intToBits(int input) {

		int length = 32;

		boolean[] result = new boolean[length];

		int twoThePower = 1;

		for (int i = length - 1; i >= 0; i--) {
			if ((input / twoThePower) % 2 == 1) {
				result[i] = true;
			}
			twoThePower *= 2;
		}

		return result;
	}

	private byte[] bitsToByteArr(boolean[] datastream, int offset, int length) {

		int byteLen = length / 8;
		byte[] result = new byte[byteLen];

		for (int i = 0; i < byteLen; i++) {
			result[i] = (byte) bitsToInt(datastream, offset + 8*i, 8);
		}

		return result;
	}

	private void readBitsIntoStream(boolean[] datastream, int offset, boolean b1, boolean b2, boolean b3, boolean b4, boolean b5, boolean b6, boolean b7, boolean b8) {

		datastream[offset+0] = b8;
		datastream[offset+1] = b7;
		datastream[offset+2] = b6;
		datastream[offset+3] = b5;
		datastream[offset+4] = b4;
		datastream[offset+5] = b3;
		datastream[offset+6] = b2;
		datastream[offset+7] = b1;
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

	/**
	 * Reads data from the data array and puts it into a continuous stream,
	 * ignoring all the inaccessible / non-data fields
	 */
	private void readDataIntoStream(boolean[] datastream) {

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

	/**
	 * Takes data from a continuous stream of boolean bits and writes
	 * it into the data field of the QR code, ignoring all the
	 * inaccessible / non-data fields
	 */
	private void writeStreamIntoDataField(boolean[] datastream) {

		int offset = 0;

		while (offset < datastream.length) {

			while (inaccessibleFields[currentX][currentY]) {
				stepOneFieldAhead();
				if (currentX < 0) {
					return;
				}
			}

			setBit(currentX, currentY, datastream[offset]);
			offset++;
			stepOneFieldAhead();
			if (currentX < 0) {
				return;
			}
		}
	}

	private void detectOrientation() {

		// first of all, figure out the orientation by getting the large blocks on the sides...
		// ... set rotation to -1 to be able to distinguish the case of no orientation matching
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
	}

	private boolean hasPositionBlockAt(int x, int y) {

		// TODO :: check better, also a bit error-aware etc.
		// (right now we are just checking the diagonal of the block ^^)
		if (data[x][y] && !data[x+1][y+1] && data[x+2][y+2] && data[x+3][y+3] && data[x+4][y+4] && !data[x+5][y+5] && data[x+6][y+6]) {
			return true;
		}

		return false;
	}

	private void detectEdcLevel() {

		boolean bit1 = rotatedBit(0, 8);
		boolean bit2 = rotatedBit(1, 8);

		edcLevel = QrCodeQualityLevel.fromInt((int) bitsToNibble(false, false, bit1, bit2) & 0xFF);
	}

	private void writeEdcLevel() {

		if (edcLevel == null) {
			return;
		}

		int edcInt = edcLevel.toInt();

		boolean[] edcBits = intToBits(edcInt);

		setRotatedBit(0, 8, edcBits[30]);
		setRotatedBit(1, 8, edcBits[31]);
	}

	private void detectMaskPattern() {

		boolean bit1 = rotatedBit(2, 8);
		boolean bit2 = rotatedBit(3, 8);
		boolean bit3 = rotatedBit(4, 8);

		maskPattern = QrCodeMaskPattern.fromInt((int) bitsToNibble(false, bit1, bit2, bit3) & 0xFF);
	}

	private void writeMaskPattern() {

		if (maskPattern == null) {
			return;
		}

		int maskInt = maskPattern.toInt();

		boolean[] maskBits = intToBits(maskInt);

		setRotatedBit(2, 8, maskBits[29]);
		setRotatedBit(3, 8, maskBits[30]);
		setRotatedBit(4, 8, maskBits[31]);
	}

	/**
	 * gets the length (in bits) of the data field - of course the data can be truncated
	 * already before by a 0 nibble, but this is the maximum amount of data that will be
	 * in a QR code of this error correction level and version
	 */
	private int getDataLength() {

		// TODO :: add the values for further versions!

		switch (edcLevel) {
			case LOW_QUALITY:
				switch (version) {
					case 1: return 8*19;
					case 2: return 8*34;
					case 3: return 8*55;
					case 4: return 8*80;
					case 5: return 8*108;
					case 6: return 8*136;
				}
				break;

			case MEDIUM_QUALITY:
				switch (version) {
					case 1: return 8*16;
					case 2: return 8*28;
					case 3: return 8*44;
					case 4: return 8*64;
					case 5: return 8*86;
					case 6: return 8*108;
				}
				break;

			case QUALITY:
				switch (version) {
					case 1: return 8*13;
					case 2: return 8*22;
					case 3: return 8*34;
					case 4: return 8*48;
					case 5: return 8*62;
					case 6: return 8*76;
				}
				break;

			case HIGH_QUALITY:
				switch (version) {
					case 1: return 8*9;
					case 2: return 8*16;
					case 3: return 8*26;
					case 4: return 8*36;
					case 5: return 8*46;
					case 6: return 8*60;
				}
				break;
		}

		return 0;
	}

	public String getContent() {

		detectOrientation();

		// if no rotation could be found, return!
		if (rotate < 0) {
			return null;
		}

		detectEdcLevel();

		detectMaskPattern();

		// TODO :: also enable other versions!
		if ((version == 2) || (version == 3) || (version == 4)) {

			// message data can never be larger than actual QR code size
			// (for small QR codes this is a waste of space, but they are small;
			// for large QR codes it is nearly optimal anyway, so whatever)
			boolean[] ds = new boolean[width*height];

			if (edcLevel == null) {
				return null;
			}

			// read out the message data bits
			switch (edcLevel) {

				// H: high quality
				case HIGH_QUALITY:
					// TODO :: use readDataIntoStream here too!
					readBitsIntoStream(ds,  0, bit(27, 25), bit(28, 25), bit(27, 26), bit(28, 26), bit(27, 27), bit(28, 27), bit(27, 28), bit(28, 28));
					readBitsIntoStream(ds,  8, bit(27, 17), bit(28, 17), bit(27, 18), bit(28, 18), bit(27, 19), bit(28, 19), bit(27, 20), bit(28, 20));
					readBitsIntoStream(ds, 16, bit(27,  9), bit(28,  9), bit(27, 10), bit(28, 10), bit(27, 11), bit(28, 11), bit(27, 12), bit(28, 12));
					readBitsIntoStream(ds, 24, bit(25, 16), bit(26, 16), bit(25, 15), bit(26, 15), bit(25, 14), bit(26, 14), bit(25, 13), bit(26, 13));
					readBitsIntoStream(ds, 32, bit(25, 16+8), bit(26, 16+8), bit(25, 15+8), bit(26, 15+8), bit(25, 14+8), bit(26, 14+8), bit(25, 13+8), bit(26, 13+8));
					readBitsIntoStream(ds, 40, bit(23, 25), bit(24, 25), bit(23, 26), bit(24, 26), bit(23, 27), bit(24, 27), bit(23, 28), bit(24, 28));
					readBitsIntoStream(ds, 48, bit(23, 12), bit(24, 12), bit(23, 13), bit(24, 13), bit(23, 14), bit(24, 14), bit(23, 15), bit(24, 15));
					break;

				// Q: good quality
				case QUALITY:
					// TODO :: implement
					break;

				// M: medium quality
				case MEDIUM_QUALITY:
					// TODO :: implement
					break;

				// L: low quality
				case LOW_QUALITY:
					currentX = width - 1;
					currentY = height - 1;
					goingUp = true;
					readingRight = true;
					readDataIntoStream(ds);
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

			int dataLength = getDataLength();

			while (cur < dataLength) {
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

					// ascii bytes (unless the eci number was fiddled with)
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

						switch (eciNumber) {
							case 1:
							case 3:
								// ISO8859-1
								result.append(new String(bitsToByteArr(ds, cur, 8*blockLength), StandardCharsets.ISO_8859_1));
								cur += 8*blockLength;
								break;
							case 25:
								// UTF-16BE
								result.append(new String(bitsToByteArr(ds, cur, 16*blockLength), StandardCharsets.UTF_16BE));
								cur += 16*blockLength;
								break;
							case 26:
								// UTF-8
								result.append(new String(bitsToByteArr(ds, cur, 8*blockLength), StandardCharsets.UTF_8));
								cur += 8*blockLength;
								break;
							case 27:
							case 170:
								// ASCII
								result.append(new String(bitsToByteArr(ds, cur, 8*blockLength), StandardCharsets.US_ASCII));
								cur += 8*blockLength;
								break;
							// TODO :: add others
							default:
								// as default, if we got nothing useful, just add as is and hope and pray...
								for (int n = 0; n < blockLength; n++) {
									result.append((char) bitsToInt(ds, cur, 8));
									cur += 8;
								}
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

	private boolean[] encodeData(String data) {

		boolean[] result;
		int cur = 0;

		byte[] byteData = data.getBytes(StandardCharsets.UTF_8);
		byte[] byteDataAscii = data.getBytes(StandardCharsets.US_ASCII);

		int blockLen;
		if (version > 9) {
			blockLen = 16;
		} else {
			blockLen = 8;
		}

		// we are just using ASCII...
		if (Arrays.equals(byteData, byteDataAscii)) {

			// 4 bit mode indicator +
			// 8 or 16 bit length indicator +
			// 8 bits per string data +
			// 4 bit end of data marker -> we always add this for simplicity, although we could leave it out under certain circumstances
			result = new boolean[4 + blockLen + (8 * byteData.length) + 4];

		} else {

			// 8 bit eci indicator +
			// 4 bit mode indicator +
			// 8 or 16 bit length indicator +
			// 8 bits per string data +
			// 4 bit end of data marker -> we always add this for simplicity, although we could leave it out under certain circumstances
			result = new boolean[8 + 4 + blockLen + (8 * byteData.length) + 4];

			// encode in UTF8
			int eciNumber = 26;

			boolean[] eciNumberArr = intToBits(eciNumber);
			result[0] = eciNumberArr[24];
			result[1] = eciNumberArr[25];
			result[2] = eciNumberArr[26];
			result[3] = eciNumberArr[27];
			result[4] = eciNumberArr[28];
			result[5] = eciNumberArr[29];
			result[6] = eciNumberArr[30];
			result[7] = eciNumberArr[31];
			cur += 8;
		}

		boolean[] stringModeArr = intToBits(4);
		result[cur+0] = stringModeArr[28];
		result[cur+1] = stringModeArr[29];
		result[cur+2] = stringModeArr[30];
		result[cur+3] = stringModeArr[31];
		cur += 4;

		boolean[] strDataLengthArr = intToBits(byteData.length);
		if (version > 9) {
			result[cur+0] = strDataLengthArr[16];
			result[cur+1] = strDataLengthArr[17];
			result[cur+2] = strDataLengthArr[18];
			result[cur+3] = strDataLengthArr[19];
			result[cur+4] = strDataLengthArr[20];
			result[cur+5] = strDataLengthArr[21];
			result[cur+6] = strDataLengthArr[22];
			result[cur+7] = strDataLengthArr[23];
			result[cur+8] = strDataLengthArr[24];
			result[cur+9] = strDataLengthArr[25];
			result[cur+10] = strDataLengthArr[26];
			result[cur+11] = strDataLengthArr[27];
			result[cur+12] = strDataLengthArr[28];
			result[cur+13] = strDataLengthArr[29];
			result[cur+14] = strDataLengthArr[30];
			result[cur+15] = strDataLengthArr[31];
			cur += 16;
		} else {
			result[cur+0] = strDataLengthArr[24];
			result[cur+1] = strDataLengthArr[25];
			result[cur+2] = strDataLengthArr[26];
			result[cur+3] = strDataLengthArr[27];
			result[cur+4] = strDataLengthArr[28];
			result[cur+5] = strDataLengthArr[29];
			result[cur+6] = strDataLengthArr[30];
			result[cur+7] = strDataLengthArr[31];
			cur += 8;
		}

		for (byte curbyte : byteData) {
			boolean[] curbits = intToBits(curbyte);
			result[cur+0] = curbits[24];
			result[cur+1] = curbits[25];
			result[cur+2] = curbits[26];
			result[cur+3] = curbits[27];
			result[cur+4] = curbits[28];
			result[cur+5] = curbits[29];
			result[cur+6] = curbits[30];
			result[cur+7] = curbits[31];
			cur += 8;
		}

		return result;
	}

	private void writeData(boolean[] datastream) {

		if (edcLevel == null) {
			return;
		}

		// write the message data bits
		switch (edcLevel) {

			// H: high quality
			case HIGH_QUALITY:
				// TODO :: implement
				break;

			// Q: good quality
			case QUALITY:
				// TODO :: implement
				break;

			// M: medium quality
			case MEDIUM_QUALITY:
				// TODO :: implement
				break;

			// L: low quality
			case LOW_QUALITY:
				currentX = width - 1;
				currentY = height - 1;
				goingUp = true;
				readingRight = true;
				writeStreamIntoDataField(datastream);
				break;
		}
	}

	private void writePaddingData(boolean[] datastream, int start) {

		// if the written datastream's length is samller than getDataLength(),
		// while that is so, write 236, 17, 236, 17, again and again...
		// THE STANDARD DEMANDS IT! \o/

		int dataLength = getDataLength();

		while (start < dataLength) {

			datastream[start+0] = true;
			datastream[start+1] = true;
			datastream[start+2] = true;
			datastream[start+3] = false;
			datastream[start+4] = true;
			datastream[start+5] = true;
			datastream[start+6] = false;
			datastream[start+7] = false;
			start += 8;

			if (start >= dataLength) {
				break;
			}

			datastream[start+0] = false;
			datastream[start+1] = false;
			datastream[start+2] = false;
			datastream[start+3] = true;
			datastream[start+4] = false;
			datastream[start+5] = false;
			datastream[start+6] = false;
			datastream[start+7] = true;
			start += 8;
		}
	}

	private void writeErrorCorrectionCode(boolean[] datastream) {

		// TODO IMPORTANT :: actually write the error correction code
	}

	public String toString() {

		return "QR Code (version " + version + ", " + edcLevel + " error correction level) containing: " + getContent();
	}

}
