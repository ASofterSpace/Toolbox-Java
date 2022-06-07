/**
 * Unlicensed code created by A Softer Space, 2022
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.coders;


/**
 * A class that tries to take any malformed nonsense-string and turn it into more human-readable text
 *
 * @author Moya (a softer space, 2022)
 */
public class UniversalTextDecoder {

	/**
	 * Takes BÃ¤ume and converts to Bäume
	 */
	public static String decode(String str) {

		// first do... whatever the hell this is!
		// (sadly, it just means that this one character cannot be understood,
		// so it could literally be anything...)
		str = str.replace("J�r�", "Jérô");
		str = str.replace("Jer�me", "Jerôme");
		str = str.replace("�", "é");

		// also do UTF8 decoding
		str = Utf8Decoder.decode(str);

		// and finally do URL decoding
		str = UrlDecoder.decode(str);

		return str;
	}

}
