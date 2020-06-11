/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.codeeditor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JTextPane;


public class TypeScriptCode extends JavaCode {

	private static final long serialVersionUID = 1L;

	// all keywords of the TypeScript language
	protected static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
		new String[] {"abstract", "as", "assert", "async", "await", "break", "case", "catch", "const", "const", "constructor", "continue", "declare", "default", "do", "else", "export", "extends", "final", "finally", "for", "from", "goto", "if", "implements", "import", "in", "instanceof", "new", "package", "private", "protected", "public", "return", "static", "switch", "synchronized", "throw", "throws", "trait", "try", "volatile", "while"}
	));

	// all primitive types of the Java language and other stuff that looks that way
	protected static final Set<String> PRIMITIVE_TYPES = new HashSet<>(Arrays.asList(
		new String[] {"boolean", "byte", "char", "class", "double", "enum", "false", "float", "function", "int", "interface", "let", "long", "module", "null", "short", "string", "super", "this", "true", "undefined", "void"}
	));


	public TypeScriptCode(JTextPane editor) {

		super(editor);
	}

	@Override
	public String addMissingImports(String origText) {
		return origText;
	}

	@Override
	public String reorganizeImports(String origText) {
		return origText;
	}

	@Override
	public String reorganizeImportsCompatible(String origText) {
		return origText;
	}

	@Override
	public String removeUnusedImports(String origText) {
		return origText;
	}

	/**
	 * We override the exact same function from the base class to access the KEYWORDS from this class instead
	 */
	@Override
	protected boolean isKeyword(String token) {
		return KEYWORDS.contains(token);
	}

	@Override
	protected boolean isPrimitiveType(String token) {
		return PRIMITIVE_TYPES.contains(token);
	}

}
