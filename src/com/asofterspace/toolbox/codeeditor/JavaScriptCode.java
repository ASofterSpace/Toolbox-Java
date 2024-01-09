/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.codeeditor;

import com.asofterspace.toolbox.codeeditor.base.Code;
import com.asofterspace.toolbox.codeeditor.base.FunctionSupplyingCode;
import com.asofterspace.toolbox.codeeditor.utils.CodeSnippetWithLocation;
import com.asofterspace.toolbox.utils.StrUtils;

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

	// all keywords of the JavaScript language
	private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
		new String[] {"as", "assert", "break", "case", "catch", "continue", "default", "do", "else", "export", "extends", "false", "finally", "for", "function", "goto", "if", "implements", "import", "in", "instanceof", "interface", "new", "null", "return", "super", "switch", "this", "throw", "throws", "trait", "true", "try", "typeof", "undefined", "while"}
	));

	// all primitive types of the JavaScript language and other stuff that looks that way
	private static final Set<String> PRIMITIVE_TYPES = new HashSet<>(Arrays.asList(
		new String[] {"abstract", "boolean", "char", "class", "const", "def", "double", "enum", "final", "int", "let", "long", "package", "private", "protected", "public", "static", "synchronized", "type", "var", "void", "volatile"}
	));

	// all string delimiters of the JavaScript language
	private static final Set<Character> STRING_DELIMITERS = new HashSet<>(Arrays.asList(
		new Character[] {'"', '\''}
	));

	// operand characters in the JavaScript language
	private static final Set<Character> OPERAND_CHARS = new HashSet<>(Arrays.asList(
		new Character[] {';', ':', '.', ',', '{', '}', '(', ')', '[', ']', '+', '-', '/', '%', '<', '=', '>', '!', '&', '|', '^', '~', '*'}
	));

	// start of single line comments in the JavaScript language
	private static final String START_SINGLELINE_COMMENT = "//";

	// start of multiline comments in the JavaScript language
	private static final String START_MULTILINE_COMMENT = "/*";

	// end of multiline comments in the JavaScript language
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
		insertStringJavalike(offset, insertedString, attrs);
	}

	protected List<String> getBaseTokens() {
		return getBaseTokensStatically();
	}

	protected static List<String> getBaseTokensStatically() {

		List<String> nextEncounteredTokens = new ArrayList<>();

		// add a few strings which we want to get proposed all the time, even if they have not been encountered yet
		nextEncounteredTokens.add("console.log();");
		nextEncounteredTokens.add("private");
		nextEncounteredTokens.add("public");
		nextEncounteredTokens.add("protected");
		nextEncounteredTokens.add("return result;");

		return nextEncounteredTokens;
	}

	// this is the main function that... well... highlights our text :)
	@Override
	protected void highlightText(int start, int length) {

		super.highlightText(start, length);

		this.encounteredTokens = nextEncounteredTokens;
		nextEncounteredTokens = getBaseTokens();

		try {
			int end = this.getLength();

			String content = this.getText(0, end);

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
					boolean singleForMultiline = false;
					boolean threeForMultiline = false;
					start = highlightString(content, start, end, singleForMultiline, threeForMultiline);

				} else {
					// please highlight the delimiter in the process ;)
					if (!Character.isWhitespace(curChar)) {
						getMe().setCharacterAttributes(start, 1, this.attrReservedChar, false);
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
			getMe().setCharacterAttributes(start, commentEnd - start + 1, this.attrComment, false);

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
		getMe().setCharacterAttributes(start, commentEnd - start + 1, this.attrComment, false);

		return commentEnd;
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

		if (nextEncounteredTokens != null) {
			nextEncounteredTokens.add(couldBeKeyword);
		}

		if (isKeyword(couldBeKeyword)) {
			if ("function".equals(couldBeKeyword)) {
				String line = StrUtils.getLineFromPosition(start, content);
				if (!line.endsWith(";") && !line.contains("(function")) {
					String functionName = getFunctionNameFromLine(line);
					if (functionName.length() > 0) {
						// add any line that contain the "function" keyword as a function
						// (later on lines with indentation > 4 will be removed from that list though!)
						functions.add(new CodeSnippetWithLocation(functionName, StrUtils.getLineStartFromPosition(start, content)));
					}
				}
			}
			getMe().setCharacterAttributes(start, couldBeKeywordEnd - start, this.attrKeyword, false);
		} else if (isPrimitiveType(couldBeKeyword)) {
			getMe().setCharacterAttributes(start, couldBeKeywordEnd - start, this.attrPrimitiveType, false);
		} else if (isAdvancedType(couldBeKeyword)) {
			getMe().setCharacterAttributes(start, couldBeKeywordEnd - start, this.attrAdvancedType, false);
		} else if (isAnnotation(couldBeKeyword)) {
			getMe().setCharacterAttributes(start, couldBeKeywordEnd - start, this.attrAnnotation, false);
		} else if ((couldBeKeywordEnd <= end) && (content.charAt(couldBeKeywordEnd) == '(')) {
			if (!"new".equals(lastCouldBeKeyword)) {
				getMe().setCharacterAttributes(start, couldBeKeywordEnd - start, this.attrFunction, false);
				if ((start > 0) && (content.charAt(start-1) == ' ')) {
					String functionName = lastCouldBeKeyword + " " + couldBeKeyword + "()";
					// add any line that contains " foo(" as a function
					// (later on lines with indentation > 4 will be removed from that list though!)
					String line = StrUtils.getLineFromPosition(start, content);
					if (!line.endsWith(";")) {
						functions.add(new CodeSnippetWithLocation(functionName, StrUtils.getLineStartFromPosition(start, content)));
					}
				}
			}
		}

		lastCouldBeKeyword = couldBeKeyword;

		return couldBeKeywordEnd;
	}

	/**
	 * Takes a line containing the word "function" like
	 *   function foobar(arg1, arg2) {
	 *   foobar: function(arg1, arg2) {
	 * and returns foobar(arg1, arg2) - the name of the function followed by the arguments
	 */
	private String getFunctionNameFromLine(String line) {

		String args = "";
		line = line.trim();

		if (line.contains("{")) {
			line = line.substring(0, line.indexOf("{")).trim();
		}

		// in case of (function () {, trim off the first (
		while (line.startsWith("(")) {
			line = line.substring(1);
		}

		if (line.contains("(")) {
			args = line.substring(line.indexOf("(") + 1);
			if (args.contains(")")) {
				args = args.substring(0, args.indexOf(")"));
			}
			line = line.substring(0, line.indexOf("(")).trim();
		}

		// we now have the args, and the line contains:
		//   function foobar
		//   foobar: function
		// soooo let's just...
		if (line.startsWith("function")) {
			line = line.substring("function".length()).trim();
		}
		if (line.contains(":")) {
			line = line.substring(0, line.indexOf(":")).trim();
		}

		if ("".equals(line)) {
			line = "anonymous function";
		}

		return line + "(" + args + ")";
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
