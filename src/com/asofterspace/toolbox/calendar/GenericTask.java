/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.calendar;

import com.asofterspace.toolbox.utils.DateUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class GenericTask {

	protected String title;

	// on which day of the month is this task scheduled?
	protected Integer scheduledOnDay;

	// in which months is this task scheduled?
	protected List<Integer> scheduledInMonths;

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


	public GenericTask(String title, Integer scheduledOnDay, List<Integer> scheduledInMonths,
		List<String> details, List<String> onDone) {

		this.title = title;
		this.scheduledOnDay = scheduledOnDay;
		this.scheduledInMonths = scheduledInMonths;
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
		this.scheduledInMonths = other.scheduledInMonths;
		this.details = other.details;
		this.onDone = other.onDone;
	}

	public GenericTask getNewInstance() {
		return new GenericTask(this);
	}

	public boolean isScheduledOn(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);

		if (scheduledOnDay != null) {
			if (!scheduledOnDay.equals(cal.get(Calendar.DAY_OF_MONTH))) {
				return false;
			}
		}

		if (scheduledInMonths != null) {
			if (scheduledInMonths.size() > 0) {
				boolean foundMonth = false;
				for (Integer month : scheduledInMonths) {
					if (month.equals(cal.get(Calendar.MONTH))) {
						foundMonth = true;
					}
				}
				if (!foundMonth) {
					return false;
				}
			}
		}

		return true;
	}

	public String getTitle() {
		return title;
	}

	public Integer getScheduledOnDay() {
		return scheduledOnDay;
	}

	public List<Integer> getScheduledInMonths() {
		return scheduledInMonths;
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