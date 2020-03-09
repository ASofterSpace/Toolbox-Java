/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.codeeditor;

import javax.swing.JTextPane;


public class GroovyCode extends JavaCode {

	private static final long serialVersionUID = 1L;

	// all keywords of the Groovy language
	private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
		new String[] { "abstract", "as", "assert", "break", "case", "catch", "const", "continue", "def", "default", "do", "else", "extends", "final", "finally", "for", "goto", "if", "implements", "import", "in", "instanceof", "new", "package", "private", "protected", "public", "return", "static", "switch", "synchronized", "throw", "throws", "trait", "try", "while", "volatile"}
	));


	public GroovyCode(JTextPane editor) {

		super(editor);
	}

}
