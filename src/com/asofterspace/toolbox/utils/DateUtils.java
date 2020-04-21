/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


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

	private static final String[] MONTH_NAMES = new String[]{"January", "February", "March",
		"April", "May", "June", "July", "August", "September", "October", "November", "December"};
	private static final String[] MONTH_NAMES_GERMAN = new String[]{"Januar", "Februar", "März",
		"April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember"};
	private static final String[] MONTH_NAMES_HALF_SHORT = new String[]{"Janr", "Febr", "Marc",
		"Aprl", "May", "June", "July", "Augs", "Sept", "Octb", "Novm", "Decm"};
	private static final String[] MONTH_NAMES_SHORT = new String[]{"Jan", "Feb", "Mar",
		"Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
	private static final String[] MONTH_NAMES_SHORT_GERMAN = new String[]{"Jan", "Feb", "Mär",
		"Apr", "Mai", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dez"};


	/**
	 * Parses just the date from either a date string or a date time string
	 */
	public static Date parseDate(String dateStr) {

		if (dateStr == null) {
			return null;
		}

		// also parse months that are written out in English or German
		dateStr = dateStr.toLowerCase();
		for (int i = 0; i < 12; i++) {
			String num = "" + (i+1);
			if (num.length() < 2) {
				num = "0" + num;
			}
			dateStr = dateStr.replaceAll(MONTH_NAMES[i].toLowerCase(), num);
			dateStr = dateStr.replaceAll(MONTH_NAMES_GERMAN[i].toLowerCase(), num);
			dateStr = dateStr.replaceAll(MONTH_NAMES_HALF_SHORT[i].toLowerCase(), num);
			dateStr = dateStr.replaceAll(MONTH_NAMES_SHORT[i].toLowerCase(), num);
			dateStr = dateStr.replaceAll(MONTH_NAMES_SHORT_GERMAN[i].toLowerCase(), num);
		}

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
			return null;
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
			return null;
		}

		return DEFAULT_DATE_FORMAT.format(date);
	}

	public static String serializeDateTime(Date dateTime) {

		if (dateTime == null) {
			return null;
		}

		return DEFAULT_DATE_TIME_FORMAT.format(dateTime);
	}

	public static String dateTimeStampNow() {
		return serializeDateTime(new Date());
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

	/**
	 * Return a list of dates from a date to a date, generated by starting
	 * with from, and adding a day again and again, stopping before to is
	 * come over (so if from is Monday at 13:00, and to is Wednesday at 15:00,
	 * then this returns Monday at 13:00, Tuesday at 13:00 and Wednesday at 13:00!)
	 */
	public static List<Date> listDaysFromTo(Date from, Date to) {
		List<Date> result = new ArrayList<>();
		Date curDate = from;
		Calendar cal = Calendar.getInstance();
		cal.setTime(curDate);
		while (curDate.before(to)) {
			result.add(curDate);
			cal.add(Calendar.DATE, 1);
			curDate = cal.getTime();
		}
		return result;
	}

	public static Integer monthNameToNum(String name) {
		if (name == null) {
			return null;
		}

		name = name.toLowerCase();
		for (int i = 0; i < 12; i++) {
			if (name.equals(MONTH_NAMES[i].toLowerCase())) {
				return i;
			}
			if (name.equals(MONTH_NAMES_GERMAN[i].toLowerCase())) {
				return i;
			}
			if (name.equals(MONTH_NAMES_HALF_SHORT[i].toLowerCase())) {
				return i;
			}
			if (name.equals(MONTH_NAMES_SHORT[i].toLowerCase())) {
				return i;
			}
			if (name.equals(MONTH_NAMES_SHORT_GERMAN[i].toLowerCase())) {
				return i;
			}
		}
		return null;
	}

	public static String monthNumToName(Integer num) {
		if (num == null) {
			return null;
		}
		while (num < 0) {
			num += 12;
		}
		while (num > 11) {
			num -= 12;
		}
		return MONTH_NAMES[num];
	}

	/**
	 * Checks if these dates represent they same day, ignoring the time
	 */
	public static boolean isSameDay(Date someDate, Date otherDate) {
		if (someDate == null) {
			return otherDate == null;
		}
		return serializeDate(someDate).equals(serializeDate(otherDate));
	}
}
