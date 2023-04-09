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

		if (language != null) {
			switch (language) {
				case JAVA:
					return new JavaCode(editor);
				case GROOVY:
					return new GroovyCode(editor);
				case CSHARP:
					return new CSharpCode(editor);
				case MARKDOWN:
					return new MarkdownCode(editor);
				case CSS:
					return new CssCode(editor);
				case HTML:
					return new HtmlCode(editor);
				case XML:
					return new XmlCode(editor);
				case PHP:
					return new PhpCode(editor);
				case JAVASCRIPT:
					return new JavaScriptCode(editor);
				case TYPESCRIPT:
					return new TypeScriptCode(editor);
				case DELPHI:
					return new DelphiCode(editor);
				case JSON:
					return new JsonCode(editor);
				case PYTHON:
					return new PythonCode(editor);
				case GO:
					return new GoCode(editor);
				case STL:
					return new StlCode(editor);
				case SHELL:
					return new ShellCode(editor);
				case BATCH:
					return new BatchCode(editor);
			}
		}

		return new PlainText(editor);
	}

}
