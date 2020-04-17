/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


/**
 * A utility class for date / time fun
 *
 * @author Tom Moya Schiller, moya@asofterspace.com
 */
public class DateUtils {

	private static final String DEFAULT_DATE_FORMAT_STR = "yyyy-MM-dd";
	private static final String FALLBACK_DATE_FORMAT_STR = "dd.MM.yyyy";
	private static final String DEFAULT_DATE_TIME_FORMAT_STR = "yyyy-MM-dd HH:mm:ss.SSS";
	private static final String NUMERICAL_DATE_TIME_FORMAT_STR = "yyyyMMddHHmmssSSS";
	private static final String DEFAULT_TIME_FORMAT_STR = "HH:mm:ss.SSS";

	public static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat(DEFAULT_DATE_FORMAT_STR);
	public static final SimpleDateFormat FALLBACK_DATE_FORMAT = new SimpleDateFormat(FALLBACK_DATE_FORMAT_STR);
	public static final SimpleDateFormat DEFAULT_DATE_TIME_FORMAT = new SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT_STR);
	public static final SimpleDateFormat NUMERICAL_DATE_TIME_FORMAT = new SimpleDateFormat(NUMERICAL_DATE_TIME_FORMAT_STR);
	public static final SimpleDateFormat DEFAULT_TIME_FORMAT = new SimpleDateFormat(DEFAULT_TIME_FORMAT_STR);


	/**
	 * Parses just the date from either a date string or a date time string
	 */
	public static Date parseDate(String dateStr) {

		if (dateStr == null) {
			dateStr = serializeDate(new Date());
		}

		// handle date time string by omitting the timestamp such that we only get a date
		if (dateStr.length() == DEFAULT_DATE_TIME_FORMAT_STR.length()) {
			dateStr = dateStr.substring(0, DEFAULT_DATE_FORMAT_STR.length());
		}

		try {
			return DEFAULT_DATE_FORMAT.parse(dateStr);
		} catch (ParseException ex) {

			dateStr = dateStr.replaceAll(" ", "");

			try {
				return FALLBACK_DATE_FORMAT.parse(dateStr);
			} catch (ParseException ex2) {
				System.err.println("Could not parse the date " + dateStr + " - using current date instead!");
				return parseDate(null);
			}
		}
	}

	/**
	 * Parses the date+time from either a date string or a date time string
	 */
	public static Date parseDateTime(String dateTimeStr) {

		if (dateTimeStr == null) {
			return new Date();
		}

		// handle date string by adding zeroes to get a date time stamp
		if (dateTimeStr.length() == DEFAULT_DATE_FORMAT_STR.length()) {
			dateTimeStr = dateTimeStr + " 00:00:00.000";
		}

		try {
			return DEFAULT_DATE_TIME_FORMAT.parse(dateTimeStr);
		} catch (ParseException ex) {
			System.err.println("Could not parse the date time " + dateTimeStr + " - using current time instead!");
			return new Date();
		}
	}

	public static String serializeDate(Date date) {

		if (date == null) {
			date = new Date();
		}

		return DEFAULT_DATE_FORMAT.format(date);
	}

	public static String serializeDateTime(Date dateTime) {

		if (dateTime == null) {
			dateTime = new Date();
		}

		return DEFAULT_DATE_TIME_FORMAT.format(dateTime);
	}

	public static String dateTimeStampNow() {
		return serializeDateTime(null);
	}

	public static String numericalDateTimeStampNow() {
		return NUMERICAL_DATE_TIME_FORMAT.format(new Date());
	}

	public static Date now() {
		return new Date();
	}

	/**
	 * Returns a date is that is the current date time plus howMany days
	 * (negative values are also allowed)
	 */
	public static Date daysInTheFuture(Integer howMany) {
		if (howMany == null) {
			return new Date();
		}

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, howMany);
		return cal.getTime();
	}
}
