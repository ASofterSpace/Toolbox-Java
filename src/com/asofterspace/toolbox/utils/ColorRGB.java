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

	private byte r;
	private byte g;
	private byte b;


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

	public byte getRByte() {
		return r;
	}

	public byte getGByte() {
		return g;
	}

	public byte getBByte() {
		return b;
	}

	public String toString() {
		return "RGB(" + r + ", " + g + ", " + b + ")";
	}

}
