/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.test.Test;
import com.asofterspace.toolbox.test.TestUtils;
import com.asofterspace.toolbox.utils.MathUtils;
import com.asofterspace.toolbox.Utils;

import java.util.ArrayList;
import java.util.List;


public class MathUtilsTest implements Test {

	@Override
	public void runAll() {

		findMinimaTest();

		findMaximaTest();

		divideIntsTest();

		averageTest();

		calculateMathStrTest();
	}

	public void findMinimaTest() {

		TestUtils.start("Find Minima");

		double[] simpleData = {10, 12, 10, 10, 11, 10, 10, 9, 7, 4, 2, 1, 3, 3, 4, 5, 6, 8, 10, 11, 10, 10, 10, 10, 12, 10};

		int[] simpleResult = MathUtils.findMinima(simpleData, 1);

		if (simpleData[0] != 10) {
			TestUtils.fail("The data got modified!");
			return;
		}

		if (simpleResult.length != 1) {
			TestUtils.fail("We got a wrong length for the simplest case!");
			return;
		}

		if (simpleResult[0] != 11) {
			TestUtils.fail("We got a wrong result for the simplest case! The result that we got: " + simpleResult[0]);
			return;
		}

		double[] moreAdvancedData = {10, 12, 10, 10, 11, 10, 10, 9, 7, 4, 2, 1, 3, 3, 4, 5, 6, 8, 10, 11, 10, 10, 10, 10, 12, 10,
		10, 12, 10, 10, 11, 10, 10, 9, 7, 4, 2, 5, 3, 3, 4, 5, 6, 8, 10, 11, 10, 10, 10, 10, 12, 10};

		int[] moreAdvancedDataResultForOne = MathUtils.findMinima(moreAdvancedData, 1);

		if (moreAdvancedDataResultForOne.length != 1) {
			TestUtils.fail("We got a wrong length for the more advanced case with one requested!");
			return;
		}

		if (moreAdvancedDataResultForOne[0] != 11) {
			TestUtils.fail("We got a wrong result for the more advanced case with one requested!");
			return;
		}

		int[] moreAdvancedDataResultForTwo = MathUtils.findMinima(moreAdvancedData, 2);

		if (moreAdvancedDataResultForTwo.length != 2) {
			TestUtils.fail("We got a wrong length for the more advanced case with two requested!");
			return;
		}

		if ((moreAdvancedDataResultForTwo[0] != 11) || (moreAdvancedDataResultForTwo[1] != 36)) {
			TestUtils.fail("We got a wrong result for the more advanced case with two requested!");
			return;
		}

		TestUtils.succeed();
	}

	public void findMaximaTest() {

		TestUtils.start("Find Maxima");

		double[] data = {16, 15, 13, 13.5, 13, 12, 14, 13.2, 13, 12, 11, 10, 10, 9, 8, 9, 10, 9.9, 10, 10.5,
			11, 12, 13, 14, 13, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, 1, 2, 3, 3, 2, 3, 2, 3, 2,
			3, 3, 4, 5, 4, 3, 2, 1, 2, 1, 2, 1, 0, 0, 1, 0, 1, 0, 0, 1};

		int[] result = MathUtils.findMaxima(data, 1);

		if (data[0] != 16) {
			TestUtils.fail("The data got modified!");
			return;
		}

		if (result.length != 1) {
			TestUtils.fail("We got a wrong length!");
			return;
		}

		// both of these are acceptable, as they have the same height
		if ((result[0] != 23) && (result[0] != 25)) {
			TestUtils.fail("We got a wrong result! The result that we got: " + result[0]);
			return;
		}

		result = MathUtils.findMaxima(data, 2);

		if (result.length != 2) {
			TestUtils.fail("We got a wrong length for the second result!");
			return;
		}

		// both of these are acceptable, as they have the same height - however,
		// only one of them should be given back (!); the same maximum should not
		// be reported twice! - therefore the much less impressive maximum on the
		// right should be reported instead as second hit!
		if (((result[0] != 23) && (result[0] != 25)) || (result[1] != 52)) {
			TestUtils.fail("We got a wrong second result! The result that we got: " + result[0] + ", " + result[1]);
			return;
		}

		double[] largeData = {16, 15, 13, 13.5, 13, 12, 14, 13.2, 13, 12, 11, 10, 10, 9, 8, 9, 10, 9.9, 10, 10.5,
			11, 12, 13, 14.1, 13, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, 1, 2, 3, 3, 2, 3, 2, 3, 2,
			3, 3, 4, 5, 4, 3, 2, 1, 2, 1, 2, 1, 0, 0, 1, 0, 1, 0, 0, 1, 2, 4, 6, 7, 9, 8, 9, 10, 9.9, 10, 10.5,
			11, 12, 15, 17, 14.5, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, };

		result = MathUtils.findMaxima(largeData, 3);

		if (result.length != 3) {
			TestUtils.fail("We got a wrong length for the third result!");
		}

		if ((result[0] != 83) || (result[1] != 23) || (result[2] != 52)) {
			TestUtils.fail("We got a wrong third result! The result that we got: " + result[0] + ", " +
				result[1] + ", " + result[2]);
			return;
		}

		TestUtils.succeed();
	}

	public void divideIntsTest() {

		TestUtils.start("Divide Ints");

		testDivideInts(0, 1, 0);
		testDivideInts(4, 2, 2);
		testDivideInts(6, 2, 3);
		testDivideInts(9, 3, 3);
		testDivideInts(10, 3, 3);
		testDivideInts(11, 3, 4);
		testDivideInts(12, 3, 4);

		TestUtils.succeed();
	}

	private void testDivideInts(int dividend, int divisor, int expectedResult) {

		int res = MathUtils.divideInts(dividend, divisor);

		if (res != expectedResult) {
			TestUtils.fail("We called x = " + dividend + " / " + divisor +
				" and did not get x == " + expectedResult + "!");
		}
	}

	public void averageTest() {

		TestUtils.start("Average of a List of Integers");

		List<Integer> testList;
		testList = new ArrayList<>();
		testAverageOfInts(testList, 0);

		testList.add(1);
		testAverageOfInts(testList, 1);

		testList.add(3);
		testAverageOfInts(testList, 2);

		testList.add(5);
		testAverageOfInts(testList, 3);

		testList.add(11);
		testAverageOfInts(testList, 5);

		testList.add(-10);
		testAverageOfInts(testList, 2);

		TestUtils.succeed();
	}

	private void testAverageOfInts(List<Integer> testList, int expectedResult) {

		int res = MathUtils.averageFast(testList);

		if (res != expectedResult) {
			StringBuilder listStr = new StringBuilder();
			String sep = "";
			for (Integer val : testList) {
				listStr.append(sep);
				sep = ", ";
				listStr.append(val);
			}
			TestUtils.fail("We called averageFast([" + listStr + "])" +
				" and did not get the expected result " + expectedResult + " but instead " + res + "!");
		}

		res = MathUtils.averageSlow(testList);

		if (res != expectedResult) {
			StringBuilder listStr = new StringBuilder();
			String sep = "";
			for (Integer val : testList) {
				listStr.append(sep);
				sep = ", ";
				listStr.append(val);
			}
			TestUtils.fail("We called averageSlow([" + listStr + "])" +
				" and did not get the expected result " + expectedResult + " but instead " + res + "!");
		}
	}

	public void calculateMathStrTest() {

		TestUtils.start("Calculate Math Str Test");

		testCalcMath("1+1", "2");
		testCalcMath("1*1", "1");
		testCalcMath("10*12", "120");
		testCalcMath("10-12", "-2");
		testCalcMath("3*(4+5)", "27");
		testCalcMath("-9", "-9");

		TestUtils.succeed();
	}

	private void testCalcMath(String calcStr, String expectedResult) {

		String actualResult = MathUtils.calculateMathStr(calcStr);
		if (!expectedResult.equals(actualResult)) {
			TestUtils.fail(calcStr + " is " + actualResult + ", NOT " + expectedResult + "!");
		}
	}

}
