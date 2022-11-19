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

	private static SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat(DEFAULT_DATE_FORMAT_STR);
	private static SimpleDateFormat FALLBACK_DATE_FORMAT = new SimpleDateFormat(FALLBACK_DATE_FORMAT_STR);
	private static SimpleDateFormat DEFAULT_DATE_TIME_FORMAT = new SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT_STR);
	private static SimpleDateFormat NUMERICAL_DATE_TIME_FORMAT = new SimpleDateFormat(NUMERICAL_DATE_TIME_FORMAT_STR);
	private static SimpleDateFormat DEFAULT_TIME_FORMAT = new SimpleDateFormat(DEFAULT_TIME_FORMAT_STR);
	private static SimpleDateFormat SHORT_TIME_FORMAT = new SimpleDateFormat(SHORT_TIME_FORMAT_STR);

	public static final String[] DAY_NAMES = new String[]{"Saturday", "Sunday", "Monday",
		"Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
	public static final String[] MONTH_NAMES = new String[]{"January", "February", "March",
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

		if ((dateStr == null) || "".equals(dateStr)) {
			return null;
		}

		String origDateStr = dateStr;

		// also parse months that are written out in English or German
		dateStr = strContainingMonthNameToStrContainingMonthNum1IsJan(dateStr);
		dateStr = dateStr.toLowerCase();

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
		} catch (ParseException | NumberFormatException | ArrayIndexOutOfBoundsException ex) {
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
		} catch (ParseException | NumberFormatException | ArrayIndexOutOfBoundsException ex2) {
			System.err.println("Could not parse the date '" + origDateStr + "' - returning null instead!");
			return null;
		}
	}

	/**
	 * Parses the date+time from either a date string or a date time string
	 */
	public static Date parseDateTime(String dateTimeStr) {

		if ((dateTimeStr == null) || "".equals(dateTimeStr)) {
			return null;
		}

		// handle date string by adding zeroes to get a date time stamp
		if (!dateTimeStr.contains(":")) {
			dateTimeStr = dateTimeStr + " 00:00:00.000";
		}

		try {
			return DEFAULT_DATE_TIME_FORMAT.parse(dateTimeStr);
		} catch (ParseException | NumberFormatException | ArrayIndexOutOfBoundsException ex) {
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

		try {
			return DEFAULT_DATE_FORMAT.format(date);
		} catch (ArrayIndexOutOfBoundsException aobE) {
			DEFAULT_DATE_FORMAT = new SimpleDateFormat(DEFAULT_DATE_FORMAT_STR);
			return DEFAULT_DATE_FORMAT.format(date);
		}
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

		return serializeDateLong(datetime, beforeUp, afterUp) + ", " + serializeTimeShort(datetime);
	}

	/**
	 * Serializes a date as e.g. 12th of October 2020
	 */
	public static String serializeDateLong(Date datetime) {
		return serializeDateLong(datetime, null, null);
	}

	/**
	 * Serializes a date as e.g. 12<span class="sup">th</span> of October 2020
	 */
	public static String serializeDateLong(Date datetime, String beforeUp, String afterUp) {

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

		return result.toString();
	}

	/**
	 * Serializes a date as e.g. 2020-05-10 01:23:45.678
	 */
	public static String serializeDateTime(Date dateTime) {

		if (dateTime == null) {
			return null;
		}

		try {
			return DEFAULT_DATE_TIME_FORMAT.format(dateTime);
		} catch (ArrayIndexOutOfBoundsException aobE) {
			DEFAULT_DATE_TIME_FORMAT = new SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT_STR);
			return DEFAULT_DATE_TIME_FORMAT.format(dateTime);
		}
	}

	public static String serializeTime(Date time) {

		if (time == null) {
			return null;
		}

		try {
			return DEFAULT_TIME_FORMAT.format(time);
		} catch (ArrayIndexOutOfBoundsException aobE) {
			DEFAULT_TIME_FORMAT = new SimpleDateFormat(DEFAULT_TIME_FORMAT_STR);
			return DEFAULT_TIME_FORMAT.format(time);
		}
	}

	public static String serializeTimeShort(Date time) {

		if (time == null) {
			return null;
		}

		try {
			return SHORT_TIME_FORMAT.format(time);
		} catch (ArrayIndexOutOfBoundsException aobE) {
			SHORT_TIME_FORMAT = new SimpleDateFormat(SHORT_TIME_FORMAT_STR);
			return SHORT_TIME_FORMAT.format(time);
		}
	}

	public static String dateTimeStampNow() {
		return serializeDateTime(new Date());
	}

	public static String numericalDateTimeStampNow() {
		try {
			return NUMERICAL_DATE_TIME_FORMAT.format(new Date());
		} catch (ArrayIndexOutOfBoundsException aobE) {
			NUMERICAL_DATE_TIME_FORMAT = new SimpleDateFormat(NUMERICAL_DATE_TIME_FORMAT_STR);
			return NUMERICAL_DATE_TIME_FORMAT.format(new Date());
		}
	}

	public static Date now() {
		return new Date();
	}

	/**
	 * The month here should start at 1, so January is 1, February is 2, etc.
	 */
	public static Date parseDateNumbers(Integer day, Integer month, Integer year) {

		if (day == null) {
			return null;
		}
		if (month == null) {
			return null;
		}
		if (year == null) {
			return null;
		}

		String dayStr = "" + day;
		String monthStr = "" + month;
		String yearStr = "" + year;

		while (dayStr.length() < 2) {
			dayStr = "0" + dayStr;
		}
		while (monthStr.length() < 2) {
			monthStr = "0" + monthStr;
		}
		while (yearStr.length() < 4) {
			yearStr = "0" + yearStr;
		}

		return parseDate(yearStr + "-" + monthStr + "-" + dayStr);
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
	 * Returns a date that is the addTo date time plus howMany seconds
	 * (negative values are also allowed)
	 */
	public static Date addSeconds(Date addTo, Integer howMany) {

		if (howMany == null) {
			return new Date();
		}

		if (addTo == null) {
			addTo = new Date();
		}

		Calendar cal = Calendar.getInstance();
		cal.setTime(addTo);
		cal.add(Calendar.SECOND, howMany);
		return cal.getTime();
	}

	/**
	 * Return a list of dates, starting on Monday and ending on Sunday,
	 * that the passed in day lies within
	 */
	public static List<Date> getWeekForDate(Date day) {

		// 2 is Monday
		int todayOfWeek = DateUtils.getDayOfWeek(day);
		if (todayOfWeek < 2) {
			todayOfWeek += 7;
		}
		Date weekStart = DateUtils.addDays(day, - (todayOfWeek - 2));

		List<Date> result = new ArrayList<>();
		Date curDate = weekStart;
		Calendar cal = Calendar.getInstance();
		cal.setTime(curDate);
		for (int i = 0; i < 7; i++) {
			result.add(curDate);
			cal.add(Calendar.DATE, 1);
			curDate = cal.getTime();
		}
		return result;
	}

	/**
	 * Return a list of dates from a date to a date, generated by starting
	 * with from, and adding a day again and again, stopping before to is
	 * come over (so if from is Monday at 13:00, and to is Wednesday at 15:00,
	 * then this returns Monday at 13:00, Tuesday at 13:00 and Wednesday at 13:00,
	 * but if from is now and to is now, then actually no days at all are returned!)
	 */
	public static List<Date> listDaysFromTo(Date from, Date to) {
		List<Date> result = new ArrayList<>();
		if ((to == null) || (from == null)) {
			return result;
		}
		if (to.before(from)) {
			Date exchange = from;
			from = to;
			to = exchange;
		}
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

	public static Integer getMinuteDifference(Date from, Date to) {
		if ((from == null) || (to == null)) {
			return null;
		}
		return (int) Math.round((to.getTime() - from.getTime()) / (1000.0 * 60.0));
	}

	/**
	 * Returns true if date a is after date b (JUST speaking about days here!),
	 * false otherwise or in case of nulls
	 */
	public static boolean dateAAfterDateB(Date a, Date b) {
		a = parseDate(serializeDate(a));
		b = parseDate(serializeDate(b));
		if ((a == null) || (b == null)) {
			return false;
		}
		return a.getTime() > b.getTime();
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
	 * Takes in something like "Tuesday", "tue", "Dienstag" and returns "Tuesday" in each case
	 */
	public static String toDayOfWeekNameEN(String weekDay) {
		Integer dayOfWeek = toDayOfWeek(weekDay);
		if (dayOfWeek == null) {
			return null;
		}
		return DateUtils.DAY_NAMES[dayOfWeek];
	}

	/**
	 * Takes in something like 3 and returns "Tuesday"
	 */
	public static String dayNumToDayOfWeekNameEN(Integer dayOfWeek) {
		if (dayOfWeek == null) {
			return null;
		}
		while (dayOfWeek < 0) {
			dayOfWeek += 7;
		}
		while (dayOfWeek > 6) {
			dayOfWeek -= 7;
		}
		return DateUtils.DAY_NAMES[dayOfWeek];
	}

	/**
	 * Takes in something like "Tuesday", "tue", "Dienstag" and returns 3 in each case
	 */
	public static Integer toDayOfWeek(String weekDay) {
		if (weekDay == null) {
			return null;
		}
		weekDay = weekDay.toLowerCase().trim();
		if (weekDay.startsWith("su") || weekDay.startsWith("so")) {
			return 1;
		}
		if (weekDay.startsWith("mo")) {
			return 2;
		}
		if (weekDay.startsWith("tu") || weekDay.startsWith("di")) {
			return 3;
		}
		if (weekDay.startsWith("we") || weekDay.startsWith("mi")) {
			return 4;
		}
		if (weekDay.startsWith("th") || weekDay.startsWith("do")) {
			return 5;
		}
		if (weekDay.startsWith("fr")) {
			return 6;
		}
		if (weekDay.startsWith("sa")) {
			return 7;
		}
		return null;
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

	public static Integer getHour(Date someDate) {
		if (someDate == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(someDate);
		return cal.get(Calendar.HOUR_OF_DAY);
	}

	public static Date setHour(Date someDate, Integer newHour) {

		if (someDate == null) {
			someDate = new Date();
		}

		if (newHour == null) {
			return someDate;
		}

		Calendar cal = Calendar.getInstance();
		cal.setTime(someDate);
		cal.set(Calendar.HOUR_OF_DAY, newHour);
		return cal.getTime();
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

	public static String getMonthNameShortEN(Date someDate) {
		Integer val = getMonth(someDate);
		if (val == null) {
			return "Non";
		}
		return MONTH_NAMES_SHORT[val - 1];
	}

	public static Integer getYear(Date someDate) {
		if (someDate == null) {
			return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(someDate);
		return cal.get(Calendar.YEAR);
	}

	public static Date getFirstDateInMonth(String monthName, int year) {
		monthName = strContainingMonthNameToStrContainingMonthNum1IsJan(monthName).trim();
		try {
			return DEFAULT_DATE_FORMAT.parse(StrUtils.leftPad0(year, 4) + "-" + monthName + "-01");
		} catch (ParseException | NumberFormatException | ArrayIndexOutOfBoundsException ex) {
			return null;
		}
	}

	public static Date getDateInMonth(String monthName, int year) {
		monthName = strContainingMonthNameToStrContainingMonthNum1IsJan(monthName).trim();
		try {
			return DEFAULT_DATE_FORMAT.parse(StrUtils.leftPad0(year, 4) + "-" + monthName + "-15");
		} catch (ParseException | NumberFormatException | ArrayIndexOutOfBoundsException ex) {
			return null;
		}
	}

	public static Date getLastDateInMonth(String monthName, int year) {
		int day = 32;
		monthName = strContainingMonthNameToStrContainingMonthNum1IsJan(monthName).trim();
		while (day > 25) {
			try {
				String dateStr = StrUtils.leftPad0(year, 4) + "-" + monthName + "-" + day;
				return DEFAULT_DATE_FORMAT.parse(dateStr);
			} catch (ParseException | NumberFormatException | ArrayIndexOutOfBoundsException ex) {
				day--;
			}
		}
		return null;
	}

	/**
	 * parse months that are written out in English or German from "January" to "01" etc.
	 */
	private static String strContainingMonthNameToStrContainingMonthNum1IsJan(String dateStr) {
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
		return dateStr;
	}

	public static boolean isLessThanXDaysInTheFuture(Date date, int x) {

		Date now = new Date();

		// get the time difference between the (future) date and now
		long diffTime = date.getTime() - now.getTime();

		// if the future date is earlier than now, then the date is not in the future at all!
		if (diffTime < 0) {
			return false;
		}

		long diffDays = diffTime / (1000 * 60 * 60 * 24);

		// the final result depends on whether the difference is less than x - or not (yet)
		return diffDays < x;
	}

	/**
	 * Does this year contain a February the 29th?
	 */
	public static boolean isLeapYear(Integer year) {
		if (year == null) {
			return false;
		}
		Date feb28 = parseDate(year + "-02-28");
		Date feb29 = addDays(feb28, 1);
		if (getDayOfMonth(feb29) == 1) {
			return false;
		}
		return true;
	}

	public static String getCurrentDateTimeStamp() {
		Date date = new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		String day = StrUtils.leftPad0(cal.get(Calendar.DAY_OF_MONTH), 2);
		String month = StrUtils.leftPad0(cal.get(Calendar.MONTH) + 1, 2);
		String year = StrUtils.leftPad0(cal.get(Calendar.YEAR), 4);
		String hours = StrUtils.leftPad0(cal.get(Calendar.HOUR_OF_DAY), 2);
		String minutes = StrUtils.leftPad0(cal.get(Calendar.MINUTE), 2);
		String seconds = StrUtils.leftPad0(cal.get(Calendar.SECOND), 2);
		String weekday = DAY_NAMES[cal.get(Calendar.DAY_OF_WEEK)];
		return year + "-" + month + "-" + day + ", " + weekday + ", " + hours + ":" + minutes + ":" + seconds;
	}

}
