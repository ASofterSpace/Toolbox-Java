/**
 * Unlicensed code created by A Softer Space, 2023
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.coders;


/**
 * A class that can encode text into its numerical representations
 *
 * @author Moya (a softer space, 2023)
 */
public class NumericalEncoder {

	public static String encode(String text) {

		StringBuilder result = new StringBuilder();
		String sep = "";

		for (int i = 0; i < text.length(); i++) {
			result.append(sep);
			sep = ",";
			result.append((int) text.charAt(i));
		}

		return result.toString();
	}
}
