/**
 * Unlicensed code created by A Softer Space, 2026
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.coders;


/**
 * A class that can (badly) encode PDF strings
 */
public class PdfTextEncoder {

	public static String encode(String text) {

		if (text == null) {
			return "()";
		}

		StringBuilder result = new StringBuilder();

		result.append("(");

		for (int i = 0; i < text.length(); i++) {

			char c = text.charAt(i);

			if (((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z'))) {
				result.append(c);
			} else {
				result.append(toPdfOctalStr(c));
			}
		}

		result.append(")");

		return result.toString();
	}

	private static String toPdfOctalStr(char c) {

		// using WinAnsiEncoding, we could hardcode some values...
		/*
		switch (c) {
			case 'ä':
				return "\\344";
			case 'Ä':
				return "\\304";
			case 'ö':
				return "\\366";
			case 'Ö':
				return "\\326";
			case 'ü':
				return "\\374";
			case 'Ü':
				return "\\334";
			case 'ß':
				return "\\337";
			case '–':
				return "\\226";
		}
		*/

		// ... but actually let's encode them as oct values generically
		int num = (int) c;
		while (num < 0) {
			num += 256;
		}
		if (num > 511) {
			return "\\" + (num / (8*8*8)) + ((num / 8) % 8) + ((num / (8*8)) % 8) + (num % 8);
		}
		if (num > 63) {
			return "\\" + (num / (8*8)) + ((num / 8) % 8) + (num % 8);
		}
		if (num > 7) {
			return "\\" + (num / 8) + (num % 8);
		}
		return "\\" + num;
	}

}
