/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.codeeditor.base.Code;
import com.asofterspace.toolbox.codeeditor.base.PublicPrivateFunctionSupplyingCode;
import com.asofterspace.toolbox.codeeditor.JavaCode;
import com.asofterspace.toolbox.codeeditor.utils.CodeField;
import com.asofterspace.toolbox.test.Test;
import com.asofterspace.toolbox.test.TestUtils;

import java.util.List;

import javax.swing.JTextPane;


public class CoderJavaTest implements Test {

	@Override
	public void runAll() {

		reorganizeImportsTest();

		removeUnusedImportsTest();

		functionListTest();

		removeCommentsAndStringsTest();

		getFieldsTest();
	}

	public void reorganizeImportsTest() {

		TestUtils.start("Reorganize Imports in Java Code Editor");

		JavaCode javaCoder = new JavaCode(null);

		String origStr = "blubb\n" +
				"meowino\n" +
				"calacat\n" +
				"\n" +
				"\n" +
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

	public void removeUnusedImportsTest() {

		TestUtils.start("Remove Unused Imports in Java Code Editor");

		JavaCode javaCoder = new JavaCode(null);

		String origStr = "blubb\n" +
				"meowino\n" +
				"calacat\n" +
				"\n" +
				"\n" +
				"\n" +
				"import stargarr.fofarr.codd\n" +
				"import foo.blubb.Alodalo;\n" +
				"import foo.bar.one;\n" +
				"import foo.bar.one;\n" +
				"import foo.bar.schlebb.*;\n" +
				"\n" +
				"import alcatar.blööö;\n" +
				"import foo.bar.one;\n" +
				"import stargarr.fofarr.Blebbel\n" +
				"import foo.bar.one;\n" +
				"\n" +
				"private void blu (Blebbel bleb) {}\n" +
				"blööö\n" +
				"GenericDalo dalo = new Alodalo();";

		String targetStr = "blubb\n" +
				"meowino\n" +
				"calacat\n" +
				"\n" +
				"\n" +
				"\n" +
				"import foo.blubb.Alodalo;\n" +
				"import foo.bar.schlebb.*;\n" +
				"\n" +
				"import alcatar.blööö;\n" +
				"import stargarr.fofarr.Blebbel\n" +
				"\n" +
				"private void blu (Blebbel bleb) {}\n" +
				"blööö\n" +
				"GenericDalo dalo = new Alodalo();";

		String resultStr = javaCoder.removeUnusedImports(origStr);

		javaCoder.discard();

		if (resultStr.equals(targetStr)) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We attempted to remove the unused imports of a Java program - but failed! (Input: " + origStr + ", output: " + resultStr + ")");
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
				"	public Fee schladdlwadd(Ber ber) {\n" +
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
				"Fee schladdlwadd(Ber ber)\n" +
				"\n" +
				"public" + PublicPrivateFunctionSupplyingCode.SPACE + "static:\n" +
				"Fee schleddelwedd(Ber ber)\n" +
				"Foo schluddelwudd(Bar bar)\n" +
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

	private void removeCommentsAndStringsTest() {

		TestUtils.start("Remove Comments and Strings in Java Code Editor");

		JTextPane editor = new JTextPane();
		JavaCode javaCode = new JavaCode(editor);

		String content =
			"/** foo bla\n" +
			" * lorem\n" +
			" * ipsum\n" +
			" */\n" +
			"package foo.bla;\n" +
			"\n" +
			"import something.someother.Thing;\n" +
			"\n" +
			"\n" +
			"// This class does something!\n" +
			"public class SomethingDoingClass {\n" +
			"\n" +
			"	// some Thing!\n" +
			"	private Thing some = new Thing();\n" +
			"\n" +
			"	private String description = \"blubb\";\n" +
			"\n" +
			"	private char descriptionChar = '?';\n" +
			"\n" +
			"}";

		String target =
			"\n" +
			"package foo.bla;\n" +
			"\n" +
			"import something.someother.Thing;\n" +
			"\n" +
			"\n" +
			"\n" +
			"public class SomethingDoingClass {\n" +
			"\n" +
			"	\n" +
			"	private Thing some = new Thing();\n" +
			"\n" +
			"	private String description = ;\n" +
			"\n" +
			"	private char descriptionChar = ;\n" +
			"\n" +
			"}";

		String removed = javaCode.removeCommentsAndStrings(content);

		if (target.equals(removed)) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We attempted to remove comments and strings from some java source code, but they were not properly removed!\n" +
			"Original source code:\n\n" + content +
			"\n\nResult:\n\n" + removed +
			"\n\nIntended result:\n\n" + target);
	}

	private void getFieldsTest() {

		TestUtils.start("Get Fields");

		JavaCode javaCode = new JavaCode(null);

		String content =
			"private String blubb;\n" +
			"\n" +
			"    private int bli;\n" +
			"\n" +
			"Foo bar = new Foo();\n" +
			"\n" +
			"\t\tpublic static char Chara;\n" +
			"\n" +
			"\t\tprivate final static Foo<Foo, Bar> bar2 = new Foo<Foo, Bar>();";

		List<CodeField> fields = javaCode.getFields(content);

		if (fields.size() != 5) {
			TestUtils.fail("We expected 5 fields, but we got " + fields.size() + "!");
			return;
		}

		checkFieldTest(fields, 0, "blubb", "Blubb", "String");
		checkFieldTest(fields, 1, "bli", "Bli", "int");
		checkFieldTest(fields, 2, "bar", "Bar", "Foo");
		checkFieldTest(fields, 3, "Chara", "Chara", "char");
		checkFieldTest(fields, 4, "bar2", "Bar2", "Foo<Foo, Bar>");

		TestUtils.succeed();
	}

	private void checkFieldTest(List<CodeField> fields, int index, String name, String nameUpCase, String type) {

		if (!name.equals(fields.get(index).getName())) {
			TestUtils.fail("We expected field " + index + " to have the name " + name +
				" but it is actually " + fields.get(index).getName() + "!");
		}

		if (!nameUpCase.equals(fields.get(index).getNameUpcase())) {
			TestUtils.fail("We expected field " + index + " to have the up-case name " + nameUpCase +
				" but it is actually " + fields.get(index).getNameUpcase() + "!");
		}

		if (!type.equals(fields.get(index).getType())) {
			TestUtils.fail("We expected field " + index + " to have the type " + type +
				" but it is actually " + fields.get(index).getType() + "!");
		}
	}
}
