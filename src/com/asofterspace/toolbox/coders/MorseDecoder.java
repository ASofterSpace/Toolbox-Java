/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.coders;

/**
 * A class that can decode text from Morse code
 *
 * @author Moya (a softer space, 2017)
 */
public class MorseDecoder {

	/**
	 * Gets all the characters that are counted as "dot"
	 * @return An array of characters that are counted as "dot"
	 */
	public static char[] getWhatCountsAsDot() {

		return ".·•*".toCharArray();
	}

	/**
	 * Gets all the characters that are counted as "dash"
	 * @return An array of characters that are counted as "dash"
	 */
	public static char[] getWhatCountsAsDash() {

		return "_-–—".toCharArray();
	}

	/**
	 * Translates a given text from its Morse code representation
	 * @param morseToTranslate the text in Morse code that is to be translated
	 * @return the text that the Morse code encoded
	 */
	public static String decode(String morseToTranslate) {

		StringBuilder result = new StringBuilder();

		for (char c : getWhatCountsAsDot()) {
			morseToTranslate = morseToTranslate.replace(c, '•');
		}

		for (char c : getWhatCountsAsDash()) {
			morseToTranslate = morseToTranslate.replace(c, '-');
		}

		// by default, we encode spaces between letters as three spaces,
		// so four spaces are enough to be sure that this is a space between words
		String[] words = morseToTranslate.split("    ");

		for (String word : words) {
			String[] letters = word.split(" ");

			boolean appendSpaceAfterWord = true;

			for (String letter : letters) {

				appendSpaceAfterWord = true;

				switch (letter) {
					case "":
						// ignore additional spaces
						break;
					case "\n":
						// ensure that if a linefeed is the last sign, then no space is appended
						// afterwards
						appendSpaceAfterWord = false;
						result.append("\n");
						break;
					case "•-":
						result.append("A");
						break;
					case "-•••":
						result.append("B");
						break;
					case "-•-•":
						result.append("C");
						break;
					case "-••":
						result.append("D");
						break;
					case "•":
						result.append("E");
						break;
					case "••-•":
						result.append("F");
						break;
					case "--•":
						result.append("G");
						break;
					case "••••":
						result.append("H");
						break;
					case "••":
						result.append("I");
						break;
					case "•---":
						result.append("J");
						break;
					case "-•-":
						result.append("K");
						break;
					case "•-••":
						result.append("L");
						break;
					case "--":
						result.append("M");
						break;
					case "-•":
						result.append("N");
						break;
					case "---":
						result.append("O");
						break;
					case "•--•":
						result.append("P");
						break;
					case "--•-":
						result.append("Q");
						break;
					case "•-•":
						result.append("R");
						break;
					case "•••":
						result.append("S");
						break;
					case "-":
						result.append("T");
						break;
					case "••-":
						result.append("U");
						break;
					case "•••-":
						result.append("V");
						break;
					case "•--":
						result.append("W");
						break;
					case "-••-":
						result.append("X");
						break;
					case "-•--":
						result.append("Y");
						break;
					case "--••":
						result.append("Z");
						break;
					case "-----":
						result.append("0");
						break;
					case "•----":
						result.append("1");
						break;
					case "••---":
						result.append("2");
						break;
					case "•••--":
						result.append("3");
						break;
					case "••••-":
						result.append("4");
						break;
					case "•••••":
						result.append("5");
						break;
					case "-••••":
						result.append("6");
						break;
					case "--•••":
						result.append("7");
						break;
					case "---••":
						result.append("8");
						break;
					case "----•":
						result.append("9");
						break;
					case "•-•-•-":
						result.append(".");
						break;
					case "--••--":
						result.append(",");
						break;
					case "••--••":
						result.append("?");
						break;
					case "•----•":
						result.append("'");
						break;
					case "-•-•--":
						result.append("!");
						break;
					case "-••-•":
						result.append("/");
						break;
					case "-•--•":
						result.append("(");
						break;
					case "-•--•-":
						result.append(")");
						break;
					case "•-•••":
						result.append("&");
						break;
					case "---•••":
						result.append(":");
						break;
					case "-•-•-•":
						result.append(";");
						break;
					case "-•••-":
						result.append("=");
						break;
					case "•-•-•":
						result.append("+");
						break;
					case "-••••-":
						result.append("-");
						break;
					case "••--•-":
						result.append("_");
						break;
					case "•-••-•":
						result.append("\"");
						break;
					case "•••-••-":
						result.append("$");
						break;
					case "•--•-•":
						result.append("@");
						break;
					case "•-•-":
						result.append("Ä");
						break;
					case "•--•-":
						result.append("Å");
						break;
					case "••--•":
						result.append("Ð");
						break;
					case "---•":
						result.append("Ö");
						break;
					case "•--••":
						result.append("Þ");
						break;
					case "••--":
						result.append("Ü");
						break;
					default:
						result.append("?");
				}
			}

			// in case of a new word, append a space!
			// (unless we just had a newline character)
			if (appendSpaceAfterWord) {
				result.append(" ");
			}
		}

		return result.toString().trim();
	}

}
