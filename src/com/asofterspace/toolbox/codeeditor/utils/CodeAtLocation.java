/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.codeeditor.utils;


/**
 * This represents an entire source code with highlight at a particular
 * location within this code - therefore, the location cannot lie outside
 * of its length
 */
public class CodeAtLocation {

	private String sourceCode;

	private int caretPos;


	public CodeAtLocation(String sourceCode, int caretPos) {

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
		if (other instanceof CodeAtLocation) {
			CodeAtLocation otherLocation = (CodeAtLocation) other;
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
