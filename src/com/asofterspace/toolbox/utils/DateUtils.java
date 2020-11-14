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
	private static final String SHORT_TIME_FORMAT_STR = "HH:mm";

	public static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat(DEFAULT_DATE_FORMAT_STR);
	public static final SimpleDateFormat FALLBACK_DATE_FORMAT = new SimpleDateFormat(FALLBACK_DATE_FORMAT_STR);
	public static final SimpleDateFormat DEFAULT_DATE_TIME_FORMAT = new SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT_STR);
	public static final SimpleDateFormat NUMERICAL_DATE_TIME_FORMAT = new SimpleDateFormat(NUMERICAL_DATE_TIME_FORMAT_STR);
	public static final SimpleDateFormat DEFAULT_TIME_FORMAT = new SimpleDateFormat(DEFAULT_TIME_FORMAT_STR);
	public static final SimpleDateFormat SHORT_TIME_FORMAT = new SimpleDateFormat(SHORT_TIME_FORMAT_STR);

	private static final String[] DAY_NAMES = new String[]{"Saturday", "Sunday", "Monday",
		"Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
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
		if (dateStr.length() > DEFAULT_DATE_TIME_FORMAT_STR.length()) {
			dateStr = dateStr.substring(0, DEFAULT_DATE_FORMAT_STR.length());
		}

		try {
			dateStr = dateStr.replaceAll(" ", "-");
			// if we do not have 02-04-2020, but more like 2020-04-02
			if (dateStr.length() > 5) {
				if (!((dateStr.charAt(2) == '-') && (dateStr.charAt(5) == '-'))) {
					return DEFAULT_DATE_FORMAT.parse(dateStr);
				}
			}
		} catch (ParseException | NumberFormatException ex) {
			// oh no! fall through to backup approach...
		}

		dateStr = dateStr.replaceAll("-", " ");

		// we want to be able to parse:
		// dd. MM. yyyy
		// dd MM yyyy
		// dd.MM.yyyy

		dateStr = dateStr.replaceAll(" ", ".");

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
		} catch (ParseException | NumberFormatException ex2) {
			System.err.println("Could not parse the date " + dateStr + " - returning null instead!");
			return null;
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
		if (!dateTimeStr.contains(":")) {
			dateTimeStr = dateTimeStr + " 00:00:00.000";
		}

		try {
			return DEFAULT_DATE_TIME_FORMAT.parse(dateTimeStr);
		} catch (ParseException | NumberFormatException ex) {
			System.err.println("Could not parse the date time " + dateTimeStr + " - using current time instead!");
			return new Date();
		}
	}

	/**
	 * Serializes a date as e.g. 2020-05-10
	 */
	public static String serializeDate(Date date) {

		if (date == null) {
			return null;
		}

		return DEFAULT_DATE_FORMAT.format(date);
	}

	/**
	 * Serializes a date-time as e.g. 12th of October 2020, 15:37
	 * (when we are unsure about the date, we probably don't need the seconds and milliseconds!)
	 */
	public static String serializeDateTimeLong(Date datetime) {
		return serializeDateTimeLong(datetime, null, null);
	}

	/**
	 * Serializes a date-time as e.g. 12<span class="sup">th</span> of October 2020, 15:37
	 */
	public static String serializeDateTimeLong(Date datetime, String beforeUp, String afterUp) {

		if (datetime == null) {
			return null;
		}

		int day = getDayOfMonth(datetime);

		StringBuilder result = new StringBuilder();

		result.append(day);

		if (beforeUp != null) {
			result.append(beforeUp);
		}

		switch (day) {
			case 1:
			case 21:
			case 31:
				result.append("st");
				break;
			case 2:
			case 22:
			case 32:
				result.append("nd");
				break;
			case 3:
			case 23:
			case 33:
				result.append("rd");
				break;
			default:
				result.append("th");
				break;
		}

		if (afterUp != null) {
			result.append(afterUp);
		}

		result.append(" of ");

		result.append(getMonthNameEN(datetime));
		result.append(" ");
		result.append(getYear(datetime));

		return result.toString() + ", " + serializeTimeShort(datetime);
	}

	/**
	 * Serializes a date as e.g. 12. October 2020
	 */
	public static String serializeDateLong(Date date) {

		if (date == null) {
			return null;
		}

		// we explicitly want the result to always be in English, so we do not want to rely on
		// standard serialization from MMM in the format string but instead serialize the month
		// part ourselves...
		String result = FALLBACK_DATE_FORMAT.format(date);
		for (int i = 0; i < MONTH_NAMES.length; i++) {
			result = result.replaceAll("\\." + StrUtils.leftPad0(i+1, 2) + "\\.", ". " + MONTH_NAMES[i] + " ");
		}
		return result;
	}

	/**
	 * Serializes a date as e.g. 2020-05-10 01:23:45.678
	 */
	public static String serializeDateTime(Date dateTime) {

		if (dateTime == null) {
			return null;
		}

		return DEFAULT_DATE_TIME_FORMAT.format(dateTime);
	}

	public static String serializeTime(Date time) {

		if (time == null) {
			return null;
		}

		return DEFAULT_TIME_FORMAT.format(time);
	}

	public static String serializeTimeShort(Date time) {

		if (time == null) {
			return null;
		}

		return SHORT_TIME_FORMAT.format(time);
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
		return addDays(new Date(), howMany);
	}

	/**
	 * Returns a date that is the addTo date time plus howMany days
	 * (negative values are also allowed)
	 */
	public static Date addDays(Date addTo, Integer howMany) {

		if (howMany == null) {
			return new Date();
		}

		if (addTo == null) {
			addTo = new Date();
		}

		Calendar cal = Calendar.getInstance();
		cal.setTime(addTo);
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

	/**
	 * How much is the different from from until to?
	 * (if we have the 2nd of January and the 4th of the January, the result will be 2...
	 * if we have the same day twice, it will be zero...
	 * if from if after to, it will be negative!)
	 */
	public static Integer getDayDifference(Date from, Date to) {
		from = parseDate(serializeDate(from));
		to = parseDate(serializeDate(to));
		if ((from == null) || (to == null)) {
			return null;
		}
		return (int) Math.round((to.getTime() - from.getTime()) / (1000.0 * 60.0 * 60.0 * 24.0));
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

	public static Integer getDayOfMonth(Date someDate) {
		if (someDate == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(someDate);
		return cal.get(Calendar.DAY_OF_MONTH);
	}

	public static Integer getDayOfWeek(Date someDate) {
		if (someDate == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(someDate);
		return cal.get(Calendar.DAY_OF_WEEK);
	}

	public static String getDayOfWeekNameEN(Date someDate) {
		Integer val = getDayOfWeek(someDate);
		if (val == null) {
			return "Nonday";
		}
		return DAY_NAMES[val];
	}

	/**
	 * Gets the month number, January is 1
	 */
	public static Integer getMonth(Date someDate) {
		if (someDate == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(someDate);
		return cal.get(Calendar.MONTH) + 1;
	}

	public static String getMonthNameEN(Date someDate) {
		Integer val = getMonth(someDate);
		if (val == null) {
			return "Nonuary";
		}
		return MONTH_NAMES[val - 1];
	}

	public static Integer getYear(Date someDate) {
		if (someDate == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(someDate);
		return cal.get(Calendar.YEAR);
	}
}
