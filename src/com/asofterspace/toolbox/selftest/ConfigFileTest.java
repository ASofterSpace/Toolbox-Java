package com.asofterspace.toolbox.selftest;

import static org.junit.Assert.*;

import org.junit.Test;

import com.asofterspace.toolbox.configuration.ConfigFile;

public class ConfigFileTest {

	/**
	 * Tests whether a config file actually saves its
	 * information to the file system - and can retrieve
	 * it again afterwards
	 */
	@Test
	public void persistenceTest() {

		String testfile = "testfile";
		String testkey = "test";
		String testvalue = "somevalue";

		ConfigFile confFile1 = new ConfigFile(testfile);

		confFile1.set(testkey, testvalue);

		ConfigFile confFile2 = new ConfigFile(testfile);
		
		if (testvalue.equals(confFile2.getValue(testkey))) {
			return;
		}

		fail("The configuration file does not seem to persist its information!");
	}

}
