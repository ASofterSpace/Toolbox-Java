package com.asofterspace.toolbox;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class Utils {

	public final static int TOOLBOX_VERSION_NUMBER = 10;

    private static final char[] ECORE_UUID_LETTERS = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'};

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
		return "A Softer Space " + getProgramTitle() + " Version " + getVersionNumber();
	}
	
	public static String generateEcoreUUID() {

		// start with a real UUID!
        UUID sourceUUID = UUID.randomUUID();
		
		// convert the UUID to base 64
		ByteBuffer sourceByteBuf = ByteBuffer.wrap(new byte[16]);
		sourceByteBuf.putLong(sourceUUID.getMostSignificantBits());
		sourceByteBuf.putLong(sourceUUID.getLeastSignificantBits());

        byte[] sourceBytes = sourceByteBuf.array();
		
		String base64UUID = Base64.getUrlEncoder().encodeToString(sourceBytes);
		
		// add starting underscore and remove trailing ==
		String ecoreUUID = "_" + base64UUID.substring(0, 22);

        return ecoreUUID;
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
}
