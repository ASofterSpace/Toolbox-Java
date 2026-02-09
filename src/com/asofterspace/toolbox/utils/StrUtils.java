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
 * @author Moya Schiller, moya@asofterspace.com
 */
public class StrUtils {

	// we use Latin-1 for binaries, as that allows us to load the entire byte range of 0..255 of ASCII files,
	// meaning that we can read any nonsensical streams and don't have to worry about incompatibilities
	// (if we just read and save we have no problem with Unicode characters either; if we actually set
	// Unicode text for some reason, then we will have to think a bit harder and maybe manually convert
	// the Unicode letters that we are aware of into their same-byte counterparts or whatever... ^^)
	public static final Charset BINARY_CHARSET = StandardCharsets.ISO_8859_1;

	private static Random randGen = null;

	private static String[] PRONOUNS = new String[]{
		"she", "he", "her", "him", "they", "them", "it", "any", "*", "none"};


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

	/**
	 * Extract the content from the haystack string that is between strbefore and strafter
	 */
	public static String extract(String haystack, String strbefore, String strafter) {
		if (haystack == null) {
			return null;
		}
		int len = strbefore.length();
		int startindex = haystack.indexOf(strbefore);
		int endindex = haystack.indexOf(strafter, startindex + len);
		if ((startindex >= 0) && (endindex >= startindex + len)) {
			return haystack.substring(startindex + len, endindex);
		}
		if (startindex >= 0) {
			return haystack.substring(startindex + len);
		}
		return null;
	}

	/**
	 * Extract the contents from the haystack string that are between strbefores and strafters
	 */
	public static List<String> extractAll(String haystack, String strbefore, String strafter) {
		if (haystack == null) {
			return null;
		}
		List<String> result = new ArrayList<>();
		int cur = 0;
		int len = strbefore.length();
		while (haystack.indexOf(strbefore, cur) >= 0) {
			int startindex = haystack.indexOf(strbefore, cur);
			int endindex = haystack.indexOf(strafter, startindex + len);
			if ((startindex >= 0) && (endindex >= startindex + len)) {
				result.add(haystack.substring(startindex + len, endindex));
				cur = endindex + strafter.length();
			} else if (startindex >= 0) {
				result.add(haystack.substring(startindex + len));
				break;
			} else {
				break;
			}
		}
		return result;
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

		if ((inHere == null) || (find == null)) {
			return 0;
		}

		int result = 0;
		int next = inHere.indexOf(find);

		while (next >= 0) {
			result++;
			next = inHere.indexOf(find, next+1);
		}

		/*
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
		*/

		return result;
	}

	public static Integer getWordAmount(String text) {
		if (text != null) {
			text = replaceAll(text, ".", " ");
			text = replaceAll(text, ",", " ");
			text = replaceAll(text, ";", " ");
			text = replaceAll(text, "!", " ");
			text = replaceAll(text, "?", " ");
			text = replaceAll(text, "\t", " ");
			text = replaceAll(text, "\n", " ");
			text = replaceAll(text, "  ", " ");
			text = replaceAll(text, "<br>", " ");
			text = replaceAll(text, "  ", " ");
			text = text.trim();
			return countCharInString(' ', text) + 1;
		}
		return null;
	}


	public static String getReadingTimeStr(String text) {
		float words = (float) getWordAmount(text);
		float readingTime = words / 135;
		int minutes = (int) Math.floor(readingTime);
		int seconds = (((int) (readingTime * 60))) % 60;

		return minutes + " min, " + seconds + " sec";
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
	 * returned without change - it will NOT be truncated!
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

	public static List<String> split(String text, String separator) {
		List<String> result = new ArrayList<>();
		if (text == null) {
			return result;
		}
		if (separator == null) {
			result.add(text);
			return result;
		}

		int cur = 0;
		int len = separator.length();
		while (true) {
			int endindex = text.indexOf(separator, cur);
			if (endindex >= 0) {
				result.add(text.substring(cur, endindex));
				cur = endindex + len;
			} else {
				result.add(text.substring(cur, text.length()));
				break;
			}
		}
		return result;
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
			return content.substring(lineStart);
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
		int lineStartExcl = 0;
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
			lineStartExcl = content.lastIndexOf("!", pos - 1) + 1;

			lineStartN = content.lastIndexOf("\\n", pos - 1) + 2;
			if (lineStartN == 1) {
				lineStartN = 0;
			}
			lineStartR = content.lastIndexOf("\\r", pos - 1) + 2;
			if (lineStartR == 1) {
				lineStartR = 0;
			}
			lineStartT = content.lastIndexOf("\\t", pos - 1) + 2;
			if (lineStartT == 1) {
				lineStartT = 0;
			}
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
		if (lineStartExcl > lineStart) {
			lineStart = lineStartExcl;
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

	public static int getLinkEndFromPosition(int start, String contentStr) {
		int result = Integer.MAX_VALUE;
		int space = contentStr.indexOf(" ", start);
		int langle = contentStr.indexOf("<", start);
		int newline = contentStr.indexOf("\n", start);
		int nbsp = contentStr.indexOf("&nbsp;", start);
		if ((space >= 0) && (space < result)) {
			result = space;
		}
		if ((langle >= 0) && (langle < result)) {
			result = langle;
		}
		if ((newline >= 0) && (newline < result)) {
			result = newline;
		}
		if ((nbsp >= 0) && (nbsp < result)) {
			result = nbsp;
		}
		if (result == Integer.MAX_VALUE) {
			return -1;
		}
		return result;
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
		int lineEndExcl = -1;

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
			lineEndExcl = content.indexOf("!", pos);

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
		if ((lineEndExcl >= 0) && (lineEndExcl < lineEnd)) {
			lineEnd = lineEndExcl;
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

		// remove currency signs
		value = value.replaceAll("€", "");
		value = value.replaceAll("$", "");

		// super-trim whitespace characters
		value = normalizeWhitespace(value);

		// ignore a plusminus (it should only be in front of zero values anyway...)
		if (value.startsWith("&plusmn;")) {
			value = value.substring(8).trim();
		}
		if (value.startsWith("±")) {
			value = value.substring(1).trim();
		}

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

	public static Integer strToInt(String value, Integer defaultValue) {
		Integer result = strToInt(value);
		if (result == null) {
			return defaultValue;
		}
		return result;
	}

	public static Byte strToByte(String value) {
		Integer result = strToInt(value);
		if (result == null) {
			return null;
		}
		return (byte) (result & 0xFF);
	}

	public static String intToStr(Integer value) {
		return ""+value;
	}

	public static String intToStr(int value) {
		return ""+value;
	}

	public static String longToHumanReadableBytes(long value) {
		if (value > 4l*1024*1024*1024*1024) {
			return MathUtils.divideLongs(value, 1024l*1024*1024*1024) + " TB";
		}
		if (value > 4l*1024*1024*1024) {
			return MathUtils.divideLongs(value, 1024l*1024*1024) + " GB";
		}
		if (value > 4l*1024*1024) {
			return MathUtils.divideLongs(value, 1024l*1024) + " MB";
		}
		if (value > 4l*1024) {
			return MathUtils.divideLongs(value, 1024l) + " KB";
		}
		return value + " B";
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

	public static String doubleToStr(double value, int digitsAfterComma) {
		String result = ""+value;
		if (result.contains(".")) {
			if (digitsAfterComma == 0) {
				result = result.substring(0, result.indexOf("."));
				return result;
			}
			int curlyAfterComma = (result.length() - result.indexOf(".")) - 1;
			while (curlyAfterComma < digitsAfterComma) {
				result += "0";
				curlyAfterComma++;
			}
			if (curlyAfterComma > digitsAfterComma) {
				result = result.substring(0, result.length() + digitsAfterComma - curlyAfterComma);
			}

		} else {
			if (digitsAfterComma == 0) {
				return result;
			}
			result += ".";
			for (int i = 0; i < digitsAfterComma; i++) {
				result += "0";
			}
		}
		return result;
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

	public static boolean startsWithOrIs(String haystack, String needle) {
		if ((haystack == null) || (needle == null)) {
			return false;
		}
		if (haystack.length() > needle.length()) {
			return haystack.startsWith(needle);
		}
		return haystack.equals(needle);
	}

	public static boolean startsWithLowerCase(String str) {
		if (str == null) {
			return false;
		}
		str = str.trim();
		if (str.length() < 1) {
			return false;
		}
		str = str.substring(0, 1);
		return str.equals(str.toLowerCase());
	}

	public static boolean startsWithUpperCase(String str) {
		if (str == null) {
			return false;
		}
		str = str.trim();
		if (str.length() < 1) {
			return false;
		}
		str = str.substring(0, 1);
		return str.equals(str.toUpperCase());
	}

	public static String replaceFirst(String origStr, String findThis, String replaceWith) {
		if (origStr == null) {
			return null;
		}
		if (findThis == null) {
			return origStr;
		}
		if (replaceWith == null) {
			replaceWith = "";
		}
		int index = origStr.indexOf(findThis);
		if (index < 0) {
			return origStr;
		}
		origStr = origStr.substring(0, index) + replaceWith + origStr.substring(index + findThis.length());
		return origStr;
	}

	public static String replaceLast(String origStr, String findThis, String replaceWith) {
		if (origStr == null) {
			return null;
		}
		if (findThis == null) {
			return origStr;
		}
		if (replaceWith == null) {
			replaceWith = "";
		}
		int index = origStr.lastIndexOf(findThis);
		if (index < 0) {
			return origStr;
		}
		origStr = origStr.substring(0, index) + replaceWith + origStr.substring(index + findThis.length());
		return origStr;
	}

	/**
	 * Replaces all occurrences of findThis in a string with replaceWith
	 * This does NOT use a regex, or any other nonsense - just plain string comparison
	 * Also, this only replaces all once - call replaceAllRepeatedly if you want to ensure
	 * that the result does not contain any occurrences of findThis anymore at all
	 * (With this method, if origStr is "foobar", and findThis is "o" and replaceWith is "oo",
	 * we return "foooobar" instead of looping forever)
	 */
	public static String replaceAll(String origStr, String findThis, String replaceWith) {
		return replaceAll(origStr, findThis, replaceWith, false, false);
	}

	/**
	 * Replaces all occurrences of findThis in a string (ignoring case) with replaceWith
	 * This does NOT use a regex, or any other nonsense - just plain string comparison
	 * Also, this only replaces all once - call replaceAllRepeatedly if you want to ensure
	 * that the result does not contain any occurrences of findThis anymore at all
	 * (With this method, if origStr is "foobar", and findThis is "o" and replaceWith is "oo",
	 * we return "foooobar" instead of looping forever)
	 */
	public static String replaceAllIgnoreCase(String origStr, String findThis, String replaceWith) {
		return replaceAll(origStr, findThis, replaceWith, true, false);
	}

	public static String replaceAll(String origStr, String findThis, String replaceWith,
		boolean ignoreCase, boolean useAsterisk) {

		if (origStr == null) {
			return null;
		}
		if (findThis == null) {
			return origStr;
		}
		if (replaceWith == null) {
			replaceWith = "";
		}
		String haystack = origStr;
		if (ignoreCase) {
			findThis = findThis.toLowerCase();
			haystack = origStr.toLowerCase();
		}
		StringBuilder result = new StringBuilder();

		// remove leading and trailing asterisks as they have no impact anyway and would lead
		// to infinite loops later on
		if (useAsterisk) {
			while (findThis.startsWith("*")) {
				findThis = findThis.substring(1);
			}
			while (findThis.endsWith("*")) {
				findThis = findThis.substring(0, findThis.length() - 1);
			}
		}

		int asteriskPos = findThis.indexOf("*");

		if (useAsterisk && (asteriskPos >= 0)) {
			String firstFindThis = findThis.substring(0, asteriskPos);
			String secondFindThis = findThis.substring(asteriskPos + 1);
			int prevEnd = 0;
			int firstIndex = haystack.indexOf(firstFindThis);
			if (firstIndex >= 0) {
				int secondIndex = haystack.indexOf(secondFindThis, firstIndex);
				while ((firstIndex >= 0) && (secondIndex >= 0)) {
					result.append(origStr.substring(prevEnd, firstIndex));
					result.append(replaceWith);
					prevEnd = secondIndex + secondFindThis.length();
					firstIndex = haystack.indexOf(firstFindThis, prevEnd);
					if (firstIndex >= 0) {
						secondIndex = haystack.indexOf(secondFindThis, firstIndex);
					} else {
						secondIndex = -1;
					}
				}
			}
			result.append(origStr.substring(prevEnd));
			return result.toString();
		}

		int index = haystack.indexOf(findThis);
		int prevEnd = 0;
		while (index >= 0) {
			result.append(origStr.substring(prevEnd, index));
			result.append(replaceWith);
			prevEnd = index + findThis.length();
			index = haystack.indexOf(findThis, prevEnd);
		}
		result.append(origStr.substring(prevEnd));
		return result.toString();
	}

	/**
	 * replaces all except if a match is immediately preceded by a > sign, in which case this
	 * match is not replaced
	 */
	public static String replaceAllIfNotInsideTag(String origStr, String findThis, String replaceWith) {
		if (origStr == null) {
			return null;
		}
		if (findThis == null) {
			return origStr;
		}
		if (replaceWith == null) {
			replaceWith = "";
		}
		StringBuilder result = new StringBuilder();
		int index = origStr.indexOf(findThis);
		while (index >= 0) {
			result.append(origStr.substring(0, index));
			if ((index > 0) && origStr.charAt(index - 1) == '>') {
				result.append(findThis);
			} else {
				result.append(replaceWith);
			}
			// TODO :: improve speed by not calling substring but keeping track of start!
			origStr = origStr.substring(index + findThis.length());
			index = origStr.indexOf(findThis);
		}
		result.append(origStr);
		return result.toString();
	}

	public static String replaceAllRepeatedly(String origStr, String findThis, String replaceWith) {
		if (origStr == null) {
			return null;
		}
		if (findThis == null) {
			return origStr;
		}
		while (origStr.contains(findThis)) {
			origStr = replaceAll(origStr, findThis, replaceWith);
		}
		return origStr;
	}

	/**
	 * replaceAllInBetween("foo ( foo ) foo (foo)", "foo", "bar", "(", ")") gives "foo ( bar ) foo (bar)"
	 */
	public static String replaceAllInBetween(String origStr, String findThis, String replaceWith,
		String betweenStart, String betweenEnd) {

		if (origStr == null) {
			return null;
		}
		if (findThis == null) {
			return origStr;
		}
		if (replaceWith == null) {
			replaceWith = "";
		}
		StringBuilder result = new StringBuilder();
		int startIndex = origStr.indexOf(betweenStart);
		int lastEndIndex = 0;
		while (startIndex >= 0) {
			int endIndex = origStr.indexOf(betweenEnd, startIndex + betweenStart.length());
			if (endIndex >= 0) {
				result.append(origStr.substring(lastEndIndex, startIndex));
				lastEndIndex = endIndex + betweenEnd.length();
				result.append(betweenStart);
				// TODO :: improve speed by not calling replaceAll inside
				// but actually doing it all in this function
				result.append(replaceAll(
					origStr.substring(startIndex + betweenStart.length(), endIndex),
					findThis,
					replaceWith)
				);
				result.append(betweenEnd);
				startIndex = origStr.indexOf(betweenStart, endIndex + betweenEnd.length());
			} else {
				break;
			}
		}
		result.append(origStr.substring(lastEndIndex));
		return result.toString();
	}

	/**
	 * Searches through the origStr, and whenever findThis is found, adds addThat after the end of the line
	 * (if you let addThat end with a newline character, then it will be on its own line after the line
	 * containing findThis)
	 */
	public static String addAfterLinesContaining(String origStr, String findThis, String addThat) {
		return addAfterLinesContainingEx(origStr, findThis, addThat, "\n", false);
	}

	/**
	 * Searches through the origStr, and whenever findThis is found, adds addThat after the end of the line
	 * where the end of line is indicated not by \n, but by whatever eol marker you passed in
	 */
	public static String addAfterLinesContaining(String origStr, String findThis, String addThat, String eolMarker) {
		return addAfterLinesContainingEx(origStr, findThis, addThat, eolMarker, false);
	}

	/**
	 * Searches through the origStr, and whenever findThis is found without being inside a tag adds,
	 * addThat after the end of the line
	 * (if you let addThat end with a newline character, then it will be on its own line after the line
	 * containing findThis)
	 */
	public static String addAfterLinesContainingNotInsideTag(String origStr, String findThis, String addThat) {
		return addAfterLinesContainingEx(origStr, findThis, addThat, "\n", true);
	}

	private static String addAfterLinesContainingEx(String origStr, String findThis, String addThat,
		String eolMarker, boolean notInsideTag) {

		if (origStr == null) {
			return null;
		}
		if (findThis == null) {
			return origStr;
		}
		if ((addThat == null) || "".equals(addThat)) {
			return origStr;
		}
		if ((eolMarker == null) || "".equals(eolMarker)) {
			return origStr;
		}
		if ("".equals(findThis)) {
			return replaceAll(origStr, "\n", "\n" + addThat) + "\n" + addThat;
		}
		StringBuilder result = new StringBuilder();
		int index = origStr.indexOf(findThis);
		while (index >= 0) {
			if (notInsideTag) {
				if ((index > 0) && origStr.charAt(index - 1) == '>') {
					index = origStr.indexOf(findThis, index + 1);
					continue;
				}
			}
			int eol = origStr.indexOf(eolMarker, index);
			if (eol < 0) {
				result.append(origStr);
				result.append(eolMarker);
				origStr = "";
			} else {
				result.append(origStr.substring(0, eol + eolMarker.length()));
				// TODO :: improve speed by not calling substring but keeping track of start!
				origStr = origStr.substring(eol + eolMarker.length());
			}
			result.append(addThat);
			index = origStr.indexOf(findThis);
		}
		result.append(origStr);
		return result.toString();
	}

	/**
	 * Trims whitespace and ensures that internal whitespaces are only the " " character (no newlines,
	 * no tabs), and that there is never more than one space, so e.g.:
	 * " foo    bar   " turns to "foo bar"
	 * In this way, you can also think about this function as "super-trim()" :)
	 */
	public static String normalizeWhitespace(String origStr) {

		if (origStr == null) {
			return null;
		}

		String value = origStr;

		// TODO :: actually make this more performant by iterating over the entire string just once
		// and building the result in a StringBuilder, char-for-char, unless the char is a whitespace...

		// remove non-breaking space character
		value = value.replaceAll("\u00a0", "");
		value = value.replace('\r', ' ');
		value = value.replace('\n', ' ');
		value = value.replace('\t', ' ');

		value = replaceAllRepeatedly(value, "  ", " ");

		// remove outer whitespace characters
		value = value.trim();

		return value;
	}

	/**
	 * remove all whitespaces in the string
	 */
	public static String removeWhitespace(String origStr) {

		if (origStr == null) {
			return null;
		}

		String value = origStr;

		// TODO :: actually make this more performant by iterating over the entire string just once
		// and building the result in a StringBuilder, char-for-char, unless the char is a whitespace...

		value = value.replaceAll("\u00a0", "");
		value = value.replaceAll("\r", "");
		value = value.replaceAll("\n", "");
		value = value.replaceAll("\t", "");
		value = value.replaceAll(" ", "");

		return value;
	}

	/**
	 * Takes some text extracted from HTML, which might contain several linebreaks,
	 * and which might contain funny multiple whitespaces, and transforms it into
	 * one trimmed line with the whitespaces between words being just one space,
	 * never more
	 */
	public static String makeIntoOneLine(String str) {
		if (str == null) {
			return null;
		}
		str = str.replace('\n', ' ');
		str = str.replace('\r', ' ');
		str = str.replace('\t', ' ');
		str = StrUtils.replaceAllRepeatedly(str, "  ", " ");
		str = str.trim();
		return str;
	}

	/**
	 * If a string contains the search string, remove the search string and anything
	 * after it, but do NOT trim the string - if a trimmed string is wanted, let
	 * the caller do that afterwards!
	 *
	 * So:
	 * " blubb (bla)", "(" => " blubb "
	 * " blubb (bla)", "[" => " blubb (bla)"
	 */
	public static String removeContainingAndAfter(String str, String searchStr) {
		int index = str.indexOf(searchStr);
		if (index >= 0) {
			return str.substring(0, index);
		}
		return str;
	}

	/**
	 * If a string ends with the search string, remove the search string and anything
	 * after it, but do NOT trim the string - if a trimmed string is wanted, let
	 * the caller do that afterwards (and possibly before calling this!)
	 *
	 * So:
	 * " blubb (bla)", "(bla)" => " blubb "
	 * " blubb (bla)", "(" => " blubb (bla)"
	 */
	public static String removeTrailing(String str, String searchStr) {
		if (str.endsWith(searchStr)) {
			return str.substring(0, str.length() - searchStr.length());
		}
		return str;
	}

	/**
	 * If a string ends with the search string, remove the search string and anything
	 * after it, but do NOT trim the string - if a trimmed string is wanted, let
	 * the caller do that afterwards (and possibly before calling this!)
	 *
	 * So:
	 * " blubb (BLA)", "(bla)" => " blubb "
	 * " blubb (bla)", "(" => " blubb (bla)"
	 */
	public static String removeTrailingCaseIndifferent(String str, String searchStr) {
		if (str.toLowerCase().endsWith(searchStr)) {
			return str.substring(0, str.length() - searchStr.length());
		}
		return str;
	}

	/**
	 * Takes a name-ish text and removes pronouns, if any are included,
	 * and trims the string
	 *
	 * So:
	 * "Moya (she/any)" => "Moya"
	 * " Hugo he/him " => "Hugo"
	 */
	public static String removeTrailingPronounsFromName(String str) {

		// assume that anything in brackets is a pronoun
		str = removeContainingAndAfter(str, "(");
		str = removeContainingAndAfter(str, "[");

		str = str.trim();

		str = removeTrailing(str, "!");

		for (String pronoun : PRONOUNS) {

			// remove trailing pronoun
			str = removeTrailingCaseIndifferent(str, " " + pronoun);

			// remove trailing pronoun/pronoun
			for (String secondPronoun : PRONOUNS) {
				str = removeTrailingCaseIndifferent(str, " " + pronoun + "/" + secondPronoun);
			}
		}

		str = str.trim();

		str = removeTrailing(str, "-");

		str = str.trim();

		return str;
	}

	public static String detectLineEndStr(String text) {

		String originalLineEndStr = "\n";
		if (text != null) {
			if (text.contains("\r\n")) {
				originalLineEndStr = "\r\n";
			} else if (text.contains("\r")) {
				originalLineEndStr = "\r";
			}
		}

		return originalLineEndStr;
	}

	public static List<String> splitLines(String text) {

		List<String> result = new ArrayList<>();

		String originalLineEndStr = detectLineEndStr(text);

		String[] lines = text.split(originalLineEndStr);

		for (String line : lines) {
			result.add(line);
		}

		return result;
	}

	public static String performMathOps(String str, String ops) {

		if (ops == null) {
			return performMathOps(performMathOps(performMathOps(performMathOps(str, "*"), "/"), "+"), "-");
		}

		int cur = 0;
		StringBuilder result = new StringBuilder();

		int pos = str.indexOf(ops);

		while (pos >= 0) {
			int posBefore = 1;
			char c = str.charAt(pos - posBefore);
			while ((c >= '0') && (c <= '9')) {
				posBefore++;
				c = str.charAt(pos - posBefore);
			}
			int posAfter = 1;
			c = str.charAt(pos + posAfter);
			while ((c >= '0') && (c <= '9')) {
				posAfter++;
				c = str.charAt(pos + posAfter);
			}
			result.append(str.substring(cur, pos + 1 - posBefore));
			int strBefore = strToInt(str.substring(pos + 1 - posBefore, pos));
			int strAfter = strToInt(str.substring(pos + 1, pos + posAfter));
			switch (ops) {
				case "+":
					result.append("" + (strBefore + strAfter));
					break;
				case "-":
					result.append("" + (strBefore - strAfter));
					break;
				case "*":
					result.append("" + (strBefore * strAfter));
					break;
				case "/":
					result.append("" + (strBefore / strAfter));
					break;
			}

			cur = pos + posAfter;
			pos = str.indexOf(ops, cur);
		}

		result.append(str.substring(cur));

		return result.toString();
	}

}
