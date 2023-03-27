/**
 * Unlicensed code created by A Softer Space, 2023
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.coders;

import com.asofterspace.toolbox.utils.StrUtils;


/**
 * A class that can prettify generic texts
 */
public class GenericPrettifier {

	public static String magicallyFixText(String text) {

		if (text.contains("&amp;") || text.contains("&nbsp;")) {
			text = HtmlDecoder.decode(text);
		}

		if (text.contains("%20")) {
			text = UrlDecoder.decode(text);
		}

		text = StrUtils.replaceAll(text, "o¨", "ö");
		text = StrUtils.replaceAll(text, "a¨", "ä");
		text = StrUtils.replaceAll(text, "u¨", "ü");
		text = StrUtils.replaceAll(text, "O¨", "Ö");
		text = StrUtils.replaceAll(text, "Ä¨", "Ä");
		text = StrUtils.replaceAll(text, "U¨", "Ü");
		text = StrUtils.replaceAll(text, "…", "...");

		// this is a special dash (not the regular -) which is used to separate word parts for a possible
		// line break - but gets in the way of reading the text when it is not hidden, so just remove it
		text = StrUtils.replaceAll(text, "­", "");

		// replace lines do not end with whitespace
		text = text + "\n";
		boolean somethingChanged = true;
		while (somethingChanged) {
			int textLen = text.length();
			text = StrUtils.replaceAll(text, "\t\r\n", "\r\n");
			text = StrUtils.replaceAll(text, " \r\n", "\r\n");
			text = StrUtils.replaceAll(text, "\t\n", "\n");
			text = StrUtils.replaceAll(text, " \n", "\n");
			somethingChanged = (textLen != text.length());
		}
		text = text.substring(0, text.length() - 1);

		return text;
	}

}
