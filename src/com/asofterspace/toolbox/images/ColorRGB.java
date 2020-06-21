/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.images;

import java.awt.Color;
import java.util.Random;


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
	private final byte a;

	private final static Random RANDOM = new Random();

	public final static ColorRGB WHITE = new ColorRGB(255, 255, 255, 255);
	public final static ColorRGB BLACK = new ColorRGB(  0,   0,   0, 255);


	// by default, just a white pixel
	public ColorRGB() {
		this(255, 255, 255, 255);
	}

	public ColorRGB(Color col) {
		this(col.getRed(), col.getGreen(), col.getBlue(), col.getAlpha());
	}

	public ColorRGB(byte r, byte g, byte b) {
		this(r, g, b, (byte) 255);
	}

	public ColorRGB(byte r, byte g, byte b, byte a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}

	public ColorRGB(int r, int g, int b) {
		this(r, g, b, 255);
	}

	public ColorRGB(int r, int g, int b, int a) {

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

		if (a > 255) {
			a = 255;
		}
		if (a < 0) {
			a = 0;
		}

		this.r = (byte) r;
		this.g = (byte) g;
		this.b = (byte) b;
		this.a = (byte) a;
	}

	public ColorRGB(long r, long g, long b) {
		this(r, g, b, 255l);
	}

	public ColorRGB(long r, long g, long b, long a) {

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

		if (a > 255) {
			a = 255;
		}
		if (a < 0) {
			a = 0;
		}

		this.r = (byte) r;
		this.g = (byte) g;
		this.b = (byte) b;
		this.a = (byte) a;
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

	public int getA() {
		return a & 0xFF;
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

	public byte getAByte() {
		return a;
	}

	public int getRGB() {
		return ((a & 0xFF) << 24) + ((r & 0xFF) << 16) + ((g & 0xFF) << 8) + (b & 0xFF);
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

	public int getPerceivedGrayness() {
		int intR = r & 0xFF;
		int intG = g & 0xFF;
		int intB = b & 0xFF;
		int result = (30 * intR) + (59 * intG) + (11 * intB);
		return result / 100;
	}

	public byte getPerceivedGrayByte() {
		int intR = r & 0xFF;
		int intG = g & 0xFF;
		int intB = b & 0xFF;
		int result = (30 * intR) + (59 * intG) + (11 * intB);
		return (byte) (result / 100);
	}

	/**
	 * Get a slightly different color
	 */
	public ColorRGB getSlightlyDifferent() {
		Random rand = new Random();
		int diff = 16;
		// we don't need to consider over- or underflows,
		// as the ColorRGB(r,g,b,a) constructor will do it for us
		int newR = getR() + rand.nextInt(2 * diff) - diff;
		int newG = getG() + rand.nextInt(2 * diff) - diff;
		int newB = getB() + rand.nextInt(2 * diff) - diff;
		int newA = getA();
		return new ColorRGB(newR, newG, newB, newA);
	}

	public int getDifferenceTo(ColorRGB other) {
		int result = 0;
		result += Math.abs(getR() - other.getR());
		result += Math.abs(getG() - other.getG());
		result += Math.abs(getB() - other.getB());
		result += Math.abs(getA() - other.getA());
		return result;
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

		if (a != otherColor.getAByte()) {
			return false;
		}

		return true;
	}

	public boolean fastEquals(ColorRGB other) {
		return (this.r == other.r) && (this.g == other.g) && (this.b == other.b) && (this.a == other.a);
	}

	public boolean fastSimilar(ColorRGB other) {

		int diffR = (int) this.r - (int) other.r;
		if ((diffR > 16) || (diffR < -16)) {
			return false;
		}

		int diffG = (int) this.g - (int) other.g;
		if ((diffG > 16) || (diffG < -16)) {
			return false;
		}

		int diffB = (int) this.b - (int) other.b;
		if ((diffB > 16) || (diffB < -16)) {
			return false;
		}

		int diffA = (int) this.a - (int) other.a;
		if ((diffA > 16) || (diffA < -16)) {
			return false;
		}

		return true;
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
			(int) (((((int) one.b) & 0xFF) * aO) + ((((int) two.b) & 0xFF) * aT)),
			(int) (((((int) one.a) & 0xFF) * aO) + ((((int) two.a) & 0xFF) * aT))
		);
	}

	public static ColorRGB multiply(ColorRGB one, ColorRGB two) {
		return new ColorRGB(
			((((int) one.r) & 0xFF) * (((int) two.r) & 0xFF)) / 255,
			((((int) one.g) & 0xFF) * (((int) two.g) & 0xFF)) / 255,
			((((int) one.b) & 0xFF) * (((int) two.b) & 0xFF)) / 255,
			((((int) one.a) & 0xFF) * (((int) two.a) & 0xFF)) / 255
		);
	}

	public static ColorRGB max(ColorRGB one, ColorRGB two) {
		return new ColorRGB(
			Math.max((((int) one.r) & 0xFF), (((int) two.r) & 0xFF)),
			Math.max((((int) one.g) & 0xFF), (((int) two.g) & 0xFF)),
			Math.max((((int) one.b) & 0xFF), (((int) two.b) & 0xFF)),
			Math.max((((int) one.a) & 0xFF), (((int) two.a) & 0xFF))
		);
	}

	/**
	 * Gets a random color (could be any RGB color, but non-transparent)
	 */
	public static ColorRGB random() {
		return new ColorRGB(RANDOM.nextInt(256), RANDOM.nextInt(256), RANDOM.nextInt(256));
	}

	/**
	 * Gets a random color (ensuring that the color is actually "colorful" in some way)
	 */
	public static ColorRGB randomColorful() {
		while (true) {
			int r = RANDOM.nextInt(256);
			int g = RANDOM.nextInt(256);
			int b = RANDOM.nextInt(256);
			if ((r > 64) && (r < 196)) {
				continue;
			}
			if ((g > 64) && (g < 196)) {
				continue;
			}
			if ((b > 64) && (b < 196)) {
				continue;
			}
			return new ColorRGB(r, g, b);
		}
	}

	/**
	 * Gets a random color (ensuring that the color is actually "colorful" in some way and is bright)
	 */
	public static ColorRGB randomColorfulBright() {
		while (true) {
			int r = RANDOM.nextInt(256);
			int g = RANDOM.nextInt(256);
			int b = RANDOM.nextInt(256);
			int diff1 = Math.abs(r - g);
			int diff2 = Math.abs(r - b);
			int diff3 = Math.abs(g - b);
			// avoid gray-ish colors
			if ((diff1 < 48) && (diff2 < 48) && (diff3 < 48)) {
				continue;
			}
			// avoid dark colors
			if (r + g + b < 112*3) {
				continue;
			}
			// we are now sure that the color is colorful (r, g and b are not too similar)
			// and bright (not dark)
			return new ColorRGB(r, g, b);
		}
	}

	public ColorRGB getEditedChannels(String baseForR, double modifierForR,
									  String baseForG, double modifierForG,
									  String baseForB, double modifierForB) {
		return new ColorRGB(
			getEditedChannel(baseForR, modifierForR),
			getEditedChannel(baseForG, modifierForG),
			getEditedChannel(baseForB, modifierForB)
		);
	}

	public ColorRGB getDampened(float amount) {

		return new ColorRGB(
			Math.max(255 - Math.round((255 - getR()) * amount), 0),
			Math.max(255 - Math.round((255 - getG()) * amount), 0),
			Math.max(255 - Math.round((255 - getB()) * amount), 0)
		);
	}

	public ColorRGB getRemovedColors() {

		byte gray = getGrayByte();

		return new ColorRGB(
			gray,
			gray,
			gray
		);
	}

	public ColorRGB getRemovedPerceivedColors() {

		byte gray = getPerceivedGrayByte();

		return new ColorRGB(
			gray,
			gray,
			gray
		);
	}

	public ColorRGB getInverted() {

		return new ColorRGB(
			255 - getR(),
			255 - getG(),
			255 - getB()
		);
	}

	public ColorRGB getBrightnessInverted1() {

		int targetGray = 255 - getPerceivedGrayness();

		return new ColorRGB(
			(getR() * targetGray) / 255,
			(getG() * targetGray) / 255,
			(getB() * targetGray) / 255,
			255
		);
	}

	public ColorRGB getBrightnessInverted2() {

		// we want to get a pixel such that the color distribution is the same as before,
		// but the perceived grayness is 255 - current perceived grayness

		int targetGray = 255 - getPerceivedGrayness();

		float factor = targetGray / 128f;
		float factorMul = 0.5f;

		int origR = getR();
		int origG = getG();
		int origB = getB();

		ColorRGB result = null;

		for (int steps = 0; steps < 25; steps++) {
			result = new ColorRGB(
				Math.round(origR * factor),
				Math.round(origG * factor),
				Math.round(origB * factor),
				255
			);

			if (result.getPerceivedGrayness() > targetGray) {
				factor -= factorMul;
			} else {
				factor += factorMul;
			}
			factorMul = factorMul * 0.8f;
		}

		return result;
	}

	private int getEditedChannel(String baseStr, double modifier) {
		int base = 0;
		if (baseStr != null) {
			switch (baseStr.toUpperCase()) {
				case "R":
					base = getR();
					break;
				case "G":
					base = getG();
					break;
				case "B":
					base = getB();
					break;
				case "A":
					base = getA();
					break;
				case "R+G":
					base = getR()+getG();
					break;
				case "G+B":
					base = getG()+getB();
					break;
				case "B+A":
					base = getB()+getA();
					break;
				case "R+B":
					base = getR()+getB();
					break;
				case "G+A":
					base = getG()+getA();
					break;
				case "R+A":
					base = getR()+getA();
					break;
				case "R+G+B":
					base = getR()+getG()+getB();
					break;
				case "G+B+A":
					base = getG()+getB()+getA();
					break;
				case "R+B+A":
					base = getR()+getB()+getA();
					break;
				case "R+G+A":
					base = getR()+getG()+getA();
					break;
				case "R+G+B+A":
					base = getR()+getG()+getB()+getA();
					break;
				case "0":
					base = 0;
					break;
				case "1":
					base = 1;
					break;
			}
		}

		return (int) (base * modifier);
	}

	public Color toColor() {
		return new Color(((int) r) & 0xFF, ((int) g) & 0xFF, ((int) b) & 0xFF, ((int) a) & 0xFF);
	}

	@Override
	public String toString() {
		return "RGB(" + (((int) r) & 0xFF) + ", " + (((int) g) & 0xFF) + ", " + (((int) b) & 0xFF) + ", " + (((int) a) & 0xFF) + ")";
	}

}
