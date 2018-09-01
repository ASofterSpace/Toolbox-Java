package com.asofterspace.toolbox.codeeditor;

import java.awt.Color;

import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class GroovyCodeDark extends GroovyCode {
	
	public GroovyCodeDark(JTextPane editor) {

		super(editor);

		// change the attribute sets
		StyleConstants.setForeground(attrAnnotation, new Color(128, 255, 128));
		StyleConstants.setBackground(attrAnnotation, new Color(0, 0, 0));

		StyleConstants.setForeground(attrComment, new Color(128, 255, 128));
		StyleConstants.setBackground(attrComment, new Color(0, 0, 0));

		StyleConstants.setForeground(attrKeyword, new Color(128, 128, 255));
		StyleConstants.setBackground(attrKeyword, new Color(0, 0, 0));

		StyleConstants.setForeground(attrRegular, new Color(255, 255, 255));
		StyleConstants.setBackground(attrRegular, new Color(0, 0, 0));

		StyleConstants.setForeground(attrPrimitiveType, new Color(255, 96, 255));
		StyleConstants.setBackground(attrPrimitiveType, new Color(0, 0, 0));

		StyleConstants.setForeground(attrString, new Color(255, 128, 128));
		StyleConstants.setBackground(attrString, new Color(0, 0, 0));
		
		// re-decorate the editor
		editor.setBackground(new Color(0, 0, 0));
	}
 
}