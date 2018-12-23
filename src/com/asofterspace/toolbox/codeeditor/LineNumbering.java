/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.codeeditor;

import com.asofterspace.toolbox.Utils;
import com.asofterspace.toolbox.utils.Callback;
import com.asofterspace.toolbox.utils.Pair;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;


public class LineNumbering extends Code {

	private final class NopCaret extends DefaultCaret {

		private final static long serialVersionUID = 1L;

		@Override
		protected void adjustVisibility(Rectangle nloc) {
			// do exactly nothing
		}
	}

	private static final long serialVersionUID = 1L;

	// styles for the different kinds of text in the document
	private MutableAttributeSet attrRight; // just regular, right-bound text
	private MutableAttributeSet attrDot; // color for dot selection
	private MutableAttributeSet attrMark; // color for mark selection
	private MutableAttributeSet attrBetween; // color for selection between both

	private JTextPane connectedEditor;


	/**
	 * Generates a Line Numbering based on a line memo (in which the numbers
	 * are to be displayed) and a connected editor (in which the source code
	 * is situated which is supposed to be numbered.)
	 */
	public LineNumbering(JTextPane lineMemo, JTextPane connectedEditor) {

		super(lineMemo);

		this.connectedEditor = connectedEditor;

		// highlight the line number on the left that the caret is currently
		// in in the editor on the right
		CaretListener caretListener = new CaretListener() {
			@Override
			public void caretUpdate(CaretEvent e) {
				highlightOnSelection(e.getDot(), e.getMark());
			}
		};

		connectedEditor.addCaretListener(caretListener);

		// do not let changes in the line memo bubble up to scroll changes
		NopCaret nopCaret = new NopCaret();
		lineMemo.setCaret(nopCaret);

		// do not let the user muck about with the line memo
		lineMemo.setEditable(false);
	}

	public void setLightScheme() {

		// change the attribute sets
		attrRight = new SimpleAttributeSet();
		StyleConstants.setForeground(attrRight, new Color(128, 128, 128));
		StyleConstants.setAlignment(attrRight, StyleConstants.ALIGN_RIGHT);
		StyleConstants.setFontSize(attrRight, fontSize);
		StyleConstants.setFontFamily(attrRight, editorFontFamily);

		attrDot = new SimpleAttributeSet();
		StyleConstants.setForeground(attrDot, new Color(0, 0, 0));

		attrMark = new SimpleAttributeSet();
		StyleConstants.setForeground(attrMark, new Color(96, 96, 128));

		attrBetween = new SimpleAttributeSet();
		StyleConstants.setForeground(attrBetween, new Color(48, 48, 96));

		super.setLightScheme();
	}

	public void setDarkScheme() {

		// change the attribute sets
		attrRight = new SimpleAttributeSet();
		StyleConstants.setForeground(attrRight, new Color(128, 128, 128));
		StyleConstants.setBackground(attrRight, new Color(0, 0, 0));
		StyleConstants.setAlignment(attrRight, StyleConstants.ALIGN_RIGHT);
		StyleConstants.setFontSize(attrRight, fontSize);
		StyleConstants.setFontFamily(attrRight, editorFontFamily);

		attrDot = new SimpleAttributeSet();
		StyleConstants.setForeground(attrDot, new Color(255, 255, 255));

		attrMark = new SimpleAttributeSet();
		StyleConstants.setForeground(attrMark, new Color(128, 128, 255));

		attrBetween = new SimpleAttributeSet();
		StyleConstants.setForeground(attrBetween, new Color(192, 192, 255));

		super.setDarkScheme();
	}

	private Integer lastDot = null;
	private Integer lastMark = null;

	// this is the main function that... well... highlights our text :)
	@Override
	void highlightText(int start, int length) {

		highlightOnSelection(lastDot, lastMark);
	}

	public void highlightOnSelection(Integer dot, Integer mark) {

		int end = this.getLength();

		// set the entire document to rightbound - and use paragraph
		// attributes for getting it right-aligned...
		this.setParagraphAttributes(0, end, attrRight, true);
		// ... and character ones for getting the color back to normal
		this.setCharacterAttributes(0, end, attrRight, true);

		if (connectedEditor == null) {
			return;
		}

		String content = connectedEditor.getText();

		Pair<Integer, Integer> dotSels = null;
		Pair<Integer, Integer> markSels = null;

		if (mark != null) {
			lastMark = mark;

			if (mark != dot) {
				markSels = highlightOnSelDotMark(mark, attrMark, content);
			}
		}

		if (dot != null) {
			lastDot = dot;

			dotSels = highlightOnSelDotMark(dot, attrDot, content);
		}

		if ((dotSels != null) && (markSels != null)) {
			int startBetween;
			int endBetween;
			if (dot < mark) {
				startBetween = dotSels.getRight();
				endBetween = markSels.getLeft();
			} else {
				startBetween = markSels.getRight();
				endBetween = dotSels.getLeft();
			}
			this.setCharacterAttributes(startBetween, endBetween - startBetween, attrBetween, false);
		}
	}

	private Pair<Integer, Integer> highlightOnSelDotMark(int pos, AttributeSet attr, String content) {

		int line = Utils.countCharInString('\n', content, pos);

		int start = 0;

		if (line < 10) {
			start += line * 2;
		} else if (line < 100) {
			start += 9*2 + (line - 9) * 3;
		} else if (line < 1000) {
			start += 9*2 + 90*3 + (line - 99) * 4;
		} else if (line < 10000) {
			start += 9*2 + 90*3 + 900*4 + (line - 999) * 5;
		} else if (line < 100000) {
			start += 9*2 + 90*3 + 900*4 + 9000*5 + (line - 9999) * 6;
		} // you got over 100000 lines of code in one file?
		// you don't need highlighting, you need some serious help :D

		int len = (""+(line+1)).length();

		this.setCharacterAttributes(start, len, attr, false);

		return new Pair<>(start, start+len);
	}
}
