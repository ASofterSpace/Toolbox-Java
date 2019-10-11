/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.codeeditor.base;

import com.asofterspace.toolbox.codeeditor.utils.CodeLocation;
import com.asofterspace.toolbox.utils.Callback;
import com.asofterspace.toolbox.utils.StrUtils;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
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


public abstract class Code extends DefaultStyledDocument {

	private static final long serialVersionUID = 1L;


	// the callback to be called when something changes
	Callback onChangeCallback;

	// the end-of-line marker
	protected static final String EOL = "\n";

	// the root element of the document, through which we can get the individual lines
	Element root;

	// the editor that is to be decorated by us - and the listeners we associate with it
	// (should most usually be a CodeEditor - but in a pitch any JTextPane will do)
	protected final JTextPane decoratedEditor;
	private KeyAdapter keyListener;
	private CaretListener caretListener;
	private MouseAdapter mouseListener;

	// the (optional) line memo to the left of the code editor, containing the line numbers
	// (should most usually be a CodeEditorLineMemo - but in a pitch any JTextPane will do)
	private JTextPane codeEditorLineMemo;

	// the list of all decorated editors
	static List<Code> instances = new ArrayList<>();

	// the fore- and background colors of the editor
	Color schemeForegroundColor;
	Color schemeBackgroundColor;

	// the font sizes, fonts and tab sets of all editors
	protected int fontSize = 14;
	protected static String editorFontFamily;
	protected Font lastFont;
	protected TabSet lastTabSet;

	// styles for the different kinds of text in the document
	protected MutableAttributeSet attrRegular;
	protected MutableAttributeSet attrBold;
	protected MutableAttributeSet attrSearch;
	protected MutableAttributeSet attrSearchSelected;
	protected MutableAttributeSet attrAnnotation; // @blubb
	protected MutableAttributeSet attrComment; // /* bla blubb */
	protected MutableAttributeSet attrKeyword; // this, null, ...
	protected MutableAttributeSet attrPrimitiveType; // int, boolean, ...
	protected MutableAttributeSet attrAdvancedType; // Integer, Boolean, ...
	protected MutableAttributeSet attrString; // "meow!"
	protected MutableAttributeSet attrReservedChar; // ,.()[]...
	protected MutableAttributeSet attrFunction; // blubb()
	protected MutableAttributeSet attrData; // <![CDATA[...]]>

	// highlight thread and a boolean used to tell it to do some highlighting
	private static Thread highlightThread;
	private volatile boolean pleaseHighlight = false;

	// selection
	private int selStart = 0;
	private int selEnd = 0;
	private int selLength = 0;

	// prevent the next n insertions / removals
	private int preventInsert = 0;
	private int preventRemove = 0;

	// configuration
	// private boolean copyOnCtrlEnter = true;
	private boolean tabEntireBlocks = true;

	// search string - the string that is currently being searched for
	private String searchStr = null;

	// all of the text versions we are aware of
	private List<String> textVersions = new ArrayList<>();

	// the text version we are currently at (not necessarily the latest, through undo)
	private int currentTextVersion = 0;

	// the parent editor, of which this here is a sub-editor
	private Code parentEditor = null;


	public Code(JTextPane editor) {

		super();

		// keep track of the editor we are decorating (useful e.g. to get and set caret pos during insert operations)
		this.decoratedEditor = editor;

		this.parentEditor = this;

		fullyStartupCodeHighlighter();
	}

	// just create this coder without creating any threads etc.
	// intended to be used for sub-highlighters, such as Javascript as part of HTML
	public Code(JTextPane editor, Code parentEditor) {

		super();

		// keep track of the editor we are decorating (useful e.g. to get and set caret pos during insert operations)
		this.decoratedEditor = editor;

		this.parentEditor = parentEditor;

		// initialize the font size, lastFont etc.
		setFontSize(parentEditor.getFontSize());

		// initialize all the attribute sets
		setParentScheme();
	}

	public Code getMe() {

		return parentEditor;
	}

	private void fullyStartupCodeHighlighter() {

		// declare which end of line marker is to be used
		putProperty(DefaultEditorKit.EndOfLineStringProperty, EOL);

		// just testing, apparently!
		if (decoratedEditor == null) {
			return;
		}

		// keep track of the root element
		root = this.getDefaultRootElement();

		// initialize the font size, lastFont etc.
		setFontSize(fontSize);

		// initialize all the attribute sets
		setLightScheme();

		// actually style the editor with... us
		int origCaretPos = decoratedEditor.getCaretPosition();
		String origContent = decoratedEditor.getText();

		textVersions.add(origContent);

		decoratedEditor.setDocument(this);
		decoratedEditor.setText(origContent);
		decoratedEditor.setCaretPosition(origCaretPos);

		synchronized (instances) {
			instances.add(this);
		}

		startHighlightThread();

		keyListener = new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				/*
				// on [Ctrl / Shift] + [Enter], duplicate current row
				if (copyOnCtrlEnter) {
					if ((event.getKeyCode() == KeyEvent.VK_ENTER) && (event.isControlDown() || event.isShiftDown())) {
						int caretPos = decoratedEditor.getCaretPosition();
						String content = decoratedEditor.getText();

						int lineStart = getLineStartFromPosition(caretPos, content);
						int lineEnd = getLineEndFromPosition(caretPos, content);

						try {
							String insertStr = content.substring(lineStart, lineEnd);
							if (!insertStr.startsWith("\n")) {
								insertStr = "\n" + insertStr;
							}
							Code.super.insertString(lineEnd, insertStr, (AttributeSet) attrRegular);
							decoratedEditor.setCaretPosition(caretPos + insertStr.length());
						} catch (BadLocationException e) {
							// oops!
						}
					}
				}
				*/

				// on [Tab] during selection, indent whole block
				// on [Ctrl / Shift] + [Tab] during selection, unindent whole block
				if (tabEntireBlocks) {
					if ((event.getKeyChar() == KeyEvent.VK_TAB)) {
						if (selLength < 1) {
							return;
						}

						if (event.isControlDown() || event.isShiftDown()) {
							unindentSelection(1, false);
						} else {
							indentSelection("\t");
						}

						// ... and prevent the next text change (coming from the \t key)
						preventInsert += 1;
						preventRemove += 1;
					}
				}
			}
		};

		decoratedEditor.addKeyListener(keyListener);

		// keep track of selection
		caretListener = new CaretListener() {
			@Override
			public void caretUpdate(CaretEvent e) {
				if (e.getDot() < e.getMark()) {
					selStart = e.getDot();
					selEnd = e.getMark();
				} else {
					selStart = e.getMark();
					selEnd = e.getDot();
				}
				selLength = selEnd - selStart;
			}
		};

		decoratedEditor.addCaretListener(caretListener);

		MouseAdapter mouseListener = new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent event) {

				if (event.isControlDown()) {
					int caretPos = decoratedEditor.getCaretPosition();
					String content = decoratedEditor.getText();

					int wordStart = getWordStartFromPosition(caretPos, content);
					int wordEnd = getWordEndFromPosition(caretPos, content);

					String clickedWord = content.substring(wordStart, wordEnd);
					// find clicked word and go there
					int nextIndex = content.indexOf(clickedWord, wordEnd);
					if (nextIndex < 0) {
						nextIndex = content.indexOf(clickedWord);
					}
					decoratedEditor.setCaretPosition(nextIndex);
					decoratedEditor.setSelectionStart(nextIndex);
					decoratedEditor.setSelectionEnd(nextIndex + clickedWord.length());
				}
			}
		};

		decoratedEditor.addMouseListener(mouseListener);
	}

	public void indentSelection(String indentWithWhat) {

		indentOrUnindent(true, indentWithWhat, 0, false);
	}

	public void unindentSelection(int levelAmount, boolean forceUnindent) {

		indentOrUnindent(false, "", levelAmount, forceUnindent);
	}

	private void indentOrUnindent(boolean doIndent, String indentWithWhat, int levelAmount, boolean forceUnindent) {

		String content = decoratedEditor.getText();
		int lineStart = getLineStartFromPosition(selStart, content);
		int lineEnd = getLineEndFromPosition(selEnd, content);

		String contentStart = content.substring(0, lineStart);
		String contentMiddle = content.substring(lineStart, lineEnd);
		String contentEnd = content.substring(lineEnd, content.length());

		// calculate this now, before we set the text, as afterwards
		// selStart, selEnd etc. change again! ;)
		int replaceAmount = 0;
		int selStartOffset = 0;

		if (doIndent) {

			contentMiddle = "\n" + contentMiddle;

			replaceAmount = StrUtils.countCharInString('\n', contentMiddle);

			contentMiddle = contentMiddle.replace("\n", "\n" + indentWithWhat);

			contentMiddle = contentMiddle.substring(1);

			selStartOffset++;

		} else {

			// use -1 argument such that trailing \n do not get ignored... see the javadoc, it is confusing! .-.
			String[] middleLines = contentMiddle.split("\n", -1);

			for (int level = 0; level < levelAmount; level++) {
				for (int curLine = 0; curLine < middleLines.length; curLine++) {
					String line = middleLines[curLine];

					if (forceUnindent || line.startsWith("\t")) {
						// line might be empty in case of forceUnindent,
						// so we have to check...
						if (line.length() > 0) {
							line = line.substring(1);
							replaceAmount--;
							if (curLine == 0) {
								selStartOffset--;
							}
						}
					} else if (line.startsWith("    ")) {
						line = line.substring(4);
						replaceAmount -= 4;
					} else if (line.startsWith("   ")) {
						line = line.substring(3);
						replaceAmount -= 3;
					} else if (line.startsWith("  ")) {
						line = line.substring(2);
						replaceAmount -= 2;
					} else if (line.startsWith(" ")) {
						line = line.substring(1);
						replaceAmount -= 1;
					}

					middleLines[curLine] = line;
				}
			}

			contentMiddle = String.join("\n", middleLines);
		}

		content = contentStart + contentMiddle + contentEnd;

		int possibleSelStart = selStart + selStartOffset;
		int possibleSelEnd = selEnd + replaceAmount;

		if (possibleSelStart > content.length()) {
			possibleSelStart = content.length();
		}

		if (possibleSelEnd > content.length()) {
			possibleSelEnd = content.length();
		}

		final int newSelStart = possibleSelStart;
		final int newSelEnd = possibleSelEnd;

		// set the text (this should go through...)
		decoratedEditor.setText(content);

		decoratedEditor.setCaretPosition(newSelEnd);

		// later (!) also restore the selection - but not now, as it would
		// all be replaced... ^^
		Thread selThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(50);
				} catch(InterruptedException e) {
					// Ooops...
				}

				decoratedEditor.setSelectionStart(newSelStart);
				decoratedEditor.setSelectionEnd(newSelEnd);

				selStart = newSelStart;
				selEnd = newSelEnd;
				selLength = selEnd - selStart;

				preventInsert = 0;
				preventRemove = 0;
			}
		});
		selThread.start();

		selStart = newSelStart;
		selEnd = newSelEnd;
		selLength = selEnd - selStart;
	}

	// does this code editor support reporting function names in the code?
	public boolean suppliesFunctions() {
		return false;
	}

	/**
	 * Gets all the functions that have currently been identified
	 */
	public List<CodeLocation> getFunctions() {
		// even though we do not support functions at all, still just return
		// an empty list rather than the much nastier null!
		return new ArrayList<>();
	}

	/**
	 * Set the text field in which function names should be reported
	 */
	public void setFunctionTextField(JTextPane functionPane) {
		// does nothing - as this does not support reporting function names :)
	}

	public void sortDocumentAlphabetically() {

		int origCaretPos = decoratedEditor.getCaretPosition();
		String origText = decoratedEditor.getText();

		String newText = sortLinesAlphabetically(origText);

		decoratedEditor.setText(newText);
		decoratedEditor.setCaretPosition(origCaretPos);
	}

	private String sortLinesAlphabetically(String origText) {

		List<String> lines = Arrays.asList(origText.split("\n"));

		Collections.sort(lines);

		StringBuilder newText = new StringBuilder();

		String newline = "";

		for (String line : lines) {
			newText.append(newline);
			newText.append(line);
			newline = "\n";
		}

		return newText.toString();
	}

	public void sortSelectedLinesAlphabetically() {

		int origCaretPos = decoratedEditor.getCaretPosition();
		String origText = decoratedEditor.getText();

		int dot = decoratedEditor.getCaret().getDot();
		int mark = decoratedEditor.getCaret().getMark();

		if (mark < dot) {
			int exchange = dot;
			dot = mark;
			mark = exchange;
		}

		while (dot >= 0) {
			if (origText.charAt(dot) == '\n') {
				dot++;
				break;
			}
			dot--;
		}

		while (mark < origText.length()) {
			if (origText.charAt(mark) == '\n') {
				break;
			}
			mark++;
		}

		String before = origText.substring(0, dot);
		String middle = origText.substring(dot, mark);
		String after = origText.substring(mark);

		middle = sortLinesAlphabetically(middle);

		decoratedEditor.setText(before + middle + after);
		decoratedEditor.setCaretPosition(origCaretPos);
	}

	public void sortSelectedStringsAlphabetically() {

		int origCaretPos = decoratedEditor.getCaretPosition();
		String origText = decoratedEditor.getText();

		int dot = decoratedEditor.getCaret().getDot();
		int mark = decoratedEditor.getCaret().getMark();

		if (mark < dot) {
			int exchange = dot;
			dot = mark;
			mark = exchange;
		}

		String before = origText.substring(0, dot);
		String middle = origText.substring(dot, mark);
		String after = origText.substring(mark);

		middle = sortStringsAlphabetically(middle);

		decoratedEditor.setText(before + middle + after);
		decoratedEditor.setCaretPosition(origCaretPos);
	}

	private String sortStringsAlphabetically(String origText) {

		origText = origText.replace("\", \"", "\"\n\"");
		origText = origText.replace("\",\"", "\"\n\"");
		origText = origText.replace("\",\n", "\"\n");

		List<String> lines = Arrays.asList(origText.split("\n"));
		List<String> sortlines = new ArrayList<>();

		for (String line : lines) {
			String sortline = line.trim();

			if (!sortline.equals("")) {
				sortlines.add(sortline);
			}
		}

		Collections.sort(sortlines);

		StringBuilder newText = new StringBuilder();

		String newline = "";

		for (String line : sortlines) {
			newText.append(newline);
			newText.append(line);
			newline = ", ";
		}

		return newText.toString();
	}

	public void reorganizeImports() {

		int origCaretPos = decoratedEditor.getCaretPosition();
		String origText = decoratedEditor.getText();

		String newText = reorganizeImports(origText);

		decoratedEditor.setText(newText);
		decoratedEditor.setCaretPosition(origCaretPos);
	}

	/**
	 * This is the stuff that is actually done when imports are reorganized;
	 * we need to have this string-in, string-out available both for testing
	 * and for using this from the outside, with the plain reorganizeImports()
	 * being more a convenience method around it, but this here is the main one!
	 */
	public String reorganizeImports(String origText) {

		// just do nothing :)
		return origText;
	}

	/**
	 * Gets the imports, assuming that they are organized like with a java class
	 * file, but possibly a different importKeyword, in the origText.
	 * Out parameters: outputBeforeImports (the code before the imports start),
	 * imports (a list of actual import lines), outputAfterImports (the code after
	 * the import block ends)
	 */
	protected void getImportsJavalike(String importKeyword, String origText, StringBuilder outputBeforeImports,
		List<String> imports, StringBuilder outputAfterImports) {

		String[] lines = origText.split("\n");

		int curLine = 0;

		for (; curLine < lines.length; curLine++) {
			String line = lines[curLine];
			if (line.startsWith(importKeyword)) {
				break;
			} else {
				outputBeforeImports.append(line);
				outputBeforeImports.append("\n");
			}
		}

		for (; curLine < lines.length; curLine++) {
			String line = lines[curLine];
			if (line.equals("") || line.startsWith(importKeyword)) {
				imports.add(line);
			} else {
				break;
			}
		}

		for (; curLine < lines.length; curLine++) {
			outputAfterImports.append("\n");
			outputAfterImports.append(lines[curLine]);
		}
	}

	/**
	 * This function can be called by extending classes if they want to use
	 * Java-style import organization, but possibly with a different keyword
	 * (such as "using" in C#)
	 */
	protected String reorganizeImportsJavalike(String importKeyword, String origText) {

		StringBuilder output = new StringBuilder();
		List<String> imports = new ArrayList<>();
		StringBuilder secondOutput = new StringBuilder();

		getImportsJavalike(importKeyword, origText, output, imports, secondOutput);


		// prepare the output: we want exactly one empty line after the first line (the package),
		// sooo we first remove all newlines following it, then append two...
		while ((output.length() > 0) && (output.charAt(output.length() - 1) == '\n')) {
			output.setLength(output.length() - 1);
		}
		output.append("\n\n");

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
			if ("".equals(importLine) || lastImport.equals(importLine)) {
				continue;
			}

			// add an empty line between imports in different namespaces
			String thisImportStart = importLine;
			if (importLine.indexOf(".") >= 0) {
				thisImportStart = importLine.substring(0, importLine.indexOf("."));
			} else {
				if (importLine.indexOf(";") >= 0) {
					thisImportStart = importLine.substring(0, importLine.indexOf(";"));
				}
			}
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

	public void removeUnusedImports() {

		int origCaretPos = decoratedEditor.getCaretPosition();
		String origText = decoratedEditor.getText();

		String newText = removeUnusedImports(origText);

		decoratedEditor.setText(newText);
		decoratedEditor.setCaretPosition(origCaretPos);
	}

	/**
	 * This is the stuff that is actually done when unused imports are removed;
	 * we need to have this string-in, string-out available both for testing
	 * and for using this from the outside, with the plain reorganizeImports()
	 * being more a convenience method around it, but this here is the main one!
	 */
	public String removeUnusedImports(String origText) {

		// just do nothing :)
		return origText;
	}

	/**
	 * This function can be called by extending classes if they want to use
	 * Java-style import organization, but possibly with a different keyword
	 */
	protected String removeUnusedImportsJavalike(String importKeyword, String origText) {

		StringBuilder output = new StringBuilder();
		List<String> imports = new ArrayList<>();
		StringBuilder secondOutput = new StringBuilder();

		getImportsJavalike(importKeyword, origText, output, imports, secondOutput);

		String codeContent = secondOutput.toString();

		for (String importLine : imports) {

			// keep empty lines
			if ("".equals(importLine)) {
				output.append("\n");
				continue;
			}

			// transform
			//   import foo.bar.Meow;
			// into
			//   Meow
			String importedClass = importLine.trim();
			if (importedClass.startsWith(importKeyword)) {
				importedClass = importedClass.substring(importKeyword.length()).trim();
			}

			if (importedClass.contains(".")) {
				importedClass = importedClass.substring(importedClass.lastIndexOf(".") + 1);
			}

			if (importedClass.endsWith(";")) {
				importedClass = importedClass.substring(0, importedClass.length() - 1).trim();
			}

			// if the code does not contain the class name
			if (!codeContent.contains(importedClass)) {
				// remove it!
				continue;
			}

			// otherwise, keep it
			output.append(importLine);
			output.append("\n");
		}

		return output.toString() + secondOutput.toString();
	}

	public static String getLineFromPosition(int pos, String content) {

		int start = getLineStartFromPosition(pos, content);
		int end = getLineEndFromPosition(pos, content);

		return content.substring(start, end);
	}

	public static int getLineNumberFromPosition(int pos, String content) {

		int result = 0;
		int until = pos;
		if (content.length() < until) {
			until = content.length();
		}
		for (int i = 0; i < until; i++) {
			char c = content.charAt(i);
			if (c == '\n') {
				result++;
			}
		}
		return result;
	}

	public static String getLineFromNumber(int number, String content) {

		int count = 0;
		int lineStart = 0;
		int lineEnd = 0;

		for (int i = 0; i < content.length(); i++) {
			char c = content.charAt(i);
			if (c == '\n') {
				count++;

				if (count == number) {
					lineStart = i + 1;
				} else if (count == number + 1) {
					lineEnd = i;
					break;
				}
			}
		}

		if (lineEnd < lineStart) {
			return "";
		}

		return content.substring(lineStart, lineEnd);
	}

	public static int getLineStartFromPosition(int pos, String content) {

		int lineStart = 0;

		if (pos > 0) {
			lineStart = content.lastIndexOf("\n", pos - 1) + 1;
		}

		return lineStart;
	}

	public static int getLineEndFromPosition(int pos, String content) {

		int lineEnd = content.indexOf("\n", pos);

		if (lineEnd < 0) {
			lineEnd = content.length();
		}

		return lineEnd;
	}

	public static String getWordFromPosition(int pos, String content) {

		int start = getWordStartFromPosition(pos, content);
		int end = getWordEndFromPosition(pos, content);

		return content.substring(start, end);
	}

	public static int getWordStartFromPosition(int pos, String content) {

		int lineStartSpace = content.lastIndexOf(" ", pos - 1) + 1;
		int lineStartNewline = content.lastIndexOf("\n", pos - 1) + 1;
		int lineStartTab = content.lastIndexOf("\t", pos - 1) + 1;
		int lineStartLAngle = content.lastIndexOf("<", pos - 1) + 1;
		int lineStartRAngle = content.lastIndexOf(">", pos - 1) + 1;
		int lineStartLBracket = content.lastIndexOf("(", pos - 1) + 1;
		int lineStartRBracket = content.lastIndexOf(")", pos - 1) + 1;
		int lineStartLSqBracket = content.lastIndexOf("[", pos - 1) + 1;
		int lineStartRSqBracket = content.lastIndexOf("]", pos - 1) + 1;
		int lineStartLParens = content.lastIndexOf("{", pos - 1) + 1;
		int lineStartRParens = content.lastIndexOf("}", pos - 1) + 1;
		int lineStartDot = content.lastIndexOf(".", pos - 1) + 1;
		int lineStartSemi = content.lastIndexOf(";", pos - 1) + 1;
		int lineStartComma = content.lastIndexOf(",", pos - 1) + 1;

		int lineStart = 0;

		if (lineStartSpace > lineStart) {
			lineStart = lineStartSpace;
		}
		if (lineStartNewline > lineStart) {
			lineStart = lineStartNewline;
		}
		if (lineStartTab > lineStart) {
			lineStart = lineStartTab;
		}
		if (lineStartLAngle > lineStart) {
			lineStart = lineStartLAngle;
		}
		if (lineStartRAngle > lineStart) {
			lineStart = lineStartRAngle;
		}
		if (lineStartLBracket > lineStart) {
			lineStart = lineStartLBracket;
		}
		if (lineStartRBracket > lineStart) {
			lineStart = lineStartRBracket;
		}
		if (lineStartLSqBracket > lineStart) {
			lineStart = lineStartLSqBracket;
		}
		if (lineStartRSqBracket > lineStart) {
			lineStart = lineStartRSqBracket;
		}
		if (lineStartLParens > lineStart) {
			lineStart = lineStartLParens;
		}
		if (lineStartRParens > lineStart) {
			lineStart = lineStartRParens;
		}
		if (lineStartDot > lineStart) {
			lineStart = lineStartDot;
		}
		if (lineStartSemi > lineStart) {
			lineStart = lineStartSemi;
		}
		if (lineStartComma > lineStart) {
			lineStart = lineStartComma;
		}

		return lineStart;
	}

	public static int getWordEndFromPosition(int pos, String content) {

		int lineEndSpace = content.indexOf(" ", pos);
		int lineEndNewline = content.indexOf("\n", pos);
		int lineEndTab = content.indexOf("\t", pos);
		int lineEndLAngle = content.indexOf("<", pos);
		int lineEndRAngle = content.indexOf(">", pos);
		int lineEndLBracket = content.indexOf("(", pos);
		int lineEndRBracket = content.indexOf(")", pos);
		int lineEndLSqBracket = content.indexOf("[", pos);
		int lineEndRSqBracket = content.indexOf("]", pos);
		int lineEndLParens = content.indexOf("{", pos);
		int lineEndRParens = content.indexOf("}", pos);
		int lineEndDot = content.indexOf(".", pos);
		int lineEndSemi = content.indexOf(";", pos);
		int lineEndComma = content.indexOf(",", pos);

		int lineEnd = Integer.MAX_VALUE;

		if ((lineEndSpace >= 0) && (lineEndSpace < lineEnd)) {
			lineEnd = lineEndSpace;
		}
		if ((lineEndNewline >= 0) && (lineEndNewline < lineEnd)) {
			lineEnd = lineEndNewline;
		}
		if ((lineEndTab >= 0) && (lineEndTab < lineEnd)) {
			lineEnd = lineEndTab;
		}
		if ((lineEndLAngle >= 0) && (lineEndLAngle < lineEnd)) {
			lineEnd = lineEndLAngle;
		}
		if ((lineEndRAngle >= 0) && (lineEndRAngle < lineEnd)) {
			lineEnd = lineEndRAngle;
		}
		if ((lineEndLBracket >= 0) && (lineEndLBracket < lineEnd)) {
			lineEnd = lineEndLBracket;
		}
		if ((lineEndRBracket >= 0) && (lineEndRBracket < lineEnd)) {
			lineEnd = lineEndRBracket;
		}
		if ((lineEndLSqBracket >= 0) && (lineEndLSqBracket < lineEnd)) {
			lineEnd = lineEndLSqBracket;
		}
		if ((lineEndRSqBracket >= 0) && (lineEndRSqBracket< lineEnd)) {
			lineEnd = lineEndRSqBracket;
		}
		if ((lineEndLParens >= 0) && (lineEndLParens < lineEnd)) {
			lineEnd = lineEndLParens;
		}
		if ((lineEndRParens >= 0) && (lineEndRParens < lineEnd)) {
			lineEnd = lineEndRParens;
		}
		if ((lineEndDot >= 0) && (lineEndDot < lineEnd)) {
			lineEnd = lineEndDot;
		}
		if ((lineEndSemi >= 0) && (lineEndSemi < lineEnd)) {
			lineEnd = lineEndSemi;
		}
		if ((lineEndComma >= 0) && (lineEndComma < lineEnd)) {
			lineEnd = lineEndComma;
		}

		if (lineEnd == Integer.MAX_VALUE) {
			lineEnd = content.length();
		}

		return lineEnd;
	}

	private synchronized void startHighlightThread() {

		if (highlightThread == null) {
			highlightThread = new Thread(new Runnable() {
				@Override
				public void run() {
					while (true) {
						synchronized (instances) {
							for (Code instance : instances) {
								if (instance.pleaseHighlight) {
									instance.pleaseHighlight = false;
									int len = instance.getLength();
									instance.highlightText(0, len);
									instance.highlightSearch(0, len);
								}
							}
						}
						//Thread.yield();
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							break;
						}
					}
					highlightThread = null;
				}
			});
			highlightThread.start();
		}
	}

	/*
	public void setCopyOnCtrlEnter(boolean value) {
		copyOnCtrlEnter = value;
	}
	*/

	public void setTabEntireBlocks(boolean value) {
		tabEntireBlocks = value;
	}

	public void setOnChange(Callback callback) {

		onChangeCallback = callback;
	}

	public void setCodeEditorLineMemo(JTextPane lineMemo) {

		codeEditorLineMemo = lineMemo;

		// refresh the line numbering in the left memo (if we did not just un-assign one)
		if (codeEditorLineMemo != null) {
			refreshLineNumbering();
		}
	}

	public int getFontSize() {

		return fontSize;
	}

	public void setFontSize(int newSize) {

		fontSize = newSize;

		initializeEditorFont();

		if (editorFontFamily == null) {
			lastFont = new Font("", Font.PLAIN, fontSize);
		} else {
			lastFont = new Font(editorFontFamily, Font.PLAIN, fontSize);
		}

		// calculate the correct tab stops
		Canvas c = new Canvas();
		FontMetrics fm = c.getFontMetrics(lastFont);
		int tabWidth = 4 * fm.charWidth(' ');

		// we create 100 tab stops... that should be enough (per line!)
		TabStop[] tabStops = new TabStop[100];

		for (int i = 0; i < tabStops.length; i++) {
			tabStops[i] = new TabStop((i+1) * tabWidth);
		}

		lastTabSet = new TabSet(tabStops);

		decoratedEditor.setFont(lastFont);

		try {
			Style style = decoratedEditor.getLogicalStyle();
			StyleConstants.setTabSet(style, lastTabSet);
			decoratedEditor.setLogicalStyle(style);
		} catch (NullPointerException npe) {
			System.err.println("Tab set could not be set on decoratedEditor!");
		}

		highlightAllText();
	}

	public static void setFontSizeForAllEditors(int newSize) {

		for (Code instance : instances) {

			instance.setFontSize(newSize);
		}
	}

	public void setLightScheme() {

		schemeForegroundColor = new Color(0, 0, 0);
		schemeBackgroundColor = new Color(255, 255, 255);

		// change the attribute sets
		attrRegular = new SimpleAttributeSet();
		StyleConstants.setForeground(attrRegular, schemeForegroundColor);
		StyleConstants.setBackground(attrRegular, schemeBackgroundColor);

		attrBold = new SimpleAttributeSet();
		StyleConstants.setBold(attrBold, true);

		attrSearch = new SimpleAttributeSet();
		StyleConstants.setForeground(attrSearch, new Color(0, 0, 0));
		StyleConstants.setBackground(attrSearch, new Color(0, 255, 255));

		attrSearchSelected = new SimpleAttributeSet();
		StyleConstants.setForeground(attrSearchSelected, new Color(0, 0, 0));
		StyleConstants.setBackground(attrSearchSelected, new Color(255, 0, 255));

		attrAnnotation = new SimpleAttributeSet();
		StyleConstants.setForeground(attrAnnotation, new Color(0, 128, 64));

		attrComment = new SimpleAttributeSet();
		StyleConstants.setForeground(attrComment, new Color(0, 128, 0));
		StyleConstants.setItalic(attrComment, true);

		attrKeyword = new SimpleAttributeSet();
		StyleConstants.setForeground(attrKeyword, new Color(96, 0, 96));
		StyleConstants.setBold(attrKeyword, true);

		attrPrimitiveType = new SimpleAttributeSet();
		StyleConstants.setForeground(attrPrimitiveType, new Color(0, 0, 128));
		StyleConstants.setBold(attrPrimitiveType, true);

		attrAdvancedType = new SimpleAttributeSet();
		StyleConstants.setForeground(attrAdvancedType, new Color(96, 48, 48));

		attrString = new SimpleAttributeSet();
		StyleConstants.setForeground(attrString, new Color(128, 0, 0));

		attrReservedChar = new SimpleAttributeSet();
		StyleConstants.setForeground(attrReservedChar, new Color(48, 0, 112));
		StyleConstants.setBold(attrReservedChar, true);

		attrFunction = new SimpleAttributeSet();
		StyleConstants.setForeground(attrFunction, new Color(48, 0, 48));

		attrData = new SimpleAttributeSet();
		StyleConstants.setForeground(attrData, new Color(48, 48, 48));

		// re-decorate the editor
		decoratedEditor.setBackground(schemeBackgroundColor);
		decoratedEditor.setCaretColor(schemeForegroundColor);

		highlightAllText();
	}

	public static void setLightSchemeForAllEditors() {

		for (Code instance : instances) {

			instance.setLightScheme();
		}
	}

	public void setDarkScheme() {

		schemeForegroundColor = new Color(255, 255, 255);
		schemeBackgroundColor = new Color(0, 0, 0);

		// change the attribute sets
		attrRegular = new SimpleAttributeSet();
		StyleConstants.setForeground(attrRegular, schemeForegroundColor);
		StyleConstants.setBackground(attrRegular, schemeBackgroundColor);

		attrBold = new SimpleAttributeSet();
		StyleConstants.setBold(attrBold, true);

		attrSearch = new SimpleAttributeSet();
		StyleConstants.setForeground(attrSearch, new Color(255, 255, 255));
		StyleConstants.setBackground(attrSearch, new Color(0, 128, 128));

		attrSearchSelected = new SimpleAttributeSet();
		StyleConstants.setForeground(attrSearchSelected, new Color(255, 255, 255));
		StyleConstants.setBackground(attrSearchSelected, new Color(128, 0, 128));

		attrAnnotation = new SimpleAttributeSet();
		StyleConstants.setForeground(attrAnnotation, new Color(128, 255, 196));

		attrComment = new SimpleAttributeSet();
		StyleConstants.setForeground(attrComment, new Color(128, 255, 128));
		StyleConstants.setItalic(attrComment, true);

		attrKeyword = new SimpleAttributeSet();
		StyleConstants.setForeground(attrKeyword, new Color(172, 64, 255));
		StyleConstants.setBold(attrKeyword, true);

		attrPrimitiveType = new SimpleAttributeSet();
		StyleConstants.setForeground(attrPrimitiveType, new Color(128, 128, 255));
		StyleConstants.setBold(attrPrimitiveType, true);

		attrAdvancedType = new SimpleAttributeSet();
		StyleConstants.setForeground(attrAdvancedType, new Color(255, 188, 188));

		attrString = new SimpleAttributeSet();
		StyleConstants.setForeground(attrString, new Color(255, 128, 128));

		attrReservedChar = new SimpleAttributeSet();
		StyleConstants.setForeground(attrReservedChar, new Color(192, 112, 225));
		StyleConstants.setBold(attrReservedChar, true);

		attrFunction = new SimpleAttributeSet();
		StyleConstants.setForeground(attrFunction, new Color(255, 178, 255));

		attrData = new SimpleAttributeSet();
		StyleConstants.setForeground(attrData, new Color(178, 178, 178));

		// re-decorate the editor
		decoratedEditor.setBackground(schemeBackgroundColor);
		decoratedEditor.setCaretColor(schemeForegroundColor);

		highlightAllText();
	}

	public static void setDarkSchemeForAllEditors() {

		for (Code instance : instances) {

			instance.setDarkScheme();
		}
	}

	public void setParentScheme() {

		schemeForegroundColor = parentEditor.getForegroundColor();
		schemeBackgroundColor = parentEditor.getBackgroundColor();

		// change the attribute sets
		attrRegular = parentEditor.attrRegular;
		attrBold = parentEditor.attrBold;
		attrSearch = parentEditor.attrSearch;
		attrSearchSelected = parentEditor.attrSearchSelected;
		attrAnnotation = parentEditor.attrAnnotation;
		attrComment = parentEditor.attrComment;
		attrKeyword = parentEditor.attrKeyword;
		attrPrimitiveType = parentEditor.attrPrimitiveType;
		attrAdvancedType = parentEditor.attrAdvancedType;
		attrString = parentEditor.attrString;
		attrReservedChar = parentEditor.attrReservedChar;
		attrFunction = parentEditor.attrFunction;
		attrData = parentEditor.attrData;
	}

	public Color getForegroundColor() {

		return schemeForegroundColor;
	}

	public Color getBackgroundColor() {

		return schemeBackgroundColor;
	}

	public void setSearchStr(String searchFor) {

		this.searchStr = searchFor;

		pleaseHighlight = true;
	}

	private static void initializeEditorFont() {

		// if no editor font family has been found yet...
		if (editorFontFamily == null) {

			// ... go hunt for one!
			String[] familiarFontFamilies = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

			// in the worst case, any "monospace" font is better than the default
			for (String fontFamily : familiarFontFamilies) {
				if (fontFamily.toLowerCase().startsWith("monospace")) {
					editorFontFamily = fontFamily;
				}
			}

			// if there is a dedicated console font (Consolas, Lucida Console, ...), use that
			for (String fontFamily : familiarFontFamilies) {
				if (fontFamily.toLowerCase().contains("consol")) {
					editorFontFamily = fontFamily;
				}
			}

			// if there is a dedicated terminal font (Terminus Font, ...), use that
			for (String fontFamily : familiarFontFamilies) {
				if (fontFamily.toLowerCase().startsWith("terminus")) {
					editorFontFamily = fontFamily;
				}
			}

			// if there is Courier New, then yayyy - use that, we like it!
			for (String fontFamily : familiarFontFamilies) {
				if (fontFamily.toLowerCase().replace(" ", "").equals("couriernew")) {
					editorFontFamily = fontFamily;
				}
			}
		}
	}

	/**
	 * A key has been pressed (like, e.g. any letter key - not just the 'A' key literally ^^)
	 */
	@Override
	public void insertString(int offset, String insertedString, AttributeSet attrs) {

		insertString(offset, insertedString, attrs, insertedString.length());
	}

	/**
	 * This is called internally for insertString
	 * In addition to the regular parameters, we also have:
	 * overrideCaretPos - the additional amount to which we should move the caret pos to the right
	 *   because of string changes that have already been performed by an extending class
	 */
	protected void insertString(int offset, String insertedString, AttributeSet attrs, int overrideCaretPos) {

		if (preventInsert > 0) {
			preventInsert--;
			return;
		}

		int origCaretPos = decoratedEditor.getCaretPosition();

		// on enter, step forward as far as there was whitespace in the current line...
		if ("\n".equals(insertedString)) {

			// (unless [Ctrl] is being held, as we want to use [Ctrl]+[Enter] = copy the current line)

			try {
				String content = this.getText(0, offset);

				int startOfThisLine = content.lastIndexOf("\n") + 1;

				StringBuilder curLineWhitespace = new StringBuilder();

				for (int i = startOfThisLine; i < content.length(); i++) {
					char curChar = content.charAt(i);
					if (Character.isWhitespace(curChar)) {
						curLineWhitespace.append(curChar);
					} else {
						break;
					}
				}

				String origWhitespace = curLineWhitespace.toString();

				// in case of {, add indent, and in case of }, remove it
				// TODO ::put this into the individual programming languages
				if (content.endsWith("{") ||
					content.endsWith("[") ||
					content.endsWith("(") ||
					content.endsWith("begin") ||
					content.endsWith("then")) {
					if (origWhitespace.endsWith(" ")) {
						curLineWhitespace.append("	");
					} else {
						curLineWhitespace.append("\t");
					}
				}
				/*
				if (content.endsWith("}") ||
					content.endsWith("]") ||
					content.endsWith(")") ||
					content.endsWith("end;")) {
					if (curLineWhitespace.toString().endsWith(" ")) {
						curLineWhitespace.setLength(Math.max(0, curLineWhitespace.length() - 4));
					} else {
						curLineWhitespace.setLength(Math.max(0, curLineWhitespace.length() - 1));
					}
				}
				*/

				insertedString += curLineWhitespace.toString();
				overrideCaretPos += curLineWhitespace.length();

				// in case of e.g. } following the {, add another curLineWhitespace (but without the
				// last append) after the caret pos, such that {} with an [ENTER] pressed in between leads to
				//	 {
				//		 |
				//	 }

				if (content.endsWith("{") ||
					content.endsWith("[") ||
					content.endsWith("(") ||
					content.endsWith("begin") ||
					content.endsWith("then")) {
					int len = "end;".length();
					len = Math.min(len, this.getLength() - offset);
					if (len > 0) {
						String followedBy = this.getText(offset, len);
						if (followedBy.startsWith("}") ||
							followedBy.startsWith("]") ||
							followedBy.startsWith(")") ||
							followedBy.startsWith("end;")) {
							insertedString += "\n" + origWhitespace;
						}
					}
				}

			} catch (BadLocationException e) {
				// oops!
			}
		}

		try {
			super.insertString(offset, insertedString, attrs);
		} catch (BadLocationException e) {
			// oops!
		}

		/*
		// highlightText(offset, insertedString.length());
		highlightAllText();

		callOnChange();
		*/

		// the caret does not need to be set, if we are inserting exactly as long a string as we
		// want to move it, that THAT move is already done internally automagically
		if (overrideCaretPos != insertedString.length()) {
			decoratedEditor.setCaretPosition(origCaretPos + overrideCaretPos);
		}
	}

	/**
	 * The delete key or something like that has been pressed
	 */
	@Override
	public void remove(int offset, int length) {

		if (preventRemove > 0) {
			preventRemove--;
			return;
		}

		try {
			super.remove(offset, length);
		} catch (BadLocationException e) {
			// oops!
		}

		/*
		// highlightText(offset, 0);
		highlightAllText();

		callOnChange();
		*/
	}

	/**
	 * Paste has been pressed (as in copy-paste)
	 */
	@Override
	protected void fireInsertUpdate(DocumentEvent event) {

		super.fireInsertUpdate(event);

		// highlightText(event.getOffset(), event.getLength());
		highlightAllText();

		callOnChange();
	}

	/**
	 * Cut has been pressed (as in cut-paste)
	 */
	@Override
	protected void fireRemoveUpdate(DocumentEvent event) {

		super.fireRemoveUpdate(event);

		// highlightText(event.getOffset(), event.getLength());
		highlightAllText();

		callOnChange();
	}

	protected void callOnChange() {

		// call the on change callback (if there is one)
		if (onChangeCallback != null) {
			onChangeCallback.call();
		}

		// refresh the line numbering in the left memo (if there is one)
		if (codeEditorLineMemo != null) {
			refreshLineNumbering();
		}

		// add text version to the undo cache
		String nextVersion = decoratedEditor.getText();
		String currentVersion = textVersions.get(currentTextVersion);

		// only do this if the new version is not empty!
		if (nextVersion.equals("")) {
			return;
		}

		// only do this if the new version is not the current version!
		// (to minimize the amount of data stored, but also to not re-store
		// the changed when undo and redo are pressed etc.)
		if (nextVersion.equals(currentVersion)) {
			return;
		}

		// if we did some undoing, and are now doing something else, then remove
		// the alternative timeline
		if (currentTextVersion < textVersions.size() - 1) {
			textVersions = textVersions.subList(0, currentTextVersion);
		}

		textVersions.add(nextVersion);

		currentTextVersion = textVersions.size() - 1;
	}

	protected void highlightAllText() {

		pleaseHighlight = true;
	}

	/**
	 * Calls the highlighter now.
	 * Intended for usage in automated tests (as they can synchronously wait
	 * for the result), NOT for usage outside of them!
	 */
	public void highlightAllTextNow() {

		int len = getLength();
		highlightText(0, len);
	}

	// this is the main function that... well... highlights our text :)
	// you might want to override it ;)
	protected void highlightText(int start, int length) {

		int end = this.getLength();

		// set the entire document back to regular
		this.setCharacterAttributes(0, end, attrRegular, true);
	}

	// highlight for search - which is called by the highlighting thread always
	// AFTER all the other highlightings have been performed!
	protected void highlightSearch(int start, int length) {

		if (searchStr == null) {
			return;
		}

		if ("".equals(searchStr)) {
			return;
		}

		try {
			String content = this.getText(0, length);

			int caretPos = decoratedEditor.getCaretPosition();

			int searchLen = searchStr.length();

			int nextPos = content.indexOf(searchStr);

			while (nextPos >= 0) {

				if ((caretPos >= nextPos) && (caretPos <= nextPos + searchLen)) {
					this.setCharacterAttributes(nextPos, searchLen, attrSearchSelected, true);
				} else {
					this.setCharacterAttributes(nextPos, searchLen, attrSearch, true);
				}

				nextPos = content.indexOf(searchStr, nextPos + 1);
			}

		} catch (BadLocationException e) {
			// oops!
		}
	}

	private int lastLineAmount = 0;

	/**
	 * Refresh the line numbering in the connected line memo - only call this function
	 * when you already know that the codeEditorLineMemo is not null! As it could very
	 * well be!
	 */
	private void refreshLineNumbering() {

		int lineAmount = StrUtils.countCharInString('\n', decoratedEditor.getText());

		if (lineAmount != lastLineAmount) {
			lastLineAmount = lineAmount;

			StringBuilder lines = new StringBuilder();
			for (int i = 1; i <= lineAmount + 1; i++) {
				lines.append(i + "\n");
			}
			codeEditorLineMemo.setText(lines.toString());
		}
	}

	/**
	 * Call this to detach the code highlighter from its text field,
	 * stop sending update callbacks and enable it to be garbage collected
	 * (this is meant to be called if you wish to change the highlighter
	 * for a code editor - in that case, discard the old one before you
	 * attach the new one, as otherwise both will handle updates and get
	 * in each other's way!)
	 */
	public void discard() {

		onChangeCallback = null;

		codeEditorLineMemo = null;

		if (decoratedEditor != null) {
			if (keyListener != null) {
				decoratedEditor.removeKeyListener(keyListener);
			}
			if (caretListener != null) {
				decoratedEditor.removeCaretListener(caretListener);
			}
			if (mouseListener != null) {
				decoratedEditor.removeMouseListener(mouseListener);
			}
		}

		synchronized (instances) {
			instances.remove(this);
		}
	}

	public void undo() {

		int origCaretPos = decoratedEditor.getCaretPosition();

		currentTextVersion--;

		if (currentTextVersion < 0) {
			currentTextVersion = 0;
		}

		decoratedEditor.setText(textVersions.get(currentTextVersion));

		decoratedEditor.setCaretPosition(origCaretPos);
	}

	public void redo() {

		int origCaretPos = decoratedEditor.getCaretPosition();

		currentTextVersion++;

		if (currentTextVersion > textVersions.size() - 1) {
			currentTextVersion = textVersions.size() - 1;
		}

		decoratedEditor.setText(textVersions.get(currentTextVersion));

		decoratedEditor.setCaretPosition(origCaretPos);
	}

}
