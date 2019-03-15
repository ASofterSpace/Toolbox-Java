/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.codeeditor;

import com.asofterspace.toolbox.codeeditor.base.FunctionSupplyingCode;
import com.asofterspace.toolbox.codeeditor.utils.CodeLocation;
import com.asofterspace.toolbox.utils.Callback;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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


public class PhpCode extends HtmlCode {

	private static final long serialVersionUID = 1L;

	private static final String PHP_START = "<?php";

	private static final String PHP_END = "?>";

	// all keywords of the PHP language
	private static final Set<String> PHP_KEYWORDS = new HashSet<>(Arrays.asList(
		new String[] { "abstract", "as", "assert", "break", "case", "catch", "const", "continue", "def", "default", "do", "else", "extends", "final", "finally", "for", "goto", "if", "implements", "import", "in", "instanceof", "interface", "new", "package", "private", "protected", "public", "require_once", "return", "static", "switch", "synchronized", "throw", "throws", "trait", "try", "use", "while", "volatile"}
	));

	// all primitive types of the PHP language and other stuff that looks that way
	private static final Set<String> PHP_PRIMITIVE_TYPES = new HashSet<>(Arrays.asList(
		new String[] {"boolean", "byte", "char", "class", "double", "enum", "false", "float", "function", "int", "long", "null", "super", "this", "true", "void"}
	));

	// all string delimiters of the PHP language
	private static final Set<Character> PHP_STRING_DELIMITERS = new HashSet<>(Arrays.asList(
		new Character[] {'"', '\''}
	));

	// operand characters in the PHP language
	private static final Set<Character> PHP_OPERAND_CHARS = new HashSet<>(Arrays.asList(
		new Character[] {';', ':', '.', ',', '{', '}', '(', ')', '[', ']', '+', '-', '/', '%', '<', '=', '>', '!', '?', '&', '|', '^', '~', '*'}
	));

	// start of single line comments in the PHP language
	private static final String PHP_START_SINGLELINE_COMMENT = "//";

	// start of multiline comments in the PHP language
	private static final String PHP_START_MULTILINE_COMMENT = "/*";

	// end of multiline comments in the PHP language
	private static final String PHP_END_MULTILINE_COMMENT = "*/";

	private int curLineStartingWhitespacePhp = 0;

	private boolean startingWhitespacePhp = false;

	private String lastCouldBeKeywordPhp = "";


	public PhpCode(JTextPane editor) {

		super(editor);
	}

	@Override
	public boolean suppliesFunctions() {
		return true;
	}

	/**
	 * In the highlight other function of the HTML Code (in which tokens are highlighted, such as
	 * <div), add awareness for the PHP Start (<?php), which then initiates PHP-specific highlighting
	 * inside
	 */
	@Override
	protected int highlightOther(String content, int start, int end) {

		if ((start > 0) && (start + PHP_START.length() < end)) {
			if (PHP_START.equals(content.substring(start - 1, start - 1 + PHP_START.length()))) {

				this.setCharacterAttributes(start - 1, PHP_START.length(), attrKeyword, false);

				return highlightPhp(content, start, end);
			}
		}

		return super.highlightOther(content, start, end);
	}

	private int highlightPhp(String content, int start, int end) {

		while (start <= end) {

			// while we have a delimiter...
			char curChar = content.charAt(start);

			startingWhitespacePhp = false;

			while (isPhpDelimiter(curChar)) {

				// prevent stuff like blubb = foo() from ending up in the function overview list
				if (curChar == '=') {
					lastCouldBeKeywordPhp = "";
				}

				if (curChar == '\n') {
					curLineStartingWhitespacePhp = 0;
					startingWhitespacePhp = true;
				} else {
					if (startingWhitespacePhp) {
						if (curChar == '\t') {
							curLineStartingWhitespacePhp += 4;
						} else {
							curLineStartingWhitespacePhp++;
						}
					}
				}

				if (isPhpEnd(content, start, end)) {
					return highlightPhpEnd(content, start, end);

				// ... check for a comment (which starts with a delimiter)
				} else if (isPhpCommentStart(content, start, end)) {
					start = highlightPhpComment(content, start, end);

				// ... and check for a quoted string
				} else if (isPhpStringDelimiter(content.charAt(start))) {

					// then let's get that string!
					start = highlightPhpString(content, start, end);

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

					return end;
				}

				curChar = content.charAt(start);
			}

			// or any other token instead?
			start = highlightPhpOther(content, start, end);
		}

		return end;
	}

	private boolean isPhpEnd(String content, int start, int end) {

		if (start + 1 > end) {
			return false;
		}

		String potentialPhpEnd = content.substring(start, start + 2);

		return PHP_END.equals(potentialPhpEnd);
	}

	private int highlightPhpEnd(String content, int start, int end) {

		this.setCharacterAttributes(start, 2, attrKeyword, false);

		return start + 2;
	}

	private boolean isPhpCommentStart(String content, int start, int end) {

		if (start + 1 > end) {
			return false;
		}

		String potentialCommentStart = content.substring(start, start + 2);

		return PHP_START_SINGLELINE_COMMENT.equals(potentialCommentStart) || PHP_START_MULTILINE_COMMENT.equals(potentialCommentStart);
	}

	private int highlightPhpComment(String content, int start, int end) {

		String commentStart = content.substring(start, start + 2);

		if (PHP_START_SINGLELINE_COMMENT.equals(commentStart)) {

			int commentEnd = content.indexOf(EOL, start + 2) - 1;

			// this is the last line
			if (commentEnd == -2) {
				commentEnd = end;
			}

			// apply single line comment highlighting
			this.setCharacterAttributes(start, commentEnd - start + 1, attrComment, false);

			return commentEnd;
		}

		// apply multiline comment highlighting
		int commentEnd = content.indexOf(PHP_END_MULTILINE_COMMENT, start + 2);

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

	private int highlightPhpString(String content, int start, int end) {

		// get the string delimiter that was actually used to start this string (so " or ') to be able to find the matching one
		String stringDelimiter = content.substring(start, start + 1);

		// find the end of text - as we do not want to go further
		int endOfText = end;

		// find the matching end of string
		int endOfString = start;

		while (true) {
			endOfString = content.indexOf(stringDelimiter, endOfString + 1);

			// if the end of string is actually escaped... well, then it is not an end of string yet, continue searching!
			if ((endOfString == -1) || (content.charAt(endOfString - 1) != '\\')) {
				break;
			}
		}

		if (endOfString == -1) {
			// the string is open-ended... go for end of text
			endOfString = endOfText;
		} else {
			// the string is not open-ended... so will the end marker or the line break be first?
			endOfString = Math.min(endOfString, endOfText);
		}

		this.setCharacterAttributes(start, endOfString - start + 1, attrString, false);

		return endOfString;
	}

	private int highlightPhpOther(String content, int start, int end) {

		int couldBeKeywordEnd = start + 1;

		while (couldBeKeywordEnd <= end) {
			if (isPhpDelimiter(content.charAt(couldBeKeywordEnd))) {
				break;
			}
			couldBeKeywordEnd++;
		}

		String couldBeKeyword = content.substring(start, couldBeKeywordEnd);

		if (isPhpKeyword(couldBeKeyword)) {
			this.setCharacterAttributes(start, couldBeKeywordEnd - start, attrKeyword, false);
		} else if (isPhpPrimitiveType(couldBeKeyword)) {
			this.setCharacterAttributes(start, couldBeKeywordEnd - start, attrPrimitiveType, false);
		} else if (isPhpAdvancedType(couldBeKeyword)) {
			this.setCharacterAttributes(start, couldBeKeywordEnd - start, attrAdvancedType, false);
		} else if (isPhpAnnotation(couldBeKeyword)) {
			this.setCharacterAttributes(start, couldBeKeywordEnd - start, attrAnnotation, false);
		} else if ((couldBeKeywordEnd <= end) && (content.charAt(couldBeKeywordEnd) == '(')) {
			if (!"new".equals(lastCouldBeKeywordPhp)) {
				this.setCharacterAttributes(start, couldBeKeywordEnd - start, attrFunction, false);
				if ((start > 0) && (content.charAt(start-1) == ' ')) {
					// ignore lines with more than 1 tab indent / 4 regular indents and line without "function" name
					if ((curLineStartingWhitespacePhp < 5) && "function".equals(lastCouldBeKeywordPhp)) {
						// now get the entire line that we found!
						// String functionName = lastCouldBeKeywordPhp + " " + couldBeKeyword + "()";
						String functionName = getLineFromPosition(start, content);
						functions.add(new CodeLocation(functionName, start));
					}
				}
			}
		}

		lastCouldBeKeywordPhp = couldBeKeyword;

		return couldBeKeywordEnd;
	}

	private boolean isPhpDelimiter(char character) {
		return Character.isWhitespace(character) || PHP_OPERAND_CHARS.contains(character) || PHP_STRING_DELIMITERS.contains(character);
	}

	private boolean isPhpStringDelimiter(char character) {
		return PHP_STRING_DELIMITERS.contains(character);
	}

	private boolean isPhpKeyword(String token) {
		return PHP_KEYWORDS.contains(token);
	}

	private boolean isPhpPrimitiveType(String token) {
		return PHP_PRIMITIVE_TYPES.contains(token);
	}

	private boolean isPhpAdvancedType(String token) {
		if (token.length() < 1) {
			return false;
		}
		return (token.charAt(0) == '$') || token.charAt(0) == '&';
	}

	private boolean isPhpAnnotation(String token) {
		return token.startsWith("@");
	}

}
