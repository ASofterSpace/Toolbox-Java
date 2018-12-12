/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.coders;


/**
 * A class that can (badly) encode UTF8 strings
 *
 * @author Moya (a softer space, 2018)
 */
public class Utf8Encoder {

	/**
	 * Takes Bäume and converts to BÃ¤ume
	 */
	public static String encode(String str) {

		str = str.replace("'", "â€™");
		str = str.replace("€", "â‚¬");
		str = str.replace("ä", "Ã¤");
		str = str.replace("Ä", "Ã„");
		str = str.replace("ö", "Ã¶");
		str = str.replace("Ö", "Ã–");
		str = str.replace("ü", "Ã¼");
		str = str.replace("Ü", "Ãœ");
		str = str.replace("á", "Ã¡");
		// Á missing
		// à missing
		str = str.replace("À", "Ã€");
		str = str.replace("é", "Ã©");
		// É missing
		str = str.replace("ó", "Ã³");
		str = str.replace("Ó", "Ã“");
		str = str.replace("ú", "Ãº");
		// Ú missing
		str = str.replace("í", "Ã­");
		// Í missing
		str = str.replace("æ", "Ã¦");
		str = str.replace("Æ", "Ã†");
		str = str.replace("ð", "Ã°");
		str = str.replace("Ð", "Ã");
		str = str.replace("þ", "Ã¾");
		str = str.replace("Þ", "Ãž");
		str = str.replace("ß", "ÃŸ");
		str = str.replace("¯", "Â¯");
		str = str.replace("-", "â€“");
		
		return str;
	}

}
