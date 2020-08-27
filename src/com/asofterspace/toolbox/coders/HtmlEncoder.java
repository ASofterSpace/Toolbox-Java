/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.coders;

import com.asofterspace.toolbox.io.XML;


/**
 * A class that can (badly) encode strings for HTML
 */
public class HtmlEncoder {

	/**
	 * Takes R&D and converts it to R&amp;D
	 */
	public static String encode(String str) {

		return XML.escapeXMLstr(str);
	}

}
