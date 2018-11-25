package com.asofterspace.toolbox.coders;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;


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

		try {
			
			return URLDecoder.decode(urlString, "UTF-8");
		
		} catch (UnsupportedEncodingException e) {
		
			return urlString;
		}
	}

}
