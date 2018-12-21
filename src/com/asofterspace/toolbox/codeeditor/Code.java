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


public abstract class Code extends DefaultStyledDocument {

	private static final long serialVersionUID = 1L;


	// the callback to be called when something changes
	Callback onChangeCallback;

	// the end-of-line marker
	static final String EOL = "\n";

	// the root element of the document, through which we can get the individual lines
	Element root;

	// the editor that is to be decorated by us
	final JTextPane decoratedEditor;
	
	// the list of all decorated editors
	static List<Code> instances = new ArrayList<>();
	
	// the background color of all editors
	Color schemeBackgroundColor;

	// the font sizes, fonts and tab sets of all editors
	int fontSize = 15;
	static String editorFontFamily;
	Font lastFont;
	TabSet lastTabSet;

	// styles for the different kinds of text in the document
	MutableAttributeSet attrRegular;

	
	public Code(JTextPane editor) {

		super();
		
		// declare which end of line marker is to be used
		putProperty(DefaultEditorKit.EndOfLineStringProperty, EOL);

		// keep track of the editor we are decorating (useful e.g. to get and set caret pos during insert operations)
		decoratedEditor = editor;

		// keep track of the root element
		root = this.getDefaultRootElement();
		
		// initialize the font size, lastFont etc.
		setFontSize(fontSize);

		// initialize all the attribute sets
		setLightScheme();
		
		// actually style the editor with... us
		int origCaretPos = decoratedEditor.getCaretPosition();
		String origContent = decoratedEditor.getText();
		decoratedEditor.setDocument(this);
		decoratedEditor.setText(origContent);
		decoratedEditor.setCaretPosition(origCaretPos);
		
		instances.add(this);
	}
	
	public void setOnChange(Callback callback) {

		onChangeCallback = callback;
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

		Style style = decoratedEditor.getLogicalStyle();
		StyleConstants.setTabSet(style, lastTabSet);
		decoratedEditor.setLogicalStyle(style);
		
		highlightAllText();
	}

	public static void setFontSizeForAllEditors(int newSize) {
	
		for (Code instance : instances) {
		
			instance.setFontSize(newSize);
		}
	}
	
	public void setLightScheme() {
	
		// change the attribute sets
		attrRegular = new SimpleAttributeSet();
		StyleConstants.setForeground(attrRegular, new Color(0, 0, 0));

		// re-decorate the editor
		schemeBackgroundColor = new Color(255, 255, 255);
		decoratedEditor.setBackground(schemeBackgroundColor);
		
		highlightAllText();
	}
	
	public static void setLightSchemeForAllEditors() {
	
		for (Code instance : instances) {
		
			instance.setLightScheme();
		}
	}
	
	public void setDarkScheme() {
	
		// change the attribute sets
		attrRegular = new SimpleAttributeSet();
		StyleConstants.setForeground(attrRegular, new Color(255, 255, 255));
		StyleConstants.setBackground(attrRegular, new Color(0, 0, 0));

		// re-decorate the editor
		schemeBackgroundColor = new Color(0, 0, 0);
		decoratedEditor.setBackground(schemeBackgroundColor);
		
		highlightAllText();
	}
	
	public static void setDarkSchemeForAllEditors() {
	
		for (Code instance : instances) {
		
			instance.setDarkScheme();
		}
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

		try {
			super.insertString(offset, insertedString, attrs);
		} catch (BadLocationException e) {
			// oops!
		}

		highlightText(offset, insertedString.length());

		callOnChange();
	}

	/**
	 * The delete key or something like that has been pressed
	 */
	@Override
	public void remove(int offset, int length) {

		try {
			super.remove(offset, length);
		} catch (BadLocationException e) {
			// oops!
		}

		highlightText(offset, 0);
		
		callOnChange();
	}
	
	/**
	 * Paste has been pressed (as in copy-paste)
	 */
	@Override
	protected void fireInsertUpdate(DocumentEvent event) {

		super.fireInsertUpdate(event);

		highlightText(event.getOffset(), event.getLength());
		
		callOnChange();
	}

	/**
	 * Cut has been pressed (as in cut-paste)
	 */
	@Override
	protected void fireRemoveUpdate(DocumentEvent event) {

		super.fireRemoveUpdate(event);

		highlightText(event.getOffset(), event.getLength());
		
		callOnChange();
	}
	
	void callOnChange() {
		if (onChangeCallback != null) {
			onChangeCallback.call();
		}
	}

	void highlightAllText() {
		highlightText(0, this.getLength());
	}

	// this is the main function that... well... highlights our text :)
	// you might want to override it ;)
	abstract void highlightText(int start, int length);

	/**
	 * Call this to detach the code highlighter from its text field,
	 * stop sending update callbacks and enable it to be garbage collected
	 */
	public void discard() {
		onChangeCallback = null;
	}

}
