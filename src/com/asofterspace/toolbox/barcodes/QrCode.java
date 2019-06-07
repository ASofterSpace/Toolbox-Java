/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.barcodes;

import com.asofterspace.toolbox.utils.BitUtils;
import com.asofterspace.toolbox.utils.ColorRGB;
import com.asofterspace.toolbox.utils.Image;


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

	// the actual data - like, ALL of it, the entire field, including inaccessible fields,
	// data, padding and error correction!
	private boolean[][] data;

	// a map of the fields which are inaccessible for data, padding and error correction
	private boolean[][] inaccessibleFields;

	// the current x and y coordinates at which we are trying to read data
	private int currentX = 0;
	private int currentY = 0;
	private boolean goingUp = true;
	private boolean readingRight = true;


	// creates a new empty qr code of a certain version
	public QrCode(int version) {

		this.version = version;

		this.rotate = 0;

		width = QrCodeUtils.versionToWidth(version);

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

	void writeInaccessibleFields() {

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

	public Image toImage() {

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

	void assign(Image img) {

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


	QrCodeQualityLevel getEdcLevel() {
		return edcLevel;
	}

	void setEdcLevel(QrCodeQualityLevel newLevel) {

		this.edcLevel = newLevel;
	}

	private void detectEdcLevel() {

		boolean bit1 = rotatedBit(0, 8);
		boolean bit2 = rotatedBit(1, 8);

		edcLevel = QrCodeQualityLevel.fromInt((int) BitUtils.bitsToNibble(false, false, bit1, bit2) & 0xFF);
	}

	void writeEdcLevel() {

		if (edcLevel == null) {
			return;
		}

		boolean[] edcBits = edcLevel.toBits();

		setRotatedBit(0, 8, edcBits[0]);
		setRotatedBit(1, 8, edcBits[1]);

		setRotatedBit(8, height - 1, edcBits[0]);
		setRotatedBit(8, height - 2, edcBits[1]);
	}


	QrCodeMaskPattern getMaskPattern() {
		return maskPattern;
	}

	void setMaskPattern(QrCodeMaskPattern newPattern) {

		this.maskPattern = newPattern;
	}

	private void detectMaskPattern() {

		boolean bit1 = rotatedBit(2, 8);
		boolean bit2 = rotatedBit(3, 8);
		boolean bit3 = rotatedBit(4, 8);

		maskPattern = QrCodeMaskPattern.fromInt((int) BitUtils.bitsToNibble(false, bit1, bit2, bit3) & 0xFF);
	}

	void writeMaskPattern() {

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


	void writeFormatStringErrorCorrection() {

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

			// jump over leading zeroes, again
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

		currentX = width - 1;
		currentY = height - 1;
		goingUp = true;
		readingRight = true;

		readDataIntoStream(ds);

		// TODO :: depending on the getEcCodewordsPerBlock and especially the (not yet existing)
		// getInterleave function, all depending on the edcLevel, actually de-interleave after reading out!
		// (right now we are just reading out as-if there was no interleaving of error codes and data,
		// which works for the smallest of QR codes at the lowest of quality settings...)

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

		return QrCodeUtils.decodeData(ds, version, edcLevel);
	}

	void writeData(boolean[] datastream) {

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

	void writePaddingData(boolean[] datastream, int start) {

		// if the written datastream's length is smaller than getDataLength(),
		// while that is so, write 236, 17, 236, 17, again and again...
		// THE STANDARD DEMANDS IT! \o/

		int dataLength = QrCodeUtils.getDataLength(version, edcLevel);

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

	void writeErrorCorrectionCode(boolean[] datastream) {

		int dataLength = QrCodeUtils.getDataLength(version, edcLevel);

		int[] messagePolynomial = BitUtils.bitsToUnsignedByteArr(datastream, 0, dataLength);

		int[] errorCorrectionCodewords = QrCodeUtils.getErrorCorrectionCodewords(messagePolynomial, version, edcLevel);

		boolean[] errorCorrectionBits = BitUtils.unsignedBytesToBits(errorCorrectionCodewords);

		System.arraycopy(errorCorrectionBits, 0, datastream, dataLength, errorCorrectionBits.length);
	}

	/**
	 * calculate the mask pattern penalty based on the current data
	 * (just as it is written right now)
	 */
	int calculatePatternPenalty() {

		// we start with a perfect score: no penalty!
		// (no worries, it WILL go downhill from here...)
		int result = 0;


		// evaluation condition 1 - penalty for five same bits in a row (or column)

		// horizontal
		for (int x = 0; x < width; x++) {
			boolean currentColor = !data[x][0];
			int currentRepetitions = 0;
			for (int y = 0; y < height; y++) {
				if (data[x][y] == currentColor) {
					currentRepetitions++;
					if (currentRepetitions == 5) {
						result += 3;
					}
					if (currentRepetitions > 5) {
						result++;
					}
				} else {
					currentColor = !currentColor;
					currentRepetitions = 1;
				}
			}
		}

		// vertical
		for (int y = 0; y < height; y++) {
			boolean currentColor = !data[0][y];
			int currentRepetitions = 0;
			for (int x = 0; x < width; x++) {
				if (data[x][y] == currentColor) {
					currentRepetitions++;
					if (currentRepetitions == 5) {
						result += 3;
					}
					if (currentRepetitions > 5) {
						result++;
					}
				} else {
					currentColor = !currentColor;
					currentRepetitions = 1;
				}
			}
		}


		// evaluation condition 2 - penalty for each same bit 2x2 area
		for (int x = 0; x < width-1; x++) {
			for (int y = 0; y < height-1; y++) {
				if ((data[x][y] == data[x+1][y]) && (data[x][y] == data[x][y+1]) && (data[x][y+1] == data[x+1][y+1])) {
					result += 3;
				}
			}
		}


		// evaluation condition 3 - penalty for patterns that look like finder patterns

		// horizontal
		for (int x = 0; x < width-10; x++) {
			for (int y = 0; y < height; y++) {
				if (data[x][y] && !data[x+1][y] && data[x+2][y] && data[x+3][y] && data[x+4][y] && !data[x+5][y] &&
					data[x+6][y] && !data[x+7][y] && !data[x+8][y] && !data[x+9][y] && !data[x+10][y]) {
					result += 40;
				}
				if (!data[x][y] && !data[x+1][y] && !data[x+2][y] && !data[x+3][y] && data[x+4][y] &&
					!data[x+5][y] && data[x+6][y] && data[x+7][y] && data[x+8][y] && !data[x+9][y] && data[x+10][y]) {
					result += 40;
				}
			}
		}

		// vertical
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height-10; y++) {
				if (data[x][y] && !data[x][y+1] && data[x][y+2] && data[x][y+3] && data[x][y+4] && !data[x][y+5] &&
					data[x][y+6] && !data[x][y+7] && !data[x][y+8] && !data[x][y+9] && !data[x][y+10]) {
					result += 40;
				}
				if (!data[x][y] && !data[x][y+1] && !data[x][y+2] && !data[x][y+3] && data[x][y+4] &&
					!data[x][y+5] && data[x][y+6] && data[x][y+7] && data[x][y+8] && !data[x][y+9] && data[x][y+10]) {
					result += 40;
				}
			}
		}


		// evaluation condition 4 - penalty for uneven distribution of white and black

		// find total
		int totalBits = width * height;

		// find dark ones
		int darkBits = 0;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (data[x][y]) {
					darkBits++;
				}
			}
		}

		// get percentage
		int percDark = (100 * darkBits) / totalBits;

		// put percentage in five-percent-brackets
		int prevFive = (percDark / 5) * 5;
		int nextFive = prevFive + 5;

		// subtract fifty
		prevFive -= 50;
		nextFive -= 50;

		// get absolutes
		if (prevFive < 0) {
			prevFive = -prevFive;
		}
		if (nextFive < 0) {
			nextFive = -nextFive;
		}

		// divide by five again .-.
		prevFive /= 5;
		nextFive /= 5;

		// take the smaller one
		int smallOne = nextFive;
		if (prevFive < nextFive) {
			smallOne = prevFive;
		}

		// add ten times that to the result .-.
		result += 10 * smallOne;


		return result;
	}

	public String toString() {

		return "QR Code (version " + version + ", " + edcLevel + " error correction level) containing: " + getContent();
	}

}
