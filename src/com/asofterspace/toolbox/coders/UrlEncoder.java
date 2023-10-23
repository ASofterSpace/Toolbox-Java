/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.coders;

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

		return encodeFormData(urlString).replace(".", "%2E").replace("+", "%20").replace("#", "%23");
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
