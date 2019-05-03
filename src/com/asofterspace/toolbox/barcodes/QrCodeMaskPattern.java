/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.barcodes;

import com.asofterspace.toolbox.Utils;


/**
 * An enum containing the different possible QR code mask patterns
 *
 * @author Moya (a softer space), 2019
 */
public enum QrCodeMaskPattern {

	PATTERN_0(0),
	PATTERN_1(1),
	PATTERN_2(2),
	PATTERN_3(3),
	PATTERN_4(4),
	PATTERN_5(5),
	PATTERN_6(6),
	PATTERN_7(7);


	// some people seem to define an additional format mask, meaning
	// that the edcLevel and maskPattern that are read out change,
	// and that the corresponding int constants change, but others
	// do neither - we opt for neither, which also works fine :)
	private int intEncodedMaskPattern;


	QrCodeMaskPattern(int maskpattern) {

		this.intEncodedMaskPattern = maskpattern;
	}

	public static QrCodeMaskPattern fromInt(int encodedMaskPattern) {

		for (QrCodeMaskPattern maskpattern : QrCodeMaskPattern.values()) {

			if (maskpattern.intEncodedMaskPattern == encodedMaskPattern) {
				return maskpattern;
			}
		}

		return null;
	}

	public int toInt() {

		return intEncodedMaskPattern;
	}

	public boolean[] toBits() {

		return Utils.intToBits(intEncodedMaskPattern);
	}

	/**
	 * applies this mask to the original bit, either flipping it - or not doing so :)
	 */
	public boolean applyMaskToBit(int x, int y, boolean originalBit) {

		// do the inversion
		switch (intEncodedMaskPattern) {
			case 0:
				if (((y*x) % 2) + ((y*x) % 3) == 0) {
					return !originalBit;
				}
				return originalBit;
			case 1:
				if (((y/2) + (x/3)) % 2 == 0) {
					return !originalBit;
				}
				return originalBit;
			case 2:
				if (((y*x) % 3 + y + x) % 2 == 0) {
					return !originalBit;
				}
				return originalBit;
			case 3:
				if (((y*x) % 3 + (y*x)) % 2 == 0) {
					return !originalBit;
				}
				return originalBit;
			case 4:
				if (y % 2 == 0) {
					return !originalBit;
				}
				return originalBit;
			case 5:
				if ((y + x) % 2 == 0) {
					return !originalBit;
				}
				return originalBit;
			case 6:
				if ((y + x) % 3 == 0) {
					return !originalBit;
				}
				return originalBit;
			case 7:
				if (x % 3 == 0) {
					return !originalBit;
				}
				return originalBit;
		}

		return originalBit;
	}
}
