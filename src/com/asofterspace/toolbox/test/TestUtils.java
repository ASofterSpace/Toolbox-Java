/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.test;

/**
 * Utility class used for testing stuff
 *
 * @author Moya (a softer space), 2018
 */
public class TestUtils {

	private static int testsRun = 0;
	private static int testsSuccess = 0;
	private static int testsFailed = 0;

	// 0 .. no info
	// 1 .. fail
	// 2 .. success
	private static int currentVerdict = 0;

	private static String currentTest;

	private static LogFunction logFunction = new LogFunction() {
		public void log(String logline) {
			System.out.println(logline);
		}
	};


	/**
	 * Starts a Test run with a given name
	 */
	public static void start(String testName) {

		finalizePreviousTest();

		currentVerdict = 0;
		currentTest = testName;

		logFunction.log("Starting the " + currentTest + " Test...");
	}

	/**
	 * Indicates that the last test was a success, unless fail or succeed
	 * has already been called before
	 */
	public static void succeed() {

		if (currentVerdict == 0) {
			currentVerdict = 2;

			logFunction.log("The " + currentTest + " Test succeeded! Whoop whoop!");
			logFunction.log("");
		}
	}

	/**
	 * Indicates that the last test was a failure, unless fail or succeed
	 * has already been called before
	 */
	public static void fail(String reason) {

		if (currentVerdict == 0) {
			currentVerdict = 1;

			logFunction.log("The " + currentTest + " Test failed... oh no!");
			logFunction.log("Reason: " + reason);
			logFunction.log("");
		}
	}

	/**
	 * Starts a Test Suite consisting of several tests
	 */
	public static void startSuite() {

		testsRun = 0;
		testsSuccess = 0;
		testsFailed = 0;
		currentVerdict = -1;

		logFunction.log("");
		logFunction.log("------------------------------");
		logFunction.log("The test suite is starting...");
		logFunction.log("------------------------------");
		logFunction.log("");
	}

	/**
	 * Runs a test within the test suite
	 */
	public static void run(Test test) {

		try {

			test.runAll();

		} catch (Throwable t) {

			TestUtils.fail("There was an exception:\n" + t);
			t.printStackTrace();
			logFunction.log("");
		}
	}

	/**
	 * Ends a Test Suite consisting of several tests
	 */
	public static void endSuite() {

		finalizePreviousTest();

		logFunction.log("------------------------------");
		logFunction.log("The test suite has finished!");
		String testsRunStr = "";
		if (testsRun == 1) {
			testsRunStr = "1 test has";
		} else {
			testsRunStr = testsRun + " tests have";
		}
		logFunction.log(testsRunStr + " been run.");
		String testResultStr = "There ";
		if (testsSuccess + testsFailed == 1) {
			testResultStr += "was ";
		} else {
			testResultStr += "were ";
		}
		if (testsSuccess == 1) {
			testResultStr += "1 success";
		} else {
			testResultStr += testsSuccess + " successes";
		}
		testResultStr += " and ";
		if (testsFailed == 1) {
			testResultStr += "1 failure.";
		} else {
			testResultStr += testsFailed + " failures.";
		}
		logFunction.log(testResultStr);
		logFunction.log("------------------------------");
	}

	/**
	 * Were all the tests that have been run (so far) successful?
	 */
	public static boolean allWereSuccessful() {
		return testsSuccess == testsRun;
	}

	private static void finalizePreviousTest() {

		// we already call finalizePreviousTest() as the start of the very first test... do nothing then!
		if (currentVerdict < 0) {
			return;
		}

		testsRun++;

		if (currentVerdict == 0) {
			fail("TestUtils.succeed() was not reached!");
		}

		if (currentVerdict == 1) {
			testsFailed++;
		}

		if (currentVerdict == 2) {
			testsSuccess++;
		}

		currentVerdict = 0;
	}

	public void setLogFunction(LogFunction logFunction) {
		TestUtils.logFunction = logFunction;
	}

}
