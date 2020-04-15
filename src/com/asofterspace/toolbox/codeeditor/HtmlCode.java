/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.codeeditor;

import com.asofterspace.toolbox.codeeditor.base.PublicPrivateFunctionSupplyingCode;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;


public class HtmlCode extends PublicPrivateFunctionSupplyingCode {

	private static final long serialVersionUID = 1L;

	// all keywords of the HTML language
	private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
		new String[] {"id", "class"}
	));

	// all string delimiters of the HTML language
	private static final Set<Character> STRING_DELIMITERS = new HashSet<>(Arrays.asList(
		new Character[] {'"', '\''}
	));

	// operand characters in the HTML language
	private static final Set<Character> OPERAND_CHARS = new HashSet<>(Arrays.asList(
		new Character[] {'<', '>', '=', '/'}
	));

	// start of multiline comments in the HTML language
	private static final String START_MULTILINE_COMMENT = "<!--";

	// end of multiline comments in the HTML language
	private static final String END_MULTILINE_COMMENT = "-->";

	// start of CDATA fields in the XHTML language
	private static final String START_CDATA = "<![CDATA[";

	// end of CDATA fields in the XHTML language
	private static final String END_CDATA = "]]>";

	// are we currently in a multiline comment?
	private boolean curMultilineComment;

	// are we currently in a CDATA section?
	private boolean curCDATA;

	// will we next continue with higlighting javascript inside the HTML?
	private boolean continueWithJavascript = false;

	// a java script coder used in case we encounter <script> elements with contents
	private JavaScriptCode javaScriptCoder;


	public HtmlCode(JTextPane editor) {

		super(editor);

		javaScriptCoder = new JavaScriptCode(editor, this);
	}


	public void setFontSize(int newSize) {

		super.setFontSize(newSize);

		if (javaScriptCoder != null) {
			javaScriptCoder.setFontSize(newSize);
		}
	}

	public void setLightScheme() {

		super.setLightScheme();

		if (javaScriptCoder != null) {
			javaScriptCoder.setParentScheme();
		}
	}

	public void setDarkScheme() {

		super.setDarkScheme();

		if (javaScriptCoder != null) {
			javaScriptCoder.setParentScheme();
		}
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

		functions = new ArrayList<>();

		continueWithJavascript = false;

		try {
			int end = this.getLength();

			String content = this.getText(0, end);

			// set the entire document back to regular
			this.setCharacterAttributes(0, end, attrRegular, true);

			// TODO :: actually use the start and length passed in as arguments!
			// (currently, they are just being ignored...)
			start = 0;
			end -= 1;

			while (start <= end) {

				// while we have a delimiter...
				char curChar = content.charAt(start);
				while (isDelimiter(curChar)) {

					if (continueWithJavascript && (curChar == '>')) {
						// if we have <script link="foobar.js"/>, do NOT do JS-highlighting!
						if (content.charAt(start - 1) != '/') {
							start = javaScriptCoder.highlightScript(content, start, end, functions, true);
						}
						continueWithJavascript = false;
					}

					// ... check for a comment (which starts with a delimiter)
					if (isCommentStart(content, start, end)) {
						start = highlightComment(content, start, end);

					} else if (isCdataStart(content, start, end)) {
						start = highlightCdata(content, start, end);

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
						updateFunctionList();
						return;
					}

					curChar = content.charAt(start);
				}

				// or any other token instead?
				start = highlightOther(content, start, end);
			}

		} catch (BadLocationException e) {
			// oops!
		}

		updateFunctionList();
	}

	private boolean isCommentStart(String content, int start, int end) {

		if (start + 3 > end) {
			return false;
		}

		String potentialCommentStart = content.substring(start, start + 4);

		return START_MULTILINE_COMMENT.equals(potentialCommentStart);
	}

	private int highlightComment(String content, int start, int end) {

		// apply multiline comment highlighting
		int commentEnd = content.indexOf(END_MULTILINE_COMMENT, start + 4);

		// the multiline comment has not been closed - let's comment out the rest of the document!
		if (commentEnd == -1) {
			commentEnd = end;
		} else {
			// +2 because of the length of END_MULTILINE_COMMENT itself
			commentEnd += 2;
		}

		// apply multiline comment highlighting
		this.setCharacterAttributes(start, commentEnd - start + 1, attrComment, false);

		return commentEnd;
	}

	private boolean isCdataStart(String content, int start, int end) {

		if (start + 8 > end) {
			return false;
		}

		String potentialCommentStart = content.substring(start, start + 9);

		return START_CDATA.equals(potentialCommentStart);
	}

	private int highlightCdata(String content, int start, int end) {

		// apply multiline comment highlighting
		int commentEnd = content.indexOf(END_CDATA, start + 9);

		// the multiline comment has not been closed - let's comment out the rest of the document!
		if (commentEnd == -1) {
			commentEnd = end;
		} else {
			// +2 because of the length of END_CDATA itself
			commentEnd += 2;
		}

		// apply multiline comment highlighting
		this.setCharacterAttributes(start, commentEnd - start + 1, attrData, false);

		return commentEnd;
	}

	private String lastCouldBeKeyword = "";

	protected int highlightOther(String content, int start, int end) {

		int couldBeKeywordEnd = start + 1;

		while (couldBeKeywordEnd <= end) {
			if (isDelimiter(content.charAt(couldBeKeywordEnd))) {
				break;
			}
			couldBeKeywordEnd++;
		}

		String couldBeKeyword = content.substring(start, couldBeKeywordEnd);

		boolean isKeyword = false;

		if (start > 0) {
			if ("<".equals(content.substring(start - 1, start))) {
				isKeyword = true;
			}
		}

		if (start > 1) {
			if ("</".equals(content.substring(start - 2, start))) {
				isKeyword = true;
			}
		}

		boolean isKey = false;

		if (couldBeKeywordEnd < end) {
			if ("=".equals(content.substring(couldBeKeywordEnd, couldBeKeywordEnd + 1))) {
				isKey = true;
			}
		}

		if (isKeyword) {
			this.setCharacterAttributes(start, couldBeKeywordEnd - start, attrKeyword, false);
			// <script> is a script opening tag!
			if ("script".equals(couldBeKeyword)) {
				continueWithJavascript = true;
				// but </script> is not!
				if (start > 1) {
					if ("</".equals(content.substring(start - 2, start))) {
						continueWithJavascript = false;
					}
				}
			}
		} else if (isKey) {
			if (isKeyword(couldBeKeyword)) {
				this.setCharacterAttributes(start, couldBeKeywordEnd - start, attrPrimitiveType, false);
			} else {
				this.setCharacterAttributes(start, couldBeKeywordEnd - start, attrAdvancedType, false);
			}
		} else if (isAnnotation(couldBeKeyword)) {
			this.setCharacterAttributes(start, couldBeKeywordEnd - start, attrAnnotation, false);
		}

		lastCouldBeKeyword = couldBeKeyword;

		return couldBeKeywordEnd;
	}

	private boolean isDelimiter(char character) {
		return Character.isWhitespace(character) || OPERAND_CHARS.contains(character) || STRING_DELIMITERS.contains(character);
	}

	private boolean isStringDelimiter(char character) {
		return STRING_DELIMITERS.contains(character);
	}

	private boolean isKeyword(String token) {
		return KEYWORDS.contains(token);
	}

	private boolean isAnnotation(String token) {
		return token.startsWith("@");
	}
}
