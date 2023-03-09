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

		return text;
	}

}
