/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.codeeditor;

import com.asofterspace.toolbox.codeeditor.base.Code;
import com.asofterspace.toolbox.codeeditor.base.FunctionSupplyingCode;
import com.asofterspace.toolbox.codeeditor.utils.CodeSnippetWithLocation;
import com.asofterspace.toolbox.utils.StrUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;


public class ShellCode extends FunctionSupplyingCode {

	private static final long serialVersionUID = 1L;

	// all keywords of the sh language
	private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
		new String[] {"break", "case", "do", "done", "elif", "else", "esac", "fi", "for", "function", "if", "in", "then", "until", "while"}
	));

	// all primitive types of the sh language and other stuff that looks that way
	private static final Set<String> PRIMITIVE_TYPES = new HashSet<>(Arrays.asList(
		new String[] {"cd", "echo", "exit", "sleep"}
	));

	// all string delimiters of the sh language
	private static final Set<Character> STRING_DELIMITERS = new HashSet<>(Arrays.asList(
		new Character[] {'"', '\''}
	));

	// operand characters in the sh language
	private static final Set<Character> OPERAND_CHARS = new HashSet<>(Arrays.asList(
		new Character[] {';', ':', '.', ',', '{', '}', '(', ')', '[', ']', '+', '-', '/', '%', '<', '=', '>', '!', '&', '|', '^', '~', '*', '#', '$'}
	));

	// start of single line comments in the sh language
	private static final String START_SINGLELINE_COMMENT = "#";

	// are we currently in a multiline comment?
	private boolean curMultilineComment;

	private String lastCouldBeKeyword = "";


	public ShellCode(JTextPane editor) {

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

		super.highlightText(start, length);

		try {
			int end = this.getLength();

			String content = this.getText(0, end);

			// TODO :: actually use the start and length passed in as arguments!
			// (currently, they are just being ignored...)
			start = 0;
			end -= 1;

			while (start <= end) {

				// while we have a delimiter...
				char curChar = content.charAt(start);
				while (isDelimiter(curChar)) {

					// ... check for a comment (which starts with a delimiter)
					if (isCommentStart(content, start, end)) {

						start = highlightComment(content, start, end);

					// ... and check for a quoted string
					} else if (isStringDelimiter(content.charAt(start))) {

						// then let's get that string!
						boolean singleForMultiline = false;
						boolean threeForMultiline = false;
						start = highlightString(content, start, end, singleForMultiline, threeForMultiline);

					} else {

						// please highlight the delimiter in the process ;)
						if (!Character.isWhitespace(curChar)) {
							this.setCharacterAttributes(start, 1, this.attrReservedChar, false);
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
				start = highlightOther(content, start, end, true);
			}

		} catch (BadLocationException e) {
			// oops!
		}

		updateFunctionList();
	}

	private boolean isCommentStart(String content, int start, int end) {

		// but $# is NOT a comment!
		if (start > 0) {
			if ("$".equals(content.substring(start - 1, start))) {
				return false;
			}
		}

		if (start > end) {
			return false;
		}

		// everything after # is a comment
		return START_SINGLELINE_COMMENT.equals(content.substring(start, start + 1));
	}

	private int highlightComment(String content, int start, int end) {

		int commentEnd = content.indexOf(EOL, start + 1) - 1;

		// this is the last line
		if (commentEnd == -2) {
			commentEnd = end;
		}

		// apply single line comment highlighting
		this.setCharacterAttributes(start, commentEnd - start + 1, this.attrComment, false);

		return commentEnd;
	}

	private int highlightOther(String content, int start, int end, boolean setAttributesAndDetectFunctions) {

		int couldBeKeywordEnd = start + 1;

		while (couldBeKeywordEnd <= end) {
			if (isDelimiter(content.charAt(couldBeKeywordEnd))) {
				break;
			}
			couldBeKeywordEnd++;
		}

		String couldBeKeyword = content.substring(start, couldBeKeywordEnd);

		if (setAttributesAndDetectFunctions) {
			if (isKeyword(couldBeKeyword)) {
				this.setCharacterAttributes(start, couldBeKeywordEnd - start, this.attrKeyword, false);
			} else if (isPrimitiveType(couldBeKeyword)) {
				this.setCharacterAttributes(start, couldBeKeywordEnd - start, this.attrPrimitiveType, false);
			} else if (isAdvancedType(couldBeKeyword)) {
				this.setCharacterAttributes(start, couldBeKeywordEnd - start, this.attrAdvancedType, false);
			} else if (isAnnotation(couldBeKeyword)) {
				this.setCharacterAttributes(start, couldBeKeywordEnd - start, this.attrAnnotation, false);
			} else if ((couldBeKeywordEnd+1 <= end) && (content.charAt(couldBeKeywordEnd) == ' ') && (content.charAt(couldBeKeywordEnd+1) == '{')) {
				if ("function".equals(lastCouldBeKeyword)) {
					this.setCharacterAttributes(start, couldBeKeywordEnd - start, this.attrFunction, false);
					// now get the entire line that we found!
					String functionName = StrUtils.getLineFromPosition(start, content).trim();
					// get rid of the opening {
					functionName = functionName.substring(0, functionName.length() - 1).trim();
					functions.add(new CodeSnippetWithLocation(functionName, StrUtils.getLineStartFromPosition(start, content)));
				}
			}
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

	private boolean isPrimitiveType(String token) {
		return PRIMITIVE_TYPES.contains(token);
	}

	private boolean isAdvancedType(String token) {
		if (token.length() < 1) {
			return false;
		}
		return Character.isUpperCase(token.charAt(0));
	}

	private boolean isAnnotation(String token) {
		return token.startsWith("@");
	}
}
