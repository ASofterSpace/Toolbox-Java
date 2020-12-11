/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.gui;

import javax.swing.JLabel;


/**
 * This is a simple clickable item right inside the main menu :)
 */
public class MenuItemForMainMenu extends JLabel {

	public static final long serialVersionUID = 5645634939065497l;


	public MenuItemForMainMenu(String labelText) {
		super(" " + labelText + " ");
	}

	@Override
	public void setText(String text) {
		super.setText(" " + text + " ");
	}

	public String getTextContent() {
		String result = getText();
		if (result.startsWith(" ")) {
			result = result.substring(1);
		}
		if (result.endsWith(" ")) {
			result = result.substring(0, result.length() - 1);
		}
		return result;
	}

	/*
	public Dimension getMaximumSize() {
		return new Dimension(
			super.getPreferredSize().width,
			super.getMaximumSize().height);
	}
	*/

}
