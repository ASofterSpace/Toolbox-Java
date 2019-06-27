/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.test.Test;
import com.asofterspace.toolbox.test.TestUtils;
import com.asofterspace.toolbox.utils.MathUtils;
import com.asofterspace.toolbox.Utils;


public class MathUtilsTest implements Test {

	@Override
	public void runAll() {

		findMinimaTest();

		findMaximaTest();
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
			11, 12, 13, 14, 13, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0};

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

		TestUtils.succeed();
	}

}
