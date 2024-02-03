/**
 * Unlicensed code created by A Softer Space, 2023
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.utils;

import java.util.Date;


/**
 * A DateHolder object holds a date - so far so good...
 * Basically, I want to avoid reading dates from JSON and writing dates to JSON
 * always needing to convert String to (java) Date and (java) Date to String.
 * Therefore, this DateHolder exists, which just parses a String by... storing it.
 * And then re-writes that String later on, no questions asked.
 * And only if and when actual deserialization is necessary will that actually happen. :)
 *
 * @author Moya Schiller, moya@asofterspace.com
 */
public class DateHolder {

	private boolean isNull = true;

	private Date date = null;

	private String dateTimeStr = null;

	private final static String EMPTY_DATE_FORMAT_STR = "0000-00-00";
	private final static String EMPTY_TIME_FORMAT_STR = "00:00:00.000";
	private final static String EMPTY_FORMAT_STR = EMPTY_DATE_FORMAT_STR + " " + EMPTY_TIME_FORMAT_STR;
	private final static int FORMAT_LENGTH = EMPTY_FORMAT_STR.length();
	private final static int DATE_FORMAT_LENGTH = EMPTY_DATE_FORMAT_STR.length();


	// constructor - not accessible from anywhere else, as only the DateUtils should build these
	// (to ensure they get properly initialized - especially just one init function should be called...)
	DateHolder() {
	}

	void initParsingExactString(String dateStrArg) {
		this.isNull = true;
		this.date = null;
		this.dateTimeStr = null;

		if (dateStrArg == null) {
			return;
		}

		if (dateStrArg.length() > FORMAT_LENGTH) {
			dateStrArg = dateStrArg.substring(0, FORMAT_LENGTH);
		}

		if (dateStrArg.length() < FORMAT_LENGTH) {
			dateStrArg = dateStrArg + EMPTY_FORMAT_STR.substring(dateStrArg.length());
		}

		this.dateTimeStr = dateStrArg;
		this.isNull = false;
	}

	/**
	 * Parse a date string into date holder object leniently - this allows for all kinds of
	 * date formats, but does use the internal java Date class and ensures the date actually
	 * makes sense
	 */
	void initWithDate(Date argDate) {
		this.isNull = true;
		this.date = null;
		this.dateTimeStr = null;

		if (argDate == null) {
			return;
		}

		this.date = argDate;

		this.isNull = false;
	}

	public Date getDate() {
		if (isNull) {
			return null;
		}

		if (date == null) {
			date = DateUtils.parseDateTime(dateTimeStr);
		}

		return date;
	}

	public String toString() {
		if (isNull) {
			return null;
		}

		if (dateTimeStr == null) {
			dateTimeStr = DateUtils.serializeDateTime(date);
		}

		return dateTimeStr;
	}

	public String serializeDate() {
		String result = toString();
		if (result == null) {
			return null;
		}
		return result.substring(0, DATE_FORMAT_LENGTH);
	}

	public String serializeDateTime() {
		return toString();
	}

	public boolean getIsNull() {
		return isNull;
	}

	@Override
	public boolean equals(Object other) {

		if (other == null) {
			return isNull;
		}

		if (other instanceof DateHolder) {
			DateHolder otherDateHolder = (DateHolder) other;

			if (otherDateHolder.isNull) {
				return isNull;
			} else {
				if (isNull) {
					return false;
				}
			}

			// If our values for toString() are different...
			if (this.toString() == null) {
				if (otherDateHolder.toString() != null) {
					// ... then we are not the same!
					return false;
				}
			} else if (!this.toString().equals(otherDateHolder.toString())) {
				// ... then we are not the same!
				return false;
			}

			// We have no reason to assume that we are not the same
			return true;
		}

		// If the other one cannot even be cast to us, then we are not the same!
		return false;
	}

	@Override
	public int hashCode() {
		if (this.isNull) {
			return 0;
		}
		return this.toString().hashCode();
	}

}
