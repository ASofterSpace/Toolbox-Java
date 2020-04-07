/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.test.Test;
import com.asofterspace.toolbox.test.TestUtils;
import com.asofterspace.toolbox.utils.StrUtils;


public class StrUtilsTest implements Test {

	@Override
	public void runAll() {

		countStringInStringTest();

		parseMoneyTest();
	}

	public void countStringInStringTest() {

		TestUtils.start("Count String in String");

		if (0 != StrUtils.countStringInString("foo", null)) {
			TestUtils.fail("We did not find foo no times in null!");
			return;
		}

		if (2 != StrUtils.countStringInString("foo", "foobarfoobar")) {
			TestUtils.fail("We did not find foo twice in foobarfoobar!");
			return;
		}

		if (1 != StrUtils.countStringInString("a softer space", "This is a softer space. Yepp!")) {
			TestUtils.fail("We did not find a softer space once in This is a softer space. Yepp!");
			return;
		}

		if (3 != StrUtils.countStringInString("findfind", "We are looking for findfindfind - or was it findfind?")) {
			TestUtils.fail("We did not find findfind thrice in We are looking for findfindfind - or was it findfind?");
			return;
		}

		TestUtils.succeed();
	}

	public void parseMoneyTest() {

		TestUtils.start("Parse Money");

		testMoneyParsing("1", 100);
		testMoneyParsing("2,5", 250);
		testMoneyParsing("3.50€", 350);
		testMoneyParsing("1,004.50€", 100450);
		testMoneyParsing("1.015,50 EUR", 101550);
		testMoneyParsing(" 1.026.500USD", 102650);

		TestUtils.succeed();
	}

	private void testMoneyParsing(String amountStr, Integer intendedResult) {

		Integer actualResult = StrUtils.parseMoney(amountStr);

		if ((actualResult == null) && (intendedResult == null)) {
			return;
		}

		if (actualResult.equals(intendedResult)) {
			return;
		}

		TestUtils.fail("We could not parse " + amountStr + " as amount of money! " +
			"(Expected " + intendedResult + ", but got " + actualResult + ")");
	}

}
