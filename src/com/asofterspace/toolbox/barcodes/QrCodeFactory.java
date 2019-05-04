/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.barcodes;

import com.asofterspace.toolbox.utils.ColorRGB;
import com.asofterspace.toolbox.utils.Image;


/**
 * A factory for creating QR code objects more easily
 *
 * @author Moya (a softer space), 2019
 */
public class QrCodeFactory {

	/**
	 * creates a QR Code based on a text that you want to encode
	 */
	public static QrCode createFromString(String data) {

		return createFromString(data, null, null);
	}

	/**
	 * creates a QR Code based on a text that you want to encode,
	 * using a specific edc level and a specific mask pattern
	 * (leave any of these two at null to use the default one)
	 */
	public static QrCode createFromString(String data, QrCodeQualityLevel edcLevel, QrCodeMaskPattern maskPattern) {

		if (edcLevel == null) {
			edcLevel = QrCodeQualityLevel.LOW_QUALITY;
		}

		// we iterate over versions (but skipping some) to find the correct one, ...
		int version = 1;

		boolean[] datastream;

		while (true) {

			// ... always trying to encode with the version that was found last...
			int tryVersion = version;

			// ... knowing that the datastream for a larger version is always the same or larger, never smaller...
			datastream = QrCodeUtils.encodeData(data, tryVersion);

			// ... and checking which one gets now found for the resulting stream...
			version = QrCodeUtils.getVersionBasedOnDataStreamLength(datastream, edcLevel);

			// ... and if it is the same, we are done!
			if (tryVersion == version) {
				break;
			}
		}

		// actually generate the QR Code object
		QrCode result = new QrCode(version);

		result.setEdcLevel(edcLevel);

		result.writeInaccessibleFields();

		// expand the datastream to also have space for the padding and the error correction codes
		boolean[] fullDatastream = new boolean[result.getWidth() * result.getHeight()];

		// add actual data to the new fullDatastream
		System.arraycopy(datastream, 0, fullDatastream, 0, datastream.length);

		// add padding between data and error correction codes to the fullDatastream
		result.writePaddingData(fullDatastream, datastream.length);

		// add error correction codes to the fullDatastream
		result.writeErrorCorrectionCode(fullDatastream);

		// if no mask pattern is explicitly requested (which is the usual case - a pattern
		// should only be explicitly requested for testing, or for other nefarious purposes
		// like to show examples during teaching about QR codes or somesuch ^^)...
		if (maskPattern == null) {

			// ... then actually decide upon the pattern based on what we want to encode!
			QrCodeMaskPattern bestPattern = null;
			int bestPenalty = Integer.MAX_VALUE;

			// to do so, iterate over all possible patterns...
			for (QrCodeMaskPattern curPattern : QrCodeMaskPattern.values()) {

				// ... for each one, pretend that we are using it and write all data with it...
				result.setMaskPattern(curPattern);
				result.writeData(fullDatastream);
				result.writeEdcLevel();
				result.writeMaskPattern();
				result.writeFormatStringErrorCorrection();

				// ... then calculate its overall penalty...
				currentPenalty = result.calculatePatternPenalty();

				// ... and if it is lower than the current best contender, the current one is the best for now!
				if (currentPenalty < bestPenalty) {
					bestPattern = curPattern;
					bestPenalty = currentPenalty;
				}
			}

			maskPattern = bestPattern;
		}

		// now actually apply the pattern...
		result.setMaskPattern(maskPattern);

		// ... and really write the data (data + padding + error correction) into the QR code
		result.writeData(fullDatastream);

		// also write the format string, consisting of edc level, mask pattern and their own error correction
		result.writeEdcLevel();
		result.writeMaskPattern();
		result.writeFormatStringErrorCorrection();

		return result;
	}

	/**
	 * creates an image containing a QR code with the given text
	 */
	public static Image createImageFromString(String data) {

		QrCode code = createFromString(data);

		return code.toImage();
	}

	/**
	 * creates an image containing a QR code with the given text,
	 * including the mandatory whitespace around the QR code itself
	 */
	public static Image createWhitespacedImageFromString(String data) {

		Image result = createImageFromString(data);

		ColorRGB white = new ColorRGB(255, 255, 255);

		// surround the QR code by three white modules
		result.expand(3, 3, 3, 3, white);

		return result;
	}

	/**
	 * attempts to read from the image with one image pixel being one qr code pixel
	 */
	public static QrCode readFromQrImage(Image img) {

		int version = QrCodeUtils.widthToVersion(img.getWidth());

		QrCode result = new QrCode(version);

		result.assign(img);

		return result;
	}

	/**
	 * attempts to read from the image a certain qr code version, resizing the image
	 */
	public static QrCode readFromQrImage(Image img, int version) {

		QrCode result = new QrCode(version);

		result.assign(img);

		return result;
	}

	/**
	 * reads a QR code from an as-arbitrary-as-possible image
	 * (that is, the QR Code might be hidden somewhere inside the image)
	 *
	 * this factory assures that if a QrCode is returned, it contains data;
	 * if no QR code can be found in the image, null is returned
	 */
	public static QrCode readFromSomewhereInImage(Image img) {

		QrCode result = readFromQrImage(img, 2);

		if (result.getContent() != null) {
			return result;
		}

		result = readFromQrImage(img, 3);

		if (result.getContent() != null) {
			return result;
		}

		result = readFromQrImage(img, 4);

		if (result.getContent() != null) {
			return result;
		}

		// great - nothing worked so far...
		// now try to read out a certainly-shaped version 3 QR code...
		// for now, let's just hardcode even the QR code location:
		// go to a column that is right-wards of the logo at the left...
		// row four (or three, or five)...
		// advance to the right until you get a very dark one, which should
		// be the top left pixel of the QR code itself!
		// (well, and then height and version are hardcoded again, gnaaahh)
		int offsetX3 = 300;
		for (; offsetX3 < img.getWidth(); offsetX3++) {
			if (img.getPixel(offsetX3, 3).getGrayness() < 96) {
				break;
			}
		}
		int offsetX4 = 300;
		for (; offsetX4 < img.getWidth(); offsetX4++) {
			if (img.getPixel(offsetX4, 4).getGrayness() < 96) {
				break;
			}
		}
		int offsetX5 = 300;
		for (; offsetX5 < img.getWidth(); offsetX5++) {
			if (img.getPixel(offsetX5, 5).getGrayness() < 96) {
				break;
			}
		}

		int offsetY = 3;
		int offsetX = offsetX3;

		if (offsetX4 < offsetX) {
			offsetY = 4;
			offsetX = offsetX4;
		}

		if (offsetX5 < offsetX) {
			offsetY = 5;
			offsetX = offsetX5;
		}

		int enlargeX = 0;
		int enlargeY = 0;

		result = new QrCode(3);

		for (int x = 0; x < result.getWidth(); x++) {
			if (offsetX + x + enlargeX >= img.getWidth()) {
				continue;
			}
			for (int y = 0; y < result.getHeight(); y++) {
				if (offsetY + y + enlargeY >= img.getHeight()) {
					continue;
				}

				result.setDatapoint(x, y, img.getPixel(offsetX + x + enlargeX, offsetY + y + enlargeY).isDark());

				if (y % 4 != 3) {
					enlargeY++;
				}
			}
			if (x % 4 != 3) {
				enlargeX++;
			}
			enlargeY = 0;
		}

		if (result.getContent() != null) {
			return result;
		}

		return null;
	}

}
