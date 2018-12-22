/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.codeeditor;


public class CodeLocation {

	private String sourceCode;

	private int caretPos;


	public CodeLocation(String sourceCode, int caretPos) {

		this.sourceCode = sourceCode;

		this.caretPos = caretPos;
	}

}
