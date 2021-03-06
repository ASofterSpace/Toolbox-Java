/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.coders;

import com.asofterspace.toolbox.utils.StrUtils;


/**
 * A class that can encode numbers into their hexadecimal representations
 *
 * @author Moya (a softer space, 2018)
 */
public class HexEncoder {

	private static char charToHex(int in) {
		switch (in) {
			case 0:
				return '0';
			case 1:
				return '1';
			case 2:
				return '2';
			case 3:
				return '3';
			case 4:
				return '4';
			case 5:
				return '5';
			case 6:
				return '6';
			case 7:
				return '7';
			case 8:
				return '8';
			case 9:
				return '9';
			case 10:
				return 'A';
			case 11:
				return 'B';
			case 12:
				return 'C';
			case 13:
				return 'D';
			case 14:
				return 'E';
			case 15:
				return 'F';
		}

		return ' ';
	}

	public static String encodeNumberToHex(int num) {
		String result = "";
		while (num > 0) {
			result = charToHex(num % 16) + result;
			num = num / 16;
		}
		return result;
	}

	public static String encodeNumberToHex(int num, int digits) {
		return StrUtils.leftPad(encodeNumberToHex(num), '0', digits);
	}

	public static String encode(String text) {

		StringBuilder result = new StringBuilder();

		for (int i = 0; i < text.length(); i++) {
			result.append(charToHex(text.charAt(i) / 16));
			result.append(charToHex(text.charAt(i) % 16));
		}

		return result.toString();
	}
}
