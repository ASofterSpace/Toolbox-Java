/**
 * Unlicensed code created by A Softer Space, 2023
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.gui;

import javax.swing.text.StyledEditorKit;
import javax.swing.text.ViewFactory;


public class CodeWrapEditorKit extends StyledEditorKit {

	private final static long serialVersionUID = 1L;

	private ViewFactory defaultFactory = new CodeWrapColumnFactory();


	public ViewFactory getViewFactory() {
		return defaultFactory;
	}

}
