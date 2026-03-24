/**
 * Unlicensed code created by A Softer Space, 2026
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.coders;


/**
 * A class that can (badly) decodes PDF strings
 */
public class PdfTextDecoder {

	/**
	 * In general, there are two PDF string encodings:
	 * <HEX> where HEX contains various hex codes, with option ignored whitespace
	 * (text) where text contains ASCII plaintext and/or escaped octal char values
	 */
	public static String decode(String str) {

		if (str == null) {
			return "";
		}

		str = str.trim();

		if (str.startsWith("<")) {
			// hex mode
			return "ERROR: Sorry, not yet implemented!";
		}

		if (str.startsWith("(")) {
			str = str.substring(1);
			if (str.endsWith(")")) {
				str = str.substring(0, str.length() - 1);
			}

			// TODO

			return str;
		}

		return "ERROR: Malformed input text!";
	}

}
