/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.io;


public class HTML {

	/**
	 * Takes in a string such as:
	 * foo<bar
	 * Returns a string such as:
	 * foo&lt;bar
	 * @param str  a string that possibly contains <, >, etc. signs
	 * @return a string in which all such signs are escaped
	 */
	public static String escapeHTMLstr(Object strToEscape) {
		return XML.escapeXMLstr(strToEscape);
	}

	public static String unescapeHTMLstr(String strToUnescape) {
		return XML.unescapeXMLstr(strToUnescape);
	}

	public static String removeXmlTagsFromText(String str) {
		return XML.removeXmlTagsFromText(str);
	}
}
