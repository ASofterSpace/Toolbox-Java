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


public class PlainText extends Code {

	private static final long serialVersionUID = 1L;


	public PlainText(JTextPane editor) {

		super(editor);
	}

	// this is the main function that... well... highlights our text :)
	@Override
	void highlightText(int start, int length) {

		try {
			int end = this.getLength();
			
			String content = this.getText(0, end);
			
			// set the entire document back to regular
			this.setCharacterAttributes(0, end, attrRegular, true);

		} catch (BadLocationException e) {
			// oops!
		}
	}
}
