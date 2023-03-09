/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox;

import com.asofterspace.toolbox.utils.BitUtils;
import com.asofterspace.toolbox.utils.DateUtils;

import java.util.Date;


public class Utils {

	public final static int TOOLBOX_VERSION_NUMBER = 101;

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

	/**
	 * Writes a log line to standard out together with debug information (the current time and JVM heap size)
	 */
	public static void debuglog(String logline) {

		long curHeap = Runtime.getRuntime().totalMemory();

		String heapStr = BitUtils.longToHumanReadableByteAmount(curHeap);

		System.out.println(DateUtils.serializeTime(new Date()) + " [heap " + heapStr + "]: " + logline);
	}

	public static void sleep(double milliseconds) {
		sleep((int) milliseconds);
	}

	/**
	 * A quick utility function to just sleep (if allowed) - and not do anything special
	 * if we are being interrupted (just return a bit earlier)
	 */
	public static void sleep(int milliseconds) {

		try {

			Thread.sleep(milliseconds);

		} catch (InterruptedException e) {
			// we were interrupted, hooray!
			// ... let's just return then :)
		}
	}

}
