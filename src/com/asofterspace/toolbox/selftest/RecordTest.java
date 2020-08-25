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
	}

	public void incDecTest() throws JsonParseException {

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
}
