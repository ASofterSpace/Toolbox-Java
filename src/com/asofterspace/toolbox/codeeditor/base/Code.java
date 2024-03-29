/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.codeeditor.base;

import com.asofterspace.toolbox.codeeditor.utils.CanonicalJavaLikeImport;
import com.asofterspace.toolbox.codeeditor.utils.CodeAtLocation;
import com.asofterspace.toolbox.codeeditor.utils.CodeField;
import com.asofterspace.toolbox.codeeditor.utils.CodeLanguage;
import com.asofterspace.toolbox.codeeditor.utils.CodeSnippetWithLocation;
import com.asofterspace.toolbox.codeeditor.utils.OpenFileCallback;
import com.asofterspace.toolbox.gui.CodeEditor;
import com.asofterspace.toolbox.utils.Callback;
import com.asofterspace.toolbox.utils.DateUtils;
import com.asofterspace.toolbox.utils.Pair;
import com.asofterspace.toolbox.utils.SortOrder;
import com.asofterspace.toolbox.utils.SortUtils;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
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
	OpenFileCallback onOpenFileCallback;

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
	protected MutableAttributeSet attrMatchingBrackets; // ( ... )
	protected MutableAttributeSet attrSuspicious; // mark unused things as suspicious
	protected MutableAttributeSet attrSqlKeyword; // mark SQL keywords inside strings
	protected MutableAttributeSet attrSqlFunction; // mark SQL function names inside strings

	// highlight thread and a boolean used to tell it to do some highlighting
	private static Thread highlightThread;
	private volatile boolean pleaseHighlight = false;
	private volatile boolean pleaseDoNotHighlightThisRound = false;
	private volatile boolean activityDetected = false;

	// do we highlight functions which the user is currently in or not?
	protected boolean doFunctionHighlighting = true;

	// selection
	private int selStart = 0;
	private int selEnd = 0;
	private int selLength = 0;
	private int prevSelStart = 0;

	// prevent the next n insertions / removals
	private int preventInsert = 0;
	private int preventRemove = 0;

	// configuration
	// private boolean copyOnCtrlEnter = true;
	private boolean tabEntireBlocks = true;
	private boolean proposeTokenAutoComplete = true;

	// search string - the string that is currently being searched for
	private String searchStr = null;

	// when searching, are we ignoring case?
	private boolean searchIgnoreCase = false;

	// when searching, are we using an asterisk?
	private boolean searchUseAsterisk = false;

	// all of the text versions we are aware of
	private List<CodeAtLocation> textVersions = new ArrayList<>();

	// the text version we are currently at (not necessarily the latest, through undo)
	private int currentTextVersion = 0;

	// the parent editor, of which this here is a sub-editor
	private Code parentEditor = null;

	// a map of imported classes and their package names which should be automatically added if missing
	protected Map<String, String> automaticallyAddedImports;

	// enable or disable setting attributes
	protected boolean attributeSetting = true;

	// the filename of the file we are working on, if we are given it
	protected String filename = null;
	protected String localFilename = null;
	protected String localFilenameWithoutExtension = null;

	// keep of track of brackets that have been highlighted before,
	// so that they do not need to be highlighted again
	private Integer lastBracketStart = null;
	private Integer lastBracketEnd = null;

	private String defaultIndentationStr = null;

	protected List<String> nextEncounteredTokens = null;
	protected List<String> encounteredTokens = null;

	// keep track of variable names and when a variable name has been encountered just once,
	// highlight it as suspicious
	protected Set<String> variableNamesSeveralTimes;
	protected Map<String, Integer> variableNamesOnce;

	// keep track of encountered strings as long as collectStrings is true
	private boolean collectStrings = false;
	private List<String> collectedStrings;

	// keep track of encountered errors
	private List<String> errorList = new ArrayList<>();


	public Code(JTextPane editor) {

		super();

		// keep track of the editor we are decorating (useful e.g. to get and set caret pos during insert operations)
		this.decoratedEditor = editor;

		this.parentEditor = this;

		this.automaticallyAddedImports = new HashMap<>();

		fullyStartupCodeHighlighter();
	}

	// just create this coder without creating any threads etc.
	// intended to be used for sub-highlighters, such as Javascript as part of HTML
	public Code(JTextPane editor, Code parentEditor) {

		super();

		// keep track of the editor we are decorating (useful e.g. to get and set caret pos during insert operations)
		this.decoratedEditor = editor;

		this.parentEditor = parentEditor;

		this.automaticallyAddedImports = new HashMap<>();

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
		CodeAtLocation orig = new CodeAtLocation(origContent, origCaretPos);

		textVersions.add(orig);

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
				// on [Ctrl / Shift] + [Enter], duplicate current row - this is commented out but still works in
				// the assEditor, as it is actually handled by the MainMenu calling AugFileTab > duplicateCurrentLine()
				if (copyOnCtrlEnter) {
					if ((event.getKeyCode() == KeyEvent.VK_ENTER) && (event.isControlDown() || event.isShiftDown())) {
						int caretPos = decoratedEditor.getCaretPosition();
						String content = decoratedEditor.getText();

						int lineStart = StrUtils.getLineStartFromPosition(caretPos, content);
						int lineEnd = StrUtils.getLineEndFromPosition(caretPos, content);

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


				// [F1] to add „“
				if (event.getKeyCode() == KeyEvent.VK_F1) {
					insertTextForFunctionKey("„“");
					event.consume();
					return;
				}

				// [F2] to add “”
				if (event.getKeyCode() == KeyEvent.VK_F2) {
					insertTextForFunctionKey("“”");
					event.consume();
					return;
				}

				// [F3] to add ‚‘
				if (event.getKeyCode() == KeyEvent.VK_F3) {
					insertTextForFunctionKey("‚‘");
					event.consume();
					return;
				}

				// [F4] to add ‘’
				if (event.getKeyCode() == KeyEvent.VK_F4) {
					insertTextForFunctionKey("‘’");
					event.consume();
					return;
				}

				// [F5] to add ’ (as that is useful more often than ‘’)
				if (event.getKeyCode() == KeyEvent.VK_F5) {
					insertTextForFunctionKey("’");
					event.consume();
					return;
				}

				// [F6] to add a date-time-stamp
				if (event.getKeyCode() == KeyEvent.VK_F6) {
					insertTextForFunctionKey(DateUtils.getCurrentDateTimeStamp());
					event.consume();
					return;
				}


				boolean proposeTokens = (decoratedEditor instanceof CodeEditor) && proposeTokenAutoComplete;

				if (proposeTokens && (event.getKeyCode() == KeyEvent.VK_UP)) {
					CodeEditor codeEditor = (CodeEditor) decoratedEditor;
					int setTo = codeEditor.getProposedTokenSelection() - 1;
					if (codeEditor.getProposedTokens() != null) {
						if (codeEditor.getProposedTokens().size() > 0) {
							if (setTo < 0) {
								setTo = codeEditor.getProposedTokens().size() - 1;
							}
							codeEditor.setProposedTokenSelection(setTo);
							int extraPrevSelStart = codeEditor.getTokenSelStart();
							decoratedEditor.setCaretPosition(extraPrevSelStart);
							selStart = extraPrevSelStart;
							prevSelStart = extraPrevSelStart;
							// codeEditor.setTokenSelStart(extraPrevSelStart);
							Thread selThread = new Thread(new Runnable() {
								@Override
								public void run() {
									try {
										Thread.sleep(50);
									} catch(InterruptedException e) {
										// Ooops...
									}
									decoratedEditor.setFocusable(true);
									decoratedEditor.requestFocus();
									decoratedEditor.setCaretPosition(extraPrevSelStart);
									decoratedEditor.setSelectionStart(extraPrevSelStart);
								}
							});
							selThread.start();
							// decoratedEditor.setCaretPosition(selStart);
							decoratedEditor.setFocusable(false);
							return;
						}
					}
				}

				if (proposeTokens && (event.getKeyCode() == KeyEvent.VK_DOWN)) {
					CodeEditor codeEditor = (CodeEditor) decoratedEditor;
					int setTo = codeEditor.getProposedTokenSelection() + 1;
					if (codeEditor.getProposedTokens() != null) {
						if (codeEditor.getProposedTokens().size() > 0) {
							if (setTo >= codeEditor.getProposedTokens().size()) {
								setTo = 0;
							}
							codeEditor.setProposedTokenSelection(setTo);
							int extraPrevSelStart = codeEditor.getTokenSelStart();
							decoratedEditor.setCaretPosition(extraPrevSelStart);
							selStart = extraPrevSelStart;
							prevSelStart = extraPrevSelStart;
							// codeEditor.setTokenSelStart(extraPrevSelStart);
							Thread selThread = new Thread(new Runnable() {
								@Override
								public void run() {
									try {
										Thread.sleep(50);
									} catch(InterruptedException e) {
										// Ooops...
									}
									decoratedEditor.setFocusable(true);
									decoratedEditor.requestFocus();
									decoratedEditor.setCaretPosition(extraPrevSelStart);
									decoratedEditor.setSelectionStart(extraPrevSelStart);
								}
							});
							selThread.start();
							decoratedEditor.setFocusable(false);
							// decoratedEditor.setCaretPosition(selStart);
							// codeEditorLineMemo.setFocus();
							return;
						}
					}
				}

				if (proposeTokens && (event.getKeyChar() == KeyEvent.VK_TAB)) {
					List<String> propTokens = ((CodeEditor) decoratedEditor).getProposedTokens();
					if ((propTokens != null) && (propTokens.size() > 0)) {
						String txt = decoratedEditor.getText();
						String token = propTokens.get(((CodeEditor) decoratedEditor).getProposedTokenSelection());
						int nextSelStart = selStart + token.length();
						// e.g. for System.out.println(); and console.log(); put the cursor inside the brackets,
						// but not for new ArrayList<>();
						if (token.endsWith(".println();") || token.endsWith(".log();")) {
							nextSelStart -= 2;
						}
						decoratedEditor.setText(
							txt.substring(0, selStart) +
							token +
							txt.substring(selStart)
						);
						decoratedEditor.setCaretPosition(nextSelStart);
						((CodeEditor) decoratedEditor).setProposedTokens(null);

						// ... and prevent the next text change (coming from the \t key)
						preventInsert += 1;
						// (we here do not prevent the removal, as we are not removing anything, as nothing
						// is selected - and even if we were, we would want to remove it)

						return;
					}
				}

				// on [Tab] during selection, indent whole block
				// on [Ctrl / Shift] + [Tab] during selection, unindent whole block
				if (tabEntireBlocks) {
					if ((event.getKeyChar() == KeyEvent.VK_TAB)) {
						if (selLength < 1) {
							return;
						}

						if (event.isControlDown() || event.isShiftDown()) {
							unindentSelection(1, false, null);
						} else {
							indentSelection("\t");
						}

						// ... and prevent the next text change (coming from the \t key)
						// (we prevent insertion AND removal, as we have several lines selected which would be
						// deleted if we allowed removal, and as we do not want to insert the tab character)
						preventInsert += 1;
						preventRemove += 1;
					}
				}

				if (!proposeTokens) {
					return;
				}

				((CodeEditor) decoratedEditor).setProposedTokens(null);

				// propose tokens for auto-complete
				if ((selLength == 0) && (encounteredTokens != null)) {
					String txt = decoratedEditor.getText();
					StringBuilder curToken = new StringBuilder();

					for (int i = selStart - 1; i >= 0; i--) {
						char c = txt.charAt(i);
						if ((c == ' ') || (c == '\t') || (c == '\n') || (c == '\r') || (c == '.') || (c == '(') || (c == ')') || (c == '!')) {
							if (selStart - i < 2) {
								return;
							}
							curToken.reverse();
							curToken.append(event.getKeyChar());
							String curTokenStr = curToken.toString();

							// ensure each token is only proposed once by going to a set
							Set<String> ourEncounteredTokenSet = new HashSet<>();
							ourEncounteredTokenSet.addAll(encounteredTokens);

							/*
							// add each line within this file - trimmed - as a tab proposal
							String[] lines = txt.split("\n");
							for (String line : lines) {
								ourEncounteredTokenSet.add(line.trim());
							}
							// actually, this slows things down a lot and is not helpful,
							// as it adds too much nonsense that we do not want to select -
							// better to show fewer suggestions of higher quality than to
							// show a million suggestions that are... less good :D
							*/

							// make tokens sortable by going back to an array
							ArrayList<String> ourEncounteredTokens = new ArrayList<>();
							ourEncounteredTokens.addAll(ourEncounteredTokenSet);

							Collections.sort(ourEncounteredTokens, new Comparator<String>() {
								public int compare(String a, String b) {
									if (a.length() == b.length()) {
										return a.toLowerCase().compareTo(b.toLowerCase());
									}
									return b.length() - a.length();
								}
							});

							ArrayList<String> proposedTokens = new ArrayList<>();
							for (String encounteredToken : ourEncounteredTokens) {
								if ((encounteredToken != null) && encounteredToken.startsWith(curTokenStr) &&
									!encounteredToken.equals(curTokenStr)) {
									proposedTokens.add(encounteredToken.substring(curTokenStr.length()));
								}
							}

							((CodeEditor) decoratedEditor).setProposedTokens(proposedTokens);
							((CodeEditor) decoratedEditor).setTokenSelStart(selStart + 1);
							((CodeEditor) decoratedEditor).setProposedTokenSelection(0);
							return;
						}
						curToken.append(c);
					}
				}
			}
		};

		decoratedEditor.addKeyListener(keyListener);

		// keep track of selection
		caretListener = new CaretListener() {
			@Override
			public void caretUpdate(CaretEvent event) {
				onCaretUpdate(event);
			}
		};

		decoratedEditor.addCaretListener(caretListener);

		MouseAdapter mouseListener = new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent event) {
				onMouseReleased(event);
				if (decoratedEditor instanceof CodeEditor) {
					((CodeEditor) decoratedEditor).setProposedTokens(null);
				}
			}

			@Override
			public void mouseClicked(MouseEvent event) {
				onMouseClicked(event);
			}
		};

		decoratedEditor.addMouseListener(mouseListener);
	}

	private void insertTextForFunctionKey(String textToInsert) {
		String txt = decoratedEditor.getText();
		int newSelStart = selStart + textToInsert.length();
		decoratedEditor.setText(
			txt.substring(0, selStart) +
			textToInsert +
			txt.substring(selEnd)
		);

		Thread selThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(50);
				} catch(InterruptedException e) {
					// Ooops...
				}
				decoratedEditor.setFocusable(true);
				decoratedEditor.requestFocus();
				decoratedEditor.setCaretPosition(newSelStart);
				decoratedEditor.setSelectionStart(newSelStart);
				decoratedEditor.setSelectionEnd(newSelStart);

				selStart = newSelStart;
				selEnd = newSelStart;
				selLength = 0;
			}
		});
		selThread.start();

		selStart = newSelStart;
		selEnd = newSelStart;
		selLength = 0;
	}

	protected void onCaretUpdate(CaretEvent e) {
		prevSelStart = selStart;
		if (e.getDot() < e.getMark()) {
			selStart = e.getDot();
			selEnd = e.getMark();
		} else {
			selStart = e.getMark();
			selEnd = e.getDot();
		}
		selLength = selEnd - selStart;

		activityDetected = true;
	}

	boolean highlightedSomething = false;
	boolean highlightedSomethingLastTime = false;

	private void highlightMatchingBrackets() {

		String text = decoratedEditor.getText();
		highlightedSomething = false;
		try {
			highlightMatchingBrackets(selStart, text);
		} catch (IllegalStateException ex) {
			// whoops, we were not allowed to highlight here...
		}
		if ((!highlightedSomething) && highlightedSomethingLastTime) {
			pleaseHighlight = true;
			highlightedSomethingLastTime = false;
		}
	}

	private void highlightMatchingBrackets(int selStart, String text) {

		int round = 0;
		int square = 0;
		int squiggle = 0;

		while ((text.length() > selStart) && (selStart > 0)) {

			char curChar = text.charAt(selStart);

			switch (curChar) {
				case '(':
					round++;
					break;
				case ')':
					round--;
					if (round < 0) {
						highlightMatchingBracketRight('(', curChar, selStart, text);
						return;
					}
					break;
				case '[':
					square++;
					break;
				case ']':
					square--;
					if (square < 0) {
						highlightMatchingBracketRight('[', curChar, selStart, text);
						return;
					}
					break;
				case '{':
					squiggle++;
					break;
				case '}':
					squiggle--;
					if (squiggle < 0) {
						highlightMatchingBracketRight('{', curChar, selStart, text);
						return;
					}
					break;
			}

			selStart++;
		}
	}

	/*
	private void highlightMatchingBracketLeft(char foundChar, char searchChar, int selStart, String text) {

		int depth = 1;
		for (int i = selStart + 1; i < text.length(); i++) {
			if (text.charAt(i) == foundChar) {
				depth++;
			}
			if (text.charAt(i) == searchChar) {
				depth--;
			}
			if (depth < 1) {
				// if we still have a lingering highlighting of the previous matched brackets,
				// re-highlight the entire text to get rid of it
				if (highlightedSomethingLastTime) {
					pleaseHighlight = false;
					highlightText(0, text.length());
					highlightSearch(0, text.length());
				}
				this.setCharacterAttributes(selStart, 1, attrMatchingBrackets, false);
				this.setCharacterAttributes(i, 1, attrMatchingBrackets, false);
				highlightedSomething = true;
				highlightedSomethingLastTime = true;
				return;
			}
		}
	}
	*/

	private void highlightMatchingBracketRight(char searchChar, char foundChar, int selStart, String text) {

		int depth = 1;
		for (int i = selStart - 1; i >= 0; i--) {
			if (text.charAt(i) == foundChar) {
				depth++;
			}
			if (text.charAt(i) == searchChar) {
				depth--;
			}
			if (depth < 1) {
				if ((lastBracketStart != null) && ((int) lastBracketStart == selStart) &&
					(lastBracketEnd != null) && ((int) lastBracketEnd == i)) {
					// do nothing, as this is already highlighted, except for setting
					// "highlightedSomething" because we did just highlight a thing...
					// which was already highlighted...
					// by not doing anything at all xD
					highlightedSomething = true;
					return;
				}

				// if we still have a lingering highlighting of the previous matched brackets,
				// re-highlight the entire text to get rid of it
				if (highlightedSomethingLastTime) {
					pleaseHighlight = false;
					highlightText(0, text.length());
					highlightSearch(0, text.length());
				}
				this.setCharacterAttributes(selStart, 1, attrMatchingBrackets, false);
				this.setCharacterAttributes(i, 1, attrMatchingBrackets, false);
				highlightedSomething = true;
				highlightedSomethingLastTime = true;
				lastBracketStart = selStart;
				lastBracketEnd = i;
				return;
			}
		}
	}

	/**
	 * When a word is pressed while [Ctrl] is held,
	 * jump to the next occurrence of the selected word
	 */
	protected void onMouseReleased(MouseEvent event) {
		if (event.isControlDown()) {
			int caretPos = decoratedEditor.getCaretPosition();
			String content = decoratedEditor.getText();

			int wordStart = StrUtils.getWordStartFromPosition(caretPos, content, true);
			int wordEnd = StrUtils.getWordEndFromPosition(caretPos, content, true);

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

	@SuppressWarnings("deprecation")
	protected void onMouseClicked(MouseEvent event) {

		if (SwingUtilities.isLeftMouseButton(event) &&
			(event.getClickCount() > 1) &&
			(event.getClickCount() < 5)) {

			// in the future, viewToModel2D is used instead, but we want to be backwards compatible...
			int caretPos = decoratedEditor.viewToModel(event.getPoint());;
			String content = decoratedEditor.getText();

			int start = 0;
			int end = 0;

			switch (event.getClickCount()) {

				// on double click...
				case 2:

					// ... select the current word!
					start = StrUtils.getWordStartFromPosition(caretPos, content, true);
					end = StrUtils.getWordEndFromPosition(caretPos, content, true);
					break;

				// on triple click...
				case 3:

					// ... select the current word cluster!
					// (e.g. like foo.bar)
					start = StrUtils.getWordStartFromPosition(caretPos, content, false);
					end = StrUtils.getWordEndFromPosition(caretPos, content, false);
					break;

				// on quadruple click...
				case 4:

					// ... select the current line!
					start = StrUtils.getLineStartFromPosition(caretPos, content);
					end = StrUtils.getLineEndFromPosition(caretPos, content);
					break;
			}

			decoratedEditor.setCaretPosition(start);
			decoratedEditor.setSelectionStart(start);
			decoratedEditor.setSelectionEnd(end);
		}
	}

	public void extractString(boolean addPrefix) {

		String content = decoratedEditor.getText();
		int strStart = selStart;
		int strEnd = selStart + 1;
		char strDel = '"';
		// go left to the start
		while (strStart >= 0) {
			strDel = content.charAt(strStart);
			if ((strDel == '"') || (strDel == '\'') || (strDel == '`')) {
				break;
			}
			strStart--;
		}
		// if no string delimiter cannot be found, return entirely
		if (strStart == 0) {
			return;
		}
		// go right to the end
		int len = content.length();
		while (strEnd < len) {
			char cur = content.charAt(strEnd);
			if (cur == strDel) {
				break;
			}
			strEnd++;
		}

		// "fooBar"
		String origStrWithDel = content.substring(strStart, strEnd + 1);

		// we are only extracting a single string, so we have no previous fields
		Set<String> extractStringExistingFields = new HashSet<>();

		content = extractString(content, origStrWithDel, "\n\n", addPrefix, extractStringExistingFields);

		decoratedEditor.setText(content);
	}

	public void extractAllStrings(boolean addPrefix) {
		extractAllStrings(false, addPrefix);
	}

	public void extractAllRepeatedStrings(boolean addPrefix) {
		extractAllStrings(true, addPrefix);
	}

	public void extractAllStrings(boolean onlyExtractRepeatedStrings, boolean addPrefix) {

		String content = decoratedEditor.getText();

		collectedStrings = new ArrayList<>();
		collectStrings = true;

		removeCommentsAndStrings(content);

		collectStrings = false;

		// only extract each string once :)
		Set<String> stringsToExtract = new HashSet<>(collectedStrings);

		if (onlyExtractRepeatedStrings) {
			stringsToExtract = SortUtils.getElementsMoreThanOnceInCollection(collectedStrings);
		}

		List<String> listOfStringsToExtract = SortUtils.reverse(SortUtils.sortAlphabetically(stringsToExtract));

		String lineSep = "\n";

		// we are extracting several strings, so we are resetting the existing fields
		// set just once here before calling extractString again and again
		Set<String> extractStringExistingFields = new HashSet<>();

		for (int i = 0; i < listOfStringsToExtract.size(); i++) {
			String stringToExtract = listOfStringsToExtract.get(i);
			if (i == listOfStringsToExtract.size() - 1) {
				lineSep = "\n\n";
			}
			content = extractString(content, stringToExtract, lineSep, addPrefix, extractStringExistingFields);
		}

		decoratedEditor.setText(content);
	}

	private static String extractString(String content, String origStrWithDel, String lineSep, boolean addPrefix,
		Set<String> extractStringExistingFields) {

		// fooBar
		String origStr = origStrWithDel.substring(1, origStrWithDel.length() - 1);

		// FOO_BAR
		String fieldName = "";
		boolean justencounteredupcase = true;
		for (int i = 0; i < origStr.length(); i++) {
			char c = origStr.charAt(i);
			if (c == Character.toUpperCase(c)) {
				if (!justencounteredupcase) {
					fieldName += "_";
				}
				justencounteredupcase = true;
			} else {
				justencounteredupcase = false;
			}
			switch (c) {
				case '-':
					fieldName += "_MINUS_";
					break;
				case '+':
					fieldName += "_PLUS_";
					break;
				case '.':
					fieldName += "_DOT_";
					break;
				case ',':
					fieldName += "_COMMA_";
					break;
				case '<':
					fieldName += "_LT_";
					break;
				case '>':
					fieldName += "_GT_";
					break;
				case '&':
					fieldName += "_AND_";
					break;
				case '#':
					fieldName += "_HASHTAG_";
					break;
				case '|':
					fieldName += "_PIPE_";
					break;
				case '\\':
					fieldName += "_BACKSLASH_";
					break;
				case '/':
					fieldName += "_SLASH_";
					break;
				case '"':
					fieldName += "_QUOT_";
					break;
				case '\'':
					fieldName += "_APOSTROPHE_";
					break;
				case '$':
					fieldName += "_DOLLAR_";
					break;
				case '€':
					fieldName += "_EURO_";
					break;
				case ' ':
				case '*':
				case '?':
				case '!':
				case ':':
				case ';':
				case '=':
				case '[':
				case ']':
				case '(':
				case ')':
				case '{':
				case '}':
					fieldName += "_";
					break;
				default:
					fieldName += Character.toUpperCase(c);
					break;
			}
		}

		// consolidate underscores in field names
		fieldName = StrUtils.replaceAllRepeatedly(fieldName, "__", "_");
		while (fieldName.startsWith("_")) {
			fieldName = fieldName.substring(1);
		}
		while (fieldName.endsWith("_")) {
			fieldName = fieldName.substring(0, fieldName.length() - 1);
		}

		if ("".equals(fieldName)) {
			fieldName = "STR";
		}

		// prevent using the same field name for different strings that just happen to have
		// similar (but slightly different) contents
		String baseFieldName = fieldName;
		int i = 1;
		while (extractStringExistingFields.contains(fieldName)) {
			i++;
			fieldName = baseFieldName + "_" + i;
		}
		extractStringExistingFields.add(fieldName);

		String fieldNameInText = fieldName;

		if (addPrefix) {
			fieldNameInText = "StrConstants." + fieldNameInText;
		}

		content = StrUtils.replaceAll(content, origStrWithDel, fieldNameInText);

		if (content.contains("{")) {
			// Java-ish language
			int pos = content.indexOf("{");
			content = content.substring(0, pos + 1) + lineSep + "\tprivate static final String " + fieldName +
				" = " + origStrWithDel + ";" + content.substring(pos + 1);
		} else {
			// generic other language
			content = "const " + fieldName + " = " + origStrWithDel + ";" + lineSep + content;
		}

		return content;
	}

	public void indentSelection(String indentWithWhat) {

		indentOrUnindent(true, indentWithWhat, 0, false);
	}

	public void unindentSelection(int levelAmount, boolean forceUnindent, String unindentWithWhat) {

		indentOrUnindent(false, unindentWithWhat, levelAmount, forceUnindent);
	}

	private void indentOrUnindent(boolean doIndent, String indentWithWhat, int levelAmount, boolean forceUnindent) {

		String content = decoratedEditor.getText();
		int lineStart = StrUtils.getLineStartFromPosition(selStart, content);
		int lineEnd = StrUtils.getLineEndFromPosition(selEnd, content);

		String contentStart = content.substring(0, lineStart);
		String contentMiddle = content.substring(lineStart, lineEnd);
		String contentEnd = content.substring(lineEnd, content.length());

		// calculate this now, before we set the text, as afterwards
		// selStart, selEnd etc. change again! ;)
		int replaceAmount = 0;
		int selStartOffset = 0;

		if (doIndent) {

			replaceAmount = StrUtils.countCharInString('\n', contentMiddle) + 1;

			StringBuilder newMiddle = new StringBuilder();
			boolean encounteredNewline = true;
			for (int i = 0; i < contentMiddle.length(); i++) {
				char c = contentMiddle.charAt(i);
				if ((c != ' ') && (c != '\t') && (c != '\r')) {
					if (encounteredNewline) {
						encounteredNewline = false;
						newMiddle.append(indentWithWhat);
					}
					if (c == '\n') {
						encounteredNewline = true;
					}
				}
				newMiddle.append(c);
			}

			contentMiddle = newMiddle.toString();

			selStartOffset++;

		} else {

			// use -1 argument such that trailing \n do not get ignored... see the javadoc, it is confusing! .-.
			String[] middleLines = contentMiddle.split("\n", -1);

			for (int level = 0; level < levelAmount; level++) {

				boolean didNotReplaceAny = true;

				for (int curLine = 0; curLine < middleLines.length; curLine++) {
					String line = middleLines[curLine];

					boolean replacedSomeInThisLine = true;

					if (forceUnindent) {
						// line might be empty in case of forceUnindent,
						// so we have to check...
						if (line.length() > 0) {
							line = line.substring(1);
							replaceAmount--;
							if (curLine == 0) {
								selStartOffset--;
							}
						}
					} else {

						if (" ".equals(indentWithWhat)) {

							replacedSomeInThisLine = false;

							// check if the line starts with " " or "\t " or "\t\t " or ...
							// and remove the trailing space
							for (int i = 0; i < line.length(); i++) {
								StringBuilder repStrBuilder = new StringBuilder();
								for (int j = 0; j < i; j++) {
									repStrBuilder.append("\t");
								}
								repStrBuilder.append(" ");
								if (line.startsWith(repStrBuilder.toString())) {
									line = line.substring(0, i) + line.substring(i + 1);
									replaceAmount -= 1;
									replacedSomeInThisLine = true;
									break;
								}
							}

							if (!replacedSomeInThisLine) {
								// now check, from longest to shortest, back down, and if there is
								// a \t somewhere at the start, replace the last \t with four spaces
								// and remove one of them (so basically replace it with three)
								for (int i = line.length(); i >= 0; i--) {
									StringBuilder repStrBuilder = new StringBuilder();
									for (int j = 0; j < i; j++) {
										repStrBuilder.append("\t");
									}
									if (line.startsWith(repStrBuilder.toString())) {
										if (i > 0) {
											line = line.substring(0, i - 1) + "   " + line.substring(i);
										}
										replaceAmount += 2;
										replacedSomeInThisLine = true;
										break;
									}
								}
							}

						} else {

							if (line.startsWith("\t")) {
								line = line.substring(1);
								replaceAmount--;
								if (curLine == 0) {
									selStartOffset--;
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
							} else {
								replacedSomeInThisLine = false;
							}
						}
					}

					if (replacedSomeInThisLine) {
						didNotReplaceAny = false;
					}

					middleLines[curLine] = line;
				}

				if (didNotReplaceAny) {
					break;
				}
			}

			contentMiddle = StrUtils.join("\n", middleLines);
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
	public List<CodeSnippetWithLocation> getFunctions() {
		// even though we do not support functions at all, still just return
		// an empty list rather than the much nastier null!
		return new ArrayList<>();
	}

	/**
	 * Gets the function that has last been clicked on
	 */
	public CodeSnippetWithLocation getClickedFunction() {
		return null;
	}

	/**
	 * Highlight the function in which the cursor currently happens to be
	 */
	protected void highlightCurrentFunction() {
		// implemented in FunctionSupplyingCode
	}

	public void startFunctionHighlighting() {
		this.doFunctionHighlighting = true;
	}

	public void stopFunctionHighlighting() {
		this.doFunctionHighlighting = false;
	}

	/**
	 * Set the text field in which function names should be reported
	 */
	public void setFunctionTextField(JTextPane functionPane) {
		// does nothing - as this does not support reporting function names :)
	}

	public void sortDocument(SortOrder sortOrder) {

		int origCaretPos = decoratedEditor.getCaretPosition();
		String origText = decoratedEditor.getText();

		String newText = sortLines(origText, sortOrder);

		decoratedEditor.setText(newText);
		decoratedEditor.setCaretPosition(origCaretPos);
	}

	private String sortLines(String origText, SortOrder sortOrder) {

		List<String> lines = Arrays.asList(origText.split("\n"));

		lines = SortUtils.sort(lines, sortOrder);

		StringBuilder newText = new StringBuilder();

		String newline = "";

		for (String line : lines) {
			newText.append(newline);
			newText.append(line);
			newline = "\n";
		}

		return newText.toString();
	}

	public void sortSelectedLines(SortOrder sortOrder) {

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

		middle = sortLines(middle, sortOrder);

		decoratedEditor.setText(before + middle + after);
		decoratedEditor.setCaretPosition(origCaretPos);
	}

	public void sortSelectedStrings(SortOrder sortOrder) {

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

		middle = sortStrings(middle, sortOrder);

		decoratedEditor.setText(before + middle + after);
		decoratedEditor.setCaretPosition(origCaretPos);
	}

	private String sortStrings(String origText, SortOrder sortOrder) {

		origText = origText.replace("\", \"", "\"\n\"");
		origText = origText.replace("\",\"", "\"\n\"");
		origText = origText.replace("\",\n", "\"\n");

		String preText = "";
		String posText = "";
		while (origText.startsWith("(") || origText.startsWith("[") || origText.startsWith("{") || origText.startsWith("<")) {
			preText += origText.charAt(0);
			origText = origText.substring(1);
		}
		while (origText.endsWith("(") || origText.endsWith("[") || origText.endsWith("{") || origText.endsWith("<")) {
			posText = origText.charAt(0) + posText;
			origText = origText.substring(0, origText.length() - 1);
		}

		List<String> lines = Arrays.asList(origText.split("\n"));
		List<String> sortlines = new ArrayList<>();

		for (String line : lines) {
			String sortline = line.trim();

			if (!sortline.equals("")) {
				sortlines.add(sortline);
			}
		}

		sortlines = SortUtils.sort(sortlines, sortOrder);

		StringBuilder newText = new StringBuilder();

		String newline = "";

		for (String line : sortlines) {
			newText.append(newline);
			newText.append(line);
			newline = ", ";
		}

		return preText + newText.toString() + posText;
	}

	public void addMissingImports() {

		int origCaretPos = decoratedEditor.getCaretPosition();
		String origText = decoratedEditor.getText();

		String newText = addMissingImports(origText);

		decoratedEditor.setText(newText);
		decoratedEditor.setCaretPosition(origCaretPos);
	}

	public String addMissingImports(String origText) {
		return origText;
	}

	public String addMissingImportsJavalike(String importKeyword, String origText) {

		int insertAt = origText.indexOf("package");
		if (insertAt < 0) {
			insertAt = 0;
		}
		insertAt = origText.indexOf(";", insertAt) + 1;
		if (insertAt < 0) {
			insertAt = 0;
		}
		insertAt = origText.indexOf("\n", insertAt) + 1;
		if (insertAt < 0) {
			insertAt = 0;
		}

		String contentBefore = origText.substring(0, insertAt);
		String contentAfter = origText.substring(insertAt);

		StringBuilder contentMiddle = new StringBuilder();

		List<String> alreadyImported = new ArrayList<>();
		getImportsJavalike(importKeyword, origText, null, alreadyImported, null);

		origText = removeCommentsAndStrings(contentAfter);

		for (Map.Entry<String, String> entry : automaticallyAddedImports.entrySet()) {
			addJavaUtilImport(origText, alreadyImported, contentMiddle, entry.getKey(), entry.getValue(), importKeyword);
		}

		return contentBefore + contentMiddle + contentAfter;
	}

	private void addJavaUtilImport(String origText, List<String> alreadyImported, StringBuilder contentMiddle, String utility, String fullUtility, String importKeyword) {

		// if we somehow ended up with "Foo<Bar>" as utility, replace with just "Foo"
		if (utility.contains("<") && fullUtility.contains("<")) {
			utility = utility.substring(0, utility.indexOf("<"));
			fullUtility = fullUtility.substring(0, fullUtility.indexOf("<"));
		}

		// if something already imports this, do not import it from a different (wrong!) source again!
		for (String alreadyImportedStr : alreadyImported) {
			if (alreadyImportedStr.endsWith("." + utility) || alreadyImportedStr.endsWith("." + utility + ";")) {
				return;
			}
		}

		// do not import nio Path if javax.ws.rs.* is already imported
		if ("Path".equals(utility)) {
			for (String alreadyImportedStr : alreadyImported) {
				if (alreadyImportedStr.equals("import javax.ws.rs.*") || alreadyImportedStr.equals("import javax.ws.rs.*;")) {
					return;
				}
			}
		}

		if (origText.contains(" " + utility + "<") || origText.contains("\t" + utility + "<") ||
			origText.contains("<" + utility + ",") || origText.contains("<" + utility + ">") ||
			origText.contains(" " + utility + ">") || origText.contains("," + utility + ">") ||
			origText.contains(" " + utility + " ") || origText.contains("\t" + utility + " ") ||
			origText.contains(" " + utility + "(") || origText.contains("\t" + utility + "(") ||
			origText.contains(" " + utility + ".") || origText.contains("\n" + utility + ".") ||
			origText.contains("\t" + utility + ".") ||
			origText.contains("(" + utility + " ") || origText.contains("(" + utility + ".") ||
			origText.contains("(" + utility + "<") || origText.contains("!" + utility + ".") ||
			origText.contains("!" + utility + "(") ||origText.contains(" instanceof " + utility + ";") ||
			origText.contains("(" + utility + ")") || origText.contains(" instanceof " + utility + ")") ||
			origText.contains("@" + utility + "\n") || origText.contains("@" + utility + "(")) {
			if (!origText.contains(importKeyword + " " + fullUtility + ";")) {
				contentMiddle.append(importKeyword + " " + fullUtility + ";\n");
			}
		}
	}

	public void reorganizeImports() {

		int origCaretPos = decoratedEditor.getCaretPosition();
		String origText = decoratedEditor.getText();

		String newText = reorganizeImports(origText);

		decoratedEditor.setText(newText);
		decoratedEditor.setCaretPosition(origCaretPos);
	}

	public void reorganizeImportsCompatible() {

		int origCaretPos = decoratedEditor.getCaretPosition();
		String origText = decoratedEditor.getText();

		String newText = reorganizeImportsCompatible(origText);

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
	 * This is the same as reorganizeImports, but generating imports organized
	 * in a way that is compatible with wonky external IDEs ;)
	 */
	public String reorganizeImportsCompatible(String origText) {

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
				if (outputBeforeImports != null) {
					outputBeforeImports.append(line);
					outputBeforeImports.append("\n");
				}
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

		if (outputAfterImports == null) {
			return;
		}

		String sep = "";
		for (; curLine < lines.length; curLine++) {
			outputAfterImports.append(sep);
			outputAfterImports.append(lines[curLine]);
			sep = "\n";
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

		if (imports.size() < 1) {
			output.append("\n");
			return output.toString() + secondOutput.toString();
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
		output.append("\n");
		if (i > 0) {
			output.append("\n");
		}

		return output.toString() + secondOutput.toString();
	}

	/**
	 * This function can be called by extending classes if they want to use
	 * Java-style import organization, but possibly with a different keyword
	 * (such as "using" in C#)
	 */
	protected String reorganizeImportsCompatibleJavalike(final String importKeyword, String origText) {

		final String staticKeyword = "static";

		StringBuilder output = new StringBuilder();
		List<String> imports = new ArrayList<>();
		StringBuilder secondOutput = new StringBuilder();

		getImportsJavalike(importKeyword, origText, output, imports, secondOutput);


		// prepare the output: we want exactly one empty line after the first line (the package),
		// sooo we first remove all newlines following it, then append two...
		while ((output.length() > 0) && (output.charAt(output.length() - 1) == '\n')) {
			output.setLength(output.length() - 1);
		}

		if (imports.size() < 1) {
			output.append("\n");
			return output.toString() + secondOutput.toString();
		}

		output.append("\n\n");

		// put imports into two different groups: one starting with java, one not doing so
		List<String> javaImports = new ArrayList<>();
		List<String> otherImports = new ArrayList<>();
		List<String> javaStaticImports = new ArrayList<>();
		List<String> otherStaticImports = new ArrayList<>();
		for (String curImport : imports) {
			if (curImport.startsWith(importKeyword + " java") ||
				curImport.startsWith(importKeyword + " " + staticKeyword + " java")) {
				if (curImport.startsWith(importKeyword + " " + staticKeyword + " ")) {
					javaStaticImports.add(curImport);
				} else {
					javaImports.add(curImport);
				}
			} else {
				if (curImport.startsWith(importKeyword + " " + staticKeyword + " ")) {
					otherStaticImports.add(curImport);
				} else {
					otherImports.add(curImport);
				}
			}
		}

		// sort imports mostly alphabetically, but not completely, so sort e.g.:
		// import foo.bar.Bat;
		// import foo.bar.adala.Cat;
		// import foo.bar.adala.Dog;
		// so a class is always sorted before a package name, even though Bat (b) comes after adala (a)
		Comparator<String> wonkyComparator = new Comparator<String>() {
			public int compare(String a, String b) {
				CanonicalJavaLikeImport aImp = new CanonicalJavaLikeImport(a, importKeyword, staticKeyword);
				CanonicalJavaLikeImport bImp = new CanonicalJavaLikeImport(b, importKeyword, staticKeyword);
				// if only one is static, that one gets sorted behind...
				// if both are or are not static, compare as usual instead...
				if (aImp.isStatic() && !bImp.isStatic()) {
					return 1;
				}
				if (!aImp.isStatic() && bImp.isStatic()) {
					return -1;
				}
				a = aImp.getImport();
				b = bImp.getImport();
				String aPackage = getPackageJavalike(a);
				String bPackage = getPackageJavalike(b);
				// in case of same packages, compare normally
				if (aPackage.equals(bPackage)) {
					return a.compareTo(b);
				}
				// in case of one package containing the other, sort them that way
				if (aPackage.startsWith(bPackage)) {
					return 1;
				}
				if (bPackage.startsWith(aPackage)) {
					return -1;
				}
				// in case of different packages, sort by package, but ensure that
				// javax.foo.bar comes before java.foo.bar
				if (aPackage.startsWith("javax.") && bPackage.startsWith("java.")) {
					return -1;
				}
				if (aPackage.startsWith("java.") && bPackage.startsWith("javax.")) {
					return 1;
				}
				return aPackage.compareTo(bPackage);
			}
		};
		Collections.sort(javaImports, wonkyComparator);
		Collections.sort(otherImports, wonkyComparator);
		Collections.sort(javaStaticImports, wonkyComparator);
		Collections.sort(otherStaticImports, wonkyComparator);


		int i = 0;

		String lastImport = "";

		for (String importLine : otherImports) {

			// remove duplicates
			if ("".equals(importLine) || lastImport.equals(importLine)) {
				continue;
			}

			// actually add the import
			output.append(importLine);
			output.append("\n");
			lastImport = importLine;
			i++;
		}

		if (i > 0) {
			output.append("\n");
		}
		i = 0;

		for (String importLine : javaImports) {

			// remove duplicates
			if ("".equals(importLine) || lastImport.equals(importLine)) {
				continue;
			}

			// actually add the import
			output.append(importLine);
			output.append("\n");
			lastImport = importLine;
			i++;
		}

		if (i > 0) {
			output.append("\n");
		}
		i = 0;

		for (String importLine : otherStaticImports) {

			// remove duplicates
			if ("".equals(importLine) || lastImport.equals(importLine)) {
				continue;
			}

			// actually add the import
			output.append(importLine);
			output.append("\n");
			lastImport = importLine;
			i++;
		}

		if (i > 0) {
			output.append("\n");
		}
		i = 0;

		for (String importLine : javaStaticImports) {

			// remove duplicates
			if ("".equals(importLine) || lastImport.equals(importLine)) {
				continue;
			}

			// actually add the import
			output.append(importLine);
			output.append("\n");
			lastImport = importLine;
			i++;
		}

		// actually have two empty lines between the import end and the class start
		output.append("\n");
		if (i > 0) {
			output.append("\n");
		}

		return output.toString() + secondOutput.toString();
	}

	/**
	 * Assumes that the input has already been canonicized
	 * (It will not be canonicized again here)
	 */
	private String getPackageJavalike(String val) {
		if (val.contains(".")) {
			val = val.substring(0, val.lastIndexOf(".") + 1);
		}
		return val;
	}

	public void removeUnusedImports() {

		int origCaretPos = decoratedEditor.getCaretPosition();
		String origText = decoratedEditor.getText();

		String newText = removeUnusedImports(origText);

		decoratedEditor.setText(newText);
		decoratedEditor.setCaretPosition(origCaretPos);
	}

	/**
	 * Take the original source code and return the same code, but with comments
	 * and strings removed, to only get the actual code itself
	 * Particular classes should override this
	 */
	public String removeCommentsAndStrings(String origText) {

		// just do nothing :)
		return origText;
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

		String codeContent = removeCommentsAndStrings(secondOutput.toString());

		// we first want to find for all import foo.bar.* just foo.bar.,
		// such that we later know that we can drop foo.bar.Fluff, as it
		// is already included...
		List<String> importStarPackages = new ArrayList<>();

		for (String importLine : imports) {

			String importedClass = importLine.trim();

			if (importedClass.endsWith(";")) {
				importedClass = importedClass.substring(0, importedClass.length() - 1).trim();
			}

			if (importedClass.startsWith(importKeyword) && importedClass.endsWith("*")) {
				importedClass = importedClass.substring(importKeyword.length()).trim();
				importedClass = importedClass.substring(0, importedClass.length() - 1).trim();
				importStarPackages.add(importedClass);
			}
		}

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

			String curPackage = "";
			if (importedClass.contains(".")) {
				curPackage = importedClass.substring(0, importedClass.lastIndexOf(".") + 1);
				importedClass = importedClass.substring(importedClass.lastIndexOf(".") + 1);
			}

			if (importedClass.endsWith(";")) {
				importedClass = importedClass.substring(0, importedClass.length() - 1).trim();
			}

			// if imported class is not just an asterisk...
			if (!"*".equals(importedClass)) {
				// ... and if the code does not contain the class name
				if (!codeContent.contains(importedClass)) {
					// remove it!
					continue;
				}
				// ... also, if the same package is already included for a star, remove it!
				boolean doRemove = false;
				for (String importStarPackage : importStarPackages) {
					if (importStarPackage.equals(curPackage)) {
						doRemove = true;
					}
				}
				if (doRemove) {
					continue;
				}
			}

			// otherwise, keep it
			output.append(importLine);
			output.append("\n");
		}

		return output.toString() + secondOutput.toString();
	}

	public void automagicallyAddSemicolons() {

		int origCaretPos = decoratedEditor.getCaretPosition();
		String origText = decoratedEditor.getText();

		String newText = automagicallyAddSemicolons(origText);

		decoratedEditor.setText(newText);
		decoratedEditor.setCaretPosition(origCaretPos);
	}

	public String automagicallyAddSemicolons(String origText) {

		// just do nothing :)
		return origText;
	}

	private synchronized void startHighlightThread() {

		if (highlightThread == null) {
			highlightThread = new Thread(new Runnable() {
				@Override
				public void run() {
					while (true) {
						synchronized (instances) {
							for (Code instance : instances) {
								// while we are entering text, we do not highlight anything, but instead
								// JUST focus on entering text quickly... so in that case, skip until
								// next round!
								if (instance.pleaseDoNotHighlightThisRound) {
									instance.pleaseDoNotHighlightThisRound = false;
									continue;
								}
								if (instance.pleaseHighlight) {
									instance.pleaseHighlight = false;
									int len = instance.getLength();
									instance.highlightText(0, len);
									instance.highlightSearch(0, len);
								}
								if (instance.activityDetected) {
									instance.highlightMatchingBrackets();
									instance.highlightCurrentFunction();
									instance.activityDetected = false;
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

	public void setProposeTokenAutoComplete(boolean value) {
		proposeTokenAutoComplete = value;
	}

	public void setOnChange(Callback callback) {
		onChangeCallback = callback;
	}

	public void setOnOpenFile(OpenFileCallback callback) {
		onOpenFileCallback = callback;
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

		attrBold = new SimpleAttributeSet();
		StyleConstants.setBold(attrBold, true);

		attrSearch = new SimpleAttributeSet();
		StyleConstants.setForeground(attrSearch, new Color(0, 0, 0));
		StyleConstants.setBackground(attrSearch, new Color(0, 255, 255));
		StyleConstants.setBold(attrSearch, true);

		attrSearchSelected = new SimpleAttributeSet();
		StyleConstants.setForeground(attrSearchSelected, new Color(0, 0, 0));
		StyleConstants.setBackground(attrSearchSelected, new Color(255, 0, 255));
		StyleConstants.setBold(attrSearchSelected, true);

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

		attrMatchingBrackets = new SimpleAttributeSet();
		StyleConstants.setForeground(attrMatchingBrackets, new Color(255, 0, 0));

		attrSuspicious = new SimpleAttributeSet();
		StyleConstants.setStrikeThrough(attrSuspicious, true);

		attrSqlKeyword = new SimpleAttributeSet();
		StyleConstants.setForeground(attrSqlKeyword, new Color(128, 96, 0));

		attrSqlFunction = new SimpleAttributeSet();
		StyleConstants.setForeground(attrSqlFunction, new Color(156, 48, 12));

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

		attrBold = new SimpleAttributeSet();
		StyleConstants.setBold(attrBold, true);

		attrSearch = new SimpleAttributeSet();
		StyleConstants.setForeground(attrSearch, new Color(255, 255, 255));
		StyleConstants.setBackground(attrSearch, new Color(0, 128, 128));
		StyleConstants.setBold(attrSearch, true);

		attrSearchSelected = new SimpleAttributeSet();
		StyleConstants.setForeground(attrSearchSelected, new Color(255, 255, 255));
		StyleConstants.setBackground(attrSearchSelected, new Color(128, 0, 128));
		StyleConstants.setBold(attrSearchSelected, true);

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

		attrMatchingBrackets = new SimpleAttributeSet();
		StyleConstants.setForeground(attrMatchingBrackets, new Color(255, 0, 0));

		attrSuspicious = new SimpleAttributeSet();
		StyleConstants.setStrikeThrough(attrSuspicious, true);

		attrSqlKeyword = new SimpleAttributeSet();
		StyleConstants.setForeground(attrSqlKeyword, new Color(255, 196, 0));

		attrSqlFunction = new SimpleAttributeSet();
		StyleConstants.setForeground(attrSqlFunction, new Color(255, 64, 48));

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
		attrSuspicious = parentEditor.attrSuspicious;
		attrSqlKeyword = parentEditor.attrSqlKeyword;
		attrSqlFunction = parentEditor.attrSqlFunction;
	}

	public Color getForegroundColor() {

		return schemeForegroundColor;
	}

	public Color getBackgroundColor() {

		return schemeBackgroundColor;
	}

	public void setSearchIgnoreCase(boolean value) {
		this.searchIgnoreCase = value;
	}

	public void setSearchUseAsterisk(boolean value) {
		this.searchUseAsterisk = value;
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

	public void setDefaultIndentation(String defaultIndentationStr) {
		this.defaultIndentationStr = defaultIndentationStr;
	}

	/**
	 * This is called internally for insertString
	 * In addition to the regular parameters, we also have:
	 * overrideCaretPos - the additional amount to which we should move the caret pos to the right
	 *   because of string changes that have already been performed by an extending class
	 */
	protected void insertString(int offset, String insertedString, AttributeSet attrs, int overrideCaretPos) {

		pleaseDoNotHighlightThisRound = true;

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

				boolean encounteredSomething = false;

				int startOfThisLine = content.lastIndexOf("\n") + 1;

				String line = content.substring(startOfThisLine, offset);
				String trimLine = line.trim();

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

				// in case of case "blubb":, indent with extra whitespace
				if (content.endsWith(":") && (trimLine.startsWith("case ") || trimLine.equals("default:"))) {
					appendWhitespace(curLineWhitespace, defaultIndentationStr, origWhitespace);
					encounteredSomething = true;
				}

				// in case of {, add indent, and in case of }, remove it
				// TODO ::put this into the individual programming languages
				if (trimLine.endsWith("{") ||
					trimLine.endsWith("[") ||
					trimLine.endsWith("(") ||
					trimLine.endsWith("begin") ||
					trimLine.endsWith("then")) {
					appendWhitespace(curLineWhitespace, defaultIndentationStr, origWhitespace);
					encounteredSomething = true;
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

				// if we are pressing enter after "Foo bar = " or "Foo bar =" (without even typing new!)...
				if (trimLine.endsWith("=")) {
					if (offset > 0) {
						String fullContent = decoratedEditor.getText();
						String additionalStr = "new";
						if (line.endsWith("=")) {
							additionalStr = " new";
						}
						// ... potentially enter new Foo(); fully automatically after it!
						int instOffset = addNewObjectInstantiation(fullContent, offset, additionalStr, false);
						if (instOffset > 0) {
							offset += instOffset;
							decoratedEditor.setCaretPosition(offset + curLineWhitespace.toString().length());
							offset += 2;
							encounteredSomething = true;
						}
					}
				}

				// if we are not already do something funny, check if the line starts with return or throw,
				// and if yes, add closed curly brace on the next line
				if (!encounteredSomething) {
					if (trimLine.startsWith("return ") || trimLine.startsWith("return;") ||
						trimLine.equals("return") || trimLine.startsWith("throw ")) {
						// remove one level of indentation
						if (curLineWhitespace.length() >= 1) {
							if (curLineWhitespace.substring(curLineWhitespace.length() - 1).equals("\t")) {
								curLineWhitespace.delete(curLineWhitespace.length() - 1, curLineWhitespace.length());
							} else {
								if (curLineWhitespace.length() >= 4) {
									if (curLineWhitespace.substring(curLineWhitespace.length() - 4).equals("    ")) {
										curLineWhitespace.delete(curLineWhitespace.length() - 4, curLineWhitespace.length());
									}
								}
							}
						}
						curLineWhitespace.append("}");
					}
				}

				insertedString += curLineWhitespace.toString();
				overrideCaretPos += curLineWhitespace.length();

				// in case of e.g. } following the {, add another curLineWhitespace (but without the
				// last append) after the caret pos, such that {} with an [ENTER] pressed in between leads to
				//	 {
				//		 |
				//	 }

				if (trimLine.endsWith("{") ||
					trimLine.endsWith("[") ||
					trimLine.endsWith("(") ||
					trimLine.endsWith("begin") ||
					trimLine.endsWith("then")) {
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
		// want to move it, as THAT move is already done internally automagically
		if (overrideCaretPos != insertedString.length()) {
			decoratedEditor.setCaretPosition(origCaretPos + overrideCaretPos);
		}
	}

	private void appendWhitespace(StringBuilder curLineWhitespace, String defaultIndentationStr, String origWhitespace) {
		if (defaultIndentationStr == null) {
			if (origWhitespace.endsWith(" ")) {
				curLineWhitespace.append("    ");
			} else {
				curLineWhitespace.append("\t");
			}
		} else {
			curLineWhitespace.append(defaultIndentationStr);
		}
	}

	protected void insertStringJavalike(int offset, String insertedString, AttributeSet attrs) {

		int overrideCaretPos = insertedString.length();

		// automagically close brackets that are being opened
		switch (insertedString) {
			case "{":
				// automagically add some empty cases and a default for a switch statement
				String content = decoratedEditor.getText();
				int lineStart = StrUtils.getLineStartFromPosition(offset, content);
				int lineEnd = StrUtils.getLineEndFromPosition(offset, content);
				String line = content.substring(lineStart, lineEnd);
				if (line.trim().startsWith("switch ")) {
					String indent = line.substring(0, line.indexOf("switch "));
					String ind4 = "\t";
					if (!indent.startsWith("\t")) {
						ind4 = "    ";
					}
					insertedString =
						"{\n" +
						indent + ind4 + "case :\n" +
						indent + ind4 + ind4 + "break;\n" +
						indent + ind4 + "case :\n" +
						indent + ind4 + ind4 + "break;\n" +
						indent + ind4 + "default:\n" +
						indent + ind4 + ind4 + "break;\n" +
						indent + "}";
					overrideCaretPos = 1;
					break;
				}

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

			case "<":
				// only autocomplete < to <> if there was no space in front of it...
				// if there was a space in front, it is more likely to be used as a comparison
				// sign like vari < 27398, and adding > would not be helpful
				content = decoratedEditor.getText();
				if ((offset - 1 >= 0) && (offset - 1 < content.length()) && (content.charAt(offset - 1) == ' ')) {
					break;
				}
				insertedString = "<>";
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

			case ",":
				if (offset < 1) {
					break;
				}
				// when you enter "(," actually put "(0,"
				content = decoratedEditor.getText();
				if (content.charAt(offset - 1) == '(') {
					insertedString = "0,";
					overrideCaretPos = 2;
				}
				break;

			case " ":
				if (offset < 2) {
					break;
				}
				content = decoratedEditor.getText();

				// in case of "  " <- that last space being entered,
				// check the rest of the current line left-wards,
				// and if there is something non-whitespacey until the next \n,
				// actually put in an equals sign to get " = "
				int matchLength = 0;
				if (content.charAt(offset - 1) == ' ') {
					matchLength = 1;
					if ((content.charAt(offset - 2) == '=') || (content.charAt(offset - 2) == '!') ||
						(content.charAt(offset - 2) == '+') || (content.charAt(offset - 2) == '-') ||
						(content.charAt(offset - 2) == '>') || (content.charAt(offset - 2) == '<')) {

						String contentStart = content.substring(0, offset - 1);
						String contentEnd = content.substring(offset);

						String newContent = contentStart + "= " + contentEnd;

						int origCaretPos = decoratedEditor.getCaretPosition();
						decoratedEditor.setText(newContent);
						decoratedEditor.setCaretPosition(origCaretPos + 1);

						// we do NOT bubble up the chain, as we already set the text explicitly!
						return;
					}
				} else if ((content.charAt(offset - 2) == ' ') && (content.charAt(offset - 1) == '!')) {
					matchLength = 2;
				}
				if (matchLength > 0) {
					boolean encounteredSomething = false;
					for (int pos = offset - (matchLength + 1); pos >= 0; pos--) {
						char c = content.charAt(pos);
						if (c == '\n') {
							break;
						}
						if (!((c == ' ') || (c == '\t') || (c == '\n') || (c == '\r'))) {
							encounteredSomething = true;
							break;
						}
					}
					if (encounteredSomething) {
						insertedString = "= ";
						overrideCaretPos = 2;
						break;
					}
				}

				if (offset < 6) {
					break;
				}

				// in case of "for " <- that last space being entered,
				// check the previous line... if it contains "Map<" (might be "HashMap<" too),
				// then automagically write:
				// for (Map.Entry<String, Object> entry : map.entrySet()) {
				//     String key = entry.getKey();
				//     Object value = entry.getValue();
				// }
				char char4 = content.charAt(offset - 4);
				boolean char4White = (char4 == ' ') || (char4 == '\t') || (char4 == '\n') || (char4 == '\r');
				if (char4White && (content.charAt(offset - 3) == 'f') &&
					(content.charAt(offset - 2) == 'o') && (content.charAt(offset - 1) == 'r')) {

					// get the two newlines before the for, nl1 is just before the for, nl2 is the one
					// just before that one
					int nl1 = content.lastIndexOf("\n", offset - 3);

					if (nl1 >= 0) {
						// keep going back with nl2 until between nl1 and nl2 we have *something* after trim()
						int nl2 = nl1;
						nl2 = content.lastIndexOf("\n", nl2 - 1);
						if (nl2 >= 0) {
							while ("".equals(content.substring(nl2, nl1).trim())) {
								nl2 = content.lastIndexOf("\n", nl2 - 1);
								if (nl2 < 0) {
									break;
								}
							}

							if (nl2 >= 0) {
								// check if there is a "Map<" in there, and if so...
								if (content.substring(nl2, nl1).contains("Map<")) {

									// ... automagically add the for command for maps! :D

									String contentStart = content.substring(0, offset);
									String contentEnd = content.substring(offset);

									String indentation = content.substring(nl1 + 1, offset - 3);

									String forStuff =
										" (Map.Entry<String, Object> entry : map.entrySet()) {\n" +
										indentation + "\tString key = entry.getKey();\n" +
										indentation + "\tObject value = entry.getValue();\n" +
										indentation + "\t";

									String forEnd = "\n" +
										indentation + "}";

									String newContent = contentStart + forStuff + forEnd + contentEnd;

									int origCaretPos = decoratedEditor.getCaretPosition();
									decoratedEditor.setText(newContent);
									decoratedEditor.setCaretPosition(origCaretPos + forStuff.length());

									// we do NOT bubble up the chain, as we already set the text explicitly!
									return;
								}
							}
						}
					}
				}

				// in case of "if (blubb) && "<- that last space being entered,
				// add a bracket after the if and close it after the cursor:
				// if ((blubb) && )
				if (offset > 10) {
					if ((content.charAt(offset - 6) == ' ') && (content.charAt(offset - 5) == '=') &&
						(content.charAt(offset - 4) == ' ') && (content.charAt(offset - 3) == 'n') &&
						(content.charAt(offset - 2) == 'e') && (content.charAt(offset - 1) == 'w')) {

						if (addNewObjectInstantiation(content, offset, "", true) > 0) {
							return;
						}
					}
				}
				if (((content.charAt(offset - 1) == '&') && (content.charAt(offset - 2) == '&')) ||
					((content.charAt(offset - 1) == '|') && (content.charAt(offset - 2) == '|'))) {

					lineStart = StrUtils.getLineStartFromPosition(offset, content);
					lineEnd = StrUtils.getLineEndFromPosition(offset, content);
					int lineOffset = offset - lineStart;

					String contentStart = content.substring(0, lineStart);
					line = content.substring(lineStart, lineEnd);
					String contentEnd = content.substring(lineEnd, content.length());

					int ifAt = line.indexOf("if (");

					if ((ifAt >= 0) && (ifAt < lineOffset)) {
						// if we have:          if (blubb = foo) && |
						// or if we have:       if (blubb = foo && |)
						// but NOT if we have:  if (blubb(foo) && |)
						if (line.indexOf("(", ifAt + 4) < 0) {
							int foundEndAt = line.indexOf(")", ifAt + 4);
							int insertedAmount = 0;

							line = line.substring(0, ifAt + 4) + "(" + line.substring(ifAt + 4);
							String newContent = contentStart + line + contentEnd;

							boolean preventTextSetting = false;

							if ((foundEndAt >= 0) && (foundEndAt < lineOffset)) {
								// we have:       if (blubb = foo) && |
								// we insert:     if ((blubb = foo) && |)

								newContent = newContent.substring(0, offset + 1) + " )" + newContent.substring(offset + 1);
								insertedAmount = 2;

							} else {
								// we have:       if (blubb = foo && |)
								// we insert:     if ((blubb = foo) && |)

								//   so do in case of: if (blubb = foo && |)
								//     and in case of: if (foo(bar) && |)
								// but not in case of: if (blubb && |)
								// so check if the inner string ("blubb = foo", "foo(bar)", "blubb")
								// contains a space or bracket, and if not, prevent text setting...
								String containedStr = newContent.substring(lineStart + ifAt + 5, offset - 2);
								if (!containedStr.contains(" ") && !containedStr.contains("\t") &&
									!containedStr.contains("(") && !containedStr.contains(")")) {
									preventTextSetting = true;
								}

								newContent = newContent.substring(0, offset - 2) + ")" +
									newContent.substring(offset - 2, offset + 1) + " " + newContent.substring(offset + 1);
								insertedAmount = 3;
							}

							if (!preventTextSetting) {
								int origCaretPos = decoratedEditor.getCaretPosition();
								decoratedEditor.setText(newContent);
								decoratedEditor.setCaretPosition(origCaretPos + insertedAmount);

								// we do NOT bubble up the chain, as we already set the text explicitly!
								return;
							}
						}
					}
				}

				break;
		}

		Code.this.insertString(offset, insertedString, attrs, overrideCaretPos);
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

		pleaseDoNotHighlightThisRound = true;

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

		pleaseDoNotHighlightThisRound = true;

		super.fireInsertUpdate(event);

		// highlightText(event.getOffset(), event.getLength());

		onChange(event);
	}

	/**
	 * Cut has been pressed (as in cut-paste)
	 */
	@Override
	protected void fireRemoveUpdate(DocumentEvent event) {

		pleaseDoNotHighlightThisRound = true;

		super.fireRemoveUpdate(event);

		// highlightText(event.getOffset(), event.getLength());

		onChange(event);
	}

	protected void onChange(DocumentEvent event) {

		// we call highlightAllText no matter the length, as it does not
		// cause any problem when being called too often - as it just notifies
		// a thread to highlight at some point
		highlightAllText();

		// call the on change callback (if there is one)
		if (onChangeCallback != null) {
			onChangeCallback.call();
		}

		// for some wonky reason, we are called in 4096-character-increments,
		// and REALLY do not want to add these intermediate calls to the cache,
		// so we try to somehow detect these occurrences...
		String nextVersion = decoratedEditor.getText();

		// System.out.println("hTACOC len: " + nextVersion.length() + " offset: " + event.getOffset() + " length: " + event.getLength());

		if (nextVersion.length() % 4096 == 0) {
			return;
		}

		// refresh the line numbering in the left memo (if there is one)
		if (codeEditorLineMemo != null) {
			refreshLineNumbering();
		}

		addToUndoCache(nextVersion);
	}

	protected void addToUndoCache(String nextVersionStr) {

		// add text version to the undo cache
		CodeAtLocation currentVersion = textVersions.get(currentTextVersion);
		CodeAtLocation nextVersion = new CodeAtLocation(nextVersionStr, decoratedEditor.getCaretPosition());

		// only do this if the new version is not empty!
		if (nextVersion.getCode().equals("")) {
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
	// you might want to override it, but do call super! ;)
	protected void highlightText(int start, int length) {

		this.errorList = new ArrayList<>();
		this.lastBracketStart = null;
		this.lastBracketEnd = null;

		int end = this.getLength();

		// set the entire document back to regular
		this.setCharacterAttributes(0, end, attrRegular, true);

		variableNamesSeveralTimes = new HashSet<>();
		variableNamesOnce = new HashMap<>();
	}

	protected void encounteredVariableName(String couldBeKeyword, int start, boolean complainIfMissing) {

		// the token we are looking at is the name of a variable... we want to track them,
		// and if a variable name only appears once in a file, highlight it as suspicious
		// due to it being unused
		if (!variableNamesSeveralTimes.contains(couldBeKeyword)) {

			// if we are not supposed to complain, or if we encountered the key before...
			if ((!complainIfMissing) || variableNamesOnce.containsKey(couldBeKeyword)) {

				// ... then remove it from the list of keys to complain about!
				variableNamesSeveralTimes.add(couldBeKeyword);
				variableNamesOnce.remove(couldBeKeyword);
			} else {

				// ... elsewise, add it to the naughty list!
				variableNamesOnce.put(couldBeKeyword, start);
			}
		}
	}

	protected void postHighlight(String content) {

		for (Map.Entry<String, Integer> entry : variableNamesOnce.entrySet()) {
			String variableName = entry.getKey();
			if (!variableNameIsANumber(variableName)) {
				int start = entry.getValue();
				this.setCharacterAttributes(start, variableName.length(), attrSuspicious, false);
			}
		}
	}

	private static boolean variableNameIsANumber(String variableName) {
		try {
			Double.parseDouble(variableName);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	// highlight for search - which is called by the highlighting thread always
	// AFTER all the other highlightings have been performed!
	protected void highlightSearch(int start, int length) {

		try {

			String content = this.getText(0, length);

			List<Pair<Integer, Integer>> foundSites = findSearchSites(content);

			int caretPos = decoratedEditor.getCaretPosition();

			for (Pair<Integer, Integer> site : foundSites) {

				int pos = site.getLeft();
				int matchLength = site.getRight();

				if ((caretPos >= pos) && (caretPos <= pos + matchLength)) {
					this.setCharacterAttributes(pos, matchLength, attrSearchSelected, true);
				} else {
					this.setCharacterAttributes(pos, matchLength, attrSearch, true);
				}
			}

		} catch (BadLocationException e) {
			// oops!
		}
	}

	/**
	 * Returns a list of sites in the code which match the current search,
	 * where each site is a pair of position and length
	 */
	public List<Pair<Integer, Integer>> findSearchSites(String content) {

		List<Pair<Integer, Integer>> result = new ArrayList<>();

		if (searchStr == null) {
			return result;
		}

		if ("".equals(searchStr)) {
			return result;
		}

		String curSearchStr = searchStr;
		String curContent = content;

		if (searchIgnoreCase) {
			curSearchStr = curSearchStr.toLowerCase();
			curContent = curContent.toLowerCase();
		}

		// remove leading and trailing asterisks as they have no impact anyway and would lead
		// to infinite loops later on
		if (searchUseAsterisk) {
			while (curSearchStr.startsWith("*")) {
				curSearchStr = curSearchStr.substring(1);
			}
			while (curSearchStr.endsWith("*")) {
				curSearchStr = curSearchStr.substring(0, curSearchStr.length() - 1);
			}
		}

		int asteriskPos = curSearchStr.indexOf("*");

		if (searchUseAsterisk && (asteriskPos >= 0)) {
			String firstFindThis = curSearchStr.substring(0, asteriskPos);
			String secondFindThis = curSearchStr.substring(asteriskPos + 1);

			int secondSearchLen = secondFindThis.length();

			int nextFirstPos = curContent.indexOf(firstFindThis);

			if (nextFirstPos >= 0) {
				int nextSecondPos = curContent.indexOf(secondFindThis, nextFirstPos);
				while ((nextFirstPos >= 0) && (nextSecondPos >= 0)) {

					result.add(new Pair<Integer, Integer>(nextFirstPos, nextSecondPos + secondSearchLen - nextFirstPos));

					nextFirstPos = curContent.indexOf(firstFindThis, nextSecondPos + secondSearchLen);
					if (nextFirstPos >= 0) {
						nextSecondPos = curContent.indexOf(secondFindThis, nextFirstPos);
					} else {
						nextSecondPos = -1;
					}
				}
			}

			return result;
		}

		int searchLen = curSearchStr.length();

		int nextPos = curContent.indexOf(curSearchStr);

		while (nextPos >= 0) {

			result.add(new Pair<Integer, Integer>(nextPos, searchLen));

			nextPos = curContent.indexOf(curSearchStr, nextPos + searchLen);
		}

		return result;
	}

	/**
	 * Highlight a string in the content, within an area from start to end
	 * singleForMultiline: if true, a single string delimiter is enough to make a string spanning several lines
	 * threeForMultiline: if true, three string delimiters can make a string spanning several lines
	 */
	protected int highlightString(String content, int start, int end, boolean singleForMultiline, boolean threeForMultiline) {

		if (threeForMultiline) {
			// in addition to regular single-line strings, we also have """...""" multi-line strings
			if (start + 3 <= content.length()) {
				String stringDelimiters = content.substring(start, start + 3);
				if ((stringDelimiters.charAt(0) == stringDelimiters.charAt(1)) &&
					(stringDelimiters.charAt(0) == stringDelimiters.charAt(2))) {

					int endOfString = content.indexOf(stringDelimiters, start + 3);

					if (endOfString < 0) {
						endOfString = content.length();
						// the string is open-ended in the first way - multi-marker is never encountered again
						errorList.add("There seems to be an open-ended multi-line string in line " + (StrUtils.getLineNumberFromPosition(start, content) + 1) + "!");
					}

					this.setCharacterAttributes(start, endOfString - start + 3, this.attrString, false);

					return endOfString;
				}
			}

			// if we found no funky triple-string-delimiter, continue with regular single-line string highlighting
		}

		// get the string delimiter that was actually used to start this string (so " or ') to be able to find the matching one
		String stringDelimiter = content.substring(start, start + 1);

		if (!singleForMultiline) {
			// find the end of line - as we do not want to go further
			int endOfLine = content.indexOf(EOL, start + 2);

			if (endOfLine >= 0) {
				end = endOfLine;
			}
		}

		// find the matching end of string
		int endOfString = start;

		while (true) {
			endOfString = content.indexOf(stringDelimiter, endOfString + 1);

			// the end of the line (or even file) has been reached without us finding another string end marker
			if (endOfString == -1) {
				break;
			}

			int i = 1;
			while (endOfString - i >= 0) {
				if (content.charAt(endOfString - i) == '\\') {
					i++;
				} else {
					break;
				}
			}
			i--;
			// if the end of the string is not escaped...
			// (because there are 0 escapes, or 2 (which escape each other), or 4, or 6, ...)
			if (i % 2 == 0) {
				// ... then it really is the end!
				break;
			}
		}

		// the string is open-ended in the second way - no such marker ever appears again (endOfString is -1)
		// or a marker appears, but only in another line (endOfString is above end)...
		// in both cases, complain and go for end of the line!
		if ((endOfString == -1) || (endOfString > end)) {
			endOfString = end;
			errorList.add("There seems to be an open-ended string in line " + (StrUtils.getLineNumberFromPosition(start, content) + 1) + "!");
		}

		if (collectStrings) {
			collectedStrings.add(content.substring(start, endOfString + 1));
		}

		this.setCharacterAttributes(start, endOfString - start + 1, attrString, false);

		return endOfString;
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

		currentTextVersion--;

		if (currentTextVersion < 0) {
			currentTextVersion = 0;
		}

		CodeAtLocation newText = textVersions.get(currentTextVersion);
		int newTextLen = newText.getLength();

		decoratedEditor.setText(newText.getCode());

		decoratedEditor.setCaretPosition(newText.getCaretPos());
	}

	public void redo() {

		currentTextVersion++;

		if (currentTextVersion > textVersions.size() - 1) {
			currentTextVersion = textVersions.size() - 1;
		}

		CodeAtLocation newText = textVersions.get(currentTextVersion);
		int newTextLen = newText.getLength();

		decoratedEditor.setText(newText.getCode());

		decoratedEditor.setCaretPosition(newText.getCaretPos());
	}

	protected boolean openFileRelativeToThis(String basePath, List<String> relativePaths, CodeLanguage language, String extraInfo) {
		if (onOpenFileCallback == null) {
			return false;
		}
		return onOpenFileCallback.openFileRelativeToThis(basePath, relativePaths, language, extraInfo);
	}

	protected List<String> getOtherFileContents(List<String> fileEndings) {
		if (onOpenFileCallback == null) {
			return new ArrayList<>();
		}
		return onOpenFileCallback.getOtherFileContents(fileEndings);
	}

	/**
	 * Gets the fields in a given (e.g. by being manually selected) piece of code
	 */
	public List<CodeField> getFields(String contentMiddle) {

		List<CodeField> fields = new ArrayList<>();

		// find all fields in contentMiddle
		for (String line : contentMiddle.split("\n")) {

			line = line.trim();

			if ("".equals(line)) {
				continue;
			}
			if (line.startsWith("//")) {
				continue;
			}
			if (line.startsWith("/**") || line.startsWith("*")) {
				continue;
			}

			// the line could look like:
			// String blubb;
			// private String blubb;
			// private final static String blubb;
			// private Map<Foo, Bar> boink = bla.blubb();
			// private Object foo = new AwesomeObject(blabliblubb);

			if (line.endsWith(";")) {
				line = line.substring(0, line.length() - 1);
				line = line.trim();
			}

			// the line could look like:
			// String blubb
			// private String blubb
			// private final static String blubb
			// private Map<Foo, Bar> boink
			// private Object foo = new AwesomeObject(blabliblubb)

			if (line.contains("=")) {
				line = line.substring(0, line.indexOf("="));
				line = line.trim();
			}

			// the line could look like:
			// String blubb
			// private String blubb
			// private final static String blubb
			// private Map<Foo, Bar> boink
			// private Object foo

			String lineName = "unknown";
			if (line.contains(" ")) {
				lineName = line.substring(line.lastIndexOf(" ") + 1);
				line = line.substring(0, line.lastIndexOf(" "));
			}

			// the lineName could look like:
			// blubb
			// blubb
			// blubb
			// boink
			// foo

			String lineType = "unknown";
			String lineGenerics = "";
			if (line.contains("<")) {
				lineGenerics = line.substring(line.indexOf("<"));
				line = line.substring(0, line.indexOf("<"));
			}
			if (line.contains(" ")) {
				lineType = line.substring(line.lastIndexOf(" ") + 1);
				line = line.substring(0, line.lastIndexOf(" "));
			} else {
				lineType = line;
				line = "";
			}

			// the lineType could look like:
			// String
			// String
			// String
			// Map
			// Object

			// the lineGenerics could look like:
			//
			//
			//
			// <Foo, Bar>
			//

			lineType += lineGenerics;

			// the lineType could look like:
			// String
			// String
			// String
			// Map<Foo, Bar>
			// Object

			CodeField newField = new CodeField(lineName, lineType);
			fields.add(newField);

			line = " " + line + " ";

			if (line.contains(" static ")) {
				newField.setIsStatic(true);
			}
		}

		return fields;
	}

	/**
	 * Add constructor based on selected fields
	 */
	public void addConstructor() {

		String content = decoratedEditor.getText();
		int lineStart = StrUtils.getLineStartFromPosition(selStart, content);
		int lineEnd = StrUtils.getLineEndFromPosition(selEnd, content);

		String contentStart = content.substring(0, lineStart);
		String contentMiddle = content.substring(lineStart, lineEnd);
		String contentEnd = content.substring(lineEnd, content.length());

		StringBuilder newCode = new StringBuilder();

		List<CodeField> fields = getFields(contentMiddle);

		newCode.append("\n\n\tpublic ");
		newCode.append(getClassName());
		newCode.append("(");
		String sep = "";

		for (CodeField field : fields) {
			newCode.append(sep);
			newCode.append(field.getType());
			newCode.append(" ");
			newCode.append(field.getName());
			sep = ", ";
		}

		newCode.append(") {\n");

		for (CodeField field : fields) {
			newCode.append("\t\tthis.");
			newCode.append(field.getName());
			newCode.append(" = ");
			newCode.append(field.getName());
			newCode.append(";\n");
		}

		newCode.append("\t}\n");

		content = contentStart + contentMiddle + newCode.toString() + contentEnd;

		decoratedEditor.setText(content);

		int selPos = contentStart.length() + contentMiddle.length() + newCode.length();
		decoratedEditor.setSelectionStart(selPos);
		decoratedEditor.setSelectionEnd(selPos);
	}

	/**
	 * Add getters for selected fields
	 */
	public void addGetters() {

		addGettersAndOrSetters(true, false);
	}

	/**
	 * Add setters for selected fields
	 */
	public void addSetters() {

		addGettersAndOrSetters(false, true);
	}


	/**
	 * Add getters and setters for selected fields
	 */
	public void addGettersAndSetters() {

		addGettersAndOrSetters(true, true);
	}

	protected void addGettersAndOrSetters(boolean addGetters, boolean addSetters) {

		String content = decoratedEditor.getText();
		int lineStart = StrUtils.getLineStartFromPosition(selStart, content);
		int lineEnd = StrUtils.getLineEndFromPosition(selEnd, content);
		String contentMiddle = content.substring(lineStart, lineEnd);
		List<CodeField> fields = getFields(contentMiddle);

		if (fields.size() < 1) {
			return;
		}

		int insertAt = content.lastIndexOf("}");
		if (insertAt < 0) {
			insertAt = 0;
		}

		String contentStart = content.substring(0, insertAt);
		String contentEnd = content.substring(insertAt, content.length());

		String ourClassName = getClassName();

		StringBuilder newCode = new StringBuilder();

		boolean extraLineNeeded = true;
		if (insertAt > 1) {
			if ((content.charAt(insertAt - 1) == '\n') && (content.charAt(insertAt - 2) == '\n')) {
				extraLineNeeded = false;
			}
		}
		if (extraLineNeeded) {
			newCode.append("\n");
		}
		String sep = "";
		for (CodeField field : fields) {

			if (addGetters) {
				newCode.append(sep);
				newCode.append("\tpublic ");
				if (field.getIsStatic()) {
					newCode.append("static ");
				}
				newCode.append(field.getType());
				newCode.append(" get");
				newCode.append(field.getNameUpcase());
				newCode.append("() {\n");
				newCode.append("\t\treturn ");
				newCode.append(field.getName());
				newCode.append(";\n");
				newCode.append("\t}\n");
				sep = "\n";
			}

			if (addSetters) {
				newCode.append(sep);
				newCode.append("\tpublic ");
				if (field.getIsStatic()) {
					newCode.append("static ");
				}
				newCode.append("void set");
				newCode.append(field.getNameUpcase());
				newCode.append("(");
				newCode.append(field.getType());
				newCode.append(" ");
				newCode.append(field.getName());
				if (field.getIsStatic()) {
					newCode.append("Arg");
				}
				newCode.append(") {\n");
				newCode.append("\t\t");
				if (!field.getIsStatic()) {
					newCode.append("this.");
				}
				newCode.append(field.getName());
				newCode.append(" = ");
				newCode.append(field.getName());
				if (field.getIsStatic()) {
					newCode.append("Arg");
				}
				newCode.append(";\n");
				newCode.append("\t}\n");
				sep = "\n";
			}
		}

		newCode.append("\n");

		content = contentStart + newCode.toString() + contentEnd;

		decoratedEditor.setText(content);

		int selPos = contentStart.length() + newCode.length();
		decoratedEditor.setSelectionStart(selPos);
		decoratedEditor.setSelectionEnd(selPos);
	}

	public String getClassName() {

		String content = decoratedEditor.getText();
		String[] lines = content.split("\n");

		for (String line : lines) {
			if (line.contains(" class ")) {
				String result = line.substring(line.indexOf(" class ") + 7);
				return result.substring(0, result.indexOf(" "));
			}
			if (line.startsWith("class ")) {
				String result = line.substring(6);
				return result.substring(0, result.indexOf(" "));
			}
		}

		// if this file does not contain any class at all, maybe it contains an enum?
		for (String line : lines) {
			if (line.contains(" enum ")) {
				String result = line.substring(line.indexOf(" enum ") + 6);
				return result.substring(0, result.indexOf(" "));
			}
			if (line.startsWith("enum ")) {
				String result = line.substring(5);
				return result.substring(0, result.indexOf(" "));
			}
		}

		return "";
	}

	public void addToString() {

		String content = decoratedEditor.getText();
		int lineStart = StrUtils.getLineStartFromPosition(selStart, content);
		int lineEnd = StrUtils.getLineEndFromPosition(selEnd, content);
		String contentMiddle = content.substring(lineStart, lineEnd);
		List<CodeField> fields = getFields(contentMiddle);

		int insertAt = content.lastIndexOf("}");
		if (insertAt < 0) {
			insertAt = 0;
		}

		String contentStart = content.substring(0, insertAt);
		String contentEnd = content.substring(insertAt, content.length());

		String ourClassName = getClassName();

		StringBuilder newCode = new StringBuilder();

		boolean extraLineNeeded = true;
		if (insertAt > 1) {
			if ((content.charAt(insertAt - 1) == '\n') && (content.charAt(insertAt - 2) == '\n')) {
				extraLineNeeded = false;
			}
		}
		if (extraLineNeeded) {
			newCode.append("\n");
		}
		newCode.append("\t@Override\n");
		newCode.append("\tpublic String toString() {\n");
		newCode.append("\t\treturn \"" + ourClassName);
		if (fields.size() > 0) {
			newCode.append(" [");
			String sep = "";
			for (CodeField field : fields) {
				newCode.append(sep);
				sep = ", ";
				newCode.append(field.getName() + ": \" + ");
				newCode.append("this." + field.getName());
				newCode.append(" + \"");
			}
			newCode.append("]");
		}
		newCode.append("\";\n");
		newCode.append("\t}\n");
		newCode.append("\n");

		content = contentStart + newCode.toString() + contentEnd;

		decoratedEditor.setText(content);

		int selPos = contentStart.length() + newCode.length();
		decoratedEditor.setSelectionStart(selPos);
		decoratedEditor.setSelectionEnd(selPos);
	}

	/**
	 * Add equals() method to the class
	 */
	public void addEquals() {

		String content = decoratedEditor.getText();
		int lineStart = StrUtils.getLineStartFromPosition(selStart, content);
		int lineEnd = StrUtils.getLineEndFromPosition(selEnd, content);
		String contentMiddle = content.substring(lineStart, lineEnd);
		List<CodeField> fields = getFields(contentMiddle);

		int insertAt = content.lastIndexOf("}");
		if (insertAt < 0) {
			insertAt = 0;
		}

		String contentStart = content.substring(0, insertAt);
		String contentEnd = content.substring(insertAt, content.length());

		String ourClassName = getClassName();

		StringBuilder newCode = new StringBuilder();

		boolean extraLineNeeded = true;
		if (insertAt > 1) {
			if ((content.charAt(insertAt - 1) == '\n') && (content.charAt(insertAt - 2) == '\n')) {
				extraLineNeeded = false;
			}
		}
		if (extraLineNeeded) {
			newCode.append("\n");
		}
		newCode.append("\t@Override\n");
		newCode.append("\tpublic boolean equals(Object other) {\n");
		newCode.append("\n");
		newCode.append("\t\t// If the other one does not even exist, we are not the same - because we exist!\n");
		newCode.append("\t\tif (other == null) {\n");
		newCode.append("\t\t\treturn false;\n");
		newCode.append("\t\t}\n");
		newCode.append("\n");
		newCode.append("\t\tif (other instanceof " + ourClassName + ") {\n");
		newCode.append("\t\t\t" + ourClassName + " other" + ourClassName + " = (" + ourClassName + ") other;\n");
		if (fields.size() > 0) {
			for (CodeField field : fields) {
				newCode.append("\n");
				newCode.append("\t\t\t// If our values for " + field.getName() + " are different...\n");
				if (StrUtils.startsWithUpperCase(field.getType())) {
					newCode.append("\t\t\tif (this." + field.getName() + " == null) {\n");
					newCode.append("\t\t\t\tif (other" + ourClassName + "." + field.getName() + " != null) {\n");
					newCode.append("\t\t\t\t\t// ... then we are not the same!\n");
					newCode.append("\t\t\t\t\treturn false;\n");
					newCode.append("\t\t\t\t}\n");
					newCode.append("\t\t\t} else if (!this." + field.getName() + ".equals(other" + ourClassName + "." + field.getName() + ")) {\n");
				} else {
					newCode.append("\t\t\tif (this." + field.getName() + " != other" + ourClassName + "." + field.getName() + ") {\n");
				}
				newCode.append("\t\t\t\t// ... then we are not the same!\n");
				newCode.append("\t\t\t\treturn false;\n");
				newCode.append("\t\t\t}\n");
			}
			newCode.append("\n");
			newCode.append("\t\t\t// We have no reason to assume that we are not the same\n");
			newCode.append("\t\t\treturn true;\n");
		} else {
			newCode.append("\n");
			newCode.append("\t\t\t// TODO - actually compare other" + ourClassName + " to this\n");
			newCode.append("\t\t\tif (this.? == other" + ourClassName + ".?) {\n");
			newCode.append("\t\t\t\treturn true;\n");
			newCode.append("\t\t\t}\n");
		}

		newCode.append("\t\t}\n");
		newCode.append("\n");
		newCode.append("\t\t// If the other one cannot even be cast to us, then we are not the same!\n");
		newCode.append("\t\treturn false;\n");
		newCode.append("\t}\n");

		newCode.append("\n");
		newCode.append("\t@Override\n");
		newCode.append("\tpublic int hashCode() {\n");
		if (fields.size() > 0) {
			newCode.append("\t\tint result = 0;\n");
			for (CodeField field : fields) {
				if ("int".equals(field.getType())) {
					newCode.append("\t\tresult += this." + field.getName() + ";\n");
				} else if (StrUtils.startsWithLowerCase(field.getType())) {
					newCode.append("\t\tresult += (int) this." + field.getName() + ";\n");
				} else {
					newCode.append("\t\tif (this." + field.getName() + " != null) {\n");
					newCode.append("\t\t\tresult += this." + field.getName() + ".hashCode();\n");
					newCode.append("\t\t}\n");
				}
			}
			newCode.append("\t\treturn result;\n");
		} else {
			newCode.append("\t\t// TODO - actually compute a useful hash\n");
			newCode.append("\t\treturn this.?;\n");
		}
		newCode.append("\t}\n");
		newCode.append("\n");

		content = contentStart + newCode.toString() + contentEnd;

		decoratedEditor.setText(content);

		int selPos = contentStart.length() + newCode.length();
		decoratedEditor.setSelectionStart(selPos);
		decoratedEditor.setSelectionEnd(selPos);
	}

	@Override
	public void setCharacterAttributes(int offset, int length, AttributeSet s, boolean replace) {
		if (attributeSetting) {
			try {
				super.setCharacterAttributes(offset, length, s, replace);
			} catch (Error e) {
				// we got a StateInvariantError, which should be avoided
				// (by not calling updates while ourselves being updated)
			}
		}
	}

	public int getSelStart() {
		return selStart;
	}

	public int getSelEnd() {
		return selEnd;
	}

	/**
	 * Returns the amount of characters that were added
	 */
	private int addNewObjectInstantiation(String content, int offset, String insertAdditionally, boolean setCaretPos) {

		int result = 0;
		int lineStart = StrUtils.getLineStartFromPosition(offset, content);
		int lineEnd = StrUtils.getLineEndFromPosition(offset, content);
		int lineOffset = offset - lineStart;

		String contentStart = content.substring(0, lineStart);
		String line = content.substring(lineStart, lineEnd);
		String contentEnd = content.substring(lineEnd, content.length());
		String lineFollowing = line.substring(lineOffset);

		if ("".equals(lineFollowing.trim()) || ";".equals(lineFollowing.trim())) {

			line = line.substring(0, lineOffset);

			int tabAt = line.indexOf("\tList<");
			int spaceAt = line.indexOf(" List<");

			if (line.substring(lineOffset).equals("")) {
				if (((tabAt >= 0) && (tabAt < lineOffset)) ||
					((spaceAt >= 0) && (spaceAt < lineOffset))) {

					line = line + insertAdditionally + " ArrayList<>();";
					String newContent = contentStart + line + contentEnd;

					int origCaretPos = decoratedEditor.getCaretPosition();
					decoratedEditor.setText(newContent);
					result = 15 + insertAdditionally.length();
					if (setCaretPos) {
						decoratedEditor.setCaretPosition(origCaretPos + result);
					}

					// we do NOT bubble up the chain, as we already set the text explicitly!
					return result;
				}
			}

			tabAt = line.indexOf("\tSet<");
			spaceAt = line.indexOf(" Set<");

			if (line.substring(lineOffset).equals("")) {
				if (((tabAt >= 0) && (tabAt < lineOffset)) ||
					((spaceAt >= 0) && (spaceAt < lineOffset))) {

					line = line + insertAdditionally + " HashSet<>();";
					String newContent = contentStart + line + contentEnd;

					int origCaretPos = decoratedEditor.getCaretPosition();
					decoratedEditor.setText(newContent);
					result = 13 + insertAdditionally.length();
					if (setCaretPos) {
						decoratedEditor.setCaretPosition(origCaretPos + result);
					}

					// we do NOT bubble up the chain, as we already set the text explicitly!
					return result;
				}
			}

			tabAt = line.indexOf("\tMap<");
			spaceAt = line.indexOf(" Map<");

			if (line.substring(lineOffset).equals("")) {
				if (((tabAt >= 0) && (tabAt < lineOffset)) ||
					((spaceAt >= 0) && (spaceAt < lineOffset))) {

					line = line + insertAdditionally + " HashMap<>();";
					String newContent = contentStart + line + contentEnd;

					int origCaretPos = decoratedEditor.getCaretPosition();
					decoratedEditor.setText(newContent);
					result = 13 + insertAdditionally.length();
					if (setCaretPos) {
						decoratedEditor.setCaretPosition(origCaretPos + result);
					}

					// we do NOT bubble up the chain, as we already set the text explicitly!
					return result;
				}
			}

			// for all others, if we have Foo bar = new, then actually automagically add:
			// Foo bar = new Foo();
			String newClazz = line.trim();
			String genericClazz = "";
			if (newClazz.contains("<")) {
				genericClazz = newClazz.substring(newClazz.indexOf("<"));
				newClazz = newClazz.substring(0, newClazz.indexOf("<"));
			}
			if (newClazz.contains(" ")) {
				newClazz = newClazz.substring(0, newClazz.indexOf(" "));
			}

			// buuut do not do it if the string contains a dot, or does not start with a
			// capital letter - so do not do it for Foo.blubb = new or bar = new
			if ((newClazz.length() > 0) &&
				(!newClazz.contains(".")) &&
				(Character.isUpperCase(newClazz.charAt(0)) || newClazz.endsWith("[]"))) {

				int caretOffset = 2;
				if (newClazz.endsWith("[]")) {
					line = line + insertAdditionally + " " + newClazz + ";";
					caretOffset = 0;
				} else {
					if (!"".equals(genericClazz)) {
						line = line + insertAdditionally + " " + newClazz + "<>();";
						caretOffset = 4;
					} else {
						line = line + insertAdditionally + " " + newClazz + "();";
					}
				}
				String newContent = contentStart + line + contentEnd;

				int origCaretPos = decoratedEditor.getCaretPosition();
				decoratedEditor.setText(newContent);
				result = newClazz.length() + insertAdditionally.length() + caretOffset;
				if (setCaretPos) {
					decoratedEditor.setCaretPosition(origCaretPos + result);
				}

				// we do NOT bubble up the chain, as we already set the text explicitly!
				return result;
			}
		}

		return result;
	}

	public List<String> getErrors() {
		return errorList;
	}

	public void setFilename(String filename) {

		this.filename = filename;

		localFilename = filename;
		if (localFilename.contains("\\")) {
			localFilename = localFilename.substring(localFilename.lastIndexOf("\\") + 1);
		}
		if (localFilename.contains("/")) {
			localFilename = localFilename.substring(localFilename.lastIndexOf("/") + 1);
		}

		localFilenameWithoutExtension = localFilename;
		if (localFilenameWithoutExtension.contains(".")) {
			localFilenameWithoutExtension = localFilenameWithoutExtension.substring(0,
				localFilenameWithoutExtension.lastIndexOf("."));
		}
	}

}
