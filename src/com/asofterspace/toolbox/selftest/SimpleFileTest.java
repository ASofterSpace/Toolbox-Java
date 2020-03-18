/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.io.SimpleFile;
import com.asofterspace.toolbox.test.Test;
import com.asofterspace.toolbox.test.TestUtils;
import com.asofterspace.toolbox.utils.TextEncoding;


public class SimpleFileTest implements Test {

	@Override
	public void runAll() {

		createTest();

		switchEncodingsTest();
	}

	public void createTest() {

		TestUtils.start("Create Simple File");

		String testFileName = AllTests.TEST_PATH + "/createTestFile.txt";

		SimpleFile testFile = new SimpleFile(testFileName);

		testFile.create();

		TestUtils.succeed();
	}

	public void switchEncodingsTest() {

		TestUtils.start("Switch Encodings");

		String testFileName = AllTests.TEST_PATH + "/testFile.txt";

		String testContent = "testcontent";

		SimpleFile testFile = new SimpleFile(testFileName);

		testFile.setEncoding(TextEncoding.UTF8_WITH_BOM);

		testFile.setContent(testContent);

		testFile.save();

		SimpleFile testFile2 = new SimpleFile(testFileName);

		testFile2.setEncoding(TextEncoding.UTF8_WITHOUT_BOM);

		String gotContent = testFile2.getContent();

		if (testContent.length() + 1 != gotContent.length()) {
			TestUtils.fail("We stored the text '" + testContent + "' of length " + testContent.length() +
				" in a file using UTF-8 with BOM, read it out again without BOM - but now the text '" +
				gotContent + "' has length " +
				gotContent.length() + " instead of the expected " + (testContent.length() + 1) +
				" (which is expected due to the BOM taking space)...");
			return;
		}

		SimpleFile testFile3 = new SimpleFile(testFileName);

		testFile3.setEncoding(TextEncoding.ISO_LATIN_1);

		gotContent = testFile3.getContent();

		if (testContent.length() + 3 != gotContent.length()) {
			TestUtils.fail("We stored the text '" + testContent + "' of length " + testContent.length() +
				" in a file using UTF-8 with BOM, read it out again as ISO Latin 1 - but now the text '" +
				gotContent + "' has length " +
				gotContent.length() + " instead of the expected " + (testContent.length() + 3) +
				" (which is expected due to the BOM taking space)...");
			return;
		}

		TestUtils.succeed();
	}

}
