/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.codeeditor;

import com.asofterspace.toolbox.utils.Callback;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.ArrayList;
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


public class JavaCode extends Code {

	private static final long serialVersionUID = 1L;

	// all keywords of the Groovy language
	private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
		new String[] {"as", "assert", "break", "case", "catch", "const", "continue", "def", "default", "do", "else", "extends", "false", "finally", "for", "goto", "if", "implements", "import", "in", "instanceof", "interface", "new", "null", "return", "super", "switch", "this", "throw", "throws", "trait", "true", "try", "while"}
	));

	// all primitive types of the Groovy language and other stuff that looks that way
	private static final Set<String> PRIMITIVE_TYPES = new HashSet<>(Arrays.asList(
		new String[] {"boolean", "char", "class", "double", "enum", "int", "final", "package", "private", "protected", "public", "static", "void"}
	));

	// all string delimiters of the Groovy language
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
	
	// styles for the different kinds of text in the document
	private MutableAttributeSet attrAnnotation; // @blubb
	private MutableAttributeSet attrComment; // /* bla blubb */
	private MutableAttributeSet attrKeyword; // this, null, ...
	private MutableAttributeSet attrPrimitiveType; // int, bool, ...
	private MutableAttributeSet attrString; // "meow!"
	private MutableAttributeSet attrReservedChar; // ,.()[]...
	private MutableAttributeSet attrFunction; // blubb()


	public JavaCode(JTextPane editor) {

		super(editor);
	}
	
	@Override
	public void setLightScheme() {
	
		// change the attribute sets
		attrAnnotation = new SimpleAttributeSet();
		StyleConstants.setForeground(attrAnnotation, new Color(0, 128, 0));

		attrComment = new SimpleAttributeSet();
		StyleConstants.setForeground(attrComment, new Color(0, 128, 0));
		StyleConstants.setItalic(attrComment, true);

		attrKeyword = new SimpleAttributeSet();
		StyleConstants.setForeground(attrKeyword, new Color(0, 0, 128));
		StyleConstants.setBold(attrKeyword, true);

		attrPrimitiveType = new SimpleAttributeSet();
		StyleConstants.setForeground(attrPrimitiveType, new Color(96, 0, 96));

		attrString = new SimpleAttributeSet();
		StyleConstants.setForeground(attrString, new Color(128, 0, 0));
		
		attrReservedChar = new SimpleAttributeSet();
		StyleConstants.setForeground(attrReservedChar, new Color(48, 0, 112));
		StyleConstants.setBold(attrReservedChar, true);

		attrFunction = new SimpleAttributeSet();
		StyleConstants.setForeground(attrFunction, new Color(48, 0, 48));

		super.setLightScheme();
	}
	
	@Override
	public void setDarkScheme() {
	
		// change the attribute sets
		attrAnnotation = new SimpleAttributeSet();
		StyleConstants.setForeground(attrAnnotation, new Color(128, 255, 128));
		StyleConstants.setBackground(attrAnnotation, new Color(0, 0, 0));

		attrComment = new SimpleAttributeSet();
		StyleConstants.setForeground(attrComment, new Color(128, 255, 128));
		StyleConstants.setBackground(attrComment, new Color(0, 0, 0));
		StyleConstants.setItalic(attrComment, true);

		attrKeyword = new SimpleAttributeSet();
		StyleConstants.setForeground(attrKeyword, new Color(128, 128, 255));
		StyleConstants.setBackground(attrKeyword, new Color(0, 0, 0));
		StyleConstants.setBold(attrKeyword, true);

		attrPrimitiveType = new SimpleAttributeSet();
		StyleConstants.setForeground(attrPrimitiveType, new Color(255, 96, 255));
		StyleConstants.setBackground(attrPrimitiveType, new Color(0, 0, 0));

		attrString = new SimpleAttributeSet();
		StyleConstants.setForeground(attrString, new Color(255, 128, 128));
		StyleConstants.setBackground(attrString, new Color(0, 0, 0));
		
		attrReservedChar = new SimpleAttributeSet();
		StyleConstants.setForeground(attrReservedChar, new Color(192, 112, 225));
		StyleConstants.setBold(attrReservedChar, true);

		attrFunction = new SimpleAttributeSet();
		StyleConstants.setForeground(attrFunction, new Color(255, 178, 255));

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

		if (overrideCaretPos) {
			decoratedEditor.setCaretPosition(origCaretPos + 1);
		}
	}

	// this is the main function that... well... highlights our text :)
	@Override
	void highlightText(int start, int length) {

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
				
					// ... check for a comment (which starts with a delimiter)
					if (isCommentStart(content, start, end)) {
						start = highlightComment(content, start, end);
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

				// now check what we have: a quoted string?
				if (isStringDelimiter(curChar)) {

					// then let's get that string!
					start = highlightString(content, start, end);

				} else {

					// or any other token instead?
					start = highlightOther(content, start, end);
				}
			}
			
		} catch (BadLocationException e) {
			// oops!
		}
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

		return endOfString + 1;
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
		} else if (isAnnotation(couldBeKeyword)) {
			this.setCharacterAttributes(start, couldBeKeywordEnd - start, attrAnnotation, false);
		} else if ((couldBeKeywordEnd <= end) && (content.charAt(couldBeKeywordEnd) == '(')) {
			this.setCharacterAttributes(start, couldBeKeywordEnd - start, attrFunction, false);
		}
			
		return couldBeKeywordEnd;
	}

	private boolean isDelimiter(char character) {
		return Character.isWhitespace(character) || OPERAND_CHARS.contains(character);
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

	private boolean isAnnotation(String token) {
		return token.startsWith("@");
	}
}
