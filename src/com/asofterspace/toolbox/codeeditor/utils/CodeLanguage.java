/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.codeeditor.utils;

import com.asofterspace.toolbox.codeeditor.base.Code;

import javax.swing.JTextPane;


public enum CodeLanguage {

	ASSEMBLER("Assembler"),
	C("C"),
	CPP("C++"),
	CSHARP("C#"),
	CSS("CSS"),
	DATEX("DaTeX"),
	DELPHI("Delphi"),
	GROOVY("Groovy"),
	HTML("HTML"),
	JAVA("Java"),
	JAVASCRIPT("JavaScript"),
	JSON("JSON"),
	MARKDOWN("Markdown"),
	PLAINTEXT("Plain Text"),
	PHP("PHP"),
	PYTHON("Python"),
	GO("Go"),
	SHELL("Shell Script"),
	XML("XML");


	String kindStr;


	CodeLanguage(String kindStr) {
		this.kindStr = kindStr;
	}

	public static CodeLanguage getFromFilename(String filename) {

		if (filename == null) {
			return CodeLanguage.PLAINTEXT;
		}

		String lowfilename = filename.toLowerCase();

		if (lowfilename.endsWith(".java")) {
			return CodeLanguage.JAVA;
		}

		if (lowfilename.endsWith(".groovy")) {
			return CodeLanguage.GROOVY;
		}

		if (lowfilename.endsWith(".cs")) {
			return CodeLanguage.CSHARP;
		}

		if (lowfilename.endsWith(".md")) {
			return CodeLanguage.MARKDOWN;
		}

		if (lowfilename.endsWith(".pas")) {
			return CodeLanguage.DELPHI;
		}

		if (lowfilename.endsWith(".php")) {
			return CodeLanguage.PHP;
		}

		if (lowfilename.endsWith(".htm") || lowfilename.endsWith(".html")) {
			return CodeLanguage.HTML;
		}

		if (lowfilename.endsWith(".xml")) {
			return CodeLanguage.XML;
		}

		if (lowfilename.endsWith(".js")) {
			return CodeLanguage.JAVASCRIPT;
		}

		if (lowfilename.endsWith(".json")) {
			return CodeLanguage.JSON;
		}

		if (lowfilename.endsWith(".css")) {
			return CodeLanguage.CSS;
		}

		if (lowfilename.endsWith(".sh")) {
			return CodeLanguage.SHELL;
		}

		if (lowfilename.endsWith(".py")) {
			return CodeLanguage.PYTHON;
		}

		if (lowfilename.endsWith(".go")) {
			return CodeLanguage.GO;
		}

		return CodeLanguage.PLAINTEXT;
	}

	public static CodeLanguage getFromString(String kindStr) {

		for (CodeLanguage ck : CodeLanguage.values()) {
			if (ck.kindStr.equals(kindStr)) {
				return ck;
			}
		}

		return null;
	}

	public Code getHighlighter(JTextPane editor) {

		return CodeHighlighterFactory.getHighlighterForLanguage(this, editor);
	}

	public String toString() {

		return kindStr;
	}

	public String toLowerCase() {

		return toString().toLowerCase();
	}

}
