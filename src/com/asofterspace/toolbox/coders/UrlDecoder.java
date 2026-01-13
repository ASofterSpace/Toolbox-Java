/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.coders;

import com.asofterspace.toolbox.utils.StrUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;


/**
 * A class that can (badly) decode URL strings
 *
 * @author Moya (a softer space, 2018)
 */
public class UrlDecoder {

	/**
	 * Takes http%3A%2F%2Fwww.foo.org%2Fsections%2Fbar
	 * and converts to http://www.foo.org/sections/bar
	 * @param url  The encoded url
	 * @return The decoded url in plain text
	 */
	public static String decode(String urlString) {

		if (urlString == null) {
			return null;
		}

		try {

			return URLDecoder.decode(urlString, "UTF-8");

		} catch (UnsupportedEncodingException e) {

			return urlString;
		}
	}

	public static String decodeLeavePlusses(String urlString) {

		if (urlString == null) {
			return null;
		}

		if (!urlString.contains("+")) {
			return decode(urlString);
		}

		List<String> strs = StrUtils.split(urlString, "+");
		List<String> newStrs = new ArrayList<>();
		for (String str : strs) {
			newStrs.add(decode(str));
		}
		return StrUtils.join("+", newStrs);
	}

}
