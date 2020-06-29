/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.test.Test;
import com.asofterspace.toolbox.test.TestUtils;
import com.asofterspace.toolbox.utils.DateUtils;
import com.asofterspace.toolbox.Utils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class DateUtilsTest implements Test {

	@Override
	public void runAll() {

		parseAndSerializeDateTest();

		parseDifficultDatesTest();

		parseAndSerializeDateTimeTest();

		createTimestampWithoutExceptionTest();

		monthToNumAndBackTest();

		listDatesTest();
	}

	public void parseAndSerializeDateTest() {

		TestUtils.start("Parse and Serialize Date");

		Date inputDate = new Date();

		String dateStr = DateUtils.serializeDate(inputDate);

		Date outputDate = DateUtils.parseDate(dateStr);

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(inputDate);
		int yearNumIn = calendar.get(Calendar.YEAR);
		int monthNumIn = calendar.get(Calendar.MONTH);
		int dayNumIn = calendar.get(Calendar.DAY_OF_MONTH);
		calendar.setTime(outputDate);
		int yearNumOut = calendar.get(Calendar.YEAR);
		int monthNumOut = calendar.get(Calendar.MONTH);
		int dayNumOut = calendar.get(Calendar.DAY_OF_MONTH);

		if ((yearNumIn != yearNumOut) || (monthNumIn != monthNumOut) || (dayNumIn != dayNumOut)) {
			TestUtils.fail("We serialized a date " + DateUtils.serializeDateTime(inputDate) + " as " + dateStr + " and parsed it again, and the resulting date " + DateUtils.serializeDateTime(outputDate) + " is not the same as before!");
		}

		String dateOutStr = DateUtils.serializeDate(outputDate);

		if (!dateStr.equals(dateOutStr)) {
			TestUtils.fail("We parsed a date and serialized it again, and the resulting date-stamp is not the same as before!");
		}

		String dateTimeStr = DateUtils.serializeDateTime(inputDate);

		Date outputDateFromDateTime = DateUtils.parseDate(dateTimeStr);

		if (!outputDate.equals(outputDateFromDateTime)) {
			TestUtils.fail("We serialized a date time and parsed it again as date and it is not the same as before!");
		}

		TestUtils.succeed();
	}

	public void parseDifficultDatesTest() {

		TestUtils.start("Parse Difficult Dates");

		compareDateParsing("2027-01-28", "2027-01-28 00:00:00.000");
		compareDateParsing("2027-01-28", "2027-01-28 01:02:03.234");

		compareDateParsing("2020-04-02", "2020-April-02");
		compareDateParsing("2020-04-02", "2020-APR-02");
		compareDateParsing("2020-04-02", "2020 04 02");
		compareDateParsing("2020-04-02", "2020 4 2");
		compareDateParsing("2020-04-02", "2020-4 2");
		compareDateParsing("2020-04-02", "2020 4-2");
		compareDateParsing("2020-04-02", "2. 4. 2020");
		compareDateParsing("2020-04-02", "02 04 2020");
		compareDateParsing("2020-04-02", "2. 04 2020");
		compareDateParsing("2020-04-02", "2. April 2020");
		compareDateParsing("2020-04-02", "2.4.2020");

		compareDateParsing("1989-12-10", "10.12.1989");
		compareDateParsing("1989-12-10", "10. Dez 1989");
		compareDateParsing("1989-12-10", "10. Dec 1989");
		compareDateParsing("1989-12-10", "10. december 1989");
		compareDateParsing("1989-12-10", "10. dezember 1989");
		compareDateParsing("1989-12-10", "1989-dec-10");

		compareDateParsing("2001-01-01", "1. Januar 2001");
		compareDateParsing("2001-02-01", "1. februARY 2001");
		compareDateParsing("2001-03-01", "1. march 2001");
		compareDateParsing("2001-04-01", "1. april 2001");
		compareDateParsing("2001-05-01", "1. may 2001");
		compareDateParsing("2001-06-01", "1. Juni 2001");
		compareDateParsing("2001-07-01", "1. July 2001");
		compareDateParsing("2001-08-01", "1. August 2001");
		compareDateParsing("2001-09-01", "1. sept 2001");
		compareDateParsing("2001-10-01", "1. October 2001");
		compareDateParsing("2001-11-01", "1. nov 2001");
		compareDateParsing("2001-12-01", "1. DEC 2001");

		TestUtils.succeed();
	}

	private void compareDateParsing(String dateStr1, String dateStr2) {

		Date date1 = DateUtils.parseDate(dateStr1);
		Date date2 = DateUtils.parseDate(dateStr2);

		if (!date1.equals(date2)) {
			TestUtils.fail("We parsed the date " + dateStr1 + " and the date " + dateStr2 +
				" but they did not result in the same date!\n" +
				"(The first gave " + DateUtils.serializeDate(date1) + " and the second gave " +
				DateUtils.serializeDate(date2) + ")");
		}
	}

	public void parseAndSerializeDateTimeTest() {

		TestUtils.start("Parse and Serialize Date Time");

		Date inputDate = new Date();

		String dateTimeStr = DateUtils.serializeDateTime(inputDate);

		Date outputDate = DateUtils.parseDateTime(dateTimeStr);

		if (!inputDate.equals(outputDate)) {
			TestUtils.fail("We serialized a date time and parsed it again, and the resulting date is not the same as before!");
		}

		String dateTimeOutStr = DateUtils.serializeDateTime(outputDate);

		if (!dateTimeStr.equals(dateTimeOutStr)) {
			TestUtils.fail("We parsed a date time and serialized it again, and the resulting date-time-stamp is not the same as before!");
		}

		String dateStr = DateUtils.serializeDate(inputDate);

		Date outputDateTimeFromDate = DateUtils.parseDateTime(dateStr);

		String dateTimeOutStrFromDate = DateUtils.serializeDate(outputDateTimeFromDate);

		if (!dateTimeOutStr.substring(0, 10).equals(dateTimeOutStrFromDate.substring(0, 10))) {
			TestUtils.fail("We serialized and deserialized a date time a few times and somewhere it stopped representing the same date!");
		}

		TestUtils.succeed();
	}

	public void createTimestampWithoutExceptionTest() {

		TestUtils.start("Create Timestamp without Exception");

		String timeStampNow = DateUtils.dateTimeStampNow();

		if (timeStampNow == null) {
			TestUtils.fail("Attempted to create a date-time-stamp, but it is null!");
		}

		TestUtils.succeed();
	}

	public void monthToNumAndBackTest() {

		TestUtils.start("Month to Num and Back");

		compareMonthToNum("Jan", 0);
		compareMonthToNum("february", 1);
		compareMonthToNum("MÄRZ", 2);
		compareNumToMonth(3, "April");
		compareMonthToNum("JUN", 5);
		compareMonthToNum("sept", 8);
		compareNumToMonth(9-12, "October");
		compareNumToMonth(10+12, "November");
		compareMonthToNum("december", 11);

		TestUtils.succeed();
	}

	private void compareMonthToNum(String monthStr, Integer monthNum) {

		if (DateUtils.monthNameToNum(monthStr) == null) {
			if (monthNum != null) {
				TestUtils.fail("The month '" + monthStr + "' parses as " + DateUtils.monthNameToNum(monthStr) +
					" instead of " + monthNum + "!");
				return;
			}
		}
		if (!DateUtils.monthNameToNum(monthStr).equals(monthNum)) {
			TestUtils.fail("The month '" + monthStr + "' parses as " + DateUtils.monthNameToNum(monthStr) +
				" instead of " + monthNum + "!");
		}
	}

	private void compareNumToMonth(Integer monthNum, String monthStr) {

		if (!DateUtils.monthNumToName(monthNum).equals(monthStr)) {
			TestUtils.fail("The month " + monthNum + " is serialized as " + DateUtils.monthNumToName(monthNum) +
				" instead of " + monthStr + "!");
		}
	}


	public void listDatesTest() {

		TestUtils.start("List Dates");

		int amount = 10;

		Date now = DateUtils.now();
		Date tenDaysAgo = DateUtils.daysInTheFuture(-amount);

		List<Date> result = DateUtils.listDaysFromTo(tenDaysAgo, now);

		if (result.size() != amount) {
			TestUtils.fail("We wanted to list the days from " + amount + " days ago until now, but did not get " + amount + " days!");
		}

		result = DateUtils.listDaysFromTo(now, tenDaysAgo);

		if (result.size() != 0) {
			TestUtils.fail("We wanted to list the days from now until " + amount + " days ago, but did not get 0 days!");
		}

		TestUtils.succeed();
	}

}
