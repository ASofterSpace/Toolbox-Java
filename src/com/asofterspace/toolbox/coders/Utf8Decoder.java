/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.coders;


/**
 * A class that can (badly) decode UTF8 strings
 *
 * @author Moya (a softer space, 2018)
 */
public class Utf8Decoder {

	/**
	 * Takes BÃ¤ume and converts to Bäume
	 */
	public static String decode(String str) {

		str = str.replace("â€™", "'");
		str = str.replace("â‚¬", "€");
		str = str.replace("Ã¤", "ä");
		str = str.replace("Ã„", "Ä");
		str = str.replace("Ã¶", "ö");
		str = str.replace("Ã–", "Ö");
		str = str.replace("Ã¼", "ü");
		str = str.replace("Ãœ", "Ü");
		str = str.replace("Ã¡", "á");
		// Á missing
		// à missing
		str = str.replace("Ã€", "À");
		str = str.replace("Ã©", "é");
		// É missing
		str = str.replace("Ã³", "ó");
		str = str.replace("Ã“", "Ó");
		str = str.replace("Ãº", "ú");
		// Ú missing
		str = str.replace("Ã­", "í");
		// Í missing
		str = str.replace("Ã¦", "æ");
		str = str.replace("Ã†", "Æ");
		str = str.replace("Ã°", "ð");
		str = str.replace("Ã", "Ð");
		str = str.replace("Ã¾", "þ");
		str = str.replace("Ãž", "Þ");
		str = str.replace("ÃŸ", "ß");
		str = str.replace("Â¯", "¯");
		str = str.replace("â€“", "-");
		
		return str;
	}

}
