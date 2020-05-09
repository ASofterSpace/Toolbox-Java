/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.test.Test;
import com.asofterspace.toolbox.test.TestUtils;
import com.asofterspace.toolbox.utils.StrUtils;

import java.util.ArrayList;
import java.util.List;


public class StrUtilsTest implements Test {

	@Override
	public void runAll() {

		countStringInStringTest();

		getLineFromPositionTest();

		getWordFromPositionTest();

		sortAndRemoveDuplicatesTest();

		strToIntTest();

		strToDoubleTest();
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

	public void sortAndRemoveDuplicatesTest() {

		TestUtils.start("Sort and Remove Duplicates");

		List<String> listIn = new ArrayList<>();
		listIn.add("foo");
		listIn.add("bar");
		listIn.add("FOO");
		listIn.add("foo");

		List<String> listOut1 = new ArrayList<>();
		listOut1.add("bar");
		listOut1.add("FOO");
		listOut1.add("foo");

		List<String> listOut2 = new ArrayList<>();
		listOut2.add("bar");
		listOut2.add("foo");
		listOut2.add("FOO");

		List<String> listActualOut = StrUtils.sortAndRemoveDuplicates(listIn);

		boolean equalsFirst = StrUtils.equals(listActualOut, listOut1);
		boolean equalsSecond = StrUtils.equals(listActualOut, listOut2);

		if ((!equalsFirst) && (!equalsSecond)) {
			TestUtils.fail("We wanted to sort a list and remove duplicates, but we did not get the expected result " +
				"- instead, we got:\n" + StrUtils.join("\n", listActualOut));
			return;
		}

		TestUtils.succeed();
	}

	public void strToIntTest() {

		TestUtils.start("strToInt");

		testStrToInt("1", 1);
		testStrToInt(" 2\t", 2);
		testStrToInt("3.1415926", 3);
		testStrToInt(" \u00a0 4 \t\r\n", 4);
		testStrToInt("5,17", 5);
		testStrToInt("1.024,28", 1024);
		testStrToInt("2,024.28", 2024);
		testStrToInt("foobar", null);

		TestUtils.succeed();
	}

	private void testStrToInt(String origStr, Integer targetInt) {

		Integer result = StrUtils.strToInt(origStr);

		if ((result == null) && (targetInt == null)) {
			return;
		}

		if ((result == null) || !result.equals(targetInt)) {
			TestUtils.fail("We called strToInt(\"" + origStr + "\") and got " + result +
				", but expected " + targetInt + "!");
		}
	}

	public void strToDoubleTest() {

		TestUtils.start("strToDouble");

		testStrToDouble("1", 0.9, 1.1);
		testStrToDouble(" 2\t", 1.9, 2.1);
		testStrToDouble("3.1415926", 3.1, 3.2);
		testStrToDouble(" \u00a0 4 \t\r\n", 3.9, 4.1);
		testStrToDouble("5,17", 5.1, 5.2);
		testStrToDouble("1.024,28", 1024.2, 1024.3);
		testStrToDouble("2,024.28", 2024.27, 2024.29);
		testStrToDouble("foobar", null, null);

		TestUtils.succeed();
	}

	private void testStrToDouble(String origStr, Double targetDoubleLow, Double targetDoubleHigh) {

		Double result = StrUtils.strToDouble(origStr);

		if ((result == null) && (targetDoubleLow == null)) {
			return;
		}

		if ((result == null) || !((result > targetDoubleLow) && (result < targetDoubleHigh))) {
			TestUtils.fail("We called strToDouble(\"" + origStr + "\") and got " + result +
				", but expected a result between " + targetDoubleLow + " and " + targetDoubleHigh + "!");
		}
	}

}
