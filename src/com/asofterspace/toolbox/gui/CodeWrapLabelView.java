/**
 * Unlicensed code created by A Softer Space, 2023
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.gui;

import javax.swing.text.Element;
import javax.swing.text.LabelView;
import javax.swing.text.View;


public class CodeWrapLabelView extends LabelView {

	public CodeWrapLabelView(Element el) {
		super(el);
	}

	public float getMinimumSpan(int axis) {
		if (axis == View.Y_AXIS) {
			return super.getMinimumSpan(axis);
		}
		return 0;
	}
}
