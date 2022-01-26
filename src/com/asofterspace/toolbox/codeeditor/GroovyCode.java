/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.codeeditor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JTextPane;


public class GroovyCode extends JavaCode {

	private static final long serialVersionUID = 1L;

	// all keywords of the Groovy language
	protected static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
		new String[] { "abstract", "as", "assert", "break", "case", "catch", "const", "continue", "def", "default", "do", "else", "extends", "final", "finally", "for", "goto", "if", "implements", "import", "in", "instanceof", "new", "package", "private", "protected", "public", "return", "static", "switch", "synchronized", "throw", "throws", "trait", "try", "while", "volatile"}
	));


	public GroovyCode(JTextPane editor) {

		super(editor);
	}

	/**
	 * In groovy, in addition to regular java strings, we also have """...""" multi-line strings,
	 * so let's override the default behavior...
	 */
	@Override
	protected int highlightString(String content, int start, int end, boolean singleForMultiline, boolean threeForMultiline) {
		singleForMultiline = false;
		threeForMultiline = true;
		return super.highlightString(content, start, end, singleForMultiline, threeForMultiline);
	}

	/**
	 * We override the exact same function from the base class to access the KEYWORDS from this class instead
	 */
	@Override
	protected boolean isKeyword(String token) {
		return KEYWORDS.contains(token);
	}

}
