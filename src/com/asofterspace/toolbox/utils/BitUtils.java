/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.utils;


/**
 * A utility class for bit operations, including:
 *			 bits .. booleans
 *		  nibbles .. four bits
 *			bytes .. eight bits (java-ly interpreted as -128..127)
 * unsigned bytes .. eight bits, as int, such that they are 0..255
 *			 ints .. java ints, so 32 bits
 *		bit array .. array or bits (so booleans)
 *
 * @author Moya (a softer space), 2019
 */
public class BitUtils {

	public static boolean equals(boolean[] left, boolean[] right) {

		if ((left == null) && (right == null)) {
			return true;
		}

		if (left == null) {
			return false;
		}

		if (right == null) {
			return false;
		}

		if (left.length != right.length) {
			return false;
		}

		for (int i = 0; i < left.length; i++) {
			if (left[i] != right[i]) {
				return false;
			}
		}

		return true;
	}

	public static boolean[] byteToBits(byte b) {

		boolean[] result = new boolean[8];
		result[0] = ((b >>> 7) & 0x1) > 0;
		result[1] = ((b >>> 6) & 0x1) > 0;
		result[2] = ((b >>> 5) & 0x1) > 0;
		result[3] = ((b >>> 4) & 0x1) > 0;
		result[4] = ((b >>> 3) & 0x1) > 0;
		result[5] = ((b >>> 2) & 0x1) > 0;
		result[6] = ((b >>> 1) & 0x1) > 0;
		result[7] = (b & 0x1) > 0;
		return result;
	}

	public static boolean[] unsignedByteToBits(int b) {

		boolean[] result = new boolean[8];
		result[0] = ((b >>> 7) & 0x1) > 0;
		result[1] = ((b >>> 6) & 0x1) > 0;
		result[2] = ((b >>> 5) & 0x1) > 0;
		result[3] = ((b >>> 4) & 0x1) > 0;
		result[4] = ((b >>> 3) & 0x1) > 0;
		result[5] = ((b >>> 2) & 0x1) > 0;
		result[6] = ((b >>> 1) & 0x1) > 0;
		result[7] = (b & 0x1) > 0;
		return result;
	}

	public static boolean[] intToBits(int input) {

		int length = 32;

		boolean[] result = new boolean[length];

		int twoThePower = 1;

		for (int i = length - 1; i >= 0; i--) {
			if ((input / twoThePower) % 2 == 1) {
				result[i] = true;
			}
			twoThePower *= 2;
		}

		return result;
	}

	public static boolean[] bytesToBits(byte[] byteArr) {

		boolean[] result = new boolean[8 * byteArr.length];

		int cur = 0;

		for (int i = 0; i < byteArr.length; i++) {
			byte b = byteArr[i];
			result[cur+0] = ((b >>> 7) & 0x1) > 0;
			result[cur+1] = ((b >>> 6) & 0x1) > 0;
			result[cur+2] = ((b >>> 5) & 0x1) > 0;
			result[cur+3] = ((b >>> 4) & 0x1) > 0;
			result[cur+4] = ((b >>> 3) & 0x1) > 0;
			result[cur+5] = ((b >>> 2) & 0x1) > 0;
			result[cur+6] = ((b >>> 1) & 0x1) > 0;
			result[cur+7] = (b & 0x1) > 0;
			cur += 8;
		}

		return result;
	}

	/**
	 * Converts an array of integers (only containing numbers between 0 and 255)
	 * to an array of boolean bits
	 */
	public static boolean[] unsignedBytesToBits(int[] byteArr) {

		boolean[] result = new boolean[8 * byteArr.length];

		int cur = 0;

		for (int i = 0; i < byteArr.length; i++) {
			int b = byteArr[i];
			result[cur+0] = ((b >>> 7) & 0x1) > 0;
			result[cur+1] = ((b >>> 6) & 0x1) > 0;
			result[cur+2] = ((b >>> 5) & 0x1) > 0;
			result[cur+3] = ((b >>> 4) & 0x1) > 0;
			result[cur+4] = ((b >>> 3) & 0x1) > 0;
			result[cur+5] = ((b >>> 2) & 0x1) > 0;
			result[cur+6] = ((b >>> 1) & 0x1) > 0;
			result[cur+7] = (b & 0x1) > 0;
			cur += 8;
		}

		return result;
	}

	public static byte bitsToNibble(boolean b1, boolean b2, boolean b3, boolean b4) {

		return (byte) ((b1?8:0) + (b2?4:0) + (b3?2:0) + (b4?1:0));
	}

	public static byte bitsToByte(boolean b1, boolean b2, boolean b3, boolean b4, boolean b5, boolean b6, boolean b7, boolean b8) {

		return (byte) ((b1?128:0) + (b2?64:0) + (b3?32:0) + (b4?16:0) + (b5?8:0) + (b6?4:0) + (b7?2:0) + (b8?1:0));
	}

	public static byte bitsToByte(boolean[] b) {

		return (byte) ((b[0]?128:0) + (b[1]?64:0) + (b[2]?32:0) + (b[3]?16:0) + (b[4]?8:0) + (b[5]?4:0) + (b[6]?2:0) + (b[7]?1:0));
	}

	public static int bitsToUnsignedByte(boolean b1, boolean b2, boolean b3, boolean b4, boolean b5, boolean b6, boolean b7, boolean b8) {

		return ((b1?128:0) + (b2?64:0) + (b3?32:0) + (b4?16:0) + (b5?8:0) + (b6?4:0) + (b7?2:0) + (b8?1:0));
	}

	public static int bitsToUnsignedByte(boolean[] b) {

		return ((b[0]?128:0) + (b[1]?64:0) + (b[2]?32:0) + (b[3]?16:0) + (b[4]?8:0) + (b[5]?4:0) + (b[6]?2:0) + (b[7]?1:0));
	}

	public static int bitsToInt(boolean[] bitArr, int offset, int length) {

		int result = 0;
		int twoThePower = 1;

		for (int i = length - 1; i >= 0; i--) {
			if (bitArr[offset + i]) {
				result += twoThePower;
			}
			twoThePower *= 2;
		}

		return result;
	}

	public static byte[] bitsToByteArr(boolean[] bitArr, int offset, int length) {

		int byteLen = length / 8;
		byte[] result = new byte[byteLen];

		for (int i = 0; i < byteLen; i++) {
			result[i] = (byte) bitsToInt(bitArr, offset + 8*i, 8);
		}

		return result;
	}

	/**
	 * Reads an array of bits (expressed as booleans) into an array of unsigned bytes -
	 * and as unsigned bytes don't exist in Java, they are represented by ints...
	 */
	public static int[] bitsToUnsignedByteArr(boolean[] bitArr, int offset, int length) {

		int byteLen = length / 8;
		int[] result = new int[byteLen];

		for (int i = 0; i < byteLen; i++) {
			result[i] = bitsToInt(bitArr, offset + 8*i, 8);
		}

		return result;
	}

	public static void readBitsIntoBitArr(boolean[] bitArr, int offset, boolean b1, boolean b2, boolean b3, boolean b4, boolean b5, boolean b6, boolean b7, boolean b8) {

		bitArr[offset+0] = b8;
		bitArr[offset+1] = b7;
		bitArr[offset+2] = b6;
		bitArr[offset+3] = b5;
		bitArr[offset+4] = b4;
		bitArr[offset+5] = b3;
		bitArr[offset+6] = b2;
		bitArr[offset+7] = b1;
	}

	public static String toString(boolean[] bitArr) {

		StringBuilder result = new StringBuilder();

		for (int i = 0; i < bitArr.length; i++) {
			result.append(bitArr[i] ? '1' : '0');
		}

		return result.toString();
	}

}
