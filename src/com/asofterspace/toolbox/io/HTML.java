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
		return XML.escapeXMLstr(strToEscape, "&#10;", true);
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

	/**
	 * Takes a line that has naively been encoded from plain text into HTML
	 * and prettifies it for nicer display in HTML
	 */
	public static String prettifyLine(String line) {

		// if we have an enumeration, with *, -, > or >> as bullet points...
		if (line.startsWith("* ") || line.startsWith("- ") || line.startsWith("&gt; ") || line.startsWith("&gt;&gt; ") ||
			line.startsWith("-&gt; ") ||
			line.startsWith("&nbsp;") || line.startsWith("&#9;") || line.startsWith(" ") || line.startsWith("\t")) {

			// ... set level 0 by default / for the top-most level...
			int spaceCounter = 0;
			// ... and then we count the level depth
			while (line.startsWith("&nbsp;") || line.startsWith("&#9;") || line.startsWith(" ") || line.startsWith("\t")) {
				if (line.startsWith("\t") || line.startsWith("&#9;")) {
					if (line.startsWith("\t")) {
						line = line.substring(1);
					} else {
						line = line.substring(4);
					}
					spaceCounter = spaceCounter + 4;
				} else {
					if (line.startsWith(" ")) {
						line = line.substring(1);
					} else {
						line = line.substring(6);
					}
					spaceCounter++;
				}
			}

			StringBuilder lineStartIndentBuilder = new StringBuilder();
			for (int i = 0; i < spaceCounter; i++) {
				lineStartIndentBuilder.append("&nbsp;");
			}

			String lineStartIndent = "";
			if (spaceCounter > 0) {
				lineStartIndent = "<span style='position:absolute;left: 0;'>" + lineStartIndentBuilder.toString() + "</span>";
			}

			// add bullet point and increase level of indentation so that the text flows vertically besides the bullet point
			// (oh and here we have space before and behind the enumeration sign in the <span> so that when text is copied
			// out, it is copied correctly with the space ^^)

			if (line.startsWith("* ")) {
				line = "<span style='position:absolute;left:" + (3*spaceCounter) + "pt;top:2pt;'>* </span>" + line.substring(2);
				spaceCounter += 3;
			} else {
				if (line.startsWith("- ")) {
					line = "<span style='position:absolute;left:" + (3*spaceCounter) + "pt;'>- </span>" + line.substring(2);
					spaceCounter += 3;
				} else {
					if (line.startsWith("&gt; ")) {
						line = "<span style='position:absolute;left:" + (3*spaceCounter) + "pt;'>&gt; </span>" + line.substring(5);
						spaceCounter += 3;
					} else {
						if (line.startsWith("&gt;&gt; ")) {
							line = "<span style='position:absolute;left:" + (3*spaceCounter) + "pt;'>&gt;&gt; </span>" + line.substring(9);
							spaceCounter += 5;
						} else {
							if (line.startsWith("-&gt; ")) {
								line = "<span style='position:absolute;left:" + (3*spaceCounter) + "pt;'>-&gt; </span>" + line.substring(6);
								spaceCounter += 5;
							}
						}
					}
				}
			}
			line = "<span style='position:relative;padding-left:" + (3*spaceCounter) + "pt;display:inline-block;'>" +
				lineStartIndent + line + "</span>";
		}

		return line;
	}

}
