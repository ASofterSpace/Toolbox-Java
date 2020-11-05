/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.codeeditor.base;

import com.asofterspace.toolbox.codeeditor.utils.CodeSnippetWithLocation;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.CaretEvent;
import javax.swing.JTextPane;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;


public abstract class FunctionSupplyingCode extends Code {

	private static final long serialVersionUID = 1L;

	protected List<CodeSnippetWithLocation> functions = new ArrayList<>();

	protected JTextPane functionPane;

	private DefaultStyledDocument functionPaneStyle;

	private List<CodePatch> boldPatches;

	private int lastTextLength = 0;


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

	@Override
	protected void highlightText(int start, int length) {

		functions = new ArrayList<>();

		super.highlightText(start, length);
	}

	public List<CodeSnippetWithLocation> getFunctions() {
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

			boldPatches = new ArrayList<>();

			StringBuilder functionText = new StringBuilder();

			int lengthBefore = 0;

			for (CodeSnippetWithLocation func : functions) {

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

			lastTextLength = functionTextStr.length();

			resetStyle();
		}
	}

	private void resetStyle() {

		if (functionPaneStyle != null) {
			// reset all...
			functionPaneStyle.setCharacterAttributes(0, lastTextLength, attrRegular, true);

			// ... and then set this one
			if (boldPatches != null) {
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

	@Override
	public CodeSnippetWithLocation getClickedFunction() {

		List<CodeSnippetWithLocation> functions = getFunctions();

		if ((functions == null) || (functions.size() < 1)) {
			return null;
		}

		int pressedLine = -1;
		int caretPos = functionPane.getCaretPosition();

		for (CodeSnippetWithLocation codeLoc : functions) {
			pressedLine++;
			caretPos -= codeLoc.getCode().length() + 1;
			if (caretPos < 0) {
				break;
			}
		}

		return functions.get(pressedLine);
	}

	@Override
	protected void onCaretUpdate(CaretEvent event) {
		super.onCaretUpdate(event);

		highlightCurrentFunction();
	}

	@Override
	protected void onMouseReleased(MouseEvent event) {
		super.onMouseReleased(event);

		highlightCurrentFunction();
	}

	@Override
	public void startFunctionHighlighting() {
		super.startFunctionHighlighting();

		highlightCurrentFunction();
	}

	/**
	 * When the user mouse-clicks somewhere or uses the arrow keys, highlight the function we are currently in...
	 */
	@Override
	protected void highlightCurrentFunction() {

		if (this.doFunctionHighlighting && (functionPane != null) && (functionPaneStyle != null)) {

			int curCaretPos = this.decoratedEditor.getCaretPosition();
			int bestFuncCaretPos = 0;
			CodeSnippetWithLocation bestFunc = null;

			for (CodeSnippetWithLocation func : functions) {
				if ((func.getCaretPos() <= curCaretPos) && (func.getCaretPos() > bestFuncCaretPos)) {
					bestFuncCaretPos = func.getCaretPos();
					bestFunc = func;
				}
			}

			// reset all...
			resetStyle();

			String functionTextStr = functionPane.getText();

			// ... and if we found a best function...
			if (bestFunc != null) {

				int funcLineNum = functions.indexOf(bestFunc);
				int start = 0;

				for (int i = 0; i < funcLineNum; i++) {
					start = functionTextStr.indexOf("\n", start+1);
				}

				int end = functionTextStr.indexOf("\n", start+1);
				if (end < start) {
					end = functionTextStr.length();
				}

				// ... highlight that one!
				functionPaneStyle.setCharacterAttributes(start, end - start, this.attrSearchSelected, true);
			}
		}
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
