/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.barcodes;

import com.asofterspace.toolbox.utils.Image;


/**
 * A factory for creating QR code objects more easily
 *
 * @author Moya (a softer space), 2019
 */
public class QrCodeFactory {

	/**
	 * Reads a QR code from an as-arbitrary-as-possible image
	 * (that is, the QR Code might be hidden somewhere inside the image)
	 *
	 * This factory assures that if a QrCode is returned, it contains data.
	 * If no QR code can be found in the image, null is returned.
	 */
	public static QrCode readFromImage(Image img) {

		QrCode result = new QrCode(img, 2);

		if (result.getContent() != null) {
			return result;
		}

		result = new QrCode(img, 3);

		if (result.getContent() != null) {
			return result;
		}

		result = new QrCode(img, 4);

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
