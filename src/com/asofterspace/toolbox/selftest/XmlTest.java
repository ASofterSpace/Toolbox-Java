/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.io.JSON;
import com.asofterspace.toolbox.io.JsonParseException;
import com.asofterspace.toolbox.io.XML;
import com.asofterspace.toolbox.io.XmlElement;
import com.asofterspace.toolbox.io.XmlFile;
import com.asofterspace.toolbox.test.Test;
import com.asofterspace.toolbox.test.TestUtils;


public class XmlTest implements Test {

	@Override
	public void runAll() throws JsonParseException {

		fromStringTest();

		fromFileTest();

		fromHeaderlessFileTest();

		toJsonAndBackTest();

		advancedToJsonAndBackTest();

		restrictedToJsonAndBackTest();
	}

	public void fromStringTest() {

		TestUtils.start("XML from String");

		String testStr = "<blubb><foo bar='test' /></blubb>";

		XML xml = new XML(testStr);

		System.out.println(xml);
		System.out.println(new JSON(xml));

		if (!xml.getName().equals("blubb")) {
			TestUtils.fail("We read <blubb><foo bar='test' /></blubb> as XML - " +
				"and did not get blubb as name of the outmost element!");
			return;
		}

		if (!((XML) xml.get("foo")).getAttributes().get("bar").equals("test")) {
			TestUtils.fail("We read <blubb><foo bar='test' /></blubb> as XML - " +
				"and did not get test inside bar inside foo inside blubb!");
			return;
		}

		TestUtils.succeed();
	}

	public void fromFileTest() {

		TestUtils.start("XML from File");

		XmlFile testFile = new XmlFile(AllTests.XML_TEST_DATA_PATH + "/simple.xml");

		if (!testFile.getRoot().getTagName().equals("foo")) {
			TestUtils.fail("We stored <foo>bar</foo> in an XML file, then read the file - and did not get foo as root!");
			return;
		}

		if (!testFile.getRoot().getInnerText().equals("bar")) {
			TestUtils.fail("We stored <foo>bar</foo> in an XML file, then read the file - and did not get bar inside foo!");
			return;
		}

		TestUtils.succeed();
	}

	public void fromHeaderlessFileTest() {

		TestUtils.start("XML from Headerless File");

		XmlFile testFile = new XmlFile(AllTests.XML_TEST_DATA_PATH + "/simpleNoHeader.xml");

		if (!testFile.getRoot().getTagName().equals("foo")) {
			TestUtils.fail("We stored <foo>bar</foo> in an XML file, then read the file - and did not get foo as root!");
			return;
		}

		if (!testFile.getRoot().getInnerText().equals("bar")) {
			TestUtils.fail("We stored <foo>bar</foo> in an XML file, then read the file - and did not get bar inside foo!");
			return;
		}

		TestUtils.succeed();
	}

	public void toJsonAndBackTest() throws JsonParseException {

		TestUtils.start("XML to JSON and Back");

		XmlElement orig = new XmlElement("test");
		orig.createChild("foo").setInnerText("bar");
		orig.createChild("foo2").setInnerText("twobar");

		XML xml = new XML(orig);

		JSON json = new JSON(xml);

		String jsonStr = json.toString();

		JSON jsonDecoded = new JSON(jsonStr);

		XmlElement target = new XmlElement(jsonDecoded);

		// TODO :: also convert XML to string and back to XML object before checking the output

		if (!target.getChild("foo").getInnerText().equals("bar")) {
			TestUtils.fail("We transferred XML to JSON and back and did not get foo: bar!");
			return;
		}

		if (!target.getChild("foo2").getInnerText().equals("twobar")) {
			TestUtils.fail("We transferred XML to JSON and back and did not get foo2: twobar!");
			return;
		}

		TestUtils.succeed();
	}

	public void advancedToJsonAndBackTest() throws JsonParseException {

		TestUtils.start("Advanced XML to JSON and Back");

		XmlElement orig = new XmlElement("test");
		orig.createChild("foo").setInnerText("bar");
		orig.createChild("2").setInnerText("two");
		orig.createChild("newline").setInnerText("\n");
		orig.createChild("textWithQuote").setInnerText("This is text with a \" sign!");
		orig.createChild("xmlEntity").setInnerText("</xmlEntity>");

		XML xml = new XML(orig);

		JSON json = new JSON(xml);

		String jsonStr = json.toString();

		JSON jsonDecoded = new JSON(jsonStr);

		XmlElement target = new XmlElement(jsonDecoded);

		// TODO :: also convert XML to string and back to XML object before checking the output

		if (!target.getChild("foo").getInnerText().equals("bar")) {
			TestUtils.fail("We transferred XML to JSON and back and did not get foo: bar!");
			return;
		}

		if (!target.getChild("2").getInnerText().equals("two")) {
			TestUtils.fail("We transferred XML to JSON and back and did not get 2: two!");
			return;
		}

		if (!target.getChild("newline").getInnerText().equals("\n")) {
			TestUtils.fail("We transferred XML to JSON and back and did not get newline: \\n!");
			return;
		}

		if (!target.getChild("textWithQuote").getInnerText().equals("This is text with a \" sign!")) {
			TestUtils.fail("We transferred XML to JSON and back and did not get textWithQuote: This is text with a \" sign!");
			return;
		}

		if (!target.getChild("xmlEntity").getInnerText().equals("</xmlEntity>")) {
			TestUtils.fail("We transferred XML to JSON and back and did not get xmlEntity: </xmlEntity>!");
			return;
		}

		TestUtils.succeed();
	}

	public void restrictedToJsonAndBackTest() throws JsonParseException {

		TestUtils.start("Restricted XML to JSON and Back");

		XML xml = new XML();
		xml.setString("one", "1");
		xml.setString("two", "2");
		xml.setString("three", "3");
		xml.setString("four", "4");

		JSON json = new JSON(xml);

		json.removeAllExcept("one", "two", "three");

		String jsonStr = json.toString();

		JSON jsonDecoded = new JSON(jsonStr);

		XML target = new XML(jsonDecoded);

		target.removeAllExcept("one", "two", "four");

		// TODO :: also convert XML to string and back to XML object before checking the output

		if (!target.getString("one").equals("1")) {
			TestUtils.fail("We transferred XML to JSON and back and did not get one: 1!");
			return;
		}

		if (!target.getString("two").equals("2")) {
			TestUtils.fail("We transferred XML to JSON and back and did not get two: 2!");
			return;
		}

		if (target.getString("three") != null) {
			TestUtils.fail("We transferred XML to JSON and back and did get three - which should have been filtered out!");
			return;
		}

		if (target.getString("four") != null) {
			TestUtils.fail("We transferred XML to JSON and back and did get four - which should have been filtered out!");
			return;
		}

		TestUtils.succeed();
	}

}
