/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.codeeditor;

import com.asofterspace.toolbox.codeeditor.base.Code;
import com.asofterspace.toolbox.codeeditor.base.FunctionSupplyingCode;
import com.asofterspace.toolbox.codeeditor.utils.CodeSnippetWithLocation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;


public class JavaScriptCode extends FunctionSupplyingCode {

	private static final long serialVersionUID = 1L;

	// all keywords of the Java language
	private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
		new String[] {"as", "assert", "break", "case", "catch", "const", "continue", "default", "do", "else", "extends", "false", "finally", "for", "function", "goto", "if", "implements", "import", "in", "instanceof", "interface", "new", "null", "return", "super", "switch", "this", "throw", "throws", "trait", "true", "try", "typeof", "undefined", "while"}
	));

	// all primitive types of the Java language and other stuff that looks that way
	private static final Set<String> PRIMITIVE_TYPES = new HashSet<>(Arrays.asList(
		new String[] {"abstract", "boolean", "char", "class", "def", "double", "enum", "final", "int", "long", "package", "private", "protected", "public", "static", "synchronized", "var", "void", "volatile"}
	));

	// all string delimiters of the Java language
	private static final Set<Character> STRING_DELIMITERS = new HashSet<>(Arrays.asList(
		new Character[] {'"', '\''}
	));

	// operand characters in the Java language
	private static final Set<Character> OPERAND_CHARS = new HashSet<>(Arrays.asList(
		new Character[] {';', ':', '.', ',', '{', '}', '(', ')', '[', ']', '+', '-', '/', '%', '<', '=', '>', '!', '&', '|', '^', '~', '*'}
	));

	// start of single line comments in the Java language
	private static final String START_SINGLELINE_COMMENT = "//";

	// start of multiline comments in the Java language
	private static final String START_MULTILINE_COMMENT = "/*";

	// end of multiline comments in the Java language
	private static final String END_MULTILINE_COMMENT = "*/";

	// end of <script> blocks (but without the end tag, as we want to be a bit fuzzy on that!)
	private static final String END_SCRIPT_BLOCK = "</script";

	// are we currently in a multiline comment?
	private boolean curMultilineComment;


	public JavaScriptCode(JTextPane editor) {

		super(editor);
	}

	public JavaScriptCode(JTextPane editor, Code parentEditor) {

		super(editor, parentEditor);
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

		try {
			int end = this.getLength();

			String content = this.getText(0, end);

			// set the entire document back to regular
			this.setCharacterAttributes(0, end, attrRegular, true);

			// TODO :: actually use the start and length passed in as arguments!
			// (currently, they are just being ignored...)
			start = 0;
			end -= 1;

			highlightScript(content, start, end, functions, false);

		} catch (BadLocationException e) {
			// oops!
		}

		updateFunctionList();
	}

	// listenForScriptEnd .. true if we return upon finding </script>, false if not
	public int highlightScript(String content, int start, int end, List<CodeSnippetWithLocation> functions, boolean listenForScriptEnd) {

		while (start <= end) {

			// while we have a delimiter...
			char curChar = content.charAt(start);
			while (isDelimiter(curChar)) {

				if (listenForScriptEnd && isScriptEnd(content, start, end)) {
					return start;
				}

				// ... check for a comment (which starts with a delimiter)
				if (isCommentStart(content, start, end)) {

					start = highlightComment(content, start, end);

				// ... and check for a quoted string
				} else if (isStringDelimiter(content.charAt(start))) {

					// then let's get that string!
					start = highlightString(content, start, end);

				} else {
					// please highlight the delimiter in the process ;)
					if (!Character.isWhitespace(curChar)) {
						getMe().setCharacterAttributes(start, 1, attrReservedChar, false);
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

			// or any other token instead?
			start = highlightOther(content, start, end, functions);
		}

		return start;
	}

	private boolean isScriptEnd(String content, int start, int end) {

		if (start + END_SCRIPT_BLOCK.length() - 1 > end) {
			return false;
		}

		String potentialEndScriptBlock = content.substring(start, start + END_SCRIPT_BLOCK.length()).toLowerCase();

		return END_SCRIPT_BLOCK.equals(potentialEndScriptBlock);
	}

	private boolean isCommentStart(String content, int start, int end) {

		if (start + 1 > end) {
			return false;
		}

		String potentialCommentStart = content.substring(start, start + 2);

		return START_SINGLELINE_COMMENT.equals(potentialCommentStart) || START_MULTILINE_COMMENT.equals(potentialCommentStart);
	}

	private int highlightComment(String content, int start, int end) {

		String commentStart = content.substring(start, start + 2);

		if (START_SINGLELINE_COMMENT.equals(commentStart)) {

			int commentEnd = content.indexOf(EOL, start + 2) - 1;

			// this is the last line
			if (commentEnd < 0) {
				commentEnd = end;
			}

			// apply single line comment highlighting
			getMe().setCharacterAttributes(start, commentEnd - start + 1, attrComment, false);

			return commentEnd;
		}

		// apply multiline comment highlighting
		int commentEnd = content.indexOf(END_MULTILINE_COMMENT, start + 2);

		// the multiline comment has not been closed - let's comment out the rest of the document!
		if (commentEnd < 0) {
			commentEnd = end;
		} else {
			// +1 because of the length of END_MULTILINE_COMMENT itself
			commentEnd += 1;
		}

		// apply multiline comment highlighting
		getMe().setCharacterAttributes(start, commentEnd - start + 1, attrComment, false);

		return commentEnd;
	}

	private int highlightString(String content, int start, int end) {

		// get the string delimiter that was actually used to start this string (so " or ') to be able to find the matching one
		String stringDelimiter = content.substring(start, start + 1);

		// find the end of line - as we do not want to go further
		int endOfLine = content.indexOf(EOL, start + 2);

		if (endOfLine < 0) {
			endOfLine = end;
		}

		// find the matching end of string
		int endOfString = start;

		while (true) {
			endOfString = content.indexOf(stringDelimiter, endOfString + 1);

			// if the end of string is actually escaped... well, then it is not an end of string yet, continue searching!
			if ((endOfString < 0) || (content.charAt(endOfString - 1) != '\\')) {
				break;
			}
		}

		if (endOfString < 0) {
			// the string is open-ended... go for end of line
			endOfString = endOfLine;
		} else {
			// the string is not open-ended... so will the end marker or the line break be first?
			endOfString = Math.min(endOfString, endOfLine);
		}

		getMe().setCharacterAttributes(start, endOfString - start + 1, attrString, false);

		return endOfString;
	}

	private String lastCouldBeKeyword = "";

	private int highlightOther(String content, int start, int end, List<CodeSnippetWithLocation> functions) {

		int couldBeKeywordEnd = start + 1;

		while (couldBeKeywordEnd <= end) {
			if (isDelimiter(content.charAt(couldBeKeywordEnd))) {
				break;
			}
			couldBeKeywordEnd++;
		}

		String couldBeKeyword = content.substring(start, couldBeKeywordEnd);

		if (isKeyword(couldBeKeyword)) {
			getMe().setCharacterAttributes(start, couldBeKeywordEnd - start, attrKeyword, false);
		} else if (isPrimitiveType(couldBeKeyword)) {
			getMe().setCharacterAttributes(start, couldBeKeywordEnd - start, attrPrimitiveType, false);
		} else if (isAdvancedType(couldBeKeyword)) {
			getMe().setCharacterAttributes(start, couldBeKeywordEnd - start, attrAdvancedType, false);
		} else if (isAnnotation(couldBeKeyword)) {
			getMe().setCharacterAttributes(start, couldBeKeywordEnd - start, attrAnnotation, false);
		} else if ((couldBeKeywordEnd <= end) && (content.charAt(couldBeKeywordEnd) == '(')) {
			if (!"new".equals(lastCouldBeKeyword)) {
				getMe().setCharacterAttributes(start, couldBeKeywordEnd - start, attrFunction, false);
				if ((start > 0) && (content.charAt(start-1) == ' ')) {
					String functionName = lastCouldBeKeyword + " " + couldBeKeyword + "()";
					functions.add(new CodeSnippetWithLocation(functionName, getLineStartFromPosition(start, content)));
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
