/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.io.JSON;
import com.asofterspace.toolbox.io.JsonFile;
import com.asofterspace.toolbox.io.SimpleFile;
import com.asofterspace.toolbox.test.Test;
import com.asofterspace.toolbox.test.TestUtils;


public class JSONTest implements Test {

	@Override
	public void runAll() {

		fromSimpleFileTest();

		fromAdvancedFileTest();

		toAdvancedFileTest();

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

	public void fromSimpleFileTest() {

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

	public void fromAdvancedFileTest() {

		TestUtils.start("JSON from Advanced File");

		JsonFile testFile = new JsonFile(AllTests.JSON_TEST_DATA_PATH + "/advanced.json");

		if (!testFile.getValue("foo").equals("")) {
			TestUtils.fail("We stored {\"foo\": \"\"} in an advanced JSON file, then read the file - and did not get an empty string when querying for foo!");
			return;
		}

		if (!testFile.getAllContents().getString("bar").equals("\"")) {
			TestUtils.fail("We stored {\"bar\": \"\\\"\"} in a JSON file, then read the file - and did not get \" when querying for bar!");
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

	public void toAdvancedFileTest() {

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
