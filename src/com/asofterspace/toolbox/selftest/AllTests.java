/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.test.TestUtils;

public class AllTests {

	public static void main(String[] args) {

		TestUtils.startSuite();

		TestUtils.run(new ConfigFileTest());

		TestUtils.run(new CoderJavaTest());

		TestUtils.run(new JSONTest());

		TestUtils.run(new XlsxTest());

		TestUtils.run(new ConverterTest());

		TestUtils.run(new UuidTest());

		TestUtils.run(new CdmTest());

		TestUtils.endSuite();
	}
}
