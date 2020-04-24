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

		getLineFromPositionTest();

		getWordFromPositionTest();
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
		testMoneyParsing("19.5", 1950);
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

	public void getLineFromPositionTest() {

		TestUtils.start("Get Line from Position");

		String line = StrUtils.getLineFromPosition(14, "  MOV AX, BX\n  MOV BX, CX\nRET");

		if (!line.equals("  MOV BX, CX")) {
			TestUtils.fail("We wanted to get the line MOV BX, CX, but instead got " + line + "!");
			return;
		}

		TestUtils.succeed();
	}

	public void getWordFromPositionTest() {

		TestUtils.start("Get Word from Position");

		String word = StrUtils.getWordFromPosition(10, "    this.someObj := 27398;");

		if (!word.equals("someObj")) {
			TestUtils.fail("We wanted to get the word someObj, but instead got " + word + "!");
			return;
		}

		TestUtils.succeed();
	}
}
