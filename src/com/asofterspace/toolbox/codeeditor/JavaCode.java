/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.codeeditor;

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


public class JavaCode extends FunctionSupplyingCode {

	private static final long serialVersionUID = 1L;

	// all keywords of the Java language
	private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
		new String[] { "abstract", "as", "assert", "break", "case", "catch", "const", "continue", "def", "default", "do", "else", "extends", "final", "finally", "for", "goto", "if", "implements", "import", "in", "instanceof", "interface", "new", "package", "private", "protected", "public", "return", "static", "switch", "synchronized", "throw", "throws", "trait", "try", "while", "volatile"}
	));

	// all primitive types of the Java language and other stuff that looks that way
	private static final Set<String> PRIMITIVE_TYPES = new HashSet<>(Arrays.asList(
		new String[] {"boolean", "char", "class", "double", "enum", "int", "long", "false", "null", "super", "this", "true", "void"}
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

	// are we currently in a multiline comment?
	private boolean curMultilineComment;

	private int curLineStartingWhitespace = 0;

	private boolean startingWhitespace = false;

	private String lastCouldBeKeyword = "";


	public JavaCode(JTextPane editor) {

		super(editor);
	}

	@Override
	public String reorganizeImports(String origText) {

		StringBuilder output = new StringBuilder();
		List<String> imports = new ArrayList<>();
		StringBuilder secondOutput = new StringBuilder();

		String[] lines = origText.split("\n");

		int curLine = 0;

		for (; curLine < lines.length; curLine++) {
			String line = lines[curLine];
			if (line.startsWith("import")) {
				break;
			} else {
				output.append(line);
				output.append("\n");
			}
		}

		for (; curLine < lines.length; curLine++) {
			String line = lines[curLine];
			if (line.equals("")) {
				continue;
			}
			if (line.startsWith("import")) {
				imports.add(line);
			} else {
				break;
			}
		}

		for (; curLine < lines.length; curLine++) {
			secondOutput.append("\n");
			String line = lines[curLine];
			secondOutput.append(line);
		}


		// sort imports alphabetically
		Collections.sort(imports, new Comparator<String>() {
			public int compare(String a, String b) {
				return a.toLowerCase().compareTo(b.toLowerCase());
			}
		});


		String lastImport = "";
		String lastImportStart = "";

		int i = 0;

		for (String importLine : imports) {

			// remove duplicates
			if (lastImport.equals(importLine)) {
				continue;
			}

			// add an empty line between imports in different namespaces
			String thisImportStart = importLine.substring(0, importLine.indexOf(".") + 1);
			if (!lastImportStart.equals(thisImportStart)) {
				if (i > 0) {
					output.append("\n");
				}
			}

			// actually add the import
			output.append(importLine);
			output.append("\n");
			lastImport = importLine;
			lastImportStart = thisImportStart;
			i++;
		}

		// actually have two empty lines between the import end and the class start
		if (i > 0) {
			output.append("\n");
		}

		return output.toString() + secondOutput.toString();
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
	void highlightText(int start, int length) {

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

			while (start <= end) {

				// while we have a delimiter...
				char curChar = content.charAt(start);

				startingWhitespace = false;

				while (isDelimiter(curChar)) {

					// prevent stuff like blubb = foo() from ending up in the function overview list
					if (curChar == '=') {
						lastCouldBeKeyword = "";
					}

					if (curChar == '\n') {
						curLineStartingWhitespace = 0;
						startingWhitespace = true;
					} else {
						if (startingWhitespace) {
							if (curChar == '\t') {
								curLineStartingWhitespace += 4;
							} else {
								curLineStartingWhitespace++;
							}
						}
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

		if (start + 1 > end) {
			return false;
		}

		String potentialCommentStart = content.substring(start, start + 2);

		return START_SINGLELINE_COMMENT.equals(potentialCommentStart) || START_MULTILINE_COMMENT.equals(potentialCommentStart);
	}

	private int highlightComment(String content, int start, int end) {

		String commentStart = content.substring(start, start + 2);

		if (START_SINGLELINE_COMMENT.equals(commentStart)) {

			int commentEnd = content.indexOf(EOL, start + 2);

			// this is the last line
			if (commentEnd == -1) {
				commentEnd = end;
			}

			// apply single line comment highlighting
			this.setCharacterAttributes(start, commentEnd - start + 1, attrComment, false);

			return commentEnd;
		}

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

	private int highlightString(String content, int start, int end) {

		// get the string delimiter that was actually used to start this string (so " or ') to be able to find the matching one
		String stringDelimiter = content.substring(start, start + 1);

		// find the end of line - as we do not want to go further
		int endOfLine = content.indexOf(EOL, start + 2);

		if (endOfLine == -1) {
			endOfLine = end;
		}

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
			// the string is open-ended... go for end of line
			endOfString = endOfLine;
		} else {
			// the string is not open-ended... so will the end marker or the line break be first?
			endOfString = Math.min(endOfString, endOfLine);
		}

		this.setCharacterAttributes(start, endOfString - start + 1, attrString, false);

		return endOfString;
	}

	private int highlightOther(String content, int start, int end) {

		int couldBeKeywordEnd = start + 1;

		while (couldBeKeywordEnd <= end) {
			if (isDelimiter(content.charAt(couldBeKeywordEnd))) {
				break;
			}
			couldBeKeywordEnd++;
		}

		String couldBeKeyword = content.substring(start, couldBeKeywordEnd);

		if (isKeyword(couldBeKeyword)) {
			this.setCharacterAttributes(start, couldBeKeywordEnd - start, attrKeyword, false);
		} else if (isPrimitiveType(couldBeKeyword)) {
			this.setCharacterAttributes(start, couldBeKeywordEnd - start, attrPrimitiveType, false);
		} else if (isAdvancedType(couldBeKeyword)) {
			this.setCharacterAttributes(start, couldBeKeywordEnd - start, attrAdvancedType, false);
		} else if (isAnnotation(couldBeKeyword)) {
			this.setCharacterAttributes(start, couldBeKeywordEnd - start, attrAnnotation, false);
		} else if ((couldBeKeywordEnd <= end) && (content.charAt(couldBeKeywordEnd) == '(')) {
			if (!"new".equals(lastCouldBeKeyword)) {
				this.setCharacterAttributes(start, couldBeKeywordEnd - start, attrFunction, false);
				if ((start > 0) && (content.charAt(start-1) == ' ')) {
					// ignore lines with more than 1 tab indent / 4 regular indents and line without the return type
					if ((curLineStartingWhitespace < 5) && !"".equals(lastCouldBeKeyword)) {
						String functionName = lastCouldBeKeyword + " " + couldBeKeyword + "()";
						functions.add(new CodeLocation(functionName, start));
					}
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
