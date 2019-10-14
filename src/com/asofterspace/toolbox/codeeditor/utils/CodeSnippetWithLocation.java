/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.codeeditor.utils;


/**
 * This represents a code snippet which comes from a distinct location
 * from some other (larger) code
 */
public class CodeSnippetWithLocation {

	private String snippetCode;

	private int caretPos;


	public CodeSnippetWithLocation(String snippetCode, int caretPos) {

		this.snippetCode = snippetCode;

		this.caretPos = caretPos;
	}

	public String getCode() {

		return snippetCode;
	}

	public int getLength() {

		return snippetCode.length();
	}

	public int getCaretPos() {

		return caretPos;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (other instanceof CodeSnippetWithLocation) {
			CodeSnippetWithLocation otherLocation = (CodeSnippetWithLocation) other;
			if (otherLocation.caretPos == caretPos) {
				if (otherLocation.snippetCode == null) {
					return snippetCode == null;
				}
				return otherLocation.snippetCode.equals(snippetCode);
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return snippetCode.hashCode() + getCaretPos();
	}

}
