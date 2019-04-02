/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.test.Test;
import com.asofterspace.toolbox.test.TestUtils;
import com.asofterspace.toolbox.Utils;


public class UtilTest implements Test {

	@Override
	public void runAll() {

		countStringInStringTest();

		intToHumanReadableByteAmountTest();
	}

	public void countStringInStringTest() {

		TestUtils.start("Count String in String");

		if (0 != Utils.countStringInString("foo", null)) {
			TestUtils.fail("We did not find foo no times in null!");
			return;
		}

		if (2 != Utils.countStringInString("foo", "foobarfoobar")) {
			TestUtils.fail("We did not find foo twice in foobarfoobar!");
			return;
		}

		if (1 != Utils.countStringInString("a softer space", "This is a softer space. Yepp!")) {
			TestUtils.fail("We did not find a softer space once in This is a softer space. Yepp!");
			return;
		}

		if (3 != Utils.countStringInString("findfind", "We are looking for findfindfind - or was it findfind?")) {
			TestUtils.fail("We did not find findfind thrice in We are looking for findfindfind - or was it findfind?");
			return;
		}

		TestUtils.succeed();
	}


	public void intToHumanReadableByteAmountTest() {

		TestUtils.start("Int to Human Readable Byte Amount");

		if (!"0 B".equals(Utils.intToHumanReadableByteAmount(0))) {
			TestUtils.fail("We tried to convert 0 B, but we got " + Utils.intToHumanReadableByteAmount(0) + "!");
			return;
		}

		if (!"1 B".equals(Utils.intToHumanReadableByteAmount(1))) {
			TestUtils.fail("We tried to convert 1 B, but we got " + Utils.intToHumanReadableByteAmount(1) + "!");
			return;
		}

		if (!"100.00 KB".equals(Utils.intToHumanReadableByteAmount(100*1024))) {
			TestUtils.fail("We tried to convert 100.00 KB, but we got " + Utils.intToHumanReadableByteAmount(100*1024) + "!");
			return;
		}

		if (!"26.76 KB".equals(Utils.intToHumanReadableByteAmount(27398))) {
			TestUtils.fail("We tried to convert 26.76 KB, but we got " + Utils.intToHumanReadableByteAmount(27398) + "!");
			return;
		}

		if (!"200.00 MB".equals(Utils.intToHumanReadableByteAmount(200*1024*1024l))) {
			TestUtils.fail("We tried to convert 200.00 MB, but we got " + Utils.intToHumanReadableByteAmount(200*1024*1024l) + "!");
			return;
		}

		if (!"300.00 GB".equals(Utils.intToHumanReadableByteAmount(300*1024*1024*1024l))) {
			TestUtils.fail("We tried to convert 300.00 GB, but we got " + Utils.intToHumanReadableByteAmount(300*1024*1024*1024l) + "!");
			return;
		}

		TestUtils.succeed();
	}

}
