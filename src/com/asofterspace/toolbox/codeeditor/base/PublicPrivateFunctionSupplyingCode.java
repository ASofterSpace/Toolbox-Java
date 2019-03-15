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


public abstract class PublicPrivateFunctionSupplyingCode extends FunctionSupplyingCode {

	private static final long serialVersionUID = 1L;


	public PublicPrivateFunctionSupplyingCode(JTextPane editor) {

		super(editor);
	}

	private void sortFunctions(List<CodeLocation> functions) {

		Collections.sort(functions, new Comparator<CodeLocation>() {
			public int compare(CodeLocation a, CodeLocation b) {
				String funcNameA = getFuncName(a.getCode());
				String funcNameB = getFuncName(b.getCode());
				return funcNameA.toLowerCase().compareTo(funcNameB.toLowerCase());
			}
		});
	}

	private void appendFunctions(List<CodeLocation> target, List<CodeLocation> origin, String type) {

		if (origin.size() > 0) {
			target.add(new CodeLocation(type + ":", 0));
			for (CodeLocation func : origin) {
				target.add(func);
			}
			target.add(new CodeLocation("", 0));
		}
	}

	@Override
	protected void updateFunctionList() {

		List<CodeLocation> publicFunctions = new ArrayList<>();
		List<CodeLocation> protectedFunctions = new ArrayList<>();
		List<CodeLocation> anyFunctions = new ArrayList<>();
		List<CodeLocation> privateFunctions = new ArrayList<>();

		for (CodeLocation func : functions) {
			String line = func.getCode().trim();
			if (line.endsWith("{")) {
				line = line.substring(0, line.length() - 1).trim();
			}
			if (line.contains("public ")) {
				publicFunctions.add(new CodeLocation(line.replace("public ", ""), func.getCaretPos()));
			} else if (line.contains("protected ")) {
				protectedFunctions.add(new CodeLocation(line.replace("protected ", ""), func.getCaretPos()));
			} else if (line.contains("private ")) {
				privateFunctions.add(new CodeLocation(line.replace("private ", ""), func.getCaretPos()));
			} else {
				anyFunctions.add(new CodeLocation(line, func.getCaretPos()));
			}
		}

		sortFunctions(publicFunctions);
		sortFunctions(protectedFunctions);
		sortFunctions(anyFunctions);
		sortFunctions(privateFunctions);

		functions = new ArrayList<>();

		appendFunctions(functions, publicFunctions, "public");
		appendFunctions(functions, protectedFunctions, "protected");
		appendFunctions(functions, anyFunctions, "package-private");
		appendFunctions(functions, privateFunctions, "private");

		super.updateFunctionList();
	}

}
