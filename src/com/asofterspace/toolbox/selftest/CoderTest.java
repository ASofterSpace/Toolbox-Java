/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.codeeditor.base.Code;
import com.asofterspace.toolbox.codeeditor.JavaCode;
import com.asofterspace.toolbox.test.Test;
import com.asofterspace.toolbox.test.TestUtils;

import javax.swing.JTextPane;


public class CoderTest implements Test {

	@Override
	public void runAll() {

		getLineFromPositionTest();

		getWordFromPositionTest();
	}

	public void getLineFromPositionTest() {

		TestUtils.start("Get Line from Position");

		String line = Code.getLineFromPosition(14, "  MOV AX, BX\n  MOV BX, CX\nRET");

		if (!line.equals("  MOV BX, CX")) {
			TestUtils.fail("We wanted to get the line MOV BX, CX, but instead got " + line + "!");
			return;
		}

		TestUtils.succeed();
	}

	public void getWordFromPositionTest() {

		TestUtils.start("Get Word from Position");

		String word = Code.getWordFromPosition(10, "    this.someObj := 27398;");

		if (!word.equals("someObj")) {
			TestUtils.fail("We wanted to get the word someObj, but instead got " + word + "!");
			return;
		}

		TestUtils.succeed();
	}
}
