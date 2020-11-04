/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.codeeditor.base;

import com.asofterspace.toolbox.codeeditor.utils.CodeSnippetWithLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JTextPane;


public abstract class PublicPrivateFunctionSupplyingCode extends FunctionSupplyingCode {

	public static final String SPACE = "\u205F";

	private static final long serialVersionUID = 1L;


	public PublicPrivateFunctionSupplyingCode(JTextPane editor) {

		super(editor);
	}

	private void sortFunctions(List<CodeSnippetWithLocation> functions) {

		Collections.sort(functions, new Comparator<CodeSnippetWithLocation>() {
			public int compare(CodeSnippetWithLocation a, CodeSnippetWithLocation b) {
				String funcNameA = getFuncName(a.getCode());
				String funcNameB = getFuncName(b.getCode());
				return funcNameA.toLowerCase().compareTo(funcNameB.toLowerCase());
			}
		});
	}

	private void appendFunctions(List<CodeSnippetWithLocation> target, List<CodeSnippetWithLocation> origin, String type) {

		if (origin.size() > 0) {
			target.add(new CodeSnippetWithLocation(type + ":", 0));
			for (CodeSnippetWithLocation func : origin) {
				target.add(func);
			}
			target.add(new CodeSnippetWithLocation("", 0));
		}
	}

	@Override
	protected void updateFunctionList() {

		List<CodeSnippetWithLocation> publicStaticFunctions = new ArrayList<>();
		List<CodeSnippetWithLocation> protectedStaticFunctions = new ArrayList<>();
		List<CodeSnippetWithLocation> anyStaticFunctions = new ArrayList<>();
		List<CodeSnippetWithLocation> privateStaticFunctions = new ArrayList<>();

		List<CodeSnippetWithLocation> publicFunctions = new ArrayList<>();
		List<CodeSnippetWithLocation> protectedFunctions = new ArrayList<>();
		List<CodeSnippetWithLocation> anyFunctions = new ArrayList<>();
		List<CodeSnippetWithLocation> privateFunctions = new ArrayList<>();

		for (CodeSnippetWithLocation func : functions) {
			String trimmedLine = func.getCode().trim();
			if (trimmedLine.endsWith("{")) {
				trimmedLine = trimmedLine.substring(0, trimmedLine.length() - 1).trim();
			}
			if (trimmedLine.contains("synchronized ")) {
				trimmedLine = trimmedLine.replace("synchronized ", "");
			}
			if (trimmedLine.contains("static ")) {
				trimmedLine = trimmedLine.replace("static ", "");
				if (trimmedLine.contains("public ")) {
					publicStaticFunctions.add(new CodeSnippetWithLocation(trimmedLine.replace("public ", ""), func.getCaretPos()));
				} else if (trimmedLine.contains("protected ")) {
					protectedStaticFunctions.add(new CodeSnippetWithLocation(trimmedLine.replace("protected ", ""), func.getCaretPos()));
				} else if (trimmedLine.contains("private ")) {
					privateStaticFunctions.add(new CodeSnippetWithLocation(trimmedLine.replace("private ", ""), func.getCaretPos()));
				} else {
					anyStaticFunctions.add(new CodeSnippetWithLocation(trimmedLine, func.getCaretPos()));
				}
			} else {
				// "export " for TypeScript, the rest for Java, Groovy and friends
				if (trimmedLine.contains("public ") || trimmedLine.startsWith("export ")) {
					publicFunctions.add(new CodeSnippetWithLocation(trimmedLine.replace("public ", "").replace("export ", ""), func.getCaretPos()));
				} else if (trimmedLine.contains("protected ")) {
					protectedFunctions.add(new CodeSnippetWithLocation(trimmedLine.replace("protected ", ""), func.getCaretPos()));
				} else if (trimmedLine.contains("private ")) {
					privateFunctions.add(new CodeSnippetWithLocation(trimmedLine.replace("private ", ""), func.getCaretPos()));
				} else {
					anyFunctions.add(new CodeSnippetWithLocation(trimmedLine, func.getCaretPos()));
				}
			}
		}

		sortFunctions(publicFunctions);
		sortFunctions(publicStaticFunctions);
		sortFunctions(protectedFunctions);
		sortFunctions(protectedStaticFunctions);
		sortFunctions(anyFunctions);
		sortFunctions(anyStaticFunctions);
		sortFunctions(privateFunctions);
		sortFunctions(privateStaticFunctions);

		functions = new ArrayList<>();

		// TODO : currently, we here add a u205 (mid space) because the highlighting in FunctionSupplyingCode
		// highlights rows based on the spaces inside and we want the entire row to be highlighted...
		// do that less hackishly! ;)
		appendFunctions(functions, publicFunctions, "public");
		appendFunctions(functions, publicStaticFunctions, "public" + SPACE + "static");
		appendFunctions(functions, protectedFunctions, "protected");
		appendFunctions(functions, protectedStaticFunctions, "protected" + SPACE + "static");
		appendFunctions(functions, anyFunctions, "package-private");
		appendFunctions(functions, anyStaticFunctions, "package-private" + SPACE + "static");
		appendFunctions(functions, privateFunctions, "private");
		appendFunctions(functions, privateStaticFunctions, "private" + SPACE + "static");

		super.updateFunctionList();
	}

}
