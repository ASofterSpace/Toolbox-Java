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

		if (caretPos > getLength()) {
			caretPos = getLength();
		}

		this.caretPos = caretPos;
	}

	public String getCode() {

		return sourceCode;
	}

	public int getLength() {

		return sourceCode.length();
	}

	public int getCaretPos() {

		return caretPos;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (other instanceof CodeLocation) {
			CodeLocation otherLocation = (CodeLocation) other;
			if (otherLocation.caretPos == caretPos) {
				if (otherLocation.sourceCode == null) {
					return sourceCode == null;
				}
				return otherLocation.sourceCode.equals(sourceCode);
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return sourceCode.hashCode() + getCaretPos();
	}

}
