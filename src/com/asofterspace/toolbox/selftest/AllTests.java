package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.test.TestUtils;

public class AllTests {

	public static void main(String[] args) {

		TestUtils.startSuite();

		TestUtils.run(new ConfigFileTest());

		TestUtils.run(new JSONTest());

		TestUtils.endSuite();
	}
}
