/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.coders;

import com.asofterspace.toolbox.utils.StrUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


/**
 * A class that can (badly) encode URL strings
 *
 * @author Moya (a softer space, 2018)
 */
public class UrlEncoder {

	/**
	 * Takes http://www.foo.org/sections/bar
	 * and converts to http%3A%2F%2Fwww.foo.org%2Fsections%2Fbar
	 */
	public static String encode(String urlString) {

		if (urlString == null) {
			return null;
		}

		return encodeFormData(urlString)
			.replace(".", "%2E")
			.replace("+", "%20")
			.replace("#", "%23")
			.replace("'", "%27");
	}

	/**
	 * Takes http://www.foo.org/sections/bar bob
	 * and converts to http://www.foo.org/sections/bar%20bob
	 */
	public static String encodePath(String urlString) {

		if (urlString == null) {
			return null;
		}

		return StrUtils.replaceAll(StrUtils.replaceAll(StrUtils.replaceAll(encode(urlString), "%2F", "/"), "%2E", "."), "%3A", ":");
	}

	// encodes to the special string format inside form requests (both GET and POST requests),
	// which is the same as usual URL encoding, but with spaces being coded to plusses instead
	// of %20s... ^^
	public static String encodeFormData(String urlString) {

		if (urlString == null) {
			return null;
		}

		try {

			return URLEncoder.encode(urlString, "UTF-8");

		} catch (UnsupportedEncodingException e) {

			return urlString;
		}
	}

}
