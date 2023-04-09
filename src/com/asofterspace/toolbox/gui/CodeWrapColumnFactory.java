/**
 * Unlicensed code created by A Softer Space, 2023
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.gui;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;


public class CodeWrapColumnFactory implements ViewFactory {

	public View create(Element el) {

		String elName = el.getName();

		if (elName != null) {
			switch (elName) {
				case AbstractDocument.ContentElementName:
					return new CodeWrapLabelView(el);
				case AbstractDocument.ParagraphElementName:
					return new ParagraphView(el);
				case AbstractDocument.SectionElementName:
					return new BoxView(el, View.Y_AXIS);
				case StyleConstants.ComponentElementName:
					return new ComponentView(el);
				case StyleConstants.IconElementName:
					return new IconView(el);
			}
		}

		return new LabelView(el);
	}
}
