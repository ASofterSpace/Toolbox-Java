/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.codeeditor.base;

import com.asofterspace.toolbox.codeeditor.utils.CodeLocation;
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

	private DefaultStyledDocument functionPaneStyle;


	public FunctionSupplyingCode(JTextPane editor) {

		super(editor);
	}

	public FunctionSupplyingCode(JTextPane editor, Code parentEditor) {

		super(editor, parentEditor);
	}

	public void setFunctionTextField(JTextPane functionPane) {

		this.functionPane = functionPane;

		functionPaneStyle = new DefaultStyledDocument();

		functionPane.setDocument(functionPaneStyle);

		applyColorToFunctionPane();
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

	/**
	 * Takes something like
	 * public void foo(String blubb)
	 * and returns something like
	 * foo
	 * - and ALWAYS returns a string; even an empty one
	 * when null is fed into it - such that it is very
	 * safe to use!
	 */
	protected String getFuncName(String functionSignature) {

		CodePatch patch = getFuncPatch(functionSignature);

		return functionSignature.substring(patch.getStart(), patch.getEnd());
	}

	protected CodePatch getFuncPatch(String functionSignature) {

		if (functionSignature == null) {
			return new CodePatch(0, 0);
		}

		int bracketStart = functionSignature.indexOf("(");
		if (bracketStart < 0) {
			bracketStart = functionSignature.length();
		}

		int funcNameStart = functionSignature.substring(0, bracketStart).lastIndexOf(" ");
		if (funcNameStart < 0) {
			funcNameStart = 0;
		}

		return new CodePatch(funcNameStart, bracketStart);
	}

	protected void updateFunctionList() {

		if (functionPane != null) {

			List<CodePatch> boldPatches = new ArrayList<>();

			StringBuilder functionText = new StringBuilder();

			int lengthBefore = 0;

			for (CodeLocation func : functions) {

				String function = func.getCode();

				CodePatch patch = getFuncPatch(function);
				patch.addOffset(lengthBefore);
				boldPatches.add(patch);

				functionText.append(function);
				functionText.append("\n");

				lengthBefore += function.length() + 1;
			}

			String functionTextStr = functionText.toString();

			functionPane.setText(functionTextStr);

			if (functionPaneStyle != null) {
				// reset all...
				functionPaneStyle.setCharacterAttributes(0, functionTextStr.length(), attrRegular, true);

				// ... and then set this one
				for (CodePatch boldPatch : boldPatches) {
					functionPaneStyle.setCharacterAttributes(boldPatch.getStart(), boldPatch.getLength(), attrBold, false);
				}
			}
		}
	}

	@Override
	public void setLightScheme() {

		super.setLightScheme();

		applyColorToFunctionPane();
	}

	@Override
	public void setDarkScheme() {

		super.setDarkScheme();

		applyColorToFunctionPane();
	}

	private void applyColorToFunctionPane() {

		if (functionPane == null) {
			return;
		}

		functionPane.setBackground(schemeBackgroundColor);
		functionPane.setCaretColor(schemeForegroundColor);
	}


	private class CodePatch {

		private int start;
		private int end;


		CodePatch(int start, int end) {
			this.start = start;
			this.end = end;
		}

		void addOffset(int offset) {
			this.start += offset;
			this.end += offset;
		}

		int getStart() {
			return this.start;
		}

		int getEnd() {
			return this.end;
		}

		int getLength() {
			return this.end - this.start;
		}
	}

}
