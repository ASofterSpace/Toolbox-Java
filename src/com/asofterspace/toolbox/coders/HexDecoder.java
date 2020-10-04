/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.coders;

import java.math.BigInteger;


/**
 * A class that can dencode numbers from their hexadecimal representations
 *
 * @author Moya (a softer space, 2018)
 */
public class HexDecoder {

	private static byte hexCharToByte(char hexChar) {
		switch (hexChar) {
			case '0':
				return 0;
			case '1':
				return 1;
			case '2':
				return 2;
			case '3':
				return 3;
			case '4':
				return 4;
			case '5':
				return 5;
			case '6':
				return 6;
			case '7':
				return 7;
			case '8':
				return 8;
			case '9':
				return 9;
			case 'a':
			case 'A':
				return 10;
			case 'b':
			case 'B':
				return 11;
			case 'c':
			case 'C':
				return 12;
			case 'd':
			case 'D':
				return 13;
			case 'e':
			case 'E':
				return 14;
			case 'f':
			case 'F':
				return 15;
		}
		// ideally, ignore these characters instead of returning 0...
		return 0;
	}

	private static String cleanUpInputStr(String hexStr) {

		hexStr = hexStr.replaceAll("0x", "");
		hexStr = hexStr.replaceAll(" ", "");
		hexStr = hexStr.replaceAll("\t", "");
		hexStr = hexStr.replaceAll("\r", "");
		hexStr = hexStr.replaceAll("\n", "");
		hexStr = hexStr.replaceAll("-", "");
		hexStr = hexStr.replaceAll("_", "");
		hexStr = hexStr.replaceAll(",", "");
		hexStr = hexStr.replaceAll(";", "");
		hexStr = hexStr.replaceAll("<", "");
		hexStr = hexStr.replaceAll(">", "");

		return hexStr;
	}

	public static byte[] decodeBytes(String hexStr) {

		hexStr = cleanUpInputStr(hexStr);

		int len = hexStr.length();

		if (len % 2 == 1) {
			hexStr = "0" + hexStr;
			len++;
		}

		len = len / 2;

		byte[] result = new byte[len];

		for (int i = 0; i < len; i++) {
			result[i] = (byte) ((16 * hexCharToByte(hexStr.charAt(i*2))) + hexCharToByte(hexStr.charAt((i*2)+1)));
		}

		return result;
	}

	public static String decode(String hexStr) {

		return new String(decodeBytes(hexStr));
	}

	public static int decodeInt(String hexStr) {

		hexStr = cleanUpInputStr(hexStr);

		int len = hexStr.length();

		int result = 0;
		int factor = 1;

		for (int i = len - 1; i >= 0; i--) {
			result += factor * (int) hexCharToByte(hexStr.charAt(i));
			factor *= 16;
		}

		return result;
	}

	public static BigInteger decodeNumber(String hexStr) {

		hexStr = cleanUpInputStr(hexStr);

		int len = hexStr.length();

		BigInteger result = BigInteger.ZERO;
		BigInteger factor = BigInteger.ONE;
		BigInteger SIXTEEN = new BigInteger("16");

		for (int i = len - 1; i >= 0; i--) {
			byte[] bytes = new byte[] {hexCharToByte(hexStr.charAt(i))};
			result = result.add(factor.multiply(new BigInteger(bytes)));
			factor = factor.multiply(SIXTEEN);
		}

		return result;
	}

}
