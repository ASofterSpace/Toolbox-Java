/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.calendar;

import com.asofterspace.toolbox.utils.DateUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class GenericTask {

	protected String title;

	// on which day of the month is this task scheduled?
	protected Integer scheduledOnDay;

	// on which days of the week is this task scheduled?
	protected List<String> scheduledOnDaysOfWeek;

	// in which months is this task scheduled?
	// (if null / empty list: every month)
	protected List<Integer> scheduledInMonths;

	// in which years is this task scheduled?
	// (if null / empty list: every year)
	protected List<Integer> scheduledInYears;

	protected List<String> details;

	// what should be done once this task is completed?
	protected List<String> onDone;

	// has this task already been done?
	protected Boolean done;

	// for which date was this task instance released for the user to look at?
	// (this might be before or after the date the user actually first saw it,
	// e.g. the user have have seen it as a future task, or it may have been
	// generated days later... this is really the date in the calendar that
	// triggered the schedule for this task to generate this instance!)
	protected Integer releasedOnDay;
	protected Integer releasedInMonth;
	protected Integer releasedInYear;

	// when was this task done?
	protected Date doneDate;

	// what interesting things did the user encounter while doing this task?
	protected String doneLog;


	public GenericTask(String title, Integer scheduledOnDay, List<String> scheduledOnDaysOfWeek, List<Integer> scheduledInMonths,
		List<Integer> scheduledInYears, List<String> details, List<String> onDone) {

		this.title = title;
		this.scheduledOnDay = scheduledOnDay;
		this.scheduledOnDaysOfWeek = scheduledOnDaysOfWeek;
		this.scheduledInMonths = scheduledInMonths;
		this.scheduledInYears = scheduledInYears;
		this.details = details;
		this.onDone = onDone;
	}

	/**
	 * Constructs a new task based on an existing without keeping the task instance details,
	 * but instead just keeping the generic task details
	 */
	public GenericTask(GenericTask other) {
		this.title = other.title;
		this.scheduledOnDay = other.scheduledOnDay;
		this.scheduledOnDaysOfWeek = other.scheduledOnDaysOfWeek;
		this.scheduledInMonths = other.scheduledInMonths;
		this.scheduledInYears = other.scheduledInYears;
		this.details = other.details;
		this.onDone = other.onDone;
	}

	public GenericTask getNewInstance() {
		return new GenericTask(this);
	}

	private static String toWeekDay(String weekDay) {
		if (weekDay == null) {
			return null;
		}
		weekDay = weekDay.toLowerCase().trim();
		if (weekDay.startsWith("su") || weekDay.startsWith("so")) {
			return DateUtils.DAY_NAMES[1];
		}
		if (weekDay.startsWith("mo")) {
			return DateUtils.DAY_NAMES[2];
		}
		if (weekDay.startsWith("tu") || weekDay.startsWith("di")) {
			return DateUtils.DAY_NAMES[3];
		}
		if (weekDay.startsWith("we") || weekDay.startsWith("mi")) {
			return DateUtils.DAY_NAMES[4];
		}
		if (weekDay.startsWith("th") || weekDay.startsWith("do")) {
			return DateUtils.DAY_NAMES[5];
		}
		if (weekDay.startsWith("fr")) {
			return DateUtils.DAY_NAMES[6];
		}
		if (weekDay.startsWith("sa")) {
			return DateUtils.DAY_NAMES[7];
		}
		return null;
	}

	public boolean isScheduledOn(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);

		if (scheduledOnDay != null) {
			if (!scheduledOnDay.equals(cal.get(Calendar.DAY_OF_MONTH))) {
				return false;
			}
		}

		if (scheduledOnDaysOfWeek != null) {
			if (scheduledOnDaysOfWeek.size() > 0) {
				boolean foundDay = false;
				String dayName = DateUtils.DAY_NAMES[cal.get(Calendar.DAY_OF_WEEK)];
				for (String weekDay : scheduledOnDaysOfWeek) {
					if (dayName.equals(toWeekDay(weekDay))) {
						foundDay = true;
						break;
					}
				}
				if (!foundDay) {
					return false;
				}
			}
		}

		if (scheduledInMonths != null) {
			if (scheduledInMonths.size() > 0) {
				boolean foundMonth = false;
				for (Integer month : scheduledInMonths) {
					if (month.equals(cal.get(Calendar.MONTH))) {
						foundMonth = true;
						break;
					}
				}
				if (!foundMonth) {
					return false;
				}
			}
		}

		if (scheduledInYears != null) {
			if (scheduledInYears.size() > 0) {
				boolean foundYear = false;
				for (Integer year : scheduledInYears) {
					if (year.equals(cal.get(Calendar.YEAR))) {
						foundYear = true;
						break;
					}
				}
				if (!foundYear) {
					return false;
				}
			}
		}

		return true;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public Integer getScheduledOnDay() {
		return scheduledOnDay;
	}

	public List<String> getScheduledOnDaysOfWeek() {
		if (scheduledOnDaysOfWeek == null) {
			return null;
		}
		List<String> result = new ArrayList<>();
		for (String dayOfWeek : scheduledOnDaysOfWeek) {
			result.add(toWeekDay(dayOfWeek));
		}
		return result;
	}

	public List<Integer> getScheduledInMonths() {
		return scheduledInMonths;
	}

	public List<Integer> getScheduledInYears() {
		return scheduledInYears;
	}

	/**
	 * Return detailed instructions for the user such that they know what to do with this task
	 */
	public List<String> getDetails() {
		return details;
	}

	public void setDetails(List<String> newDetails) {
		this.details = newDetails;
	}

	public void setDetailsStr(String newDetails) {

		if (newDetails == null) {
			this.details = null;
			return;
		}

		List<String> detailsList = new ArrayList<>();
		for (String detail : newDetails.split("\n")) {
			detailsList.add(detail);
		}

		this.details = detailsList;
	}

	public List<String> getOnDone() {
		return onDone;
	}

	public Date getReleaseDate() {
		return DateUtils.parseDate(getReleasedInYear() + "-" + (getReleasedInMonth() + 1) + "-" + getReleasedOnDay());
	}

	public Boolean hasBeenDone() {
		return done;
	}

	public void setDone(Boolean done) {
		this.done = done;
	}

	public Integer getReleasedOnDay() {
		return releasedOnDay;
	}

	public void setReleasedOnDay(Integer releasedOnDay) {
		this.releasedOnDay = releasedOnDay;
	}

	public Integer getReleasedInMonth() {
		return releasedInMonth;
	}

	public void setReleasedInMonth(Integer releasedInMonth) {
		this.releasedInMonth = releasedInMonth;
	}

	public Integer getReleasedInYear() {
		return releasedInYear;
	}

	public void setReleasedInYear(Integer releasedInYear) {
		this.releasedInYear = releasedInYear;
	}

	public String getReleasedDateStr() {
		String day = ""+getReleasedOnDay();
		if (day.length() < 2) {
			day = "0" + day;
		}
		String month = ""+(getReleasedInMonth()+1);
		if (month.length() < 2) {
			month = "0" + month;
		}
		return getReleasedInYear() + "-" + month + "-" + day;
	}

	public void setReleasedDate(Date day) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(day);
		setReleasedOnDay(cal.get(Calendar.DAY_OF_MONTH));
		setReleasedInMonth(cal.get(Calendar.MONTH));
		setReleasedInYear(cal.get(Calendar.YEAR));
	}

	/**
	 * Has this task been released in the future?
	 * (only makes sense for task instances - tasks that are not instances have not been released, period)
	 */
	public boolean releasedInTheFuture() {

		if ((getReleasedInYear() == null) || (getReleasedInMonth() == null) || (getReleasedOnDay() == null)) {
			// not released at all!
			return false;
		}

		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());

		if (cal.get(Calendar.YEAR) < getReleasedInYear()) {
			return true;
		}

		if (cal.get(Calendar.MONTH) < getReleasedInMonth()) {
			return true;
		}

		if (cal.get(Calendar.DAY_OF_MONTH) < getReleasedOnDay()) {
			return true;
		}

		return false;
	}

	public Date getDoneDate() {
		return doneDate;
	}

	public void setDoneDate(Date doneDate) {
		this.doneDate = doneDate;
	}

	public String getDoneLog() {
		return doneLog;
	}

	public void setDoneLog(String doneLog) {
		this.doneLog = doneLog;
	}

	public boolean matches(String searchFor) {
		if ("".equals(searchFor)) {
			return true;
		}
		if (getTitle().replace("\\n", "").toLowerCase().contains(searchFor.toLowerCase())) {
			return true;
		}
		if (details != null) {
			for (String detail : details) {
				if (detail.replace("\\n", "").toLowerCase().contains(searchFor.toLowerCase())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Checks if the task instances are the same, so the same task released on different days
	 * will get false
	 */
	@Override
	public boolean equals(Object other) {

		// If the other one does not even exist, we are not the same - because we exist!
		if (other == null) {
			return false;
		}

		if (other instanceof GenericTask) {
			GenericTask otherGenericTask = (GenericTask) other;

			// If our values for title are different...
			if (this.title == null) {
				if (otherGenericTask.title != null) {
					// ... then we are not the same!
					return false;
				}
			} else if (!this.title.equals(otherGenericTask.title)) {
				// ... then we are not the same!
				return false;
			}

			// If our values for done are different...
			if (this.done == null) {
				if (otherGenericTask.done != null) {
					// ... then we are not the same!
					return false;
				}
			} else if (!this.done.equals(otherGenericTask.done)) {
				// ... then we are not the same!
				return false;
			}

			// If our values for releasedOnDay are different...
			if (this.releasedOnDay == null) {
				if (otherGenericTask.releasedOnDay != null) {
					// ... then we are not the same!
					return false;
				}
			} else if (!this.releasedOnDay.equals(otherGenericTask.releasedOnDay)) {
				// ... then we are not the same!
				return false;
			}

			// If our values for releasedInMonth are different...
			if (this.releasedInMonth == null) {
				if (otherGenericTask.releasedInMonth != null) {
					// ... then we are not the same!
					return false;
				}
			} else if (!this.releasedInMonth.equals(otherGenericTask.releasedInMonth)) {
				// ... then we are not the same!
				return false;
			}

			// If our values for releasedInYear are different...
			if (this.releasedInYear == null) {
				if (otherGenericTask.releasedInYear != null) {
					// ... then we are not the same!
					return false;
				}
			} else if (!this.releasedInYear.equals(otherGenericTask.releasedInYear)) {
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
		if ((done == null) || done.equals(false)) {
			return -1;
		}
		return (this.releasedOnDay == null ? 0 : this.releasedOnDay) +
				64 * (this.releasedInMonth == null ? 0 : this.releasedInMonth) +
				1024 * (this.releasedInYear == null ? 0 : this.releasedInYear);
	}

}
