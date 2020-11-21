/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.calendar;

import com.asofterspace.toolbox.utils.DateUtils;
import com.asofterspace.toolbox.utils.Record;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


public class TaskCtrlBase {

	protected final String TASKS = "tasks";
	protected final String TASK_INSTANCES = "taskInstances";
	protected final String LAST_TASK_GENERATION = "lastTaskGeneration";
	protected final String GENERIC = "generic";
	protected final String KIND = "kind";
	protected final String TITLE = "title";
	protected final String DAY = "day";
	protected final String DAYS_OF_WEEK = "daysOfWeek";
	protected final String MONTH = "month";
	protected final String MONTHS = "months";
	protected final String YEAR = "year";
	protected final String YEARS = "years";
	protected final String DETAILS = "details";
	protected final String ON_DONE = "onDone";
	protected final String DONE = "done";
	protected final String RELEASED_ON_DAY = "releasedOnDay";
	protected final String RELEASED_IN_MONTH = "releasedInMonth";
	protected final String RELEASED_IN_YEAR = "releasedInYear";
	protected final String DONE_DATE = "doneDate";
	protected final String DONE_LOG = "doneLog";
	protected final String DATE = "date";
	protected final String ROWS = "rows";
	protected final String AMOUNT = "amount";
	protected final String ACCOUNT = "account";

	// contains one instance of each task, such that for a given day we can check which of
	// these potential tasks actually occurs on that day
	protected List<GenericTask> tasks;

	// contains the actual released and potentially worked on tasks
	protected List<GenericTask> taskInstances;

	protected Date lastTaskGeneration;


	protected void loadFromRoot(Record root) {

		if (root == null) {
			return;
		}

		List<Record> taskRecordsInDatabase = root.getArray(TASKS);
		this.tasks = new ArrayList<>();
		for (Record curTask : taskRecordsInDatabase) {
			GenericTask task = taskFromRecord(curTask);
			if (task != null) {
				tasks.add(task);
			}
		}

		List<Record> taskInstanceRecordsInDatabase = root.getArray(TASK_INSTANCES);
		this.taskInstances = new ArrayList<>();
		for (Record curTask : taskInstanceRecordsInDatabase) {
			GenericTask task = taskInstanceFromRecord(curTask);
			if (task != null) {
				taskInstances.add(task);
			}
		}

		this.lastTaskGeneration = DateUtils.parseDate(root.getString(LAST_TASK_GENERATION));
	}

	public void generateNewInstances(Date until) {

		List<Date> daysToGenerate = DateUtils.listDaysFromTo(lastTaskGeneration, until);

		// we ignore the very first day that is returned,
		// as we already reported tasks for that one last time!
		for (int i = 1; i < daysToGenerate.size(); i++) {

			Date day = daysToGenerate.get(i);

			for (GenericTask task : tasks) {
				if (task.isScheduledOn(day)) {
					releaseTaskOn(task, day);
				}
			}

			lastTaskGeneration = day;
		}
	}

	public GenericTask addAdHocTask(String title, String details, Date scheduleDate) {

		if (scheduleDate == null) {
			return null;
		}

		List<String> detailsList = new ArrayList<>();
		for (String detail : details.split("\n")) {
			detailsList.add(detail);
		}

		List<String> onDone = new ArrayList<>();

		// this is an ad-hoc task which is not scheduled ever
		Integer scheduledOnDay = null;
		List<String> scheduledOnDaysOfWeek = null;
		List<Integer> scheduledInMonths = null;
		List<Integer> scheduledInYears = null;

		GenericTask newTask = createTask(title, scheduledOnDay, scheduledOnDaysOfWeek, scheduledInMonths,
			scheduledInYears, detailsList, onDone);

		return releaseTaskOn(newTask, scheduleDate);
	}

	/**
	 * Releases a task by copying it as an instance and returning the new task instance
	 */
	protected GenericTask releaseTaskOn(GenericTask task, Date day) {
		GenericTask taskInstance = task.getNewInstance();
		taskInstance.setDone(false);
		taskInstance.setReleasedDate(day);
		taskInstance.setDoneDate(null);
		taskInstances.add(taskInstance);
		return taskInstance;
	}

	protected GenericTask taskFromRecord(Record recordTask) {

		List<String> daysOfWeek = recordTask.getArrayAsStringList(DAYS_OF_WEEK);

		List<Integer> months = new ArrayList<>();
		List<String> monthNames = recordTask.getArrayAsStringList(MONTHS);
		if (monthNames.size() < 1) {
			Integer month = DateUtils.monthNameToNum(recordTask.getString(MONTH));
			if (month != null) {
				months.add(month);
			}
		} else {
			for (String month : monthNames) {
				months.add(DateUtils.monthNameToNum(month));
			}
		}

		List<Integer> years = recordTask.getArrayAsIntegerList(YEARS);
		if (years.size() < 1) {
			Integer yearVal = recordTask.getInteger(YEAR);
			if (yearVal != null) {
				years.add(yearVal);
			}
		}

		return createTask(
			recordTask.getString(TITLE),
			recordTask.getInteger(DAY),
			daysOfWeek,
			months,
			years,
			recordTask.getArrayAsStringList(DETAILS),
			recordTask.getArrayAsStringList(ON_DONE)
		);
	}

	protected GenericTask createTask(String title, Integer scheduledOnDay, List<String> scheduledOnDaysOfWeek,
		List<Integer> scheduledInMonths, List<Integer> scheduledInYears, List<String> details, List<String> onDone) {

		return new GenericTask(title, scheduledOnDay, scheduledOnDaysOfWeek, scheduledInMonths,
			scheduledInYears, details, onDone);
	}

	protected GenericTask taskInstanceFromRecord(Record recordTask) {
		GenericTask result = taskFromRecord(recordTask);
		if (result == null) {
			return null;
		}
		result.setDone(recordTask.getBoolean(DONE));
		result.setReleasedOnDay(recordTask.getInteger(RELEASED_ON_DAY));
		result.setReleasedInMonth(recordTask.getInteger(RELEASED_IN_MONTH));
		result.setReleasedInYear(recordTask.getInteger(RELEASED_IN_YEAR));
		result.setDoneDate(DateUtils.parseDate(recordTask.getString(DONE_DATE)));
		result.setDoneLog(recordTask.getString(DONE_LOG));
		return result;
	}

	/**
	 * Get all the tasks that have ever been released (both ad-hoc tasks and scheduled tasks which have
	 * been released when their scheduled date arrived)
	 */
	public List<GenericTask> getTaskInstances() {

		Collections.sort(taskInstances, new Comparator<GenericTask>() {
			public int compare(GenericTask a, GenericTask b) {
				if (a.getReleasedInYear().equals(b.getReleasedInYear())) {
					if (a.getReleasedInMonth().equals(b.getReleasedInMonth())) {
						if (a.getReleasedOnDay().equals(b.getReleasedOnDay())) {
							return a.getTitle().compareTo(b.getTitle());
						}
						return b.getReleasedOnDay() - a.getReleasedOnDay();
					}
					return b.getReleasedInMonth() - a.getReleasedInMonth();
				}
				return b.getReleasedInYear() - a.getReleasedInYear();
			}
		});

		return taskInstances;
	}

	/**
	 * Get all the tasks that have ever been released and which are not yet done
	 * (both ad-hoc tasks and scheduled tasks which have been released when their scheduled date arrived)
	 */
	public List<GenericTask> getCurrentTaskInstances(boolean ordered) {

		List<GenericTask> result = new ArrayList<>();

		List<GenericTask> tasks = null;

		if (ordered) {
			tasks = getTaskInstances();
		} else {
			tasks = taskInstances;
		}

		for (GenericTask task : tasks) {
			if ((task.hasBeenDone() == null) || !task.hasBeenDone()) {
				result.add(task);
			}
		}

		return result;
	}

	/**
	 * Get all the tasks that have ever been released and which are not yet done
	 * (both ad-hoc tasks and scheduled tasks which have been released when their scheduled date arrived)
	 */
	public List<GenericTask> getCurrentTaskInstances() {
		boolean ordered = true;
		return getCurrentTaskInstances(ordered);
	}

	public List<GenericTask> getUpcomingTaskInstances(int upcomingDays) {

		List<GenericTask> results = new ArrayList<>();

		List<GenericTask> origTasks = new ArrayList<>(taskInstances);
		Date origLastTaskGeneration = lastTaskGeneration;

		// generate future instances, but do not save them!
		generateNewInstances(DateUtils.addDays(DateUtils.now(), upcomingDays));

		List<GenericTask> tasks = getTaskInstances();

		for (GenericTask task : tasks) {
			// if needs to be done...
			if ((task.hasBeenDone() == null) || !task.hasBeenDone()) {
				// ... and has not been a task instance before
				if (!origTasks.contains(task)) {
					results.add(task);
				}
			}
		}

		taskInstances = origTasks;
		lastTaskGeneration = origLastTaskGeneration;

		return results;
	}

	protected Record taskToRecord(GenericTask task) {
		Record taskRecord = Record.emptyObject();
		taskRecord.setOrRemove(KIND, GENERIC);
		taskRecord.setOrRemove(TITLE, task.getTitle());
		taskRecord.setOrRemove(DAY, task.getScheduledOnDay());
		if ((task.getScheduledOnDaysOfWeek() == null) || (task.getScheduledOnDaysOfWeek().size() == 0)) {
			taskRecord.remove(DAYS_OF_WEEK);
		} else {
			taskRecord.set(DAYS_OF_WEEK, task.getScheduledOnDaysOfWeek());
		}

		List<Integer> months = task.getScheduledInMonths();
		taskRecord.remove(MONTH);
		taskRecord.remove(MONTHS);
		if (months != null) {
			if (months.size() > 0) {
				if (months.size() == 1) {
					taskRecord.set(MONTH, DateUtils.monthNumToName(months.get(0)));
				} else {
					List<String> monthNames = new ArrayList<>();
					for (Integer month : months) {
						monthNames.add(DateUtils.monthNumToName(month));
					}
					taskRecord.set(MONTHS, monthNames);
				}
			}
		}

		List<Integer> years = task.getScheduledInYears();
		taskRecord.remove(YEAR);
		taskRecord.remove(YEARS);
		if (years != null) {
			if (years.size() > 0) {
				if (years.size() == 1) {
					taskRecord.set(YEAR, years.get(0));
				} else {
					taskRecord.set(YEARS, years);
				}
			}
		}

		List<String> details = task.getDetails();
		if ((details == null) || (details.size() == 0) || ((details.size() == 1) && ("".equals(details.get(0))))) {
			taskRecord.remove(DETAILS);
		} else {
			taskRecord.set(DETAILS, details);
		}
		List<String> onDone = task.getOnDone();
		if ((onDone == null) || (onDone.size() == 0)) {
			taskRecord.remove(ON_DONE);
		} else {
			taskRecord.set(ON_DONE, onDone);
		}
		return taskRecord;
	}

	protected Record getTasksAsRecord() {
		Record base = Record.emptyArray();
		for (GenericTask task : tasks) {
			Record taskRecord = taskToRecord(task);
			base.append(taskRecord);
		}
		return base;
	}

	protected Record getTaskInstancesAsRecord() {
		Record base = Record.emptyArray();
		for (GenericTask task : taskInstances) {
			Record taskRecord = taskToRecord(task);
			taskRecord.set(DONE, task.hasBeenDone());
			taskRecord.set(RELEASED_ON_DAY, task.getReleasedOnDay());
			taskRecord.set(RELEASED_IN_MONTH, task.getReleasedInMonth());
			taskRecord.set(RELEASED_IN_YEAR, task.getReleasedInYear());
			taskRecord.setOrRemove(DONE_DATE, DateUtils.serializeDate(task.getDoneDate()));
			taskRecord.setOrRemove(DONE_LOG, task.getDoneLog());
			base.append(taskRecord);
		}
		return base;
	}

	public void saveIntoRecord(Record root) {
		root.set(TASKS, getTasksAsRecord());
		root.set(TASK_INSTANCES, getTaskInstancesAsRecord());
		root.set(LAST_TASK_GENERATION, DateUtils.serializeDate(lastTaskGeneration));
	}

}
