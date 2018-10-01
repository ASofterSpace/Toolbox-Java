package com.asofterspace.toolbox;

import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;


public class Utils {

	public final static int TOOLBOX_VERSION_NUMBER = 24;

	// these values are set once at the startup of the program which contains
	// the Utils and are constant from then onwards
	public static String PROGRAM_TITLE;
	public static String VERSION_NUMBER;
	public static String VERSION_DATE;

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

	public static String strListToString(List<String> stringList) {

		StringBuilder sb = new StringBuilder();

		if (stringList != null) {
			for (String jsonStr : stringList) {
				sb.append(jsonStr);
			}
		}

		return sb.toString();
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
	 * Writes a log line to standard out together with debug information (the current time and JVM heap size)
	 */
	public static void debuglog(String logline) {
	
		long curHeap = Runtime.getRuntime().totalMemory();
		
		String heapStr = curHeap + " B";
		
		if (curHeap > 5000l) {
			heapStr = (curHeap / 1000l) + " KB";
		}
		if (curHeap > 5000000l) {
			heapStr = (curHeap / 1000000l) + " MB";
		}
		if (curHeap > 5000000000l) {
			heapStr = (curHeap / 1000000000l) + " GB";
		}
	
		SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");
		System.out.println(format.format(new Date()) + " [heap " + heapStr + "]: " + logline);
	}
	
	public static String xmlEscape(String text) {
	
		StringBuilder result = new StringBuilder();
		
		for (int i = 0; i < text.length(); i++) {
		
			char c = text.charAt(i);
			
			switch (c) {
				case '<':
					result.append("&lt;");
					break;
				case '>':
					result.append("&gt;");
					break;
				case '\n':
					result.append("&#10;");
					break;
				case '\r':
					break;
				case '\t':
					result.append("&#9;");
					break;
				case '&':
					result.append("&amp;");
					break;
				case '\'':
					result.append("&apos;");
					break;
				case '\"':
					result.append("&quot;");
					break;
			default:
				if (c > 0x7e) {
					result.append("&#");
					result.append((int) c);
					result.append(";");
				} else {
					result.append(c);
				}
			}
		}

		return result.toString();
	}
}
