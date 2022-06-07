/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.accounting.FinanceUtils;
import com.asofterspace.toolbox.test.Test;
import com.asofterspace.toolbox.test.TestUtils;


public class FinanceUtilsTest implements Test {

	@Override
	public void runAll() {

		parseMoneyTest();
	}

	public void parseMoneyTest() {

		TestUtils.start("Parse Money");

		testMoneyParsing("1", 100);
		testMoneyParsing("2,5", 250);
		testMoneyParsing("19.5", 1950);
		testMoneyParsing("3.50€", 350);
		testMoneyParsing("1,004.50€", 100450);
		testMoneyParsing("1.015,50 EUR", 101550);
		testMoneyParsing(" 1.026.500USD", 102650000);
		testMoneyParsing("\t\n1,002 ", 100200);
		testMoneyParsing("3, 141 592", 314);

		TestUtils.succeed();
	}

	private void testMoneyParsing(String amountStr, Integer intendedResult) {

		Integer actualResult = FinanceUtils.parseMoney(amountStr);

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
