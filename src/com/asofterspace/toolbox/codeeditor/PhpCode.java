/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.codeeditor;

import com.asofterspace.toolbox.codeeditor.base.Code;
import com.asofterspace.toolbox.codeeditor.utils.CodeSnippetWithLocation;
import com.asofterspace.toolbox.utils.StrUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JTextPane;


public class PhpCode extends HtmlCode {

	private static final long serialVersionUID = 1L;

	private static final String PHP_START = "<?php";

	private static final String PHP_END = "?>";

	// all keywords of the PHP language
	private static final Set<String> PHP_KEYWORDS = new HashSet<>(Arrays.asList(
		new String[] {"abstract", "and", "as", "assert", "break", "case", "catch", "continue", "count", "def", "default", "do", "else", "elseif", "extends", "final", "finally", "for", "foreach", "goto", "if", "implements", "import", "in", "instanceof", "interface", "isset", "namespace", "new", "or", "package", "private", "protected", "public", "require_once", "return", "static", "substr", "switch", "synchronized", "throw", "throws", "trait", "try", "use", "volatile", "while"}
	));

	// all primitive types of the PHP language and other stuff that looks that way
	private static final Set<String> PHP_PRIMITIVE_TYPES = new HashSet<>(Arrays.asList(
		new String[] {"$this", "boolean", "byte", "char", "class", "const", "double", "enum", "false", "float", "function", "int", "long", "null", "true", "var", "void"}
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

	/**
	 * In the highlight other function of the HTML Code (in which tokens are highlighted, such as
	 * <div), add awareness for the PHP Start (<?php), which then initiates PHP-specific highlighting
	 * inside
	 */
	@Override
	protected int highlightOther(String content, int start, int end) {

		if ((start > 0) && (start + PHP_START.length() < end)) {
			if (PHP_START.equals(content.substring(start - 1, start - 1 + PHP_START.length()))) {

				this.setCharacterAttributes(start - 1, PHP_START.length(), this.attrKeyword, false);

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
						this.setCharacterAttributes(start, 1, this.attrReservedChar, false);
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

		this.setCharacterAttributes(start, 2, this.attrKeyword, false);

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
			this.setCharacterAttributes(start, commentEnd - start + 1, this.attrComment, false);

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
		this.setCharacterAttributes(start, commentEnd - start + 1, this.attrComment, false);

		return commentEnd;
	}

	private int highlightPhpString(String content, int start, int end) {
		boolean singleForMultiline = true;
		boolean threeForMultiline = false;
		return highlightString(content, start, end, singleForMultiline, threeForMultiline);
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
			this.setCharacterAttributes(start, couldBeKeywordEnd - start, this.attrKeyword, false);
		} else if (isPhpPrimitiveType(couldBeKeyword)) {
			this.setCharacterAttributes(start, couldBeKeywordEnd - start, this.attrPrimitiveType, false);
		} else if (isPhpAdvancedType(couldBeKeyword)) {
			this.setCharacterAttributes(start, couldBeKeywordEnd - start, this.attrAdvancedType, false);
		} else if (isPhpAnnotation(couldBeKeyword)) {
			this.setCharacterAttributes(start, couldBeKeywordEnd - start, this.attrAnnotation, false);
		} else if ((couldBeKeywordEnd <= end) && (content.charAt(couldBeKeywordEnd) == '(')) {
			if (!"new".equals(lastCouldBeKeywordPhp)) {
				this.setCharacterAttributes(start, couldBeKeywordEnd - start, this.attrFunction, false);
				if ((start > 0) && (content.charAt(start-1) == ' ')) {
					// ignore lines with more than 1 tab indent / 4 regular indents and line without "function" name
					if ((curLineStartingWhitespacePhp < 5) && "function".equals(lastCouldBeKeywordPhp)) {
						// now get the entire line that we found!
						// String functionName = lastCouldBeKeywordPhp + " " + couldBeKeyword + "()";
						String functionName = StrUtils.getLineFromPosition(start, content);
						if (functionName.startsWith("function ")) {
							functionName = functionName.substring(9);
						}
						functions.add(new CodeSnippetWithLocation(functionName, StrUtils.getLineStartFromPosition(start, content)));
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
