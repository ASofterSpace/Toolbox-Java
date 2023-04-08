/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.codeeditor;

import com.asofterspace.toolbox.codeeditor.base.Code;

import java.util.Set;

import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;


public class PlainText extends Code {

	private static final long serialVersionUID = 1L;


	public PlainText(JTextPane editor) {
		super(editor);
	}

	@Override
	public void insertString(int offset, String insertedString, AttributeSet attrs) {

		int origCaretPos = decoratedEditor.getCaretPosition();

		boolean overrideCaretPos = false;

		// automagically close brackets that are being opened
		switch (insertedString) {
			case "{":
				insertedString = "{}";
				overrideCaretPos = true;
				break;
			case "(":
				insertedString = "()";
				overrideCaretPos = true;
				break;
			case "[":
				insertedString = "[]";
				overrideCaretPos = true;
				break;
		}

		super.insertString(offset, insertedString, attrs);

		highlightText(offset, insertedString.length());

		if (overrideCaretPos) {
			decoratedEditor.setCaretPosition(origCaretPos + 1);
		}
	}

	// this is the main function that... well... highlights our text :)
	@Override
	protected void highlightText(int start, int length) {

		super.highlightText(start, length);

		int end = this.getLength();
		this.setCharacterAttributes(0, end, this.attrBold, false);
	}
}
