/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.codeeditor.JavaCode;
import com.asofterspace.toolbox.test.Test;
import com.asofterspace.toolbox.test.TestUtils;


public class CoderJavaTest implements Test {

	@Override
	public void runAll() {

		reorganizeImports();
	}

	public void reorganizeImports() {

		TestUtils.start("Reorganize Imports in Java Codeeditor");

		JavaCode javaCoder = new JavaCode(null);

		String origStr = "blubb\n" +
				"meowino\n" +
				"calacat\n" +
				"\n" +
				"import stargarr.fofarr.codd\n" +
				"import foo.blubb.two;\n" +
				"import foo.bar.one;\n" +
				"import foo.bar.one;\n" +
				"\n" +
				"import alcatar.blebb\n" +
				"import foo.bar.one;\n" +
				"import stargarr.fofarr.blobb\n" +
				"import foo.bar.one;\n" +
				"\n" +
				"blebbel\n" +
				"blööö\n" +
				"alodalo";

		String targetStr = "blubb\n" +
				"meowino\n" +
				"calacat\n" +
				"\n" +
				"import alcatar.blebb\n" +
				"\n" +
				"import foo.bar.one;\n" +
				"import foo.blubb.two;\n" +
				"\n" +
				"import stargarr.fofarr.blobb\n" +
				"import stargarr.fofarr.codd\n" +
				"\n" +
				"\n" +
				"blebbel\n" +
				"blööö\n" +
				"alodalo";

		String resultStr = javaCoder.reorganizeImportsStr(origStr);

		if (resultStr.equals(targetStr)) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We attempted to reorganize the imports of a Java program - but failed! (Input: " + origStr + ", output: " + resultStr + ")");
	}
}
