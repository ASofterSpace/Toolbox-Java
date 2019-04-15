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


	/**
	 * Starts a Test run with a given name
	 */
	public static void start(String testName) {

		finalizePreviousTest();

		currentVerdict = 0;
		currentTest = testName;

		System.out.println("Starting the " + currentTest + " Test...");
	}

	/**
	 * Indicates that the last test was a success
	 */
	public static void succeed() {

		currentVerdict = 2;

		System.out.println("The " + currentTest + " Test succeeded! Whoop whoop!");
		System.out.println("");
	}

	/**
	 * Indicates that the last test was a failure
	 */
	public static void fail(String reason) {

		currentVerdict = 1;

		System.out.println("The " + currentTest + " Test failed... oh no!");
		System.out.println("Reason: " + reason);
		System.out.println("");
	}

	/**
	 * Starts a Test Suite consisting of several tests
	 */
	public static void startSuite() {

		testsRun = -1;
		testsSuccess = 0;
		testsFailed = 0;

		System.out.println("");
		System.out.println("------------------------------");
		System.out.println("The test suite is starting...");
		System.out.println("------------------------------");
		System.out.println("");
	}

	/**
	 * Runs a test within the test suite
	 */
	public static void run(Test test) {

//		try {

			test.runAll();

/*
		} catch (Throwable t) {

			TestUtils.fail("There was an exception:\n" + t);
		}
*/
	}

	/**
	 * Ends a Test Suite consisting of several tests
	 */
	public static void endSuite() {

		finalizePreviousTest();

		System.out.println("------------------------------");
		System.out.println("The test suite has finished!");
		String testsRunStr = "";
		if (testsRun == 1) {
			testsRunStr = "1 test has";
		} else {
			testsRunStr = testsRun + " tests have";
		}
		System.out.println(testsRunStr + " been run.");
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
		System.out.println(testResultStr);
		System.out.println("------------------------------");
	}

	private static void finalizePreviousTest() {

		testsRun++;

		if (currentVerdict == 1) {
			testsFailed++;
		}

		if (currentVerdict == 2) {
			testsSuccess++;
		}
	}
}
