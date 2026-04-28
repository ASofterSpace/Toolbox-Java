/**
 * Unlicensed code created by A Softer Space, 2026
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.coders;

import com.asofterspace.toolbox.utils.StrUtils;


/**
 * A class that can encode eMail-encoded text
 *
 * @author Moya (a softer space, 2026)
 */
public class EmailEncoder {

	public static String encode(String textToTranslate) {

		String result = textToTranslate;

		result = StrUtils.replaceAll(result, "ä", "=C3=A4");
		result = StrUtils.replaceAll(result, "ö", "=C3=B6");
		result = StrUtils.replaceAll(result, "ü", "=C3=BC");
		result = StrUtils.replaceAll(result, "ß", "=C3=9F");

		return result;
	}
}
