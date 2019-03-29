/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.test.TestUtils;


public class AllTests {

	static final String TEST_PATH = "test";

	static final String IMAGE_TEST_DATA_PATH = "testdata/images";

	static final String CDM_TEST_DATA_PATH = "testdata/cdm";


	public static void main(String[] args) {

		TestUtils.startSuite();

		TestUtils.run(new ConfigFileTest());

		TestUtils.run(new CoderJavaTest());

		TestUtils.run(new JSONTest());

		TestUtils.run(new ImageTest());

		TestUtils.run(new ImageDefaultTest());

		TestUtils.run(new ImagePpmTest());

		TestUtils.run(new QrCodeTest());

		TestUtils.run(new XlsxTest());

		TestUtils.run(new ConverterTest());

		TestUtils.run(new UuidTest());

		TestUtils.run(new CdmTest());

		TestUtils.endSuite();

		// code editor components keep one single highlighting thread running,
		// and the JVM will not stop until all threads are stopped, so we just
		// tell the JVM to stop :)
		System.exit(0);
	}

	// ensure the test directory is clear
	static void clearTestDirectory() {

		Directory testDir = new Directory(AllTests.TEST_PATH);

		testDir.clear();
	}
}
