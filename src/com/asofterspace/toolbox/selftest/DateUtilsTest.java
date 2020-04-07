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


public class DateUtilsTest implements Test {

	@Override
	public void runAll() {

		parseAndSerializeDateTest();

		parseAndSerializeDateTimeTest();

		createTimestampWithoutExceptionTest();
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
}
