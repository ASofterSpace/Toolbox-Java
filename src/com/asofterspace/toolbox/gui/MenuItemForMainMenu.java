/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.gui;

import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JMenuItem;


/**
 * This is a simple clickable item right inside the main menu :)
 */
public class MenuItemForMainMenu extends JButton {

	public static final long serialVersionUID = 56456349390653297l;


	public MenuItemForMainMenu(String labelText) {
		super(labelText);

		setOpaque(true);
		setContentAreaFilled(false);
		setBorderPainted(false);
		setFocusable(false);
	}

	/*
	public Dimension getMaximumSize() {
		return new Dimension(
			super.getPreferredSize().width,
			super.getMaximumSize().height);
	}*/

}
