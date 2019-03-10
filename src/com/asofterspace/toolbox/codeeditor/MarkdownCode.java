/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.codeeditor;

import com.asofterspace.toolbox.codeeditor.base.Code;
import com.asofterspace.toolbox.utils.Callback;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.event.DocumentEvent;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;


public class MarkdownCode extends Code {

	private static final long serialVersionUID = 1L;

	// indicates boldness
	private static final char BOLD_INDICATOR = '*';

	// are we currently in a multiline bold area?
	private boolean curMultilineBold;

	// styles for the different kinds of text in the document
	private MutableAttributeSet attrBold;


	public MarkdownCode(JTextPane editor) {

		super(editor);
	}

	@Override
	public void setLightScheme() {

		// change the attribute
		attrBold = new SimpleAttributeSet();
		StyleConstants.setForeground(attrBold, new Color(0, 0, 0));
		StyleConstants.setBold(attrBold, true);

		super.setLightScheme();
	}

	@Override
	public void setDarkScheme() {

		// change the attribute sets
		attrBold = new SimpleAttributeSet();
		StyleConstants.setForeground(attrBold, new Color(255, 255, 255));
		StyleConstants.setBold(attrBold, true);

		super.setDarkScheme();
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

		try {
			int end = this.getLength();

			String content = this.getText(0, end);

			// set the entire document back to regular
			this.setCharacterAttributes(0, end, attrRegular, true);

			// TODO :: actually use the start and length passed in as arguments!
			// (currently, they are just being ignored...)
			start = 0;
			int cur = start;
			end -= 1;

			Integer boldStart = null;

			while (cur <= end) {

				if (BOLD_INDICATOR == content.charAt(cur)) {
					if (boldStart == null) {
						boldStart = cur;
					} else {
						this.setCharacterAttributes(boldStart, cur - boldStart + 1, attrBold, false);
						boldStart = null;
					}
				}

				cur++;
			}

		} catch (BadLocationException e) {
			// oops!
		}
	}
}
