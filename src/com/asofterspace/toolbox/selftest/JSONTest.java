/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.io.JSON;
import com.asofterspace.toolbox.test.Test;
import com.asofterspace.toolbox.test.TestUtils;


public class JSONTest implements Test {

	@Override
	public void runAll() {

		fromStringTest();

		malformedFromStringTest();

		toStringTest();

		fromStringTestEscapedQuotes();

		toStringTestEscapedQuotes();

		fromStringTestEscapedChars();

		toStringTestEscapedChars();

		fromStringTestSpecialChars();

		toStringTestSpecialChars();

		largeFromStringTest();

		largeToStringTest();

		alphabeticalSortingTest();
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

	public void malformedFromStringTest() {

		TestUtils.start("Malformed JSON from String");

		JSON testObject = new JSON("{files:[]}");

		if (testObject.toString().equals("{\"files\": []}")) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We stored a malformed JSON string in a JSON object and would expect it to cope with that - but it did not!\nOutput: " + testObject.toString());
	}

	public void toStringTest() {

		TestUtils.start("JSON to String");

		String orig = "{\"foo\": \"bar\"}";

		JSON testObject = new JSON(orig);

		if (testObject.toString().equals(orig)) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We stored foo:bar in a JSON object, then converted the object back to JSON - and did not get our input back!");
	}

	public void fromStringTestEscapedQuotes() {

		TestUtils.start("JSON from String (with Escaped Quotes)");

		JSON testObject = new JSON("{\"foo\":\"ba\\\"r\"}");

		if (testObject.getString("foo").toString().equals("ba\"r")) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We stored foo:ba\"r in a JSON object, then read the key foo - and did not get ba\"r!");
	}

	public void toStringTestEscapedQuotes() {

		TestUtils.start("JSON to String (with Escaped Quotes)");

		String orig = "{\"foo\": \"ba\\\"r\"}";

		JSON testObject = new JSON(orig);

		if (testObject.toString().equals(orig)) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We stored foo:ba\"r in a JSON object, then converted the object back to JSON - and did not get our input back!");
	}

	public void fromStringTestEscapedChars() {

		TestUtils.start("JSON from String (with Escaped Characters)");

		JSON testObject = new JSON("{\"foo\":\"ba\\n\\r\"}");

		if (testObject.getString("foo").toString().equals("ba\n\r")) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We stored foo:ba\\n\\r in a JSON object, then read the key foo - and did not get ba\\n\\r!");
	}

	public void toStringTestEscapedChars() {

		TestUtils.start("JSON to String (with Escaped Characters)");

		String orig = "{\"foo\": \"ba\\n\\r\"}";

		JSON testObject = new JSON(orig);

		if (testObject.toString().equals(orig)) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We stored foo:ba\\n\\r in a JSON object, then converted the object back to JSON - and did not get our input back!");
	}

	public void fromStringTestSpecialChars() {

		TestUtils.start("JSON from String (with Special Characters)");

		JSON testObject = new JSON("{\"foo\":\"bär\"}");

		if (testObject.getString("foo").toString().equals("bär")) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We stored foo:bär in a JSON object, then read the key foo - and did not get bär!");
	}

	public void toStringTestSpecialChars() {

		TestUtils.start("JSON to String (with Special Characters)");

		String orig = "{\"foo\": \"bär\"}";

		JSON testObject = new JSON(orig);

		if (testObject.toString().equals(orig)) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We stored foo:bär in a JSON object, then converted the object back to JSON - and did not get our input back!");
	}

	public void largeFromStringTest() {

		TestUtils.start("JSON from String (large!)");

		JSON testObject = new JSON("{\"foo\": \"bar\", \"large\": {\"foo1\": \"bar1\", \"foo2\": \"bar2\"}}");

		if (testObject.getString("foo").toString().equals("bar")) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We stored foo:bar (and some other stuff) in a JSON object, then read the key foo - and did not get bar!");
	}

	public void largeToStringTest() {

		TestUtils.start("JSON to String (large!)");

		String orig = "{\"data\": {\"foo1\": \"bar1\", \"foo2\": \"bar2\"}, \"foo\": \"bar\", \"large\": {\"foo1\": \"bar1\", \"foo2\": \"bar2\"}}";

		JSON testObject = new JSON(orig);

		if (testObject.toString().equals(orig)) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We stored foo:bar (and some other stuff) in a JSON object, then converted the object back to JSON - and did not get our input back!");
	}

	public void alphabeticalSortingTest() {

		TestUtils.start("JSON Alphabetically Sorted");

		JSON testObject = new JSON("{\"foo\": \"bar\", \"data\": {\"foo2\": \"bar2\", \"foo1\": \"bar1\"}}");

		if (testObject.toString().equals("{\"data\": {\"foo1\": \"bar1\", \"foo2\": \"bar2\"}, \"foo\": \"bar\"}")) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We stored some not-alphabetically sorted stuff in a JSON object, then converted the object back to JSON - and expected it to be alphabetically sorted, but oh no, it was not!");
	}

}
