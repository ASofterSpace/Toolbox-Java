package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.web.JSON;
import com.asofterspace.toolbox.test.Test;
import com.asofterspace.toolbox.test.TestUtils;

public class JSONTest implements Test {

	@Override
	public void runAll() {

		fromStringTest();

		toStringTest();
	}

	public void fromStringTest() {

		TestUtils.start("JSON from String");

		JSON testObject = new JSON("{\"foo\":\"bar\"}");

		if (testObject.getString("foo").toString().equals("bar")) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We stored foo:bar in a JSON object, then read the key foo - and did not get bar!");
	}

	public void toStringTest() {

		TestUtils.start("JSON to String");

		JSON testObject = new JSON("{\"foo\": \"bar\"}");

		if (testObject.toString().equals("{\"foo\": \"bar\"}")) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We stored foo:bar in a JSON object, then converted the object back to JSON - and did not get our input back!");
	}

}
