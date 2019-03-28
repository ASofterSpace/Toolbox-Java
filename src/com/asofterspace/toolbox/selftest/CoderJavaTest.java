/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.codeeditor.JavaCode;
import com.asofterspace.toolbox.test.Test;
import com.asofterspace.toolbox.test.TestUtils;

import javax.swing.JTextPane;


public class CoderJavaTest implements Test {

	@Override
	public void runAll() {

		reorganizeImports();

		functionListTest();
	}

	public void reorganizeImports() {

		TestUtils.start("Reorganize Imports in Java Code Editor");

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

		String resultStr = javaCoder.reorganizeImports(origStr);

		javaCoder.discard();

		if (resultStr.equals(targetStr)) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We attempted to reorganize the imports of a Java program - but failed! (Input: " + origStr + ", output: " + resultStr + ")");
	}

	public void functionListTest() {

		TestUtils.start("Function List in Java Code Editor");

		JTextPane editor = new JTextPane();
		JTextPane functionPane = new JTextPane();

		JavaCode javaCoder = new JavaCode(editor);
		javaCoder.setFunctionTextField(functionPane);

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
				"public class Meow {\n" +
				"\n" +
				"	public Meow() {\n" +
				"	}\n" +
				"\n" +
				"	public static Foo schluddelwudd(Bar bar) {\n" +
				"	}\n" +
				"\n" +
				"	static public Fee schleddelwedd(Ber ber) {\n" +
				"	}\n" +
				"\n" +
				"	private String blubb() {\n" +
				"	}\n" +
				"\n" +
				"	private void blebb() {\n" +
				"	}\n" +
				"\n" +
				"	protected int blöbb() {\n" +
				"	}\n" +
				"\n" +
				"	List<String> foobar(bar foo) {\n" +
				"		barfoo = new Meow();\n" +
				"	}\n" +
				"}";

		String targetStr = "public:\n" +
				"static Fee schleddelwedd(Ber ber)\n" +
				"static Foo schluddelwudd(Bar bar)\n" +
				"\n" +
				"protected:\n" +
				"int blöbb()\n" +
				"\n" +
				"package-private:\n" +
				"List<String> foobar(bar foo)\n" +
				"\n" +
				"private:\n" +
				"void blebb()\n" +
				"String blubb()";

		editor.setText(origStr);

		// instead of waiting for maybe too short a time...
		/*
		try {
			// await the text update
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			TestUtils.fail("We got interrupted!");
			return;
		}
		*/

		// ... just call the highlighter synchronously!
		javaCoder.highlightAllTextNow();

		String resultStr = functionPane.getText().trim();

		javaCoder.discard();

		if (resultStr.equals(targetStr)) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We attempted to read out the function list of a Java program - but failed! (Input: " + origStr + ", output: " + resultStr + ")");
	}
}
