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

	// encodes to regular string format
	public static String encode(String urlString) {
		return encodeFormData(urlString).replace("+", "%20");
	}
	
	// encodes to the special string format inside form requests (both GET and POST requests),
	// which is the same as usual URL encoding, but with spaces being coded to plusses instead
	// of %20s... ^^
	public static String encodeFormData(String urlString) {
	
		try {

			return URLEncoder.encode(urlString, "UTF-8");
		
		} catch (UnsupportedEncodingException e) {
		
			return urlString;
		}
	}

}
