/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.calendar;

import com.asofterspace.toolbox.utils.DateHolder;
import com.asofterspace.toolbox.utils.DateUtils;
import com.asofterspace.toolbox.utils.SortUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class GenericTask {

	protected String title;

	// on which Xth weekday of the month is this task scheduled?
	protected Integer scheduledOnXDayOfMonth;

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

	// purely for speeding up the function getReleaseDate(), we also have:
	private Date releasedDate;

	// when was this task done?
	protected DateHolder doneDate;

	// when was this task set to done? (so e.g. I can today set the task to being done yesterday,
	// then doneDate will be yesterday - as date - but today, now o'clock, will be the setToDoneDateTime)
	protected DateHolder setToDoneDateTime;

	// what interesting things did the user encounter while doing this task?
	protected String doneLog;

	// for a repeating task: is this task bi-weekly, on even weeks?
	protected Boolean biweeklyEven;

	// for a repeating task: is this task bi-weekly, on odd weeks?
	protected Boolean biweeklyOdd;


	public GenericTask(String title, Integer scheduledOnXDayOfMonth, Integer scheduledOnDay, List<String> scheduledOnDaysOfWeek,
		List<Integer> scheduledInMonths, List<Integer> scheduledInYears,
		List<String> details, List<String> onDone, Boolean biweeklyEven, Boolean biweeklyOdd) {

		this.title = title;
		this.scheduledOnXDayOfMonth = scheduledOnXDayOfMonth;
		this.scheduledOnDay = scheduledOnDay;
		this.scheduledOnDaysOfWeek = scheduledOnDaysOfWeek;
		this.scheduledInMonths = scheduledInMonths;
		this.scheduledInYears = scheduledInYears;
		this.details = details;
		this.onDone = onDone;
		this.biweeklyEven = biweeklyEven;
		this.biweeklyOdd = biweeklyOdd;
	}

	/**
	 * Constructs a new task based on an existing without keeping the task instance details,
	 * but instead just keeping the generic task details
	 */
	public GenericTask(GenericTask other) {
		this.title = other.title;
		this.scheduledOnXDayOfMonth = other.scheduledOnXDayOfMonth;
		this.scheduledOnDay = other.scheduledOnDay;
		this.scheduledOnDaysOfWeek = other.scheduledOnDaysOfWeek;
		this.scheduledInMonths = other.scheduledInMonths;
		this.scheduledInYears = other.scheduledInYears;
		this.details = other.details;
		this.onDone = other.onDone;
		this.biweeklyEven = other.biweeklyEven;
		this.biweeklyOdd = other.biweeklyOdd;
	}

	public GenericTask getNewInstance() {
		return new GenericTask(this);
	}

	public boolean isScheduledOn(Calendar cal) {

		// handle weekdays before day of month and month (as they have special 29th-Feb-code)
		if (scheduledOnDaysOfWeek != null) {
			if (scheduledOnDaysOfWeek.size() > 0) {
				boolean foundDay = false;
				String dayName = DateUtils.DAY_NAMES[cal.get(Calendar.DAY_OF_WEEK)];
				for (String weekDay : scheduledOnDaysOfWeek) {
					if (dayName.equals(DateUtils.toDayOfWeekNameEN(weekDay))) {
						// if scheduled on Xth weekday of the month, also check if it actually is that
						if (scheduledOnXDayOfMonth != null) {
							int curXDayOfMonth = ((cal.get(Calendar.DAY_OF_MONTH) - 1) / 7) + 1;
							if (curXDayOfMonth != scheduledOnXDayOfMonth) {
								// if not, keep searching
								continue;
							}
							// if yes, found it!
						}
						foundDay = true;
						break;
					}
				}
				if (!foundDay) {
					return false;
				}
			}
		}

		// handle years before day of month and month (as they have special 29th-Feb-code)
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

		// handle biweekly-ness
		if ((biweeklyEven != null) && biweeklyEven) {
			if (cal.get(Calendar.WEEK_OF_YEAR) % 2 == 1) {
				return false;
			}
		}
		if ((biweeklyOdd != null) && biweeklyOdd) {
			if (cal.get(Calendar.WEEK_OF_YEAR) % 2 == 0) {
				return false;
			}
		}

		// day of month before month (as day of month includes special 29th-Feb-code which
		// also checks the month and returns true for both together)
		if (scheduledOnDay != null) {
			if (!scheduledOnDay.equals(cal.get(Calendar.DAY_OF_MONTH))) {

				// extra case: if this is scheduled on the 29th, ...
				if (scheduledOnDay == 29) {
					// ... and it is right now the 1st...
					if (cal.get(Calendar.DAY_OF_MONTH) == 1) {
						// ... and it is March right now ...
						if (cal.get(Calendar.MONTH) == 2) {
							// ... and it is scheduled for February ...
							boolean scheduledForFebruary = true;
							if (scheduledInMonths != null) {
								if (scheduledInMonths.size() > 0) {
									scheduledForFebruary = false;
									for (Integer month : scheduledInMonths) {
										if (month != null) {
											if (month == 1) {
												scheduledForFebruary = true;
												break;
											}
										}
									}
								}
							}
							if (scheduledForFebruary) {
								// ... and this year does not have a February the 29th...
								if (!DateUtils.isLeapYear(cal.get(Calendar.YEAR))) {
									// ... then actually this IS scheduled for today!
									return true;
								}
							}
						}
					}
				}

				return false;
			}
		}

		// handle months after day of month (as that one has special 29th Feb code handling day AND month,
		// just for that one day)
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

		return true;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setScheduledOnXDayOfMonth(Integer scheduledOnXDayOfMonth) {
		this.scheduledOnXDayOfMonth = scheduledOnXDayOfMonth;
	}

	public void setScheduledOnDay(Integer scheduledOnDay) {
		this.scheduledOnDay = scheduledOnDay;
	}

	public void setScheduledOnDaysOfWeek(List<String> scheduledOnDaysOfWeek) {
		this.scheduledOnDaysOfWeek = scheduledOnDaysOfWeek;
	}

	public void setScheduledInMonths(List<Integer> scheduledInMonths) {
		this.scheduledInMonths = scheduledInMonths;
	}

	public void setScheduledInYears(List<Integer> scheduledInYears) {
		this.scheduledInYears = scheduledInYears;
	}

	public Integer getScheduledOnXDayOfMonth() {
		return scheduledOnXDayOfMonth;
	}

	public String getScheduledOnXDayOfMonthStr() {
		if (scheduledOnXDayOfMonth == null) {
			return "";
		}
		return ""+scheduledOnXDayOfMonth;
	}

	public Integer getScheduledOnDay() {
		return scheduledOnDay;
	}

	public List<String> getScheduledOnDaysOfWeek() {

		if (scheduledOnDaysOfWeek == null) {
			return null;
		}

		List<Integer> weekDays = new ArrayList<>();
		for (String dayOfWeek : scheduledOnDaysOfWeek) {
			Integer weekDay = DateUtils.toDayOfWeek(dayOfWeek);
			if (weekDay != null) {
				// sort saturday and sunday to the end
				if (weekDay < 2) {
					weekDay += 7;
				}
				weekDays.add(weekDay);
			}
		}

		weekDays = SortUtils.sortIntegers(weekDays);

		List<String> result = new ArrayList<>();
		for (Integer weekDay : weekDays) {
			result.add(DateUtils.dayNumToDayOfWeekNameEN(weekDay));
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
		if (releasedDate != null) {
			return releasedDate;
		}
		if (!isInstance()) {
			return null;
		}
		releasedDate = DateUtils.parseDate(getReleasedInYear() + "-" + (getReleasedInMonth() + 1) + "-" + getReleasedOnDay());
		return releasedDate;
	}

	public boolean hasBeenDone() {
		if (done == null) {
			return false;
		}
		return done;
	}

	public void setDone(Boolean done) {
		this.done = done;
	}

	public Date getSetToDoneDateTime() {
		return getSetToDoneDateTimeHolder().getDate();
	}

	public DateHolder getSetToDoneDateTimeHolder() {
		if (setToDoneDateTime == null) {
			setToDoneDateTime = DateUtils.createNullDateHolder();
		}
		return setToDoneDateTime;
	}

	public void setSetToDoneDateTime(Date setToDoneDateTime) {
		this.setToDoneDateTime = DateUtils.createDateHolder(setToDoneDateTime);
	}

	public void setSetToDoneDateTimeHolder(DateHolder setToDoneDateTime) {
		this.setToDoneDateTime = setToDoneDateTime;
	}

	public boolean isDoneDateProblematicTaskInstance() {
		if (hasBeenDone()) {
			if (getDoneDate() == null) {
				return true;
			}
			if (getSetToDoneDateTime() == null) {
				return true;
			}
			// if the difference between doneDate and setToDoneDateTime is more than 21 days, report it!
			return Math.abs(getDoneDate().getTime() - getSetToDoneDateTime().getTime()) / (1000.0 * 60.0 * 60.0 * 24.0) > 21;
		}

		return false;
	}

	public boolean isInstance() {
		return releasedOnDay != null;
	}

	public Integer getReleasedOnDay() {
		return releasedOnDay;
	}

	public void setReleasedOnDay(Integer releasedOnDay) {
		releasedDate = null;
		this.releasedOnDay = releasedOnDay;
	}

	public Integer getReleasedInMonth() {
		return releasedInMonth;
	}

	public void setReleasedInMonth(Integer releasedInMonth) {
		releasedDate = null;
		this.releasedInMonth = releasedInMonth;
	}

	public Integer getReleasedInYear() {
		return releasedInYear;
	}

	public void setReleasedInYear(Integer releasedInYear) {
		releasedDate = null;
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
		if (day == null) {
			return;
		}
		releasedDate = null;
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

		if (cal.get(Calendar.YEAR) > getReleasedInYear()) {
			return false;
		}

		if (cal.get(Calendar.MONTH) < getReleasedInMonth()) {
			return true;
		}

		if (cal.get(Calendar.MONTH) > getReleasedInMonth()) {
			return false;
		}

		if (cal.get(Calendar.DAY_OF_MONTH) < getReleasedOnDay()) {
			return true;
		}

		return false;
	}

	/**
	 * Gets the main date on which a task instance lies, so if it was done already,
	 * the done date, and if not, the release date.
	 * If this is a repeating task rather than a single task instance, returns null.
	 */
	public Date getMainDateForTaskInstance() {
		Date result = getDoneDate();
		if (result != null) {
			return result;
		}
		return getReleaseDate();
	}

	public Date getDoneDate() {
		return getDoneDateHolder().getDate();
	}

	public DateHolder getDoneDateHolder() {
		if (doneDate == null) {
			doneDate = DateUtils.createNullDateHolder();
		}
		return doneDate;
	}

	public void setDoneDate(Date doneDate) {
		this.doneDate = DateUtils.createDateHolder(doneDate);
	}

	public void setDoneDateHolder(DateHolder doneDate) {
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
	 * Returns a string representation of the times on which this base task is scheduled
	 */
	public String getScheduleDateStr() {

		String result = "";

		List<String> daysOfWeek = getScheduledOnDaysOfWeek();
		if (daysOfWeek != null) {
			for (String day : daysOfWeek) {
				day = DateUtils.toDayOfWeekNameEN(day);
				if (day != null) {
					result += day.substring(0, 2);
					result += ", ";
				}
			}
			if (daysOfWeek.size() > 0) {
				if (scheduledOnXDayOfMonth != null) {
					result = scheduledOnXDayOfMonth + ". " + result;
				}
			}
		}

		Integer schedDay = getScheduledOnDay();
		if (schedDay != null) {
			result += schedDay + ". ";
		}

		String sep = "";

		List<Integer> schedMonths = getScheduledInMonths();
		if (schedMonths != null) {
			for (Integer month : schedMonths) {
				String monthName = DateUtils.monthNumToName(month);
				if (monthName != null) {
					result += sep;
					result += monthName.substring(0, 3);
					sep = ", ";
				}
			}
		}

		List<Integer> schedYears = getScheduledInYears();
		if (schedYears != null) {
			for (Integer year : schedYears) {
				if (year != null) {
					result += sep;
					result += year;
					sep = ", ";
				}
			}
		}

		if (result.endsWith(", ")) {
			result = result.substring(0, result.length() - 2);
		}

		result = result.trim();

		if ("".equals(result)) {
			result = "always";
		}
		return result;
	}

	public boolean appliesToRange(Date from, Date to, Date today) {

		if ((from == null) && (to == null)) {
			return true;
		}

		boolean fromResult = true;
		boolean toResult = true;

		// entries which are done apply to the date on which they were done
		if (hasBeenDone()) {
			Date done = getDoneDate();
			if (from != null) {
				fromResult = done.after(from) || DateUtils.isSameDay(from, done);
			}
			if (to != null) {
				toResult = done.before(to) || DateUtils.isSameDay(to, done);
			}
			return toResult || fromResult;
		}

		// entries which are not yet done apply to their release date...
		Date displayDate = getReleaseDate();

		// ... or, if they were released before today, they apply to today
		if (displayDate.before(today)) {
			displayDate = today;
		}

		if (from != null) {
			fromResult = displayDate.after(from) || DateUtils.isSameDay(from, displayDate);
		}
		if (to != null) {
			toResult = displayDate.before(to) || DateUtils.isSameDay(to, displayDate);
		}
		return toResult || fromResult;
	}

	public boolean appliesToDay(Date day, Date today) {

		// entries which are done apply to the date on which they were done
		if (hasBeenDone()) {
			return DateUtils.isSameDay(day, getDoneDate());
		}

		// entries which are not yet done apply to their release date...
		Date displayDate = getReleaseDate();

		// ... or, if they were released before today, they apply to today
		if (displayDate.before(today)) {
			displayDate = today;
		}

		return DateUtils.isSameDay(day, displayDate);
	}

	public Boolean getBiweeklyEven() {
		return biweeklyEven;
	}

	public void setBiweeklyEven(Boolean biweeklyEven) {
		this.biweeklyEven = biweeklyEven;
	}

	public Boolean getBiweeklyOdd() {
		return biweeklyOdd;
	}

	public void setBiweeklyOdd(Boolean biweeklyOdd) {
		this.biweeklyOdd = biweeklyOdd;
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
