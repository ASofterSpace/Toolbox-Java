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
	static Color schemeBackgroundColor;

	// the font sizes, fonts and tab sets of all editors
	static int fontSize = 15;
	static String editorFontFamily;
	static Font lastFont;
	static TabSet lastTabSet;

	// styles for the different kinds of text in the document
	MutableAttributeSet attrRegular;

	
	public Code(JTextPane editor) {

		super();
		
		// declare which end of line marker is to be used
		putProperty(DefaultEditorKit.EndOfLineStringProperty, EOL);
		
		// initialize the font size, lastFont etc. if they have not been initialized before
		if (lastFont == null) {
			setFontSize(fontSize);
		}

		// keep track of the editor we are decorating (useful e.g. to get and set caret pos during insert operations)
		decoratedEditor = editor;

		// keep track of the root element
		root = this.getDefaultRootElement();

		// initialize all the attribute sets
		setLightScheme();
		
		// actually style the editor with... us
		decoratedEditor.setDocument(this);
		applySchemeAndFontToOurEditor();
		
		instances.add(this);
	}
	
	public void setOnChange(Callback callback) {

		onChangeCallback = callback;
	}
	
	public static void setFontSize(int newSize) {
	
		fontSize = newSize;
		
		initializeEditorFont();
		
		if (editorFontFamily == null) {
			lastFont = new Font("", Font.PLAIN, fontSize);
		} else {
			lastFont = new Font(editorFontFamily, Font.PLAIN, fontSize);
		}
		
		applySchemeAndFontToAllEditors();
	}

	public void setLightScheme() {
	
		// change the attribute sets
		attrRegular = new SimpleAttributeSet();
		StyleConstants.setForeground(attrRegular, new Color(0, 0, 0));

		// re-decorate the editor
		schemeBackgroundColor = new Color(255, 255, 255);
		applySchemeAndFontToAllEditors();
	}
	
	public void setDarkScheme() {
	
		// change the attribute sets
		attrRegular = new SimpleAttributeSet();
		StyleConstants.setForeground(attrRegular, new Color(255, 255, 255));
		StyleConstants.setBackground(attrRegular, new Color(0, 0, 0));

		// re-decorate the editor
		schemeBackgroundColor = new Color(0, 0, 0);
		applySchemeAndFontToAllEditors();
	}
	
	static void applySchemeAndFontToAllEditors() {
		
		// ignore calls to this function before fonts have actually
		// been initialized (which can happen if a style is statically
		// chosen already before the very first editor has been created)
		if (lastFont == null) {
			return;
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
		
		// actually apply it all
		for (Code instance : instances) {
		
			instance.applySchemeAndFontToOurEditor();

			instance.highlightAllText();
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

	private void applySchemeAndFontToOurEditor() {
		
		decoratedEditor.setBackground(schemeBackgroundColor);

		decoratedEditor.setFont(lastFont);

		Style style = decoratedEditor.getLogicalStyle();
		StyleConstants.setTabSet(style, lastTabSet);
		decoratedEditor.setLogicalStyle(style);
	}
	
	void highlightAllText() {
		highlightText(0, this.getLength());
	}

	// this is the main function that... well... hightlights our text :)
	// you might want to override it ;)
	abstract void highlightText(int start, int length);

}
