/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.codeeditor;

import com.asofterspace.toolbox.codeeditor.utils.CodeSnippetWithLocation;
import com.asofterspace.toolbox.utils.StrUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JTextPane;


public class TypeScriptCode extends JavaCode {

	private static final long serialVersionUID = 1L;

	// all keywords of the TypeScript language
	protected static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
		new String[] {"abstract", "as", "assert", "async", "await", "break", "case", "catch", "constructor", "continue", "declare", "default", "do", "else", "export", "extends", "final", "finally", "for", "from", "goto", "if", "implements", "import", "in", "instanceof", "new", "package", "private", "protected", "public", "return", "static", "switch", "synchronized", "throw", "throws", "trait", "try", "volatile", "while"}
	));

	// all primitive types of the Java language and other stuff that looks that way
	protected static final Set<String> PRIMITIVE_TYPES = new HashSet<>(Arrays.asList(
		new String[] {"any", "bigint", "boolean", "class", "const", "enum", "false", "function", "interface", "let", "module", "never", "null", "number", "object", "string", "super", "symbol", "this", "true", "undefined", "var", "void"}
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

	@Override
	protected int highlightOther(String content, int start, int end, boolean setAttributesAndDetectFunctions) {

		int couldBeKeywordEnd = start + 1;

		while (couldBeKeywordEnd <= end) {
			if (isDelimiter(content.charAt(couldBeKeywordEnd))) {
				break;
			}
			couldBeKeywordEnd++;
		}

		String couldBeKeyword = content.substring(start, couldBeKeywordEnd);

		if (setAttributesAndDetectFunctions) {
			if (isKeyword(couldBeKeyword)) {
				this.setCharacterAttributes(start, couldBeKeywordEnd - start, attrKeyword, false);
			} else if (isPrimitiveType(couldBeKeyword)) {
				this.setCharacterAttributes(start, couldBeKeywordEnd - start, attrPrimitiveType, false);
			} else if (isAdvancedType(couldBeKeyword)) {
				this.setCharacterAttributes(start, couldBeKeywordEnd - start, attrAdvancedType, false);
			} else if (isAnnotation(couldBeKeyword)) {
				this.setCharacterAttributes(start, couldBeKeywordEnd - start, attrAnnotation, false);
			} else {
				// get the entire line that we found!
				String functionName = StrUtils.getLineFromPosition(start, content).trim();
				boolean addFunction = false;
				if (functionName.startsWith("export ") || functionName.startsWith("function ")) {
					addFunction = true;
				} else if ((couldBeKeywordEnd <= end) && (content.charAt(couldBeKeywordEnd) == '(')) {
					if (!"new".equals(lastCouldBeKeyword)) {
						this.setCharacterAttributes(start, couldBeKeywordEnd - start, attrFunction, false);
						if ((start > 0) && (content.charAt(start-1) == ' ')) {
							// ignore lines with more than 1 tab indent / 4 regular indents and line without the return type
							if ((curLineStartingWhitespace < 5) && !"".equals(lastCouldBeKeyword)) {
								// so far this is like in Java, but we also have calls like next() directly on the four-spaces-indent,
								// so in addition to all else also just add it as a function if it ends with {
								if (functionName.endsWith("{")) {
									addFunction = true;
								}
							}
						}
					}
				}
				if (addFunction) {
					CodeSnippetWithLocation codeLoc = new CodeSnippetWithLocation(functionName, StrUtils.getLineStartFromPosition(start, content));
					if (!functions.contains(codeLoc)) {
						functions.add(codeLoc);
					}
				}
			}
		}

		lastCouldBeKeyword = couldBeKeyword;

		return couldBeKeywordEnd;
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
