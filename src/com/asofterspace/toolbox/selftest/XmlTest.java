/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.io.JSON;
import com.asofterspace.toolbox.io.XmlElement;
import com.asofterspace.toolbox.io.XmlFile;
import com.asofterspace.toolbox.test.Test;
import com.asofterspace.toolbox.test.TestUtils;


public class XmlTest implements Test {

	@Override
	public void runAll() {

		fromFileTest();

		fromHeaderlessFileTest();

		toJsonAndBackTest();

		advancedToJsonAndBackTest();
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

	public void toJsonAndBackTest() {

		TestUtils.start("XML to JSON and Back");

		XmlElement orig = new XmlElement("test");
		orig.createChild("foo").setInnerText("bar");
		orig.createChild("foo2").setInnerText("twobar");

		JSON json = orig.toJson();

		XmlElement target = json.toXml();

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

	public void advancedToJsonAndBackTest() {

		TestUtils.start("Advanced XML to JSON and Back");

		XmlElement orig = new XmlElement("test");
		orig.createChild("foo").setInnerText("bar");
		orig.createChild("2").setInnerText("two");
		orig.createChild("newline").setInnerText("\n");
		orig.createChild("textWithQuote").setInnerText("This is text with a \" sign!");
		orig.createChild("xmlEntity").setInnerText("</xmlEntity>");

		JSON json = orig.toJson();

		XmlElement target = json.toXml();

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

}
