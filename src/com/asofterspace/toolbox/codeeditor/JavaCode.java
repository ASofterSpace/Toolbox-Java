/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.codeeditor;

import com.asofterspace.toolbox.codeeditor.base.PublicPrivateFunctionSupplyingCode;
import com.asofterspace.toolbox.codeeditor.utils.CodeLanguage;
import com.asofterspace.toolbox.codeeditor.utils.CodeSnippetWithLocation;
import com.asofterspace.toolbox.utils.StrUtils;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;


public class JavaCode extends PublicPrivateFunctionSupplyingCode {

	private static final long serialVersionUID = 1L;

	// all keywords of the Java language
	protected static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
		new String[] { "abstract", "as", "assert", "break", "case", "catch", "const", "continue", "default", "do", "else", "extends", "final", "finally", "for", "goto", "if", "implements", "import", "in", "instanceof", "new", "package", "private", "protected", "public", "return", "static", "switch", "synchronized", "throw", "throws", "trait", "try", "while", "volatile"}
	));

	// all primitive types of the Java language and other stuff that looks that way
	protected static final Set<String> PRIMITIVE_TYPES = new HashSet<>(Arrays.asList(
		new String[] {"boolean", "byte", "char", "class", "double", "enum", "false", "float", "int", "interface", "long", "null", "short", "super", "this", "true", "void"}
	));

	// all string delimiters of the Java language
	protected static final Set<Character> STRING_DELIMITERS = new HashSet<>(Arrays.asList(
		new Character[] {'"', '\''}
	));

	// operand characters in the Java language
	protected static final Set<Character> OPERAND_CHARS = new HashSet<>(Arrays.asList(
		new Character[] {';', ':', '.', ',', '{', '}', '(', ')', '[', ']', '+', '-', '/', '%', '<', '=', '>', '!', '?', '&', '|', '^', '~', '*'}
	));

	// start of single line comments in the Java language
	protected static final String START_SINGLELINE_COMMENT = "//";

	// start of multiline comments in the Java language
	protected static final String START_MULTILINE_COMMENT = "/*";

	// end of multiline comments in the Java language
	protected static final String END_MULTILINE_COMMENT = "*/";

	// are we currently in a multiline comment?
	private boolean curMultilineComment;

	private int curLineStartingWhitespace = 0;

	private boolean startingWhitespace = false;

	private String lastCouldBeKeyword = "";


	public JavaCode(JTextPane editor) {

		super(editor);

		clearAutomaticallyAddedImports();
	}

	private void clearAutomaticallyAddedImports() {

		automaticallyAddedImports = new HashMap<>();

		automaticallyAddedImports.put("BorderLayout", "java.awt.BorderLayout");
		automaticallyAddedImports.put("CardLayout", "java.awt.CardLayout");
		automaticallyAddedImports.put("Color", "java.awt.Color");
		automaticallyAddedImports.put("Desktop", "java.awt.Desktop");
		automaticallyAddedImports.put("Dimension", "java.awt.Dimension");
		automaticallyAddedImports.put("ActionEvent", "java.awt.event.ActionEvent");
		automaticallyAddedImports.put("ActionListener", "java.awt.event.ActionListener");
		automaticallyAddedImports.put("ComponentAdapter", "java.awt.event.ComponentAdapter");
		automaticallyAddedImports.put("ComponentEvent", "java.awt.event.ComponentEvent");
		automaticallyAddedImports.put("ItemEvent", "java.awt.event.ItemEvent");
		automaticallyAddedImports.put("ItemListener", "java.awt.event.ItemListener");
		automaticallyAddedImports.put("KeyEvent", "java.awt.event.KeyEvent");
		automaticallyAddedImports.put("KeyListener", "java.awt.event.KeyListener");
		automaticallyAddedImports.put("MouseAdapter", "java.awt.event.MouseAdapter");
		automaticallyAddedImports.put("MouseEvent", "java.awt.event.MouseEvent");
		automaticallyAddedImports.put("MouseListener", "java.awt.event.MouseListener");
		automaticallyAddedImports.put("GridBagLayout", "java.awt.GridBagLayout");
		automaticallyAddedImports.put("GridLayout", "java.awt.GridLayout");
		automaticallyAddedImports.put("Point", "java.awt.Point");
		automaticallyAddedImports.put("BufferedInputStream", "java.io.BufferedInputStream");
		automaticallyAddedImports.put("BufferedReader", "java.io.BufferedReader");
		automaticallyAddedImports.put("InputStream", "java.io.InputStream");
		automaticallyAddedImports.put("InputStreamWriter", "java.io.InputStreamWriter");
		automaticallyAddedImports.put("IOException", "java.io.IOException");
		automaticallyAddedImports.put("OutputStream", "java.io.OutputStream");
		automaticallyAddedImports.put("OutputStreamWriter", "java.io.OutputStreamWriter");
		automaticallyAddedImports.put("HttpURLConnection", "java.net.HttpURLConnection");
		automaticallyAddedImports.put("URL", "java.net.URL");
		automaticallyAddedImports.put("URLConnection", "java.net.URLConnection");
		automaticallyAddedImports.put("StandardCharsets", "java.nio.charset.StandardCharsets");
		automaticallyAddedImports.put("Files", "java.nio.file.Files");
		automaticallyAddedImports.put("ArrayList", "java.util.ArrayList");
		automaticallyAddedImports.put("Collection", "java.util.Collection");
		automaticallyAddedImports.put("Collections", "java.util.Collections");
		automaticallyAddedImports.put("Comparator", "java.util.Comparator");
		automaticallyAddedImports.put("Date", "java.util.Date");
		automaticallyAddedImports.put("HashMap", "java.util.HashMap");
		automaticallyAddedImports.put("HashSet", "java.util.HashSet");
		automaticallyAddedImports.put("List", "java.util.List");
		automaticallyAddedImports.put("Map", "java.util.Map");
		automaticallyAddedImports.put("Set", "java.util.Set");
		automaticallyAddedImports.put("AbstractButton", "javax.swing.AbstractButton");
		automaticallyAddedImports.put("BorderFactory", "javax.swing.BorderFactory");
		automaticallyAddedImports.put("ButtonGroup", "javax.swing.ButtonGroup");
		automaticallyAddedImports.put("DefaultCellEditor", "javax.swing.DefaultCellEditor");
		automaticallyAddedImports.put("DocumentEvent", "javax.swing.event.DocumentEvent");
		automaticallyAddedImports.put("DocumentListener", "javax.swing.event.DocumentListener");
		automaticallyAddedImports.put("TreeModelEvent", "javax.swing.event.TreeModelEvent");
		automaticallyAddedImports.put("TreeModelListener", "javax.swing.event.TreeModelListener");
		automaticallyAddedImports.put("JButton", "javax.swing.JButton");
		automaticallyAddedImports.put("JCheckBoxMenuItem", "javax.swing.JCheckBoxMenuItem");
		automaticallyAddedImports.put("JComboBox", "javax.swing.JComboBox");
		automaticallyAddedImports.put("JDialog", "javax.swing.JDialog");
		automaticallyAddedImports.put("JFileChooser", "javax.swing.JFileChooser");
		automaticallyAddedImports.put("JFrame", "javax.swing.JFrame");
		automaticallyAddedImports.put("JLabel", "javax.swing.JLabel");
		automaticallyAddedImports.put("JList", "javax.swing.JList");
		automaticallyAddedImports.put("JMenu", "javax.swing.JMenu");
		automaticallyAddedImports.put("JMenuBar", "javax.swing.JMenuBar");
		automaticallyAddedImports.put("JMenuItem", "javax.swing.JMenuItem");
		automaticallyAddedImports.put("JOptionPane", "javax.swing.JOptionPane");
		automaticallyAddedImports.put("JPanel", "javax.swing.JPanel");
		automaticallyAddedImports.put("JPopupMenu", "javax.swing.JPopupMenu");
		automaticallyAddedImports.put("JRadioButton", "javax.swing.JRadioButton");
		automaticallyAddedImports.put("JScrollPane", "javax.swing.JScrollPane");
		automaticallyAddedImports.put("JTextArea", "javax.swing.JTextArea");
		automaticallyAddedImports.put("JTextField", "javax.swing.JTextField");
		automaticallyAddedImports.put("JTextPane", "javax.swing.JTextPane");
		automaticallyAddedImports.put("KeyStroke", "javax.swing.KeyStroke");
		automaticallyAddedImports.put("BasicScrollBarUI", "javax.swing.plaf.basic.BasicScrollBarUI");
		automaticallyAddedImports.put("SwingUtilities", "javax.swing.SwingUtilities");
		automaticallyAddedImports.put("AttributeSet", "javax.swing.text.AttributeSet");
		automaticallyAddedImports.put("BadLocationException", "javax.swing.text.BadLocationException");
	}

	@Override
	public String addMissingImports(String origText) {

		// we get the default java imports
		clearAutomaticallyAddedImports();

		// and if we are a regular java file with a package name...
		// (necessary because we want to avoid importing files from our own package ^^)
		String ourPackageStr = null;
		for (String line : decoratedEditor.getText().split("\n")) {
			if (line.startsWith("package ")) {
				ourPackageStr = line.substring(8).trim();
				if (ourPackageStr.endsWith(";")) {
					ourPackageStr = ourPackageStr.substring(0, ourPackageStr.length() - 1).trim();
				}
				break;
			}
		}

		// ... then we add missing imports based on all the open java files
		if (ourPackageStr != null) {
			List<String> filesToOpen = new ArrayList<>();
			filesToOpen.add(".java");
			filesToOpen.add(".groovy");

			List<String> contents = getOtherFileContents(filesToOpen);

			for (String content : contents) {
				String packageStr = null;
				String classNameStr = null;
				if (content == null) {
					continue;
				}
				for (String line : content.split("\n")) {
					if (line.startsWith("package ")) {
						packageStr = line.substring(8).trim();
						if (packageStr.endsWith(";")) {
							packageStr = packageStr.substring(0, packageStr.length() - 1).trim();
						}
					}
					line = line.trim();
					if (line.contains(" class ") && line.endsWith("{")) {
						classNameStr = line.substring(line.indexOf(" class ") + 7);
					}
					if (line.startsWith("class ") && line.endsWith("{")) {
						classNameStr = line.substring(6);
					}
					if (classNameStr != null) {
						if (classNameStr.contains(" ")) {
							classNameStr = classNameStr.substring(0, classNameStr.indexOf(" "));
						}
						break;
					}
				}
				// for each file, check if a package and classname were found
				if ((packageStr != null) && (classNameStr != null) && !packageStr.equals(ourPackageStr)) {
					// we here override java default classes if the opened files contain the same class names...
					// ... which is exactly the behavior we like :)
					automaticallyAddedImports.put(classNameStr, packageStr + "." + classNameStr);
				}
			}
		}

		return addMissingImportsJavalike("import", origText);
	}

	@Override
	public String reorganizeImports(String origText) {
		return reorganizeImportsJavalike("import", origText);
	}

	@Override
	public String reorganizeImportsCompatible(String origText) {
		return reorganizeImportsCompatibleJavalike("import", origText);
	}

	@Override
	public String removeUnusedImports(String origText) {
		return removeUnusedImportsJavalike("import", origText);
	}

	@Override
	public void insertString(int offset, String insertedString, AttributeSet attrs) {
		insertStringJavalike(offset, insertedString, attrs);
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
							// we are checking this because otherwise we are getting exceptions... but... why?
							// at this point, this should always be the case o.o
							if (start <= end) {
								this.setCharacterAttributes(start, 1, attrReservedChar, false);
							}
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

	protected int highlightString(String content, int start, int end) {

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

			if (endOfString == -1) {
				break;
			}
			// if the end of string is actually escaped... well, then it is not an end of string yet, continue searching!
			if (content.charAt(endOfString - 1) != '\\') {
				break;
			}
			// but if the escaping is escaped - so \\ - then actually the end of string is not escaped, just the escape string xD
			if ((endOfString > 1) && (content.charAt(endOfString - 1) == '\\') && (content.charAt(endOfString - 2) == '\\')) {
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
						// now get the entire line that we found!
						// String functionName = lastCouldBeKeyword + " " + couldBeKeyword + "()";
						String functionName = getLineFromPosition(start, content);
						functions.add(new CodeSnippetWithLocation(functionName, getLineStartFromPosition(start, content)));
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

	protected boolean isKeyword(String token) {
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

	@Override
	protected void onMouseReleased(MouseEvent event) {

		// if the user [Ctrl]-clicks on an import line, try to jump to that file
		if (event.isControlDown()) {
			int caretPos = decoratedEditor.getCaretPosition();
			String content = decoratedEditor.getText();
			String[] lines = content.split("\n");
			String line = getLineFromPosition(caretPos, content).trim();
			String importLine = null;

			if ((line != null) && line.startsWith("import ") && line.endsWith(";")) {
				// the user clicked on a line like import foo.bar.nonsense.Object;
				importLine = line;
			} else {
				String curWord = getWordFromPosition(caretPos, content);
				// perform the following only for words starting with upper-case characters (so, in Java, classnames),
				// NOT for any words (as we e.g. want to jump around function names within the file - if we did not
				// check this, then for every time we Ctrl+click on a function name here we would search half the
				// disk for non-existant java files...)
				if (Character.isUpperCase(curWord.charAt(0))) {
					for (String curLine : lines) {
						curLine = curLine.trim();
						if (curLine.startsWith("import ") && curLine.endsWith("." + curWord + ";")) {
							// the user clicked on Object somewhere in the code and we found a line like
							// import foo.bar.nonsense.Object; at the top
							importLine = curLine;
							break;
						}
					}

					// if there was no import line for the current word...
					if (importLine == null) {
						// ... then attempt to open the file directly in the same folder (as it might be a direct sibling)
						List<String> filesToOpen = new ArrayList<>();
						filesToOpen.add(curWord + ".java");
						filesToOpen.add(curWord + ".groovy");
						if (openFileRelativeToThis("", filesToOpen, CodeLanguage.JAVA, null)) {
							return;
						}
					}
				}
			}

			if (importLine != null) {

				importLine = importLine.substring(7).trim();
				importLine = importLine.substring(0, importLine.length() - 1).trim();

				List<String> openFilePaths = new ArrayList<>();
				openFilePaths.add(importLine.replaceAll("\\.", "/") + ".java");
				openFilePaths.add(importLine.replaceAll("\\.", "/") + ".groovy");
				String basePath = "";

				// get own package name
				int packageAmount = 0;
				for (String curLine : lines) {
					if (curLine.startsWith("package ")) {
						packageAmount = StrUtils.countCharInString('.', curLine);
					}
				}

				// go up packageAmount+1 (so for package foo.bar.nonsense;, go up three)
				for (int i = 0; i < packageAmount+1; i++) {
					basePath = "../" + basePath;
				}

				// now that the path has been resolved, attempt to open that file!
				if (openFileRelativeToThis(basePath, openFilePaths, CodeLanguage.JAVA, importLine)) {
					return;
				}
			}
		}

		// elsewise, do the regular thing (on [Ctrl]-click, jump to next occurrence of the word)
		super.onMouseReleased(event);
	}
}
