/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.codeeditor.utils;


public class CodeLocation {

	private String sourceCode;

	private int caretPos;


	public CodeLocation(String sourceCode, int caretPos) {

		this.sourceCode = sourceCode;

		this.caretPos = caretPos;
	}

	public String getCode() {

		return sourceCode;
	}

	public int getCaretPos() {

		return caretPos;
	}

}
