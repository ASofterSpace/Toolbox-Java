/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;


/**
 * A utility class for stringy fun
 *
 * @author Tom Moya Schiller, moya@asofterspace.com
 */
public class StrUtils {

	// we use Latin-1 for binaries, as that allows us to load the entire byte range of 0..255 of ASCII files,
	// meaning that we can read any nonsensical streams and don't have to worry about incompatibilities
	// (if we just read and save we have no problem with Unicode characters either; if we actually set
	// Unicode text for some reason, then we will have to think a bit harder and maybe manually convert
	// the Unicode letters that we are aware of into their same-byte counterparts or whatever... ^^)
	public static final Charset BINARY_CHARSET = StandardCharsets.ISO_8859_1;

	private static Random randGen = null;


	/**
	 * Returns a randomly chosen char representing an ascii lower case or upper case letter,
	 * or an ascii digit
	 * This is NOT crypo-secure, but just intended for quick and dirty randomness :)
	 */
	public static char getRandomChar() {

		if (randGen == null) {
			randGen = new Random();
		}

		int charKind = randGen.nextInt(3);

		switch (charKind) {

			// a digit
			case 0:
				return (char) (48 + randGen.nextInt(10));

			// a lower-case letter
			case 1:
				return (char) (97 + randGen.nextInt(26));

			// an upper-case letter
			case 2:
				return (char) (65 + randGen.nextInt(26));
		}

		// chosen by fair dice roll
		// guaranteed to be random
		return '4';
	}

	public static String getRandomString(int length) {

		StringBuilder result = new StringBuilder();

		for (int i = 0; i < length; i++) {
			result.append(getRandomChar());
		}

		return result.toString();
	}

	public static String strListToString(Collection<String> stringList) {

		StringBuilder sb = new StringBuilder();

		if (stringList != null) {
			for (String jsonStr : stringList) {
				sb.append(jsonStr);
			}
		}

		return sb.toString();
	}

	public static String[] strListToArray(Collection<String> stringList) {

		String[] result = new String[stringList.size()];

		int i = 0;

		for (String str : stringList) {
			result[i] = str;
			i++;
		}

		return result;
	}

	/**
	 * The methods haystack.startsWith(needle) and haystack.endsWith(needle)
	 * exist; this here is a more generic form which can for any position
	 * check - with minimal overhead! - if the needle is at exactly that
	 * location in the haystack
	 */
	public static boolean hasAt(String haystack, String needle, int pos) {

		// well, we cannot contain this at this position if it is out of range - duh!
		if (pos + needle.length() > haystack.length()) {
			return false;
		}
		if (pos < 0) {
			return false;
		}

		// if, by going over the whole needle...
		for (int i = 0; i < needle.length(); i++) {
			// ... we find a character that is different from the haystack at that location...
			if (haystack.charAt(pos + i) != needle.charAt(i)) {
				// ... then apparently the needle was not matching the haystack!
				return false;
			}
		}

		// if we found no discrepancy, then apparently the needle is matching the haystack!
		return true;
	}

	public static int countCharInString(char find, String inHere) {

		return countCharInString(find, inHere, inHere.length());
	}

	public static int countCharInString(char find, String inHere, int untilPos) {

		if (inHere == null) {
			return 0;
		}

		if (untilPos > inHere.length()) {
			untilPos = inHere.length();
		}

		int result = 0;

		for (int i = 0; i < untilPos; i++) {
			if (find == inHere.charAt(i)) {
				result++;
			}
		}

		return result;
	}

	public static int countStringInString(String find, String inHere) {

		if (inHere == null) {
			return 0;
		}

		int result = 0;

		for (int i = 0; i < 1 + inHere.length() - find.length(); i++) {
			boolean found = true;
			for (int j = 0; j < find.length(); j++) {
				if (find.charAt(j) != inHere.charAt(i+j)) {
					found = false;
					break;
				}
			}
			if (found) {
				result++;
			}
		}

		return result;
	}

	/**
	 * Takes a number, e.g. 2 or 6 or 11 or 42, and returns it as ordinal string, e.g. 2nd, 6th, 11th or 42nd
	 */
	public static String th(int i) {

		// 11, 12 and 13 are special - they end in 1, 2 and 3, but get th anyway
		switch (i % 100) {
			case 11:
			case 12:
			case 13:
				return i + "th";
		}

		// all others are simpler: if they end with 1 - st, if with 2 - nd, if with 3 - rd, else - th
		switch (i % 10) {
			case 1:
				return i + "st";
			case 2:
				return i + "nd";
			case 3:
				return i + "rd";
		}

		return i + "th";
	}

	/**
	 * Takes an amount and a thing and returns, depending on the input,
	 * no things
	 * 1 thing
	 * 2 things
	 * ...
	 */
	public static String thingOrThings(int amount, String thing) {
		return thingOrThings(amount, thing, thing + "s");
	}

	/**
	 * Takes an amount, a thing, and the plural of that thing and returns, depending on the input,
	 * no things
	 * 1 thing
	 * 2 things
	 * ...
	 */
	public static String thingOrThings(int amount, String thing, String things) {

		if (amount == 0) {
			return "no " + things;
		}

		if (amount == 1) {
			return "1 " + thing;
		}

		return amount + " " + things;
	}

	public static String leftPadW(int origStr, int length) {
		return leftPad(""+origStr, ' ', length);
	}

	public static String leftPadW(String origStr, int length) {
		return leftPad(origStr, ' ', length);
	}

	public static String leftPad0(int origStr, int length) {
		return leftPad(""+origStr, '0', length);
	}

	public static String leftPad0(String origStr, int length) {
		return leftPad(origStr, '0', length);
	}

	public static String leftPad(int origStr, char padWith, int length) {
		return leftPad(""+origStr, padWith, length);
	}

	/**
	 * Takes a string, e.g. blubb, and leftpads it with a character, e.g. _,
	 * until it reaches a certain length, e.g. 7 - which would give __blubb.
	 * If length is smaller than the length of origStr, origStr will be
	 * return without change - it will NOT be truncated!
	 */
	public static String leftPad(String origStr, char padWith, int length) {

		StringBuilder result = new StringBuilder();

		result.append(origStr);

		while (result.length() < length) {
			result.insert(0, padWith);
		}

		return result.toString();
	}

	/**
	 * In Java 7, String.join did not yet exist, so we have this to fall back to
	 */
	public static String join(String separator, Collection<?> strList) {
		StringBuilder result = new StringBuilder();
		boolean firstElem = true;
		for (Object elem : strList) {
			if (elem != null) {
				if (firstElem) {
					firstElem = false;
				} else {
					result.append(separator);
				}
				result.append(elem.toString());
			}
		}
		return result.toString();
	}
	public static String join(String separator, Object[] strList) {
		StringBuilder result = new StringBuilder();
		boolean firstElem = true;
		for (Object elem : strList) {
			if (elem != null) {
				if (firstElem) {
					firstElem = false;
				} else {
					result.append(separator);
				}
				result.append(elem.toString());
			}
		}
		return result.toString();
	}

	public static String getLineFromPosition(int pos, String content) {

		int start = getLineStartFromPosition(pos, content);
		int end = getLineEndFromPosition(pos, content);

		if (end >= start) {
			return content.substring(start, end);
		}

		return "";
	}

	public static int getLineNumberFromPosition(int pos, String content) {

		int result = 0;
		int until = pos;
		if (content.length() < until) {
			until = content.length();
		}
		for (int i = 0; i < until; i++) {
			char c = content.charAt(i);
			if (c == '\n') {
				result++;
			}
		}
		return result;
	}

	public static int getLineStartFromNumber(int number, String content) {

		int count = 0;
		int lineStart = 0;
		int lineEnd = 0;

		for (int i = 0; i < content.length(); i++) {
			char c = content.charAt(i);
			if (c == '\n') {
				count++;

				if (count == number) {
					return i + 1;
				}
			}
		}

		return content.length() - 1;
	}

	public static String getLineFromNumber(int number, String content) {

		int count = 0;
		int lineStart = 0;
		int lineEnd = 0;

		for (int i = 0; i < content.length(); i++) {
			char c = content.charAt(i);
			if (c == '\n') {
				count++;

				if (count == number) {
					lineStart = i + 1;
				} else if (count == number + 1) {
					lineEnd = i;
					break;
				}
			}
		}

		if (lineEnd < lineStart) {
			return "";
		}

		return content.substring(lineStart, lineEnd);
	}

	public static int getLineStartFromPosition(int pos, String content) {

		int lineStart = 0;

		if (pos > 0) {
			lineStart = content.lastIndexOf("\n", pos - 1) + 1;
		}

		return lineStart;
	}

	public static int getLineEndFromPosition(int pos, String content) {

		int lineEnd = content.indexOf("\n", pos);

		if (lineEnd < 0) {
			lineEnd = content.length();
		}

		return lineEnd;
	}

	public static String getWordFromPosition(int pos, String content) {

		int start = getWordStartFromPosition(pos, content, true);
		int end = getWordEndFromPosition(pos, content, true);

		return content.substring(start, end);
	}

	public static int getWordStartFromPosition(int pos, String content, boolean splitWordClusters) {

		int lineStartSpace = content.lastIndexOf(" ", pos - 1) + 1;
		int lineStartNewline = content.lastIndexOf("\n", pos - 1) + 1;
		int lineStartTab = content.lastIndexOf("\t", pos - 1) + 1;
		int lineStartLAngle = content.lastIndexOf("<", pos - 1) + 1;
		int lineStartRAngle = content.lastIndexOf(">", pos - 1) + 1;
		int lineStartLBracket = content.lastIndexOf("(", pos - 1) + 1;
		int lineStartRBracket = content.lastIndexOf(")", pos - 1) + 1;
		int lineStartLSqBracket = content.lastIndexOf("[", pos - 1) + 1;
		int lineStartRSqBracket = content.lastIndexOf("]", pos - 1) + 1;
		int lineStartLParens = content.lastIndexOf("{", pos - 1) + 1;
		int lineStartRParens = content.lastIndexOf("}", pos - 1) + 1;
		int lineStartSemi = content.lastIndexOf(";", pos - 1) + 1;
		int lineStartComma = content.lastIndexOf(",", pos - 1) + 1;

		int lineStartDot = 0;
		int lineStartDoubleDot = 0;
		int lineStartEquals = 0;
		int lineStartApo = 0;
		int lineStartQuot = 0;
		int lineStartSlash = 0;
		int lineStartN = 0;
		int lineStartR = 0;
		int lineStartT = 0;

		if (splitWordClusters) {
			lineStartDot = content.lastIndexOf(".", pos - 1) + 1;
			lineStartDoubleDot = content.lastIndexOf(":", pos - 1) + 1;
			lineStartEquals = content.lastIndexOf("=", pos - 1) + 1;
			lineStartApo = content.lastIndexOf("'", pos - 1) + 1;
			lineStartQuot = content.lastIndexOf("\"", pos - 1) + 1;
			lineStartSlash = content.lastIndexOf("/", pos - 1) + 1;

			lineStartN = content.lastIndexOf("\\n", pos - 1) + 2;
			lineStartR = content.lastIndexOf("\\r", pos - 1) + 2;
			lineStartT = content.lastIndexOf("\\t", pos - 1) + 2;
		}

		int lineStart = 0;

		if (lineStartSpace > lineStart) {
			lineStart = lineStartSpace;
		}
		if (lineStartNewline > lineStart) {
			lineStart = lineStartNewline;
		}
		if (lineStartTab > lineStart) {
			lineStart = lineStartTab;
		}
		if (lineStartLAngle > lineStart) {
			lineStart = lineStartLAngle;
		}
		if (lineStartRAngle > lineStart) {
			lineStart = lineStartRAngle;
		}
		if (lineStartLBracket > lineStart) {
			lineStart = lineStartLBracket;
		}
		if (lineStartRBracket > lineStart) {
			lineStart = lineStartRBracket;
		}
		if (lineStartLSqBracket > lineStart) {
			lineStart = lineStartLSqBracket;
		}
		if (lineStartRSqBracket > lineStart) {
			lineStart = lineStartRSqBracket;
		}
		if (lineStartLParens > lineStart) {
			lineStart = lineStartLParens;
		}
		if (lineStartRParens > lineStart) {
			lineStart = lineStartRParens;
		}
		if (lineStartSemi > lineStart) {
			lineStart = lineStartSemi;
		}
		if (lineStartComma > lineStart) {
			lineStart = lineStartComma;
		}

		if (lineStartDot > lineStart) {
			lineStart = lineStartDot;
		}
		if (lineStartDoubleDot > lineStart) {
			lineStart = lineStartDoubleDot;
		}
		if (lineStartEquals > lineStart) {
			lineStart = lineStartEquals;
		}
		if (lineStartApo > lineStart) {
			lineStart = lineStartApo;
		}
		if (lineStartQuot > lineStart) {
			lineStart = lineStartQuot;
		}
		if (lineStartSlash > lineStart) {
			lineStart = lineStartSlash;
		}

		if (lineStartN > lineStart) {
			lineStart = lineStartN;
		}
		if (lineStartR > lineStart) {
			lineStart = lineStartR;
		}
		if (lineStartT > lineStart) {
			lineStart = lineStartT;
		}

		return lineStart;
	}

	public static int getWordEndFromPosition(int pos, String content, boolean splitWordClusters) {

		int lineEndSpace = content.indexOf(" ", pos);
		int lineEndNewline = content.indexOf("\n", pos);
		int lineEndTab = content.indexOf("\t", pos);
		int lineEndLAngle = content.indexOf("<", pos);
		int lineEndRAngle = content.indexOf(">", pos);
		int lineEndLBracket = content.indexOf("(", pos);
		int lineEndRBracket = content.indexOf(")", pos);
		int lineEndLSqBracket = content.indexOf("[", pos);
		int lineEndRSqBracket = content.indexOf("]", pos);
		int lineEndLParens = content.indexOf("{", pos);
		int lineEndRParens = content.indexOf("}", pos);
		int lineEndSemi = content.indexOf(";", pos);
		int lineEndComma = content.indexOf(",", pos);

		int lineEndDot = -1;
		int lineEndDoubleDot = -1;
		int lineEndEquals = -1;
		int lineEndApo = -1;
		int lineEndQuot = -1;
		int lineEndSlash = -1;

		int lineEndN = -1;
		int lineEndR = -1;
		int lineEndT = -1;

		if (splitWordClusters) {
			lineEndDot = content.indexOf(".", pos);
			lineEndDoubleDot = content.indexOf(":", pos);
			lineEndEquals = content.indexOf("=", pos);
			lineEndApo = content.indexOf("'", pos);
			lineEndQuot = content.indexOf("\"", pos);
			lineEndSlash = content.indexOf("/", pos);

			lineEndN = content.indexOf("\\n", pos);
			lineEndR = content.indexOf("\\r", pos);
			lineEndT = content.indexOf("\\t", pos);
		}

		int lineEnd = content.length();

		if ((lineEndSpace >= 0) && (lineEndSpace < lineEnd)) {
			lineEnd = lineEndSpace;
		}
		if ((lineEndNewline >= 0) && (lineEndNewline < lineEnd)) {
			lineEnd = lineEndNewline;
		}
		if ((lineEndTab >= 0) && (lineEndTab < lineEnd)) {
			lineEnd = lineEndTab;
		}
		if ((lineEndLAngle >= 0) && (lineEndLAngle < lineEnd)) {
			lineEnd = lineEndLAngle;
		}
		if ((lineEndRAngle >= 0) && (lineEndRAngle < lineEnd)) {
			lineEnd = lineEndRAngle;
		}
		if ((lineEndLBracket >= 0) && (lineEndLBracket < lineEnd)) {
			lineEnd = lineEndLBracket;
		}
		if ((lineEndRBracket >= 0) && (lineEndRBracket < lineEnd)) {
			lineEnd = lineEndRBracket;
		}
		if ((lineEndLSqBracket >= 0) && (lineEndLSqBracket < lineEnd)) {
			lineEnd = lineEndLSqBracket;
		}
		if ((lineEndRSqBracket >= 0) && (lineEndRSqBracket< lineEnd)) {
			lineEnd = lineEndRSqBracket;
		}
		if ((lineEndLParens >= 0) && (lineEndLParens < lineEnd)) {
			lineEnd = lineEndLParens;
		}
		if ((lineEndRParens >= 0) && (lineEndRParens < lineEnd)) {
			lineEnd = lineEndRParens;
		}
		if ((lineEndSemi >= 0) && (lineEndSemi < lineEnd)) {
			lineEnd = lineEndSemi;
		}
		if ((lineEndComma >= 0) && (lineEndComma < lineEnd)) {
			lineEnd = lineEndComma;
		}

		if ((lineEndDot >= 0) && (lineEndDot < lineEnd)) {
			lineEnd = lineEndDot;
		}
		if ((lineEndDoubleDot >= 0) && (lineEndDoubleDot < lineEnd)) {
			lineEnd = lineEndDoubleDot;
		}
		if ((lineEndEquals >= 0) && (lineEndEquals < lineEnd)) {
			lineEnd = lineEndEquals;
		}
		if ((lineEndApo >= 0) && (lineEndApo < lineEnd)) {
			lineEnd = lineEndApo;
		}
		if ((lineEndQuot >= 0) && (lineEndQuot < lineEnd)) {
			lineEnd = lineEndQuot;
		}
		if ((lineEndSlash >= 0) && (lineEndSlash < lineEnd)) {
			lineEnd = lineEndSlash;
		}

		if ((lineEndN >= 0) && (lineEndN < lineEnd)) {
			lineEnd = lineEndN;
		}
		if ((lineEndR >= 0) && (lineEndR < lineEnd)) {
			lineEnd = lineEndR;
		}
		if ((lineEndT >= 0) && (lineEndT < lineEnd)) {
			lineEnd = lineEndT;
		}

		if ((lineEnd > 0) && (content.charAt(lineEnd - 1) == '\\')) {
			lineEnd--;
		}

		return lineEnd;
	}

	/**
	 * Sorts a list of strings case-insensitively, then removes duplicates case-sensitively,
	 * so if we put in:
	 * ["foo", "bar", "FOO", "foo"]
	 * we expect as output:
	 * ["bar", "foo", "FOO"] or ["bar", "FOO", "foo"]
	 */
	public static List<String> sortAndRemoveDuplicates(List<String> stringList) {

		// we sort first case-sensitively (to get e.g. foo, foo, BAR, FOO)
		Collections.sort(stringList, new Comparator<String>() {
			public int compare(String a, String b) {
				return a.compareTo(b);
			}
		});

		// we then sort case-insensitively (to get e.g. BAR, foo, foo, FOO)
		Collections.sort(stringList, new Comparator<String>() {
			public int compare(String a, String b) {
				return a.toLowerCase().compareTo(b.toLowerCase());
			}
		});

		// due to the two sorts (with a *stable* sorting algorithm!), we know
		// now that case-sensitive duplicates are really direct neighbours
		// (if we had only done the second sorting, we might have ended up
		// with foo, FOO, foo, and then the now following duplicate removal
		// step would have occasionally failed!)
		List<String> result = new ArrayList<>();
		String lastStr = null;
		for (String str : stringList) {
			if (!str.equals(lastStr)) {
				result.add(str);
				lastStr = str;
			}
		}
		return result;
	}

	/**
	 * Checks two string lists for equality
	 */
	public static boolean equals(List<String> someList, List<String> otherList) {
		if (someList == null) {
			return otherList == null;
		}
		if (otherList == null) {
			return false;
		}
		if (someList.size() != otherList.size()) {
			return false;
		}
		for (int i = 0; i < someList.size(); i++) {
			if (someList.get(i) == null) {
				if (otherList.get(i) == null) {
					continue;
				} else {
					return false;
				}
			}
			if (!someList.get(i).equals(otherList.get(i))) {
				return false;
			}
		}
		return true;
	}

	public static String prepareForParsing(String value) {

		// remove non-breaking space character
		value = value.replaceAll("\u00a0", "");

		// remove regular whitespace characters
		value = value.trim();

		// ensure this ends with a dot, and all commas are taken out
		int lastComma = value.lastIndexOf(",");
		int lastDot = value.lastIndexOf(".");
		boolean endsWithDot = false;
		// if we have both commas and dots...
		if ((lastComma > 0) && (lastDot > 0)) {
			// ... then we end with the last of the two
			endsWithDot = (lastComma < lastDot);
		} else {
			// if we do not have both, but we have a dot, we end with a dot
			if (lastDot > 0) {
				endsWithDot = true;
			}
		}
		if (endsWithDot) {
			value = value.replaceAll(",", "");
		} else {
			value = value.replaceAll("\\.", "");
			value = value.replaceAll(",", ".");
		}

		return value;
	}

	public static Integer strToInt(String value) {

		if (value == null) {
			return null;
		}

		value = prepareForParsing(value);

		if ("".equals(value)) {
			return null;
		}

		try {
			return Integer.valueOf(value);
		} catch (NumberFormatException e) {
			try {
				return (Integer) (int) Math.round(Double.valueOf(value));
			} catch (NumberFormatException e2) {
				return null;
			}
		}
	}

	public static String intToStr(Integer value) {
		return ""+value;
	}

	public static String intToStr(int value) {
		return ""+value;
	}

	public static Double strToDouble(String value) {

		if (value == null) {
			return null;
		}

		value = prepareForParsing(value);

		if ("".equals(value)) {
			return null;
		}

		try {
			return Double.valueOf(value);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	public static String doubleToStr(Double value) {
		return ""+value;
	}

	public static String doubleToStr(double value) {
		return ""+value;
	}

	/**
	 * The numbers we are getting here are German-ly numbers, so 1.546,00...
	 * so we first replace . with nothing, then , with .
	 */
	public static Integer germanStrToInt(String value) {
		if (value == null) {
			return null;
		}
		value = value.trim();
		value = value.replaceAll("\\.", "");
		value = value.replaceAll(",", ".");
		Integer result = strToInt(value);
		return result;
	}

	public static String upcaseFirstLetter(String str) {
		if (str == null) {
			return null;
		}
		if (str.length() < 1) {
			return str;
		}
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}

	public static String lowcaseFirstLetter(String str) {
		if (str == null) {
			return null;
		}
		if (str.length() < 1) {
			return str;
		}
		return str.substring(0, 1).toLowerCase() + str.substring(1);
	}
}
