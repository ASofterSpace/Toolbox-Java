/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * A utility class for date / time fun
 *
 * @author Tom Moya Schiller, moya@asofterspace.com
 */
public class DateUtils {

	public static final SimpleDateFormat DEFAULT_DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	public static final SimpleDateFormat DEFAULT_TIME_FORMAT = new SimpleDateFormat("HH:mm:ss.SSS");


	public static Date parseDateTime(String dateTimeStr) {

		if (dateTimeStr == null) {
			return new Date();
		}

		try {
			return DEFAULT_DATE_TIME_FORMAT.parse(dateTimeStr);
		} catch (ParseException ex) {
			System.err.println("Could not parse the date time " + dateTimeStr + " - using current time instead!");
			return new Date();
		}
	}

	public static String serializeDateTime(Date dateTime) {

		if (dateTime == null) {
			dateTime = new Date();
		}

		return DEFAULT_DATE_TIME_FORMAT.format(dateTime);
	}
}
