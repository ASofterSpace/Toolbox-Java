/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.io;

import com.asofterspace.toolbox.utils.StrUtils;


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

	/**
	 * Takes in a string such as:
	 *   foo < bar
	 * Returns a string such as:
	 * &nbsp;&nbsp;foo&nbsp;&lt;&nbsp;bar
	 * That is, replaces all spaces with &nbsp;
	 * @param str  a string that possibly contains <, >, etc. signs
	 * @return a string in which all such signs are escaped
	 */
	public static String escapeHTMLstrNbsp(Object strToEscape) {
		String escapedStr = escapeHTMLstr(strToEscape);
		return StrUtils.replaceAll(escapedStr, " ", "&nbsp;");
	}

	/**
	 * Takes in a string such as:
	 *   foo < bar
	 * Returns a string such as:
	 * &nbsp;&nbsp;foo &lt; bar
	 * That is, replaces front spaces with &nbsp;
	 * @param str  a string that possibly contains <, >, etc. signs
	 * @return a string in which all such signs are escaped
	 */
	public static String escapeHTMLstrNbspStart(Object strToEscape) {
		String escapedStr = escapeHTMLstr(strToEscape);

		// if nothing needs doing, do nothing
		if (!escapedStr.startsWith(" ")) {
			return escapedStr;
		}

		// if something needs doing, do that - that is, replace front spaces with &nbsp;
		StringBuilder result = new StringBuilder();
		while (escapedStr.startsWith(" ")) {
			escapedStr = escapedStr.substring(1);
			result.append("&nbsp;");
		}
		result.append(escapedStr);
		return result.toString();
	}

	public static String unescapeHTMLstr(String strToUnescape) {
		strToUnescape = StrUtils.replaceAll(strToUnescape, "&nbsp;", " ");
		return XML.unescapeXMLstr(strToUnescape);
	}

	public static String removeHtmlTagsFromText(String str) {
		return XML.removeXmlTagsFromText(str);
	}
}
