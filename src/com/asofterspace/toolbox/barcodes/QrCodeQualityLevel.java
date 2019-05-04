/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.barcodes;

import com.asofterspace.toolbox.Utils;


/**
 * An enum containing the different possible QR code quality levels
 *
 * @author Moya (a softer space), 2019
 */
public enum QrCodeQualityLevel {

	LOW_QUALITY(3, "L / low"),

	MEDIUM_QUALITY(2, "M / medium"),

	QUALITY(1, "Q / quality"),

	HIGH_QUALITY(0, "H / high quality");


	private String kindStr;

	// some people seem to define an additional format mask, meaning
	// that the edcLevel and maskPattern that are read out change,
	// and that the corresponding int constants change, but others
	// do neither - we opt for neither, which also works fine :)
	private int intEncodedLevel;


	QrCodeQualityLevel(int level, String kindStr) {

		this.intEncodedLevel = level;

		this.kindStr = kindStr;
	}

	public static QrCodeQualityLevel fromInt(int encodedQualityLevel) {

		for (QrCodeQualityLevel quality : QrCodeQualityLevel.values()) {

			if (quality.intEncodedLevel == encodedQualityLevel) {
				return quality;
			}
		}

		return null;
	}

	public int toInt() {

		return intEncodedLevel;
	}

	public boolean[] toBits() {

		boolean[] result = new boolean[2];

		boolean[] bits = Utils.intToBits(intEncodedLevel);

		result[0] = bits[30];
		result[1] = bits[31];

		return result;
	}

	public boolean[] toNonXoredBits() {

		boolean[] result = toBits();

		result[0] = !result[0];

		return result;
	}

	public String toString() {

		return kindStr;
	}
}
