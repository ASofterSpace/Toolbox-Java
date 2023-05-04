/**
 * Unlicensed code created by A Softer Space, 2023
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.coders;

import com.asofterspace.toolbox.utils.StrUtils;


/**
 * A class that can decode numerical representations back into text
 *
 * @author Moya (a softer space, 2023)
 */
public class NumericalDecoder {

	public static String decode(String text) {

		StringBuilder result = new StringBuilder();
		int cur = 0;

		while (cur < text.length()) {
			int nextTextEnd = text.length();
			int nextComma = text.indexOf(",", cur);
			if (nextComma < 0) {
				nextComma = nextTextEnd;
			}
			int nextSemiComma = text.indexOf(";", cur);
			if (nextSemiComma < 0) {
				nextSemiComma = nextTextEnd;
			}
			int nextSpace = text.indexOf(" ", cur);
			if (nextSpace < 0) {
				nextSpace = nextTextEnd;
			}
			int until = Math.min(Math.min(nextTextEnd, nextComma), Math.min(nextSemiComma, nextSpace));

			String numStr = text.substring(cur, until).trim();
			if (numStr.length() > 0) {
				int num = StrUtils.strToInt(numStr);
				result.append((char) num);
			}
			cur = until + 1;
		}

		return result.toString();
	}
}
