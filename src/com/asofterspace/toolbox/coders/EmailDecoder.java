/**
 * Unlicensed code created by A Softer Space, 2026
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.coders;

import com.asofterspace.toolbox.utils.StrUtils;


/**
 * A class that can decode eMail-encoded text
 *
 * @author Moya (a softer space, 2026)
 */
public class EmailDecoder {

	public static String decode(String textToTranslate) {

		String result = textToTranslate;

		result = StrUtils.replaceAll(result, "=\n", "");

		result = StrUtils.replaceAll(result, "=C3=A4", "ä");
		result = StrUtils.replaceAll(result, "=C3=B6", "ö");
		result = StrUtils.replaceAll(result, "=C3=BC", "ü");
		result = StrUtils.replaceAll(result, "=C3=9F", "ß");

		return result;
	}
}
