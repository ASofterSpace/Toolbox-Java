/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.io.XmlElement;
import com.asofterspace.toolbox.io.XmlFile;
import com.asofterspace.toolbox.test.Test;
import com.asofterspace.toolbox.test.TestUtils;


public class XmlTest implements Test {

	@Override
	public void runAll() {

		fromFileTest();

		fromHeaderlessFileTest();
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

}
