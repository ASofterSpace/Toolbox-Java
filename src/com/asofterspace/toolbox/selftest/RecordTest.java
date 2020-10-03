/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.io.JsonParseException;
import com.asofterspace.toolbox.test.Test;
import com.asofterspace.toolbox.test.TestUtils;
import com.asofterspace.toolbox.utils.Record;


public class RecordTest implements Test {

	@Override
	public void runAll() throws JsonParseException {

		incDecTest();

		advancedListAccessTest();
	}

	private void incDecTest() throws JsonParseException {

		TestUtils.start("Record inc/dec");

		Record rec = Record.emptyObject();
		rec.set("foo", 2);
		rec.set("bar", 9);
		rec.set("blubb", "blobb");

		int fooRes = rec.inc("foo");
		int barRes = rec.dec("bar");

		if (fooRes != 3) {
			TestUtils.fail("We stored 2 in foo, increased it, and did not get 3 returned!");
			return;
		}

		if (barRes != 8) {
			TestUtils.fail("We stored 9 in bar, decreased it, and did not get 8 returned!");
			return;
		}

		if ((int) rec.getInteger("foo") != 3) {
			TestUtils.fail("We stored 2 in foo, increased it, got 3 returned but got " +
				rec.getInteger("foo") + " when querying for it again!");
			return;
		}

		if ((int) rec.getInteger("bar") != 8) {
			TestUtils.fail("We stored 9 in bar, decreased it, got 8 returned but got " +
				rec.getInteger("bar") + " when querying for it again!");
			return;
		}

		// ensures this throws no exception, as the test would otherwise end here!
		Integer nullResult = rec.inc("blubb");

		if (nullResult != null) {
			TestUtils.fail("We stored a text, increased it, but did not get null!");
			return;
		}

		if (!"blobb".equals(rec.getString("blubb"))) {
			TestUtils.fail("We stored a text, increased it, and it changed to " + rec.getString("blubb") + "!");
			return;
		}

		TestUtils.succeed();
	}

	private void advancedListAccessTest() {

		TestUtils.start("Record Advanced List Access");

		Record rec = Record.emptyObject();

		Record arr1 = Record.emptyArray();
		rec.set("foo", arr1);

		Record str = new Record("test");
		rec.set("bar", str);

		Record str0 = new Record("test0");
		arr1.set(0, str0);

		Record str1 = new Record("test1");
		arr1.set(1, str1);

		if (!rec.get("foo").getString(0).equals("test0")) {
			TestUtils.fail("rec.get(\"foo\").getString(0) did not give test0!");
		}

		if (!rec.get("foo").getString(1).equals("test1")) {
			TestUtils.fail("rec.get(\"foo\").getString(1) did not give test1!");
		}

		if (rec.get("foo").getString(2) != null) {
			TestUtils.fail("rec.get(\"foo\").getString(2) did not give null");
		}

		if (!rec.getString("bar").equals("test")) {
			TestUtils.fail("rec.getString(\"bar\") did not give test!");
		}

		// check array-access for single string member, in which case we pretend to have an array
		// with exactly one element when actually we have just a string
		if (!rec.get("bar").getString(0).equals("test")) {
			TestUtils.fail("rec.get(\"bar\").getString(0) did not give test!");
		}

		if (rec.get("bar").getString(1) != null) {
			TestUtils.fail("rec.get(\"bar\").getString(1) did not give null!");
		}

		TestUtils.succeed();
	}
}
