/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.codeeditor;

import com.asofterspace.toolbox.codeeditor.base.Code;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;


public class MarkdownCode extends PlainText {

	private static final long serialVersionUID = 1L;

	// indicates boldness
	private static final char BOLD_INDICATOR = '*';

	// are we currently in a multiline bold area?
	private boolean curMultilineBold;

	// styles for the different kinds of text in the document


	public MarkdownCode(JTextPane editor) {

		super(editor);
	}

	// this is the main function that... well... highlights our text :)
	@Override
	protected void highlightText(int start, int length) {

		super.highlightText(start, length);

		try {
			int end = this.getLength();

			String content = this.getText(0, end);

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
						this.setCharacterAttributes(boldStart, cur - boldStart + 1, this.attrBold, false);
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
