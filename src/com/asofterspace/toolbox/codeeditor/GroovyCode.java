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
	 * In groovy, in addition to regular java strings, we also have """...""" multi-line strings
	 */
	@Override
	protected int highlightString(String content, int start, int end) {

		if (start + 3 <= content.length()) {
			String stringDelimiters = content.substring(start, start + 3);
			if ((stringDelimiters.charAt(0) == stringDelimiters.charAt(1)) &&
				(stringDelimiters.charAt(0) == stringDelimiters.charAt(2))) {

				int endOfString = content.indexOf(stringDelimiters, start + 3);

				if (endOfString < 0) {
					endOfString = content.length();
				}

				this.setCharacterAttributes(start, endOfString - start + 3, this.attrString, false);

				return endOfString;
			}
		}

		// if we found no funky triple-string-delimiter, do regular java string highlighting
		return super.highlightString(content, start, end);
	}

	/**
	 * We override the exact same function from the base class to access the KEYWORDS from this class instead
	 */
	@Override
	protected boolean isKeyword(String token) {
		return KEYWORDS.contains(token);
	}

}
