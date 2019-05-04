/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.utils;


/**
 * A simple RGB triplet
 *
 * @author Moya (a softer space), 2019
 */
public class ColorRGB {

	// these fields are final as we want to be able to re-use pixels between images,
	// and just because someone paints one image green we do not want all others to
	// be green too! ;)
	private final byte r;
	private final byte g;
	private final byte b;


	// by default, just a white pixel
	public ColorRGB() {
		this(255, 255, 255);
	}

	public ColorRGB(byte r, byte g, byte b) {
		this.r = r;
		this.g = g;
		this.b = b;
	}

	public ColorRGB(int r, int g, int b) {

		if (r > 255) {
			r = 255;
		}
		if (r < 0) {
			r = 0;
		}

		if (g > 255) {
			g = 255;
		}
		if (g < 0) {
			g = 0;
		}

		if (b > 255) {
			b = 255;
		}
		if (b < 0) {
			b = 0;
		}

		this.r = (byte) r;
		this.g = (byte) g;
		this.b = (byte) b;
	}

	public boolean isDark() {
		// make the bytes unsigned ints, as Java has no unsigned byte...
		int intR = r & 0xFF;
		int intG = g & 0xFF;
		int intB = b & 0xFF;
		int result = intR + intG + intB;
		return result < (255 * 3) / 2;
	}

	// is this pixel the one we would expect?
	public boolean is(int r, int g, int b) {
		return (this.r == (byte) r) && (this.g == (byte) g) && (this.b == (byte) b);
	}

	public int getR() {
		return r & 0xFF;
	}

	public int getG() {
		return g & 0xFF;
	}

	public int getB() {
		return b & 0xFF;
	}

	public byte getRByte() {
		return r;
	}

	public byte getGByte() {
		return g;
	}

	public byte getBByte() {
		return b;
	}

	public int getGrayness() {
		int intR = r & 0xFF;
		int intG = g & 0xFF;
		int intB = b & 0xFF;
		int result = intR + intG + intB;
		return result / 3;
	}

	public byte getGrayByte() {
		int intR = r & 0xFF;
		int intG = g & 0xFF;
		int intB = b & 0xFF;
		int result = intR + intG + intB;
		return (byte) (result / 3);
	}

	@Override
	public int hashCode() {
		return (int) r + (int) g + (int) b;
	}

	@Override
	public boolean equals(Object other) {

		if (other == null) {
			return false;
		}

		if (!(other instanceof ColorRGB)) {
			return false;
		}

		ColorRGB otherColor = (ColorRGB) other;

		if (r != otherColor.getRByte()) {
			return false;
		}

		if (g != otherColor.getGByte()) {
			return false;
		}

		if (b != otherColor.getBByte()) {
			return false;
		}

		return true;
	}

	public boolean fastEquals(ColorRGB other) {
		return (this.r == other.r) && (this.g == other.g) && (this.b == other.b);
	}

	/**
	 * intermix two colors, where the amount of the first color in the mix is given,
	 * e.g. 0.45 for 45% one, 55% two
	 */
	public static ColorRGB intermix(ColorRGB one, ColorRGB two, double amountOfOne) {

		if (amountOfOne < 0) {
			amountOfOne = 0;
		}
		if (amountOfOne > 1) {
			amountOfOne = 1;
		}

		double aO = amountOfOne;
		double aT = 1 - amountOfOne;

		return new ColorRGB(
			(int) (((((int) one.r) & 0xFF) * aO) + ((((int) two.r) & 0xFF) * aT)),
			(int) (((((int) one.g) & 0xFF) * aO) + ((((int) two.g) & 0xFF) * aT)),
			(int) (((((int) one.b) & 0xFF) * aO) + ((((int) two.b) & 0xFF) * aT))
		);
	}

	public String toString() {
		return "RGB(" + r + ", " + g + ", " + b + ")";
	}

}
