package com.asofterspace.toolbox.codeeditor;

import java.awt.Color;
import java.awt.Font;
import java.util.Arrays;
import java.util.HashSet;
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
import javax.swing.text.StyleConstants;

public class GroovyCode extends DefaultStyledDocument {

	// the end-of-line marker
	private static final String EOL = "\n";

	// all keywords of the Groovy language
	private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
		new String[] {"as", "assert", "break", "case", "catch", "class", "const", "continue", "def", "default", "do", "else", "enum", "extends", "false", "finally", "for", "goto", "if", "implements", "import", "in", "instanceof", "interface", "new", "null", "package", "return", "super", "switch", "this", "throw", "throws", "trait", "true", "try", "while"}
	));

	// all primitive types of the Groovy language and other stuff that looks that way
	private static final Set<String> PRIMITIVE_TYPES = new HashSet<>(Arrays.asList(
		new String[] {"boolean", "char", "double", "int", "private", "protected", "public", "static", "void"}
	));

	// all string delimiters of the Groovy language
	private static final Set<Character> STRING_DELIMITERS = new HashSet<>(Arrays.asList(
		new Character[] {'"', '\''}
	));

	// operand characters in the Groovy language
	private static final Set<Character> OPERAND_CHARS = new HashSet<>(Arrays.asList(
		new Character[] {';', ':', '{', '}', '(', ')', '[', ']', '+', '-', '/', '%', '<', '=', '>', '!', '&', '|', '^', '~', '*'}
	));

	// start of single line comments in the Groovy language
	private static final String START_SINGLELINE_COMMENT = "//";

	// start of multiline comments in the Groovy language
	private static final String START_MULTILINE_COMMENT = "/*";

	// end of multiline comments in the Groovy language
	private static final String END_MULTILINE_COMMENT = "*/";

	// the root element of the document, through which we can get the individual lines
	private Element root;

	// are we currently in a multiline comment?
	private boolean curMultilineComment;

	// styles for the different kinds of text in the document
	protected MutableAttributeSet attrAnnotation;
	protected MutableAttributeSet attrComment;
	protected MutableAttributeSet attrKeyword;
	protected MutableAttributeSet attrRegular;
	protected MutableAttributeSet attrPrimitiveType;
	protected MutableAttributeSet attrString;

	// the editor that is to be decorated by us
	private final JTextPane decoratedEditor;


	public GroovyCode(JTextPane editor) {

		super();

		// keep track of the editor we are decorating (useful e.g. to get and set caret pos during insert operations)
		decoratedEditor = editor;

		// keep track of the root element
		root = this.getDefaultRootElement();

		// declare which end of line marker is to be used
		putProperty(DefaultEditorKit.EndOfLineStringProperty, EOL);

		// initialize all the attribute sets
		initAttributeSets();

		// actually style the editor with... us
		decoratedEditor.setDocument(this);
		decoratedEditor.setBackground(new Color(255, 255, 255));
		decoratedEditor.setFont(new Font("Courier New", Font.PLAIN, 15));
	}

	private void initAttributeSets() {

		attrAnnotation = new SimpleAttributeSet();
		StyleConstants.setForeground(attrAnnotation, new Color(0, 128, 0));

		attrComment = new SimpleAttributeSet();
		StyleConstants.setForeground(attrComment, new Color(0, 128, 0));
		StyleConstants.setItalic(attrComment, true);

		attrKeyword = new SimpleAttributeSet();
		StyleConstants.setForeground(attrKeyword, new Color(0, 0, 128));
		StyleConstants.setBold(attrKeyword, true);

		attrRegular = new SimpleAttributeSet();
		StyleConstants.setForeground(attrRegular, new Color(0, 0, 0));

		attrPrimitiveType = new SimpleAttributeSet();
		StyleConstants.setForeground(attrPrimitiveType, new Color(96, 0, 96));

		attrString = new SimpleAttributeSet();
		StyleConstants.setForeground(attrString, new Color(128, 0, 0));
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

		try {
			super.insertString(offset, insertedString, attrs);
		} catch (BadLocationException e) {
			// oops!
		}

		highlightText(offset, insertedString.length());

		if (overrideCaretPos) {
			decoratedEditor.setCaretPosition(origCaretPos + 1);
		}
	}

	@Override
	protected void fireInsertUpdate(DocumentEvent event) {

		super.fireInsertUpdate(event);

		highlightText(event.getOffset(), event.getLength());
	}

	@Override
	public void remove(int offset, int length) {

		try {
			super.remove(offset, length);
		} catch (BadLocationException e) {
			// oops!
		}

		highlightText(offset, 0);
	}

	@Override
	protected void fireRemoveUpdate(DocumentEvent event) {

		super.fireRemoveUpdate(event);

		highlightText(event.getOffset(), event.getLength());
	}

	// this is the main function that... well... hightlights our text :)
	private void highlightText(int start, int length) {

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
				while (isDelimiter(content.charAt(start))) {
				
					// ... check for a comment (which starts with a delimiter) ...
					if (isCommentStart(content, start, end)) {
						start = highlightComment(content, start, end);
					}

					if (start < end) {

						// ... or, if there is no comment, jump forward and try again1
						start++;

					} else {
						return;
					}
				}

				// now check what we have: a quoted string?
				if (isStringDelimiter(content.charAt(start))) {

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

			return commentEnd - 1;
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
		
		return commentEnd - 1;
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