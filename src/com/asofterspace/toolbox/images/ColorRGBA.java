/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.images;

import com.asofterspace.toolbox.coders.HexDecoder;
import com.asofterspace.toolbox.coders.HexEncoder;
import com.asofterspace.toolbox.utils.StrUtils;

import java.awt.Color;
import java.util.List;
import java.util.Random;


/**
 * A simple RGB triplet
 *
 * @author Moya (a softer space), 2019
 */
public class ColorRGBA {

	// these fields are final as we want to be able to re-use pixels between images,
	// and just because someone paints one image green we do not want all others to
	// be green too! ;)
	private final byte r;
	private final byte g;
	private final byte b;
	private final byte a;

	private final static Random RANDOM = new Random();

	public final static ColorRGBA WHITE = new ColorRGBA(255, 255, 255, 255);
	public final static ColorRGBA BLACK = new ColorRGBA(  0,   0,   0, 255);
	public final static boolean DEFAULT_ALLOW_OVERFLOW = false;


	// by default, just a white pixel
	public ColorRGBA() {
		this(255, 255, 255, 255);
	}

	public ColorRGBA(Color col) {
		this(col.getRed(), col.getGreen(), col.getBlue(), col.getAlpha());
	}

	public ColorRGBA(byte r, byte g, byte b) {
		this(r, g, b, (byte) 255);
	}

	public ColorRGBA(byte r, byte g, byte b, byte a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}

	public ColorRGBA(int r, int g, int b) {
		this(r, g, b, 255);
	}

	public ColorRGBA(ColorRGBA other, int a) {
		this.r = other.getRByte();
		this.g = other.getGByte();
		this.b = other.getBByte();

		if (a > 255) {
			a = 255;
		}
		if (a < 0) {
			a = 0;
		}

		this.a = (byte) a;
	}

	public ColorRGBA(int r, int g, int b, int a) {
		this(r, g, b, a, DEFAULT_ALLOW_OVERFLOW);
	}

	public ColorRGBA(int r, int g, int b, int a, boolean allowOverflow) {

		if (!allowOverflow) {
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
		}

		this.r = (byte) r;
		this.g = (byte) g;
		this.b = (byte) b;
		this.a = (byte) a;
	}

	public ColorRGBA(long r, long g, long b) {
		this(r, g, b, 255l);
	}

	public ColorRGBA(long r, long g, long b, long a) {

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

	// does this color draw transparently onto others (so is it not fully opaque?)
	public boolean hasTransparency() {
		int intA = a & 0xFF;
		return (intA < 255);
	}

	public ColorRGBA getWithoutTransparency() {
		int intA = a & 0xFF;
		if (intA > 254) {
			return this;
		}
		return new ColorRGBA(r, g, b);
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

	public int getSum() {
		int intR = r & 0xFF;
		int intG = g & 0xFF;
		int intB = b & 0xFF;
		int result = intR + intG + intB;
		return result;
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
	public ColorRGBA getSlightlyDifferent() {
		Random rand = new Random();
		int diff = 16;
		// we don't need to consider over- or underflows,
		// as the ColorRGBA(r,g,b,a) constructor will do it for us
		int newR = getR() + rand.nextInt(2 * diff) - diff;
		int newG = getG() + rand.nextInt(2 * diff) - diff;
		int newB = getB() + rand.nextInt(2 * diff) - diff;
		int newA = getA();
		return new ColorRGBA(newR, newG, newB, newA);
	}

	public int getDifferenceTo(ColorRGBA other) {
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

		if (!(other instanceof ColorRGBA)) {
			return false;
		}

		ColorRGBA otherColor = (ColorRGBA) other;

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

	public boolean fastEquals(ColorRGBA other) {
		return (this.r == other.r) && (this.g == other.g) && (this.b == other.b) && (this.a == other.a);
	}

	public boolean fastSimilar(ColorRGBA other) {

		int diffR = (((int) this.r) & 0xFF) - (((int) other.r) & 0xFF);
		if ((diffR > 16) || (diffR < -16)) {
			return false;
		}

		int diffG = (((int) this.g) & 0xFF) - (((int) other.g) & 0xFF);
		if ((diffG > 16) || (diffG < -16)) {
			return false;
		}

		int diffB = (((int) this.b) & 0xFF) - (((int) other.b) & 0xFF);
		if ((diffB > 16) || (diffB < -16)) {
			return false;
		}

		int diffA = (((int) this.a) & 0xFF) - (((int) other.a) & 0xFF);
		if ((diffA > 16) || (diffA < -16)) {
			return false;
		}

		return true;
	}

	public boolean fastVaguelySimilar(ColorRGBA other) {

		int different = 0;

		int diffR = (((int) this.r) & 0xFF) - (((int) other.r) & 0xFF);
		if ((diffR > 16) || (diffR < -16)) {
			different++;
		}

		int diffG = (((int) this.g) & 0xFF) - (((int) other.g) & 0xFF);
		if ((diffG > 16) || (diffG < -16)) {
			different++;
		}

		int diffB = (((int) this.b) & 0xFF) - (((int) other.b) & 0xFF);
		if ((diffB > 16) || (diffB < -16)) {
			different++;
		}

		int diffA = (((int) this.a) & 0xFF) - (((int) other.a) & 0xFF);
		if ((diffA > 16) || (diffA < -16)) {
			different++;
		}

		return different < 2;
	}

	public int fastDiff(ColorRGBA other) {

		int diffR = (((int) this.r) & 0xFF) - (((int) other.r) & 0xFF);
		int diffG = (((int) this.g) & 0xFF) - (((int) other.g) & 0xFF);
		int diffB = (((int) this.b) & 0xFF) - (((int) other.b) & 0xFF);
		int diffA = (((int) this.a) & 0xFF) - (((int) other.a) & 0xFF);

		return Math.abs(diffR) + Math.abs(diffG) + Math.abs(diffB) + Math.abs(diffA);
	}

	/**
	 * Draw this current color onto a background color and return the result
	 */
	public ColorRGBA drawTransparentlyOnto(ColorRGBA backgroundColor) {
		// shortcut in case we are at fully opaque / non-transparent drawing
		int intA = ((int) a) & 0xFF;
		if (intA > 254) {
			return this;
		}

		double amountOfOne = intA / 255.0;
		double amountOfTwo = 1 - amountOfOne;

		return new ColorRGBA(
			(int) (((((int) r) & 0xFF) * amountOfOne) + ((((int) backgroundColor.r) & 0xFF) * amountOfTwo)),
			(int) (((((int) g) & 0xFF) * amountOfOne) + ((((int) backgroundColor.g) & 0xFF) * amountOfTwo)),
			(int) (((((int) b) & 0xFF) * amountOfOne) + ((((int) backgroundColor.b) & 0xFF) * amountOfTwo)),
			255
		);
	}

	/**
	 * intermix two colors, where the amount of the first color in the mix is given,
	 * e.g. 0.45 for 45% one, 55% two
	 */
	public static ColorRGBA intermix(ColorRGBA one, ColorRGBA two, double amountOfOne) {

		if (amountOfOne < 0) {
			amountOfOne = 0;
		}
		if (amountOfOne > 1) {
			amountOfOne = 1;
		}

		double amountOfTwo = 1 - amountOfOne;

		return new ColorRGBA(
			(int) (((((int) one.r) & 0xFF) * amountOfOne) + ((((int) two.r) & 0xFF) * amountOfTwo)),
			(int) (((((int) one.g) & 0xFF) * amountOfOne) + ((((int) two.g) & 0xFF) * amountOfTwo)),
			(int) (((((int) one.b) & 0xFF) * amountOfOne) + ((((int) two.b) & 0xFF) * amountOfTwo)),
			(int) (((((int) one.a) & 0xFF) * amountOfOne) + ((((int) two.a) & 0xFF) * amountOfTwo))
		);
	}

	/**
	 * intermix lots of colors, ignoring null ones
	 */
	public static ColorRGBA mixPix(List<ColorRGBA> pixList) {

		int amountOfPix = 0;
		int curR = 0;
		int curG = 0;
		int curB = 0;
		int curA = 0;

		for (ColorRGBA pix : pixList) {
			if (pix != null) {
				amountOfPix++;
				curR += ((int) pix.r) & 0xFF;
				curG += ((int) pix.g) & 0xFF;
				curB += ((int) pix.b) & 0xFF;
				curA += ((int) pix.a) & 0xFF;
			}
		}

		if (amountOfPix < 1) {
			return null;
		}

		return new ColorRGBA(
			(curR / amountOfPix),
			(curG / amountOfPix),
			(curB / amountOfPix),
			(curA / amountOfPix)
		);
	}

	public static ColorRGBA multiply(ColorRGBA one, ColorRGBA two) {
		return new ColorRGBA(
			((((int) one.r) & 0xFF) * (((int) two.r) & 0xFF)) / 255,
			((((int) one.g) & 0xFF) * (((int) two.g) & 0xFF)) / 255,
			((((int) one.b) & 0xFF) * (((int) two.b) & 0xFF)) / 255,
			((((int) one.a) & 0xFF) * (((int) two.a) & 0xFF)) / 255
		);
	}

	public static ColorRGBA max(ColorRGBA one, ColorRGBA two) {
		return new ColorRGBA(
			Math.max((((int) one.r) & 0xFF), (((int) two.r) & 0xFF)),
			Math.max((((int) one.g) & 0xFF), (((int) two.g) & 0xFF)),
			Math.max((((int) one.b) & 0xFF), (((int) two.b) & 0xFF)),
			Math.max((((int) one.a) & 0xFF), (((int) two.a) & 0xFF))
		);
	}

	public static ColorRGBA min(ColorRGBA one, ColorRGBA two) {
		return new ColorRGBA(
			Math.min((((int) one.r) & 0xFF), (((int) two.r) & 0xFF)),
			Math.min((((int) one.g) & 0xFF), (((int) two.g) & 0xFF)),
			Math.min((((int) one.b) & 0xFF), (((int) two.b) & 0xFF)),
			Math.min((((int) one.a) & 0xFF), (((int) two.a) & 0xFF))
		);
	}

	/**
	 * Gets a random color (could be any RGB color, but non-transparent)
	 */
	public static ColorRGBA random() {
		return new ColorRGBA(RANDOM.nextInt(256), RANDOM.nextInt(256), RANDOM.nextInt(256));
	}

	/**
	 * Gets a random color (ensuring that the color is actually "colorful" in some way)
	 */
	public static ColorRGBA randomColorful() {
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
			return new ColorRGBA(r, g, b);
		}
	}

	/**
	 * Gets a random color (ensuring that the color is actually "colorful" in some way and is bright)
	 */
	public static ColorRGBA randomColorfulBright() {
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
			return new ColorRGBA(r, g, b);
		}
	}

	public ColorRGBA getEditedChannels(String baseForR, double modifierForR,
									  String baseForG, double modifierForG,
									  String baseForB, double modifierForB) {
		return getEditedChannels(
			baseForR, modifierForR,
			baseForG, modifierForG,
			baseForB, modifierForB,
			DEFAULT_ALLOW_OVERFLOW);
	}

	public ColorRGBA getEditedChannels(String baseForR, double modifierForR,
									  String baseForG, double modifierForG,
									  String baseForB, double modifierForB,
									  boolean allowOverflow) {
		return new ColorRGBA(
			getEditedChannel(baseForR, modifierForR),
			getEditedChannel(baseForG, modifierForG),
			getEditedChannel(baseForB, modifierForB),
			getA(),
			allowOverflow
		);
	}

	public ColorRGBA getEditedChannels(String baseForR, double modifierForR,
									  String baseForG, double modifierForG,
									  String baseForB, double modifierForB,
									  String baseForA, double modifierForA) {
		return getEditedChannels(
			baseForR, modifierForR,
			baseForG, modifierForG,
			baseForB, modifierForB,
			baseForA, modifierForA,
			DEFAULT_ALLOW_OVERFLOW);
	}

	public ColorRGBA getEditedChannels(String baseForR, double modifierForR,
									  String baseForG, double modifierForG,
									  String baseForB, double modifierForB,
									  String baseForA, double modifierForA,
									  boolean allowOverflow) {
		return new ColorRGBA(
			getEditedChannel(baseForR, modifierForR),
			getEditedChannel(baseForG, modifierForG),
			getEditedChannel(baseForB, modifierForB),
			getEditedChannel(baseForA, modifierForA),
			allowOverflow
		);
	}

	public ColorRGBA getDampened(float amount) {

		return new ColorRGBA(
			Math.max(255 - Math.round((255 - getR()) * amount), 0),
			Math.max(255 - Math.round((255 - getG()) * amount), 0),
			Math.max(255 - Math.round((255 - getB()) * amount), 0),
			getA()
		);
	}

	public ColorRGBA getRemovedColors() {

		byte gray = getGrayByte();

		return new ColorRGBA(
			gray,
			gray,
			gray,
			getAByte()
		);
	}

	public ColorRGBA getRemovedPerceivedColors() {

		byte gray = getPerceivedGrayByte();

		return new ColorRGBA(
			gray,
			gray,
			gray,
			getAByte()
		);
	}

	public ColorRGBA getInverted() {

		return new ColorRGBA(
			255 - getR(),
			255 - getG(),
			255 - getB(),
			getA()
		);
	}

	public ColorRGBA getBrightnessInverted1() {

		int targetGray = 255 - getPerceivedGrayness();

		return new ColorRGBA(
			(getR() * targetGray) / 255,
			(getG() * targetGray) / 255,
			(getB() * targetGray) / 255,
			getA()
		);
	}

	public ColorRGBA getBrightnessInverted2() {

		// we want to get a pixel such that the color distribution is the same as before,
		// but the perceived grayness is 255 - current perceived grayness

		int targetGray = 255 - getPerceivedGrayness();

		float factor = targetGray / 128f;
		float factorMul = 0.5f;

		int origR = getR();
		int origG = getG();
		int origB = getB();

		ColorRGBA result = null;

		for (int steps = 0; steps < 25; steps++) {
			result = new ColorRGBA(
				Math.round(origR * factor),
				Math.round(origG * factor),
				Math.round(origB * factor),
				getA()
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

	public static ColorRGBA fromString(String str) {

		if (str == null) {
			System.err.println("Color string <null> could not be parsed!");
			return new ColorRGBA();
		}

		str = StrUtils.replaceAll(str, " ", "");
		str = StrUtils.replaceAll(str, "\t", "");
		str = StrUtils.replaceAll(str, ";", ",");
		str = str.toLowerCase();

		// interpret both rgb and rgba in the same way - depending on how many arguments follow
		if (str.startsWith("rgb")) {
			str = str.substring(str.indexOf("(") + 1);
			String rStr = str.substring(0, str.indexOf(","));
			str = str.substring(str.indexOf(",") + 1);
			String gStr = str.substring(0, str.indexOf(","));
			str = str.substring(str.indexOf(",") + 1);
			String bStr = "0";
			if (str.indexOf(",") < 0) {
				bStr = str.substring(0, str.indexOf(")"));
				return new ColorRGBA(StrUtils.strToInt(rStr), StrUtils.strToInt(gStr), StrUtils.strToInt(bStr));
			} else {
				bStr = str.substring(0, str.indexOf(","));
				str = str.substring(str.indexOf(",") + 1);
				String aStr = str.substring(0, str.indexOf(")"));
				return new ColorRGBA(StrUtils.strToInt(rStr), StrUtils.strToInt(gStr), StrUtils.strToInt(bStr), StrUtils.strToInt(aStr));
			}
		}

		if (str.startsWith("#")) {
			// #RRGGBB
			if (str.length() == 7) {
				String rStr = "" + str.charAt(1) + str.charAt(2);
				String gStr = "" + str.charAt(3) + str.charAt(4);
				String bStr = "" + str.charAt(5) + str.charAt(6);
				return new ColorRGBA(HexDecoder.decodeInt(rStr), HexDecoder.decodeInt(gStr), HexDecoder.decodeInt(bStr));
			}
			// #RGB
			if (str.length() == 4) {
				String rStr = "" + str.charAt(1) + str.charAt(1);
				String gStr = "" + str.charAt(2) + str.charAt(2);
				String bStr = "" + str.charAt(3) + str.charAt(3);
				return new ColorRGBA(HexDecoder.decodeInt(rStr), HexDecoder.decodeInt(gStr), HexDecoder.decodeInt(bStr));
			}
			// #RRGGBBAA
			if (str.length() == 9) {
				String rStr = "" + str.charAt(1) + str.charAt(2);
				String gStr = "" + str.charAt(3) + str.charAt(4);
				String bStr = "" + str.charAt(5) + str.charAt(6);
				String aStr = "" + str.charAt(7) + str.charAt(8);
				return new ColorRGBA(HexDecoder.decodeInt(rStr), HexDecoder.decodeInt(gStr), HexDecoder.decodeInt(bStr), HexDecoder.decodeInt(aStr));
			}
			// #RGBA
			if (str.length() == 5) {
				String rStr = "" + str.charAt(1) + str.charAt(1);
				String gStr = "" + str.charAt(2) + str.charAt(2);
				String bStr = "" + str.charAt(3) + str.charAt(3);
				String aStr = "" + str.charAt(4) + str.charAt(4);
				return new ColorRGBA(HexDecoder.decodeInt(rStr), HexDecoder.decodeInt(gStr), HexDecoder.decodeInt(bStr), HexDecoder.decodeInt(aStr));
			}
		}

		System.err.println("The color string '" + str + "' could not be parsed!");
		return new ColorRGBA();
	}

	@Override
	public String toString() {
		return "rgba(" + (((int) r) & 0xFF) + ", " + (((int) g) & 0xFF) + ", " + (((int) b) & 0xFF) + ", " + (((int) a) & 0xFF) + ")";
	}

	public String toHexString() {
		return "#" + HexEncoder.encodeNumberToHex(((int) r) & 0xFF, 2) + HexEncoder.encodeNumberToHex(((int) g) & 0xFF, 2) + HexEncoder.encodeNumberToHex(((int) b) & 0xFF, 2);
	}

}
