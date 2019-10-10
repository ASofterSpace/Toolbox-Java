/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.test.Test;
import com.asofterspace.toolbox.test.TestUtils;
import com.asofterspace.toolbox.Utils;


public class UtilTest implements Test {

	@Override
	public void runAll() {

		callSomeFunctionsTest();
	}

	public void callSomeFunctionsTest() {

		TestUtils.start("Call Some Functions (and hope they don't throw exceptions)");

		Utils.debuglog("This is a test log!");

		Utils.sleep(10);

		TestUtils.succeed();
	}

}
