/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.barcodes;

import com.asofterspace.toolbox.utils.ColorRGB;
import com.asofterspace.toolbox.utils.Image;
import com.asofterspace.toolbox.Utils;

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

	// convert between numbers and alphas (exponents of 2 in the GF256 according to the QR standard)
	private static int[] NUM_TO_ALPHA = null;
	private static int[] ALPHA_TO_NUM = null;


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

		// expand the datastream to also have space for the padding and the error correction codes
		boolean[] fullDatastream = new boolean[width*height];

		// add actual data to the new fullDatastream
		System.arraycopy(datastream, 0, fullDatastream, 0, datastream.length);

		// add padding between data and error correction codes to the fullDatastream
		writePaddingData(fullDatastream, datastream.length);

		// add error correction codes to the fullDatastream
		writeErrorCorrectionCode(fullDatastream);

		// TODO IMPORTANT :: actually decide upon the pattern based on what we want to encode!
		this.maskPattern = QrCodeMaskPattern.PATTERN_0;

		// actually write the data (data + padding + error correction) into the QR code
		writeData(fullDatastream);

		// now write the format string, consisting of edc level, mask pattern and their own error correction
		writeEdcLevel();
		writeMaskPattern();
		writeFormatStringErrorCorrection();

		// TODO IMPORTANT :: add the error correction code for the edc level and mask pattern
		// into the inaccessible area!
	}

	// creates a new empty qr code of a certain version
	public QrCode(int version) {

		initBasedOnVersion(version);
	}

	private void initBasedOnVersion(int version) {

		generateLookupTables();

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

	private void generateLookupTables() {

		if (NUM_TO_ALPHA != null) {
			return;
		}

		NUM_TO_ALPHA = new int[256];
		ALPHA_TO_NUM = new int[256];

		int prev = 1;
		NUM_TO_ALPHA[prev] = 0;
		ALPHA_TO_NUM[0] = prev;

		for (int i = 1; i < 256; i++) {
			prev = prev * 2;

			if (prev > 255) {
				prev = prev ^ 285;
			}

			NUM_TO_ALPHA[prev] = i;
			ALPHA_TO_NUM[i] = prev;
		}

		// we have a^0 = 1 AND a^255 = 1, but it is advantageous to insist on 1 = a^0, not 255 = a^0
		NUM_TO_ALPHA[1] = 0;
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

	public QrCodeQualityLevel getEdcLevel() {
		return edcLevel;
	}

	/**
	 * The EDC level should be given in the constructor or autodetected during the construction;
	 * this explicit setter is used for testing only!
	 */
	public void setEdcLevel(QrCodeQualityLevel newLevel) {

		this.edcLevel = newLevel;
	}

	private void detectEdcLevel() {

		boolean bit1 = rotatedBit(0, 8);
		boolean bit2 = rotatedBit(1, 8);

		edcLevel = QrCodeQualityLevel.fromInt((int) Utils.bitsToNibble(false, false, bit1, bit2) & 0xFF);
	}

	private void writeEdcLevel() {

		if (edcLevel == null) {
			return;
		}

		boolean[] edcBits = edcLevel.toBits();

		setRotatedBit(0, 8, edcBits[0]);
		setRotatedBit(1, 8, edcBits[1]);

		setRotatedBit(8, height - 1, edcBits[0]);
		setRotatedBit(8, height - 2, edcBits[1]);
	}

	private void detectMaskPattern() {

		boolean bit1 = rotatedBit(2, 8);
		boolean bit2 = rotatedBit(3, 8);
		boolean bit3 = rotatedBit(4, 8);

		maskPattern = QrCodeMaskPattern.fromInt((int) Utils.bitsToNibble(false, bit1, bit2, bit3) & 0xFF);
	}

	private void writeMaskPattern() {

		if (maskPattern == null) {
			return;
		}

		boolean[] maskBits = maskPattern.toBits();

		setRotatedBit(2, 8, maskBits[0]);
		setRotatedBit(3, 8, maskBits[1]);
		setRotatedBit(4, 8, maskBits[2]);

		setRotatedBit(8, height - 3, maskBits[0]);
		setRotatedBit(8, height - 4, maskBits[1]);
		setRotatedBit(8, height - 5, maskBits[2]);
	}

	private void writeFormatStringErrorCorrection() {

		boolean[] formatPolynomial = new boolean[15];

		boolean[] edcBits = edcLevel.toNonXoredBits();
		formatPolynomial[0] = edcBits[0];
		formatPolynomial[1] = edcBits[1];

		boolean[] maskBits = maskPattern.toNonXoredBits();
		formatPolynomial[2] = maskBits[0];
		formatPolynomial[3] = maskBits[1];
		formatPolynomial[4] = maskBits[2];

		boolean[] generatorPolynomial = {true, false, true, false, false, true, true, false, true, true, true};


		// jump over leading zeroes
		int curpos = 0;

		while ((curpos < formatPolynomial.length) && (formatPolynomial[curpos] == false)) {
			curpos++;
		}

		while (formatPolynomial.length - curpos >= generatorPolynomial.length) {

			for (int c = curpos; c < formatPolynomial.length; c++) {
				int i = c - curpos;
				if (i < generatorPolynomial.length) {
					formatPolynomial[c] = formatPolynomial[c] ^ generatorPolynomial[i];
				}
			}

			while ((curpos < formatPolynomial.length) && (formatPolynomial[curpos] == false)) {
				curpos++;
			}
		}

		// the actual bits that we are interested in are now in formatPolynomial, 5 through 14...
		// however, we still have to XOR with 10010:
		formatPolynomial[10] = !formatPolynomial[10];
		formatPolynomial[13] = !formatPolynomial[13];

		setRotatedBit(5, 8, formatPolynomial[5]);
		setRotatedBit(7, 8, formatPolynomial[6]);
		setRotatedBit(8, 8, formatPolynomial[7]);
		setRotatedBit(8, 7, formatPolynomial[8]);
		setRotatedBit(8, 5, formatPolynomial[9]);
		setRotatedBit(8, 4, formatPolynomial[10]);
		setRotatedBit(8, 3, formatPolynomial[11]);
		setRotatedBit(8, 2, formatPolynomial[12]);
		setRotatedBit(8, 1, formatPolynomial[13]);
		setRotatedBit(8, 0, formatPolynomial[14]);

		setRotatedBit(8, height - 6, formatPolynomial[5]);
		setRotatedBit(8, height - 7, formatPolynomial[6]);
		setRotatedBit(width - 8, 8, formatPolynomial[7]);
		setRotatedBit(width - 7, 8, formatPolynomial[8]);
		setRotatedBit(width - 6, 8, formatPolynomial[9]);
		setRotatedBit(width - 5, 8, formatPolynomial[10]);
		setRotatedBit(width - 4, 8, formatPolynomial[11]);
		setRotatedBit(width - 3, 8, formatPolynomial[12]);
		setRotatedBit(width - 2, 8, formatPolynomial[13]);
		setRotatedBit(width - 1, 8, formatPolynomial[14]);
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

	private int getEcCodewordsPerBlock() {

		// TODO :: add the EC codewords per block for further versions!

		// TODO :: add a function to get the amount of blocks, as needed in higher versions!

		switch (edcLevel) {
			case LOW_QUALITY:
				switch (version) {
					case 1: return 7;
					case 2: return 10;
					case 3: return 15;
					case 4: return 20;
					case 5: return 26;
					case 6: return 18; // 2 blocks!
				}
				break;

			case MEDIUM_QUALITY:
				switch (version) {
					case 1: return 10;
					case 2: return 16;
					case 3: return 26;
					case 4: return 18; // 2 blocks!
					case 5: return 24; // 2 blocks!
					case 6: return 16; // 4 blocks!
				}
				break;

			case QUALITY:
				switch (version) {
					case 1: return 13;
					case 2: return 22;
					case 3: return 18; // 2 blocks!
					case 4: return 26; // 2 blocks!
					case 5: return 18; // 2 blocks twice!
					case 6: return 24; // 4 blocks!
				}
				break;

			case HIGH_QUALITY:
				switch (version) {
					case 1: return 17;
					case 2: return 28;
					case 3: return 22; // 2 blocks!
					case 4: return 16; // 4 blocks!
					case 5: return 22; // 2 blocks twice!
					case 6: return 28; // 4 blocks!
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
				Utils.readBitsIntoStream(ds,  0, bit(27, 25), bit(28, 25), bit(27, 26), bit(28, 26), bit(27, 27), bit(28, 27), bit(27, 28), bit(28, 28));
				Utils.readBitsIntoStream(ds,  8, bit(27, 17), bit(28, 17), bit(27, 18), bit(28, 18), bit(27, 19), bit(28, 19), bit(27, 20), bit(28, 20));
				Utils.readBitsIntoStream(ds, 16, bit(27,  9), bit(28,  9), bit(27, 10), bit(28, 10), bit(27, 11), bit(28, 11), bit(27, 12), bit(28, 12));
				Utils.readBitsIntoStream(ds, 24, bit(25, 16), bit(26, 16), bit(25, 15), bit(26, 15), bit(25, 14), bit(26, 14), bit(25, 13), bit(26, 13));
				Utils.readBitsIntoStream(ds, 32, bit(25, 16+8), bit(26, 16+8), bit(25, 15+8), bit(26, 15+8), bit(25, 14+8), bit(26, 14+8), bit(25, 13+8), bit(26, 13+8));
				Utils.readBitsIntoStream(ds, 40, bit(23, 25), bit(24, 25), bit(23, 26), bit(24, 26), bit(23, 27), bit(24, 27), bit(23, 28), bit(24, 28));
				Utils.readBitsIntoStream(ds, 48, bit(23, 12), bit(24, 12), bit(23, 13), bit(24, 13), bit(23, 14), bit(24, 14), bit(23, 15), bit(24, 15));
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
			StringBuilder Info = new StringBuilder();
			for (int i = 0; i < ds.length; i++) {
				if (ds[i]) {
					Info.append("1");
				} else {
					Info.append("0");
				}
			}
			System.out.println(Info);
		}

		StringBuilder result = new StringBuilder();

		// now read the encoded data blocks...
		int cur = 0;

		// by default (if no ECI is given) use ASCII...
		int eciNumber = 27;

		int dataLength = getDataLength();

		while (cur < dataLength) {
			// ... with each data block starting with a 4 bit mode indicator
			byte modeIndicator = Utils.bitsToNibble(ds[cur], ds[cur+1], ds[cur+2], ds[cur+3]);
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
						blockLength = Utils.bitsToInt(ds, cur, 16);
						cur += 16;
					} else {
						blockLength = Utils.bitsToInt(ds, cur, 8);
						cur += 8;
					}

					switch (eciNumber) {
						case 1:
						case 3:
							// ISO8859-1
							result.append(new String(Utils.bitsToByteArr(ds, cur, 8*blockLength), StandardCharsets.ISO_8859_1));
							cur += 8*blockLength;
							break;
						case 25:
							// UTF-16BE
							result.append(new String(Utils.bitsToByteArr(ds, cur, 16*blockLength), StandardCharsets.UTF_16BE));
							cur += 16*blockLength;
							break;
						case 26:
							// UTF-8
							result.append(new String(Utils.bitsToByteArr(ds, cur, 8*blockLength), StandardCharsets.UTF_8));
							cur += 8*blockLength;
							break;
						case 27:
						case 170:
							// ASCII
							result.append(new String(Utils.bitsToByteArr(ds, cur, 8*blockLength), StandardCharsets.US_ASCII));
							cur += 8*blockLength;
							break;
						// TODO :: add others
						default:
							// as default, if we got nothing useful, just add as is and hope and pray...
							for (int n = 0; n < blockLength; n++) {
								result.append((char) Utils.bitsToInt(ds, cur, 8));
								cur += 8;
							}
					}

					break;

				// ECI - extended channel interpretation
				case 7:

					if (ds[cur] == false) {
						eciNumber = Utils.bitsToInt(ds, cur, 8);
						cur += 8;
					} else {
						if (ds[cur+1] == false) {
							eciNumber = Utils.bitsToInt(ds, cur, 16);
							cur += 16;
						} else {
							eciNumber = Utils.bitsToInt(ds, cur, 24);
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

			boolean[] eciNumberArr = Utils.intToBits(eciNumber);
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

		boolean[] stringModeArr = Utils.intToBits(4);
		result[cur+0] = stringModeArr[28];
		result[cur+1] = stringModeArr[29];
		result[cur+2] = stringModeArr[30];
		result[cur+3] = stringModeArr[31];
		cur += 4;

		boolean[] strDataLengthArr = Utils.intToBits(byteData.length);
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
			boolean[] curbits = Utils.intToBits(curbyte);
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

		int dataLength = getDataLength();

		int[] messagePolynomial = Utils.bitsToUnsignedByteArr(datastream, 0, dataLength);

		int[] errorCorrectionCodewords = getErrorCorrectionCodewords(messagePolynomial);

		boolean[] errorCorrectionBits = Utils.unsignedBytesToBits(errorCorrectionCodewords);

		System.arraycopy(errorCorrectionBits, 0, datastream, dataLength, errorCorrectionBits.length);
	}

	/**
	 * add two alphas, so a^alphaOne + a^alphaTwo, in the Galois field 256
	 * public for testing purposes
	 */
	public int addAlpha(int alphaOne, int alphaTwo) {

		// we XOR the numerical representations of the alphas...
		int result = ALPHA_TO_NUM[alphaOne] ^ ALPHA_TO_NUM[alphaTwo];

		// ... and convert back to alphas
		return NUM_TO_ALPHA[result];
	}

	/**
	 * multiply two alphas, so a^alphaOne * a^alphaTwo, in the Galois field 256
	 */
	private int multiplyAlpha(int alphaOne, int alphaTwo) {

		int alphaSum = alphaOne + alphaTwo;

		return (alphaSum % 256) + (alphaSum / 256);
	}

	/**
	 * gets the generator polynomial, expressed in alphas
	 * public for testing
	 */
	public int[] getGeneratorPolynomialAlphas(int ecAmount) {

		int length = ecAmount - 1;

		int[] result = new int[length + 2];

		// we multiply (a^0 x + a^i) for increasing i from 0 to ecAmount
		// so e.g. for i = 2 we have (a^0 x + a^0) (a^0 x + a^1) (a^0 x + a^2)

		// therefore, we use recursion, ending at (a^0 x + a^0)...

		if (ecAmount < 2) {
			result[0] = 0;
			result[1] = 0;
			return result;
		}

		// ... and just generating the very last step here, in which we now have e.g.
		//   (a^k x^2 + a^m x + a^n) (a^0 x + a^ecAmount)
		// = a^k a^0 x^3 + (a^k a^ecAmount + a^m a^0) x^2 + (a^m a^ecAmount + a^n a^0) x + a^n a^ecAmount
		// = a^k x^3 + (a^k a^ecAmount + a^m) x^2 + (a^m a^ecAmount + a^n) x + a^n a^ecAmount
		int[] smallerPolynomial = getGeneratorPolynomialAlphas(ecAmount - 1);

		result[0] = smallerPolynomial[0];
		for (int i = 1; i < length + 1; i++) {
			result[i] = addAlpha(multiplyAlpha(smallerPolynomial[i - 1], length), smallerPolynomial[i]);
		}
		result[length + 1] = multiplyAlpha(smallerPolynomial[length], length);

		return result;
	}

	private int[] polynomialToNum(int[] polynomial) {

		for (int i = 0; i < polynomial.length; i++) {
			polynomial[i] = ALPHA_TO_NUM[polynomial[i]];
		}

		return polynomial;
	}

	private int[] polynomialToAlpha(int[] polynomial) {

		for (int i = 0; i < polynomial.length; i++) {
			polynomial[i] = NUM_TO_ALPHA[polynomial[i]];
		}

		return polynomial;
	}

	/**
	 * public for testing, not intended for outside use
	 */
	public int[] getErrorCorrectionCodewords(int[] messagePolynomial) {

		// TODO IMPORTANT :: actually calculate the error correction code correctly

		int ecAmount = getEcCodewordsPerBlock();

		int[] result = new int[ecAmount];

		// we get the generator polynomial in alpha notation
		int[] generatorPolynomial = getGeneratorPolynomialAlphas(ecAmount);

		// we transfer the generator polynomial to numerical notation
		generatorPolynomial = polynomialToNum(generatorPolynomial);

		// multiply message polynomial by x^n, which basically just means making the array n wider,
		// where n is the ecAmount
		int[] messagePolyWider = new int[messagePolynomial.length + ecAmount];
		System.arraycopy(messagePolynomial, 0, messagePolyWider, 0, messagePolynomial.length);

		// and some space to work within...
		int[] curGenPolyWider = new int[messagePolyWider.length];

		// multiply generator poly by lead term of message poly
		for (int curpos = 0; curpos < messagePolynomial.length; curpos++) {
			int leadTerm = messagePolyWider[curpos];
			leadTerm = NUM_TO_ALPHA[leadTerm];
			for (int j = 0; j < messagePolyWider.length; j++) {
				int i = j - curpos;
				if ((i > 0) && (i < generatorPolynomial.length) && (generatorPolynomial[i] > 0)) {
					curGenPolyWider[j] = ALPHA_TO_NUM[multiplyAlpha(leadTerm, NUM_TO_ALPHA[generatorPolynomial[i]])];
				} else {
					curGenPolyWider[j] = 0;
				}
				// XOR generatorPolyWider with the message polynomial
				messagePolyWider[j] = curGenPolyWider[j] ^ messagePolyWider[j];
			}
		}

		// assign resulting coefficients as result
		System.arraycopy(messagePolyWider, messagePolyWider.length - ecAmount, result, 0, ecAmount);

		return result;
	}

	public String toString() {

		return "QR Code (version " + version + ", " + edcLevel + " error correction level) containing: " + getContent();
	}

}
