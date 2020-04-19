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

		// also parse months that are written out in English or German
		dateStr = dateStr.toLowerCase();
		dateStr = dateStr.replaceAll("january", "01");
		dateStr = dateStr.replaceAll("januar", "01");
		dateStr = dateStr.replaceAll("jan", "01");
		dateStr = dateStr.replaceAll("february", "02");
		dateStr = dateStr.replaceAll("februar", "02");
		dateStr = dateStr.replaceAll("feb", "02");
		dateStr = dateStr.replaceAll("march", "03");
		dateStr = dateStr.replaceAll("märz", "03");
		dateStr = dateStr.replaceAll("mar", "03");
		dateStr = dateStr.replaceAll("april", "04");
		dateStr = dateStr.replaceAll("apr", "04");
		dateStr = dateStr.replaceAll("may", "05");
		dateStr = dateStr.replaceAll("mai", "05");
		dateStr = dateStr.replaceAll("june", "06");
		dateStr = dateStr.replaceAll("juni", "06");
		dateStr = dateStr.replaceAll("jun", "06");
		dateStr = dateStr.replaceAll("july", "07");
		dateStr = dateStr.replaceAll("juli", "07");
		dateStr = dateStr.replaceAll("jul", "07");
		dateStr = dateStr.replaceAll("august", "08");
		dateStr = dateStr.replaceAll("aug", "08");
		dateStr = dateStr.replaceAll("september", "09");
		dateStr = dateStr.replaceAll("sept", "09");
		dateStr = dateStr.replaceAll("sep", "09");
		dateStr = dateStr.replaceAll("october", "10");
		dateStr = dateStr.replaceAll("oktober", "10");
		dateStr = dateStr.replaceAll("oct", "10");
		dateStr = dateStr.replaceAll("okt", "10");
		dateStr = dateStr.replaceAll("november", "11");
		dateStr = dateStr.replaceAll("nov", "11");
		dateStr = dateStr.replaceAll("december", "12");
		dateStr = dateStr.replaceAll("dezember", "12");
		dateStr = dateStr.replaceAll("dec", "12");
		dateStr = dateStr.replaceAll("dez", "12");

		// handle date time string by omitting the timestamp such that we only get a date
		if (dateStr.length() == DEFAULT_DATE_TIME_FORMAT_STR.length()) {
			dateStr = dateStr.substring(0, DEFAULT_DATE_FORMAT_STR.length());
		}

		try {
			return DEFAULT_DATE_FORMAT.parse(dateStr);
		} catch (ParseException ex) {

			// we want to be able to parse:
			// dd. MM. yyyy
			// dd MM yyyy
			// dd.MM.yyyy

			dateStr = dateStr.replaceAll(" ", ". ");

			// we are now at:
			// dd.. MM.. yyyy
			// dd. MM. yyyy
			// dd.MM.yyyy

			dateStr = dateStr.replaceAll(" ", "");

			// we are now at:
			// dd..MM..yyyy
			// dd.MM.yyyy
			// dd.MM.yyyy

			dateStr = dateStr.replaceAll("\\.\\.", ".");

			// we are now at:
			// dd.MM.yyyy
			// dd.MM.yyyy
			// dd.MM.yyyy

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
