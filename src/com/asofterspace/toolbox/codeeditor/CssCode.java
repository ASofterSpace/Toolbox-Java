/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.codeeditor;

import com.asofterspace.toolbox.codeeditor.base.Code;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;


public class CssCode extends Code {

	private static final long serialVersionUID = 1L;

	// all string delimiters of the CSS language
	private static final Set<Character> STRING_DELIMITERS = new HashSet<>(Arrays.asList(
		new Character[] {'"', '\''}
	));

	// operand characters in the CSS language
	private static final Set<Character> OPERAND_CHARS = new HashSet<>(Arrays.asList(
		new Character[] {';', ':', '.', ',', '{', '}', '(', ')', '[', ']', '+', '-', '/', '%', '<', '=', '>', '!', '&', '|', '^', '~', '*', '#'}
	));

	// start of multiline comments in the CSS language
	private static final String START_MULTILINE_COMMENT = "/*";

	// end of multiline comments in the CSS language
	private static final String END_MULTILINE_COMMENT = "*/";

	// are we currently in a multiline comment?
	private boolean curMultilineComment;


	public CssCode(JTextPane editor) {

		super(editor);
	}

	@Override
	public void insertString(int offset, String insertedString, AttributeSet attrs) {

		int overrideCaretPos = insertedString.length();

		// automagically close brackets that are being opened
		switch (insertedString) {
			case "{":
				insertedString = "{}";
				overrideCaretPos = 1;
				break;
			case "(":
				insertedString = "()";
				overrideCaretPos = 1;
				break;
			case "[":
				insertedString = "[]";
				overrideCaretPos = 1;
				break;
			case "\"":
				insertedString = "\"\"";
				overrideCaretPos = 1;
				break;
			case "'":
				// replace ' with '' in code - but not in comment mode!
				if (attrComment.equals(attrs)) {
					break;
				}
				insertedString = "''";
				overrideCaretPos = 1;
				break;
		}

		super.insertString(offset, insertedString, attrs, overrideCaretPos);
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
			end -= 1;

			char lastDelimiter = ' ';

			while (start <= end) {

				// while we have a delimiter...
				char curChar = content.charAt(start);
				while (isDelimiter(curChar)) {

					lastDelimiter = curChar;

					// ... check for the start of an object
					if (curChar == '{') {
						start = highlightObject(content, start, end);

					// ... check for a comment (which starts with a delimiter)
					} else if (isCommentStart(content, start, end)) {
						start = highlightComment(content, start, end);

					// ... and check for a quoted string
					} else if (isStringDelimiter(content.charAt(start))) {

						// then let's get that string!
						start = highlightString(content, start, end);

					} else {
						// please highlight the delimiter in the process ;)
						if (!Character.isWhitespace(curChar)) {
							this.setCharacterAttributes(start, 1, attrReservedChar, false);
						}
					}

					if (start < end) {

						// jump forward and try again!
						start++;

					} else {
						return;
					}

					curChar = content.charAt(start);
				}

				// or any other token instead?
				start = highlightOther(content, start, end, lastDelimiter);
			}

		} catch (BadLocationException e) {
			// oops!
		}
	}

	private int highlightObject(String content, int start, int end) {

		while (start <= end) {

			// while we have a delimiter...
			char curChar = content.charAt(start);
			while (true) {

				if (isDelimiter(curChar)) {
					// ... check for the end of the object
					if (curChar == '}') {
						return start + 1;

					// ... check for a comment (which starts with a delimiter)
					} else if (isCommentStart(content, start, end)) {
						start = highlightComment(content, start, end);

					// ... and check for a quoted string
					} else if (isStringDelimiter(content.charAt(start))) {

						// then let's get that string!
						start = highlightString(content, start, end);

					} else {
						// please highlight the delimiter in the process ;)
						if (!Character.isWhitespace(curChar)) {
							this.setCharacterAttributes(start, 1, attrReservedChar, false);
						}
					}
				} else {
					int keyEnd = content.indexOf(":", start);
					int markLength = keyEnd - start;
					if (markLength > 0) {
						this.setCharacterAttributes(start, markLength, attrFunction, false);
					}
					if (keyEnd > 0) {
						keyEnd = content.indexOf(";", keyEnd);
						start = keyEnd - 1;
					}
				}

				if (start < end) {

					// jump forward and try again!
					start++;

				} else {
					return start;
				}

				curChar = content.charAt(start);
			}
		}

		return start;
	}

	private boolean isCommentStart(String content, int start, int end) {

		if (start + 1 > end) {
			return false;
		}

		return START_MULTILINE_COMMENT.equals(content.substring(start, start + 2));
	}

	private int highlightComment(String content, int start, int end) {

		String commentStart = content.substring(start, start + 2);

		// apply multiline comment highlighting
		int commentEnd = content.indexOf(END_MULTILINE_COMMENT, start + 2);

		// the multiline comment has not been closed - let's comment out the rest of the document!
		if (commentEnd == -1) {
			commentEnd = end;
		} else {
			// +1 because of the length of END_MULTILINE_COMMENT itself
			commentEnd += 1;
		}

		// apply multiline comment highlighting
		this.setCharacterAttributes(start, commentEnd - start + 1, attrComment, false);

		return commentEnd;
	}

	private int highlightOther(String content, int start, int end, char lastDelimiter) {

		int couldBeKeywordEnd = start + 1;

		while (couldBeKeywordEnd <= end) {
			if (isDelimiter(content.charAt(couldBeKeywordEnd))) {
				break;
			}
			couldBeKeywordEnd++;
		}

		String couldBeKeyword = content.substring(start, couldBeKeywordEnd);

		if (lastDelimiter == '#') {
			this.setCharacterAttributes(start, couldBeKeywordEnd - start, attrKeyword, false);
		} else if (lastDelimiter == '.') {
			this.setCharacterAttributes(start, couldBeKeywordEnd - start, attrPrimitiveType, false);
		} else if (lastDelimiter == ':') {
			this.setCharacterAttributes(start, couldBeKeywordEnd - start, attrAdvancedType, false);
		} else if (isAnnotation(couldBeKeyword)) {
			this.setCharacterAttributes(start, couldBeKeywordEnd - start, attrAnnotation, false);
		}

		return couldBeKeywordEnd;
	}

	private boolean isDelimiter(char character) {
		return Character.isWhitespace(character) || OPERAND_CHARS.contains(character) || STRING_DELIMITERS.contains(character);
	}

	private boolean isStringDelimiter(char character) {
		return STRING_DELIMITERS.contains(character);
	}

	private boolean isAnnotation(String token) {
		return token.startsWith("@");
	}
}
