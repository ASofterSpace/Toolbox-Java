/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.configuration.ConfigFile;
import com.asofterspace.toolbox.io.JsonParseException;
import com.asofterspace.toolbox.test.Test;
import com.asofterspace.toolbox.test.TestUtils;
import com.asofterspace.toolbox.utils.Record;


public class ConfigFileTest implements Test {

	@Override
	public void runAll() throws JsonParseException {

		persistenceTest();
	}

	/**
	 * Tests whether a config file actually saves its
	 * information to the file system - and can retrieve
	 * it again afterwards
	 */
	public void persistenceTest() throws JsonParseException {

		TestUtils.start("Config File Persistence");

		String testfile = "testfile";
		String testkey = "test";
		String testvalue = "somevalue";

		// save a new ConfigFile where no file was before
		ConfigFile confFile1 = new ConfigFile(testfile, false, Record.emptyObject());

		confFile1.set(testkey, testvalue);

		// open an existing config file
		ConfigFile confFile2 = new ConfigFile(testfile);

		if (testvalue.equals(confFile2.getValue(testkey))) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("The configuration file does not seem to persist its information!");
	}

}
