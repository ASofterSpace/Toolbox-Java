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

	public void setText(String text) {
		super.setText(" " + text + " ");
	}

	/*
	public Dimension getMaximumSize() {
		return new Dimension(
			super.getPreferredSize().width,
			super.getMaximumSize().height);
	}
	*/

}
