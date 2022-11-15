/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.coders;

import com.asofterspace.toolbox.io.HTML;


/**
 * A class that can (badly) decode HTML-encoded strings
 */
public class HtmlDecoder {

	/**
	 * Takes R&amp;D and converts to R&D
	 */
	public static String decode(String str) {

		return HTML.unescapeHTMLstr(str);
	}

}
