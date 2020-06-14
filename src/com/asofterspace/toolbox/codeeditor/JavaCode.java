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
import java.util.Map;
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
	protected boolean curMultilineComment;

	protected int curLineStartingWhitespace = 0;

	protected boolean startingWhitespace = false;

	protected String lastCouldBeKeyword = "";


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
		automaticallyAddedImports.put("FocusEvent", "java.awt.event.FocusEvent");
		automaticallyAddedImports.put("FocusListener", "java.awt.event.FocusListener");
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
		automaticallyAddedImports.put("InputStreamReader", "java.io.InputStreamReader");
		automaticallyAddedImports.put("InputStreamWriter", "java.io.InputStreamWriter");
		automaticallyAddedImports.put("IOException", "java.io.IOException");
		automaticallyAddedImports.put("OutputStream", "java.io.OutputStream");
		automaticallyAddedImports.put("OutputStreamReader", "java.io.OutputStreamReader");
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
		automaticallyAddedImports.put("Random", "java.util.Random");
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
		automaticallyAddedImports.put("JRadioButtonMenuItem", "javax.swing.JRadioButtonMenuItem");
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
		for (String line : removeCommentsAndStrings(decoratedEditor.getText()).split("\n")) {
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

			// we explicitly do not want to import e.g. java.io.File if we already have
			// com.asofterspace.toolbox.io.File, even if we are inside com.asofterspace.
			// toolbox.io ourselves, such that the ass io File is not actually present
			// in the final import block!
			List<String> doNotImport = new ArrayList<>();

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
				content = removeCommentsAndStrings(content);
				for (String line : content.split("\n")) {
					line = line.trim();
					// add imports of other files also as possible imports, but guard against
					// multi-line ones
					if ((line.startsWith("import ") && line.contains(".") && line.endsWith(";"))) {
						String thisFullImport = line.substring(7);
						thisFullImport = thisFullImport.substring(0, thisFullImport.length() - 1).trim();
						String thisImportClass = thisFullImport.substring(thisFullImport.lastIndexOf(".") + 1);
						String thisImportPackageStr = thisFullImport.substring(0, thisFullImport.lastIndexOf("."));
						if ((!thisImportPackageStr.equals(ourPackageStr)) && (!thisImportClass.equals("*")) &&
							!thisImportPackageStr.equals("java.lang")) {
							automaticallyAddedImports.put(thisImportClass, thisFullImport);
						}
					}
					// add other source files directly as possible imports
					if (line.startsWith("package ")) {
						packageStr = line.substring(8).trim();
						if (packageStr.endsWith(";")) {
							packageStr = packageStr.substring(0, packageStr.length() - 1).trim();
						}
					}
					if (line.contains(" class ")) {
						classNameStr = line.substring(line.indexOf(" class ") + 7);
					}
					if (line.startsWith("class ")) {
						classNameStr = line.substring(6);
					}
					if (line.contains(" interface ")) {
						classNameStr = line.substring(line.indexOf(" interface ") + 11);
					}
					if (line.startsWith("interface ")) {
						classNameStr = line.substring(10);
					}
					if (line.contains(" enum ")) {
						classNameStr = line.substring(line.indexOf(" enum ") + 6);
					}
					if (line.startsWith("enum ")) {
						classNameStr = line.substring(5);
					}
					// once a class name has been found, interrupt - as there will be no further
					// class name or import
					if (classNameStr != null) {
						if (classNameStr.contains(" ")) {
							classNameStr = classNameStr.substring(0, classNameStr.indexOf(" "));
						}
						if (classNameStr.contains("<")) {
							classNameStr = classNameStr.substring(0, classNameStr.indexOf("<"));
						}
						break;
					}
				}
				if ((packageStr != null) && (classNameStr != null) && packageStr.equals(ourPackageStr)) {
					doNotImport.add(classNameStr);
				}
				// for each file, check if a package and classname were found
				if ((packageStr != null) && (classNameStr != null) && !packageStr.equals(ourPackageStr) &&
					!classNameStr.equals("*") && !packageStr.equals("java.lang")) {
					// we here override java default classes if the opened files contain the same class names...
					// ... which is exactly the behavior we like :)
					automaticallyAddedImports.put(classNameStr, packageStr + "." + classNameStr);
				}
			}

			for (String doNotImportKey : doNotImport) {
				automaticallyAddedImports.remove(doNotImportKey);
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

	@Override
	public String removeCommentsAndStrings(String content) {

		StringBuilder result = new StringBuilder();

		attributeSetting = false;

		int start = 0;
		int prev = 0;
		int end = content.length() - 1;

		while (start <= end) {

			// while we have a delimiter...
			char curChar = content.charAt(start);

			startingWhitespace = false;

			while (isDelimiter(curChar)) {

				// ... check for a comment (which starts with a delimiter)
				if (isCommentStart(content, start, end)) {
					start = highlightComment(content, start, end);

				// ... and check for a quoted string
				} else if (isStringDelimiter(content.charAt(start))) {

					// then let's get that string!
					start = highlightString(content, start, end);

				} else {
					result.append(curChar);
				}

				if (start < end) {

					// jump forward and try again!
					start++;

				} else {
					attributeSetting = true;
					return result.toString();
				}

				curChar = content.charAt(start);
			}

			// or any other token instead?
			prev = start;
			start = highlightOther(content, start, end, false);
			result.append(content.substring(prev, start));
		}

		attributeSetting = true;
		return "";
	}

	// this is the main function that... well... highlights our text :)
	@Override
	protected void highlightText(int start, int length) {

		super.highlightText(start, length);

		String content = "";

		try {
			int end = this.getLength();

			content = this.getText(0, end);

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
						postHighlight(content);
						return;
					}

					curChar = content.charAt(start);
				}

				// or any other token instead?
				start = highlightOther(content, start, end, true);
			}

			postHighlight(content);
			return;

		} catch (BadLocationException e) {
			// oops!
		}

		postHighlight(content);
	}

	protected void postHighlight(String content) {
		updateFunctionList();
	}

	protected boolean isCommentStart(String content, int start, int end) {

		if (start + 1 > end) {
			return false;
		}

		String potentialCommentStart = content.substring(start, start + 2);

		return START_SINGLELINE_COMMENT.equals(potentialCommentStart) || START_MULTILINE_COMMENT.equals(potentialCommentStart);
	}

	protected int highlightComment(String content, int start, int end) {

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

	protected int highlightOther(String content, int start, int end, boolean setAttributesAndDetectFunctions) {

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
							String functionName = StrUtils.getLineFromPosition(start, content);
							functions.add(new CodeSnippetWithLocation(functionName, StrUtils.getLineStartFromPosition(start, content)));
						}
					}
				}
			}
		}

		lastCouldBeKeyword = couldBeKeyword;

		return couldBeKeywordEnd;
	}

	protected boolean isDelimiter(char character) {
		return Character.isWhitespace(character) || OPERAND_CHARS.contains(character) || STRING_DELIMITERS.contains(character);
	}

	protected boolean isStringDelimiter(char character) {
		return STRING_DELIMITERS.contains(character);
	}

	protected boolean isKeyword(String token) {
		return KEYWORDS.contains(token);
	}

	protected boolean isPrimitiveType(String token) {
		return PRIMITIVE_TYPES.contains(token);
	}

	protected boolean isAdvancedType(String token) {
		if (token.length() < 1) {
			return false;
		}
		return Character.isUpperCase(token.charAt(0));
	}

	protected boolean isAnnotation(String token) {
		return token.startsWith("@");
	}

	@Override
	protected void onMouseReleased(MouseEvent event) {

		// if the user [Ctrl]-clicks on an import line, try to jump to that file
		if (event.isControlDown()) {
			int caretPos = decoratedEditor.getCaretPosition();
			String content = decoratedEditor.getText();
			String[] lines = content.split("\n");
			String line = StrUtils.getLineFromPosition(caretPos, content).trim();
			String importLine = null;

			if ((line != null) && line.startsWith("import ") && line.endsWith(";")) {
				// the user clicked on a line like import foo.bar.nonsense.Object;
				importLine = line;
			} else {
				String curWord = StrUtils.getWordFromPosition(caretPos, content);
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
