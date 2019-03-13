/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.codeeditor.base;

import com.asofterspace.toolbox.codeeditor.utils.CodeLocation;
import com.asofterspace.toolbox.utils.Callback;
import com.asofterspace.toolbox.Utils;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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
	private boolean copyOnCtrlEnter = true;
	private boolean tabEntireBlocks = true;

	// search string - the string that is currently being searched for
	private String searchStr = null;

	// all of the text versions we are aware of
	private List<String> textVersions = new ArrayList<>();

	// the text version we are currently at (not necessarily the latest, through undo)
	private int currentTextVersion = 0;


	public Code(JTextPane editor) {

		super();

		// declare which end of line marker is to be used
		putProperty(DefaultEditorKit.EndOfLineStringProperty, EOL);

		// keep track of the editor we are decorating (useful e.g. to get and set caret pos during insert operations)
		decoratedEditor = editor;

		// no editor was given... great ^^ (we are probably testing!)
		if (editor == null) {
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

				// on [Tab] during selection, indent whole block
				// on [Ctrl / Shift] + [Tab] during selection, unindent whole block
				if (tabEntireBlocks) {
					if ((event.getKeyChar() == KeyEvent.VK_TAB) && (selLength > 0)) {
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

						if (event.isControlDown() || event.isShiftDown()) {
							// un-indent
							replaceAmount = - Utils.countStringInString("\n\t", contentMiddle);

							// the very first line (only) does not start with \n!
							if (lineStart == 0) {
								if (contentMiddle.startsWith("\t")) {
									replaceAmount--;
									selStartOffset--;
									contentMiddle = contentMiddle.substring(1);
								}

							}

							if (contentMiddle.startsWith("\n\t")) {
								selStartOffset--;
							}

							// TODO :: replacing "\n " (four times) is just done as an
							// afterthought, but is not done properly - e.g. the caret
							// pos will behave wonkily...
							contentMiddle = contentMiddle.replace("\n ", "\n");
							contentMiddle = contentMiddle.replace("\n ", "\n");
							contentMiddle = contentMiddle.replace("\n ", "\n");
							contentMiddle = contentMiddle.replace("\n ", "\n");

							contentMiddle = contentMiddle.replace("\n\t", "\n");
						} else {
							// indent
							replaceAmount = Utils.countCharInString('\n', contentMiddle);

							contentMiddle = contentMiddle.replace("\n", "\n\t");

							// the very first line (only) does not start with \n!
							if (lineStart == 0) {
								replaceAmount++;
								contentMiddle = "\t" + contentMiddle;
							}

							selStartOffset++;
						}

						final int newSelEnd = selEnd + replaceAmount;
						final int newSelStart = selStart + selStartOffset;

						content = contentStart + contentMiddle + contentEnd;

						// set the text (this should go through...)
						decoratedEditor.setText(content);

						// ... and prevent the next text change (coming from the \t key)
						if (event.isControlDown() || event.isShiftDown()) {
							preventInsert += 1;
							preventRemove += 1;
						} else {
							preventInsert += 1;
							preventRemove += 1;
						}

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

	public void reorganizeImports() {

		String origText = decoratedEditor.getText();

		String newText = reorganizeImports(origText);

		decoratedEditor.setText(newText);
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

	protected String getLineFromPosition(int pos, String content) {

		int start = getLineStartFromPosition(pos, content);
		int end = getLineEndFromPosition(pos, content);

		return content.substring(start, end);
	}

	private int getLineStartFromPosition(int pos, String content) {

		int lineStart = 0;

		if (pos > 0) {
			lineStart = content.lastIndexOf("\n", pos - 1);
		}

		if (lineStart < 0) {
			lineStart = 0;
		}

		return lineStart;
	}

	private int getLineEndFromPosition(int pos, String content) {

		int lineEnd = content.indexOf("\n", pos);

		if (lineEnd < 0) {
			lineEnd = content.length();
		}

		return lineEnd;
	}

	private synchronized void startHighlightThread() {

		if (highlightThread == null) {
			Thread highlightThread = new Thread(new Runnable() {
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
							return;
						}
					}
				}
			});
			highlightThread.start();
		}
	}

	public void setCopyOnCtrlEnter(boolean value) {
		copyOnCtrlEnter = value;
	}

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

	public Color getForegroundColor() {

		return schemeForegroundColor;
	}

	public Color getBackgroundColor() {

		return schemeBackgroundColor;
	}

	public static void setDarkSchemeForAllEditors() {

		for (Code instance : instances) {

			instance.setDarkScheme();
		}
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

			// ... unless [Ctrl] is being held, as we want to use [Ctrl]+[Enter] = copy the current line
			// TODO - this (and add it as option? but then again, this would need to be configurable, meh...)

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

				// TODO :: in case of e.g. } following the {, add another curLineWhitespace (but without the
				// last append) after the caret pos, such that {} with an [ENTER] pressed in between leads to
				//	 {
				//		 |
				//	 }

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

		int lineAmount = Utils.countCharInString('\n', decoratedEditor.getText());

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
