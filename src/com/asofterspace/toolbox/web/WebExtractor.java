/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.web;

import com.asofterspace.toolbox.io.JSON;
import com.asofterspace.toolbox.io.JsonParseException;
import com.asofterspace.toolbox.utils.Record;

import java.util.List;


/**
 * This provides utility functions for extracting data from crawled HTML pages
 */
public class WebExtractor {

	/**
	 * Extract a JSON map / dictionary from a plain html text.
	 * You can specify to complain upon a missing dict, with a certain object shown in the message.
	 */
	public static Record extractJsonDict(String html, String strbefore, String strafter,
		boolean complainIfMissing, Object complainAbout) {

		String keyvaluesraw = extract(html, strbefore, strafter);

		if (keyvaluesraw == null) {
			if (complainIfMissing) {
				System.err.println("Could not extract JSON dict for " + complainAbout + " with strbefore: " +
					strbefore);
			}
			return Record.emptyObject();
		}

		keyvaluesraw = "{" + keyvaluesraw + "}";
		try {
			return new JSON(keyvaluesraw
				.replaceAll("&szlig;", "ß").replaceAll("&ouml;", "ö").replaceAll("&auml;", "ä")
				.replaceAll("&uuml;", "ü"));
		} catch (JsonParseException e) {
			System.err.println("Could not parse " + keyvaluesraw + " for " + complainAbout);
		}
		return Record.emptyObject();
	}

	public static String getJsonValue(Record dictionary, String key) {
		String value = dictionary.getString(key);
		if (value == null) {
			return "";
		}
		if (value.equals("no_information")) {
			return "";
		}
		return value;
	}

	public static Integer getJsonInt(Record dictionary, String key) {
		return dictionary.getInteger(key);
	}

	public static String extract(String html, String strbefore, String strafter) {
		int len = strbefore.length();
		int startindex = html.indexOf(strbefore);
		int endindex = html.indexOf(strafter, startindex + len);
		if (startindex != -1) {
			return html.substring(startindex + len, endindex);
		}
		return null;
	}

	public static Integer getNumberFromHtml(String html, String strbefore, String strafter) {
		int len = strbefore.length();
		int startindex = html.indexOf(strbefore);
		int endindex = html.indexOf(strafter, startindex + len);
		String z = html.substring(startindex + len, endindex);
		return Integer.valueOf(z);
	}

	/**
	 * We search again and again and take the highest number we found
	 */
	public static Integer getHighestNumberFromHtml(String html, String strbefore, String strafter) {
		int len = strbefore.length();
		int startindex = 0;
		int startindexnew = 0;
		Integer result = 1;
		while (true) {
			startindex = html.indexOf(strbefore, startindexnew);
			if (startindex < 0) {
				return result;
			}
			int endindex = html.indexOf(strafter, startindex + len);
			String z = html.substring(startindex + len, endindex);
			Integer curResult = Integer.valueOf(z);
			if (curResult > result) {
				result = curResult;
			}
			startindexnew = endindex;
		}
	}

	/**
	 * Get the highest number for each tuple of before and after (they are not combined in all combinations,
	 * just the first before with the first after, the second before with the second after, etc.!)
	 */
	public static Integer getHighestNumberFromHtml(String html, List<String> strsbefore, List<String> strsafter) {
		Integer result = null;
		if ((strsbefore == null) || (strsafter == null)) {
			return result;
		}
		int len = Math.min(strsbefore.size(), strsafter.size());
		for (int i = 0; i < len; i++) {
			Integer cur = getHighestNumberFromHtml(html, strsbefore.get(i), strsafter.get(i));
			if (cur == null) {
				continue;
			}
			if (result == null) {
				result = cur;
			} else {
				if (result < cur) {
					result = cur;
				}
			}
		}
		return result;
	}
}
