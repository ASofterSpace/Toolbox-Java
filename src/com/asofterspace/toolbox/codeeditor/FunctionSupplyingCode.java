/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.codeeditor;

import com.asofterspace.toolbox.utils.Callback;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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


public abstract class FunctionSupplyingCode extends Code {

	private static final long serialVersionUID = 1L;

	protected List<CodeLocation> functions = new ArrayList<>();

	protected JTextPane functionPane;


	public FunctionSupplyingCode(JTextPane editor) {

		super(editor);
	}

	public void setFunctionTextField(JTextPane functionPane) {

		this.functionPane = functionPane;
	}

	// does this code editor support reporting function names in the code?
	// (we are saying true, but if we are really supposed to supply something, the constructor
	// also needs to be called with the function pane!)
	@Override
	public boolean suppliesFunctions() {
		return true;
	}

	public List<CodeLocation> getFunctions() {
		return functions;
	}

	protected void updateFunctionList() {

		if (functionPane != null) {

			StringBuilder functionText = new StringBuilder();

			for (CodeLocation func : functions) {
				functionText.append(func.getCode());
				functionText.append("\n");
			}

			functionPane.setText(functionText.toString());
		}
	}

}
