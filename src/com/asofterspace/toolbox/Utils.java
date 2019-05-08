/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;


public class Utils {

	public final static int TOOLBOX_VERSION_NUMBER = 42;

	// these values are set once at the startup of the program which contains
	// the Utils and are constant from then onwards
	public static String PROGRAM_TITLE;
	public static String VERSION_NUMBER;
	public static String VERSION_DATE;

	// we use Latin-1 for binaries, as that allows us to load the entire byte range of 0..255 of ASCII files,
	// meaning that we can read any nonsensical streams and don't have to worry about incompatibilities
	// (if we just read and save we have no problem with Unicode characters either; if we actually set
	// Unicode text for some reason, then we will have to think a bit harder and maybe manually convert
	// the Unicode letters that we are aware of into their same-byte counterparts or whatever... ^^)
	public static final Charset BINARY_CHARSET = StandardCharsets.ISO_8859_1;

	private static Random randGen = null;



	public static void setProgramTitle(String programTitle) {
		PROGRAM_TITLE = programTitle;
	}

	public static void setVersionNumber(String versionNumber) {
		VERSION_NUMBER = versionNumber;
	}

	public static void setVersionDate(String versionDate) {
		VERSION_DATE = versionDate;
	}

	public static String getProgramTitle() {
		return PROGRAM_TITLE;
	}

	public static String getVersionNumber() {
		return VERSION_NUMBER;
	}

	public static String getVersionDate() {
		return VERSION_DATE;
	}

	public static String getFullProgramIdentifier() {
		return "A Softer Space " + getProgramTitle() + " version " + getVersionNumber();
	}

	public static String getFullProgramIdentifierWithDate() {
		return getFullProgramIdentifier() + " (" + getVersionDate() + ")";
	}

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

	public static String strListToString(List<String> stringList) {

		StringBuilder sb = new StringBuilder();

		if (stringList != null) {
			for (String jsonStr : stringList) {
				sb.append(jsonStr);
			}
		}

		return sb.toString();
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

	public static String longToHumanReadableByteAmountLocal(long byteAmount) {

		if (byteAmount > 5 * 1024 * 1024 * 1024) {
			return String.format("%.2f", byteAmount / (1024.0 * 1024.0 * 1024.0)) + " GB";
		}

		if (byteAmount > 5 * 1024 * 1024) {
			return String.format("%.2f", byteAmount / (1024.0 * 1024.0)) + " MB";
		}

		if (byteAmount > 5 * 1024) {
			return String.format("%.2f", byteAmount / 1024.0) + " KB";
		}

		return byteAmount + " B";
	}

	public static String longToHumanReadableByteAmount(long byteAmount) {

		return longToHumanReadableByteAmountLocal(byteAmount).replace(",", ".");

	}

	/**
	 * Writes a log line to standard out together with debug information (the current time and JVM heap size)
	 */
	public static void debuglog(String logline) {

		long curHeap = Runtime.getRuntime().totalMemory();

		String heapStr = longToHumanReadableByteAmount(curHeap);

		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");
		System.out.println(format.format(new Date()) + " [heap " + heapStr + "]: " + logline);
	}

}
