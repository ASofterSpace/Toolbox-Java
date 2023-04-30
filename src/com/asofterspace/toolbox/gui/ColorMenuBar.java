/**
 * Unlicensed code created by A Softer Space, 2023
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Graphics;

import javax.swing.JMenuBar;


/**
 * This is just a regular menu bar - but with the ability to adjust its color!
 */
public class ColorMenuBar extends JMenuBar  {

	private final static long serialVersionUID = 1L;

	private Color bgCol = Color.WHITE;


	public void setBackgroundColor(Color col) {
		bgCol = col;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (g instanceof Graphics2D) {
			Graphics2D graphics2d = (Graphics2D) g;
			graphics2d.setColor(bgCol);
			graphics2d.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
		}
	}

}
