/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.calendar;

import com.asofterspace.accountant.tasks.Task;
import com.asofterspace.toolbox.utils.DateUtils;
import com.asofterspace.toolbox.utils.Record;

import java.util.ArrayList;
import java.util.Calendar;
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
	protected final String MONTH = "month";
	protected final String MONTHS = "months";
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

	protected void releaseTaskOn(GenericTask task, Date day) {
		GenericTask taskInstance = task.getNewInstance();
		Calendar cal = Calendar.getInstance();
		cal.setTime(day);
		taskInstance.setDone(false);
		taskInstance.setReleasedOnDay(cal.get(Calendar.DAY_OF_MONTH));
		taskInstance.setReleasedInMonth(cal.get(Calendar.MONTH));
		taskInstance.setReleasedInYear(cal.get(Calendar.YEAR));
		taskInstance.setDoneDate(null);
		taskInstances.add(taskInstance);
	}

	protected GenericTask taskFromRecord(Record recordTask) {

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

		return new GenericTask(
			recordTask.getString(TITLE),
			recordTask.getInteger(DAY),
			months,
			recordTask.getArrayAsStringList(DETAILS),
			recordTask.getArrayAsStringList(ON_DONE)
		);
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

	protected Record taskToRecord(GenericTask task) {
		Record taskRecord = Record.emptyObject();
		taskRecord.set(KIND, GENERIC);
		taskRecord.set(TITLE, task.getTitle());
		taskRecord.set(DAY, task.getScheduledOnDay());

		List<Integer> months = task.getScheduledInMonths();
		taskRecord.set(MONTH, null);
		taskRecord.remove(MONTHS);
		if (months != null) {
			if (months.size() == 1) {
				taskRecord.set(MONTH, DateUtils.monthNumToName(months.get(0)));
			} else {
				List<String> monthNames = new ArrayList<>();
				for (Integer month : months) {
					monthNames.add(DateUtils.monthNumToName(month));
				}
				taskRecord.remove(MONTH);
				taskRecord.set(MONTHS, monthNames);
			}
		}

		taskRecord.set(DETAILS, task.getDetails());
		taskRecord.set(ON_DONE, task.getOnDone());
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
			taskRecord.set(DONE_DATE, DateUtils.serializeDate(task.getDoneDate()));
			taskRecord.set(DONE_LOG, task.getDoneLog());
			base.append(taskRecord);
		}
		return base;
	}

}
