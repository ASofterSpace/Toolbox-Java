/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.codeeditor.utils;

import com.asofterspace.toolbox.codeeditor.base.Code;
import com.asofterspace.toolbox.codeeditor.BatchCode;
import com.asofterspace.toolbox.codeeditor.CSharpCode;
import com.asofterspace.toolbox.codeeditor.CssCode;
import com.asofterspace.toolbox.codeeditor.DelphiCode;
import com.asofterspace.toolbox.codeeditor.GoCode;
import com.asofterspace.toolbox.codeeditor.GroovyCode;
import com.asofterspace.toolbox.codeeditor.HtmlCode;
import com.asofterspace.toolbox.codeeditor.JavaCode;
import com.asofterspace.toolbox.codeeditor.JavaScriptCode;
import com.asofterspace.toolbox.codeeditor.JsonCode;
import com.asofterspace.toolbox.codeeditor.MarkdownCode;
import com.asofterspace.toolbox.codeeditor.PhpCode;
import com.asofterspace.toolbox.codeeditor.PlainText;
import com.asofterspace.toolbox.codeeditor.PythonCode;
import com.asofterspace.toolbox.codeeditor.ShellCode;
import com.asofterspace.toolbox.codeeditor.StlCode;
import com.asofterspace.toolbox.codeeditor.TypeScriptCode;
import com.asofterspace.toolbox.codeeditor.XmlCode;

import javax.swing.JTextPane;


public class CodeHighlighterFactory {

	public static Code getHighlighterForLanguage(CodeLanguage language, JTextPane editor) {

		if (language == null) {
			return new PlainText(editor);
		}

		Code highlighter = null;

		switch (language) {
			case JAVA:
				highlighter = new JavaCode(editor);
				break;
			case GROOVY:
				highlighter = new GroovyCode(editor);
				break;
			case CSHARP:
				highlighter = new CSharpCode(editor);
				break;
			case MARKDOWN:
				highlighter = new MarkdownCode(editor);
				break;
			case CSS:
				highlighter = new CssCode(editor);
				break;
			case HTML:
				highlighter = new HtmlCode(editor);
				break;
			case XML:
				highlighter = new XmlCode(editor);
				break;
			case PHP:
				highlighter = new PhpCode(editor);
				break;
			case JAVASCRIPT:
				highlighter = new JavaScriptCode(editor);
				break;
			case TYPESCRIPT:
				highlighter = new TypeScriptCode(editor);
				break;
			case DELPHI:
				highlighter = new DelphiCode(editor);
				break;
			case JSON:
				highlighter = new JsonCode(editor);
				break;
			case PYTHON:
				highlighter = new PythonCode(editor);
				break;
			case GO:
				highlighter = new GoCode(editor);
				break;
			case STL:
				highlighter = new StlCode(editor);
				break;
			case SHELL:
				highlighter = new ShellCode(editor);
				break;
			case BATCH:
				highlighter = new BatchCode(editor);
				break;
			default:
				highlighter = new PlainText(editor);
		}

		return highlighter;
	}

}
