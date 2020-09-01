/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.io.JSON;
import com.asofterspace.toolbox.io.JsonFile;
import com.asofterspace.toolbox.io.JsonParseException;
import com.asofterspace.toolbox.io.SimpleFile;
import com.asofterspace.toolbox.test.Test;
import com.asofterspace.toolbox.test.TestUtils;


public class JSONTest implements Test {

	@Override
	public void runAll() throws JsonParseException {

		fromSimpleFileTest();

		fromAdvancedFileTest();

		toAdvancedFileTest();

		fromStringTest();

		fromNumberStringTest();

		malformedFromStringTest();

		openEndedStringTest();

		doubleBackslashBeforeNewlineTest();

		toStringTest();

		fromStringTestEscapedQuotes();

		toStringTestEscapedQuotes();

		fromStringTestEscapedBackslash();

		toStringTestEscapedBackslash();

		throwsOnOpenString();

		throwsOnOpenKey();

		throwsOnMissingColon();

		fromStringTestEscapedChars();

		toStringTestEscapedChars();

		fromStringTestSpecialChars();

		toStringTestSpecialChars();

		largeFromStringTest();

		largeToStringTest();

		alphabeticalSortingTest();

		throwsExceptionTest();
	}

	public void fromSimpleFileTest() throws JsonParseException {

		TestUtils.start("JSON from Simple File");

		JsonFile testFile = new JsonFile(AllTests.JSON_TEST_DATA_PATH + "/simple.json");

		if (!testFile.getValue("foo").equals("bar")) {
			TestUtils.fail("We stored {\"foo\": \"bar\"} in a JSON file, then read the file directly - and did not get bar when querying for foo!");
			return;
		}

		if (!testFile.getAllContents().getString("foo").equals("bar")) {
			TestUtils.fail("We stored {\"foo\": \"bar\"} in a JSON file, then read the file as JSON getting a string - and did not get bar when querying for foo!");
			return;
		}

		if (!testFile.getAllContents().get("foo").asString().equals("bar")) {
			TestUtils.fail("We stored {\"foo\": \"bar\"} in a JSON file, then read the file as JSON as string - and did not get bar when querying for foo!");
			return;
		}

		TestUtils.succeed();
	}

	public void fromAdvancedFileTest() throws JsonParseException {

		TestUtils.start("JSON from Advanced File");

		JsonFile testFile = new JsonFile(AllTests.JSON_TEST_DATA_PATH + "/advanced.json");

		if (!testFile.getValue("foo").equals("")) {
			TestUtils.fail("We stored {\"foo\": \"\"} in an advanced JSON file, then read the file - and did not get an empty string when querying for foo!");
			return;
		}

		if (!testFile.getAllContents().getString("bar").equals("\"")) {
			TestUtils.fail("We stored {\"bar\": \"\\\"\"} in a JSON file, then read the file - and did not get \" when querying for bar, but instead " + testFile.getAllContents().getString("bar") + "!");
			return;
		}

		if (!testFile.getAllContents().get("blu").asString().equals("blubb")) {
			TestUtils.fail("We stored {'blu': \"blubb\"} in a JSON file, then read the file - and did not get blubb when querying for blu!");
			return;
		}

		if (!testFile.getValue("newline").equals("\n")) {
			TestUtils.fail("We stored {\"newline\": \"\\n\"} in an advanced JSON file, then read the file - and did not get \\n when querying for newline!");
			return;
		}

		if (!testFile.getAllContents().getString("leftout").equals("the Gänsefüßchen")) {
			TestUtils.fail("We stored {leftout: 'the Gänsefüßchen'} in a JSON file, then read the file - and did not get the Gänsefüßchen when querying for leftout!");
			return;
		}

		if (!testFile.getAllContents().getBoolean("otherbool").equals(false)) {
			TestUtils.fail("We stored {\"otherbool\": false} in a JSON file, then read the file - and did not get false when querying for otherbool!");
			return;
		}

		if (!testFile.getValue("possible").equals("right?")) {
			TestUtils.fail("We stored {\"possible\": \"right?\"} in an advanced JSON file, then read the file - and did not get right? when querying for possible!");
			return;
		}

		// TODO :: check for more of the examples from the advanced file!

		TestUtils.succeed();
	}

	public void toAdvancedFileTest() throws JsonParseException {

		TestUtils.start("JSON to Advanced File");

		AllTests.clearTestDirectory();

		JsonFile testFile = new JsonFile(AllTests.JSON_TEST_DATA_PATH + "/advanced.json");

		JSON testData = testFile.getAllContents();

		String outFileName = AllTests.TEST_PATH + "/advanced_saved.json";

		JsonFile outFile = new JsonFile(outFileName);

		outFile.setAllContents(testData);

		outFile.save();

		SimpleFile inFile = new SimpleFile(outFileName);

		String writtenFile = inFile.getContent();

		if (writtenFile.equals("{\n	\"a\": \"line\",\n	\"are\": \"also\",\n	\"array\": [\n		\"of\",\n		\"string\"\n	],\n	\"array2\": " +
							   "[\n		{\n			\"of\": \"obj\"\n		}\n	],\n	\"bar\": \"\\\"\",\n	\"blu\": \"blubb\",\n	\"bool\": true,\n" +
							   "	\"foo\": \"\",\n	\"indented\": \"nonsense\",\n	\"int\": 9,\n	\"leftout\": \"the Gänsefüßchen\",\n	\"newline\":" +
							   " \"\\n\",\n	\"otherbool\": false,\n	\"possible\": \"right?\",\n	\"several\": \"in\",\n	\"subobject\": {\n		\"is\": " +
							   "\"allowed\"\n	}\n}")) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We loaded an advanced JSON file, then saved the file, loaded it again (as plain text file) - and did not get what we expected!");
	}

	public void fromStringTest() throws JsonParseException {

		TestUtils.start("JSON from String");

		JSON testObject = new JSON("{\"foo\":\"bar\",\n\r\"nullkey\": null , \t}");

		if (!testObject.getString("foo").equals("bar")) {
			TestUtils.fail("We stored foo:bar in a JSON object, then read the key foo - and did not get bar!");
			return;
		}

		if (testObject.getString("nullkey") != null) {
			TestUtils.fail("We stored \"nullkey\": null in a JSON object, then read the key nullkey - and did not get null, but instead \"" + testObject.getString("nullkey") + "\"!");
			return;
		}

		TestUtils.succeed();
	}

	public void fromNumberStringTest() throws JsonParseException {

		TestUtils.start("JSON from Number String");

		JSON testObject = new JSON("{\"foo\":9, \"foo5\":\"5\", \"foo1\":1.0, \"foo2\":\"2.0\", \"foo3\":\"3,0\"}");

		if (!testObject.getString("foo").toString().equals("9")) {
			TestUtils.fail("We stored \"foo\":9 in a JSON object, then read the key foo as string - and did not get 9!");
			return;
		}

		if (testObject.getInteger("foo") != 9) {
			TestUtils.fail("We stored \"foo\":9 in a JSON object, then read the key foo as int - and did not get 9!");
			return;
		}

		if (testObject.getInteger("foo11") != null) {
			TestUtils.fail("We stored nothing for key foo11 in a JSON object, then read the key foo11 as int - and did not get null!");
			return;
		}

		if (testObject.getInteger("foo5") != 5) {
			TestUtils.fail("We stored \"foo5\":\"5\" in a JSON object, then read the key foo5 as int - and did not get 5!");
			return;
		}

		if (testObject.getInteger("foo1") != 1) {
			TestUtils.fail("We stored \"foo1\":1.0 in a JSON object, then read the key foo1 as int - and did not get 1!");
			return;
		}

		if (testObject.getInteger("foo2") != 2) {
			TestUtils.fail("We stored \"foo2\":\"2.0\" in a JSON object, then read the key foo2 as int - and did not get 2!");
			return;
		}

		if (testObject.getInteger("foo3") != 3) {
			TestUtils.fail("We stored \"foo3\":\"3,0\" in a JSON object, then read the key foo3 as int - and did not get 3!");
			return;
		}

		TestUtils.succeed();
	}

	public void malformedFromStringTest() throws JsonParseException {

		TestUtils.start("Malformed JSON from String");

		JSON testObject = new JSON("{files:[]}");

		if (testObject.toString().equals("{\"files\": []}")) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We stored a malformed JSON string in a JSON object and would expect it to cope with that - but it did not!\nOutput: " + testObject.toString());
	}

	public void openEndedStringTest() {

		TestUtils.start("JSON with Open Ended String");

		try {
			JSON testObject = new JSON("{\"foo\":\"bar}");
			TestUtils.fail("We tried to parse JSON containing an open-ended string, and it just parsed, without error!");
		} catch (JsonParseException e) {
			TestUtils.succeed();
		}
	}

	public void doubleBackslashBeforeNewlineTest() throws JsonParseException {

		TestUtils.start("Double Backslash before Newline");

		JSON testObject = new JSON("{\"key\":\"value\\\\right?\"}");

		if (testObject.getString("key").equals("value\\" + "right?")) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We loaded a JSON string containing backslash backslash r - which should give backslash r, but instead got some worrisome newline or whatever!\nOutput: " + testObject.getString("key"));
	}

	public void toStringTest() throws JsonParseException {

		TestUtils.start("JSON to String");

		String orig = "{\"foo\": \"bar\"}";

		JSON testObject = new JSON(orig);

		if (testObject.toString().equals(orig)) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We stored foo:bar in a JSON object, then converted the object back to JSON - and did not get our input back!");
	}

	public void fromStringTestEscapedQuotes() throws JsonParseException {

		TestUtils.start("JSON from String (with Escaped Quotes)");

		JSON testObject = new JSON("{\"foo\":\"ba\\\"r\"}");

		if (testObject.getString("foo").toString().equals("ba\"r")) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We stored foo:ba\"r in a JSON object, then read the key foo - and did not get ba\"r!");
	}

	public void toStringTestEscapedQuotes() throws JsonParseException {

		TestUtils.start("JSON to String (with Escaped Quotes)");

		String orig = "{\"foo\": \"ba\\\"r\"}";

		JSON testObject = new JSON(orig);

		if (testObject.toString().equals(orig)) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We stored foo:ba\"r in a JSON object, then converted the object back to JSON - and did not get our input back!");
	}

	public void fromStringTestEscapedBackslash() throws JsonParseException {

		TestUtils.start("JSON from String (with Escaped Backslash)");

		JSON testObject = new JSON("{\"foo\":\"ba\\\\\"}");

		if (testObject.getString("foo").toString().equals("ba\\")) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We stored foo:ba\\ in a JSON object, then read the key foo - and did not get ba\\ but instead " + testObject.getString("foo") + "!");
	}

	public void toStringTestEscapedBackslash() throws JsonParseException {

		TestUtils.start("JSON to String (with Escaped Backslash)");

		String orig = "{\"foo\": \"ba\\\\\"}";

		JSON testObject = new JSON(orig);

		if (testObject.toString().equals(orig)) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We stored foo:ba\\ in a JSON object, then converted the object back to JSON - and did not get our input back, but instead we got: " + testObject.toString());
	}

	public void throwsOnOpenString() {

		TestUtils.start("JSON throws on open string");

		try {
			JSON testObject = new JSON("{\"foo\":\"ba}");
		} catch (JsonParseException e) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We wanted to get an exception because of an unclosed string, but got none!");
	}

	public void throwsOnOpenKey() {

		TestUtils.start("JSON throws on open key");

		try {
			JSON testObject = new JSON("{\"foo\":\"bar\", \"schlu");
		} catch (JsonParseException e) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We wanted to get an exception because of an unclosed key, but got none!");
	}

	public void throwsOnMissingColon() {

		TestUtils.start("JSON throws on missing colon");

		try {
			JSON testObject = new JSON("{\"foo\" \"bar\"}");
		} catch (JsonParseException e) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We wanted to get an exception because of a missing colon, but got none!");
	}

	public void fromStringTestEscapedChars() throws JsonParseException {

		TestUtils.start("JSON from String (with Escaped Characters)");

		JSON testObject = new JSON("{\"foo\":\"ba\\n\\r\"}");

		if (testObject.getString("foo").toString().equals("ba\n\r")) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We stored foo:ba\\n\\r in a JSON object, then read the key foo - and did not get ba\\n\\r!");
	}

	public void toStringTestEscapedChars() throws JsonParseException {

		TestUtils.start("JSON to String (with Escaped Characters)");

		String orig = "{\"foo\": \"ba\\n\\r\"}";

		JSON testObject = new JSON(orig);

		if (testObject.toString().equals(orig)) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We stored foo:ba\\n\\r in a JSON object, then converted the object back to JSON - and did not get our input back!");
	}

	public void fromStringTestSpecialChars() throws JsonParseException {

		TestUtils.start("JSON from String (with Special Characters)");

		JSON testObject = new JSON("{\"foo\":\"bär\"}");

		if (testObject.getString("foo").toString().equals("bär")) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We stored foo:bär in a JSON object, then read the key foo - and did not get bär!");
	}

	public void toStringTestSpecialChars() throws JsonParseException {

		TestUtils.start("JSON to String (with Special Characters)");

		String orig = "{\"foo\": \"bär\"}";

		JSON testObject = new JSON(orig);

		if (testObject.toString().equals(orig)) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We stored foo:bär in a JSON object, then converted the object back to JSON - and did not get our input back!");
	}

	public void largeFromStringTest() throws JsonParseException {

		TestUtils.start("JSON from String (large!)");

		JSON testObject = new JSON("{\"foo\": \"bar\", \"large\": {\"foo1\": \"bar1\", \"foo2\": \"bar2\"}}");

		if (testObject.getString("foo").toString().equals("bar")) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We stored foo:bar (and some other stuff) in a JSON object, then read the key foo - and did not get bar!");
	}

	public void largeToStringTest() throws JsonParseException {

		TestUtils.start("JSON to String (large!)");

		String orig = "{\"data\": {\"foo1\": \"bar1\", \"foo2\": \"bar2\"}, \"foo\": \"bar\", \"large\": {\"foo1\": \"bar1\", \"foo2\": \"bar2\"}}";

		JSON testObject = new JSON(orig);

		if (testObject.toString().equals(orig)) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We stored foo:bar (and some other stuff) in a JSON object, then converted the object back to JSON - and did not get our input back!");
	}

	public void alphabeticalSortingTest() throws JsonParseException {

		TestUtils.start("JSON Alphabetically Sorted");

		JSON testObject = new JSON("{\"foo\": \"bar\", \"data\": {\"foo2\": \"bar2\", \"foo1\": \"bar1\"}}");

		if (testObject.toString().equals("{\"data\": {\"foo1\": \"bar1\", \"foo2\": \"bar2\"}, \"foo\": \"bar\"}")) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We stored some not-alphabetically sorted stuff in a JSON object, then converted the object back to JSON - and expected it to be alphabetically sorted, but oh no, it was not!");
	}

	public void throwsExceptionTest() {

		TestUtils.start("JSON Throws Exception (when parsing is impossible)");

		try {
			// we are here missing the { in front of "foo2" - and we WANT to get an exception in this case!
			JSON testJson = new JSON("[{\"foo\": \"bar\"}, \"foo2\": \"bar2\"}]");
			TestUtils.fail("We tried to parse some VERY broken JSON (too broken for autofixing it), and would have expected an exception - but got none!");
			return;
		} catch (JsonParseException e) {
			// yay, we got an exception! :)
		}

		TestUtils.succeed();
	}

}
