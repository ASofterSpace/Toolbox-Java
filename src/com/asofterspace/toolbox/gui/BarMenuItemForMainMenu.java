/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.gui;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;


/**
 * This is a bar item right inside the main menu with a bar that
 * can be moved by the user
 */
public class BarMenuItemForMainMenu extends MenuItemForMainMenu {

	public static final long serialVersionUID = 3458397457249723l;


	private int min;
	private int max;
	private int pos;

	private List<BarListener> listeners;


	public BarMenuItemForMainMenu() {
		super("");

		setMinimum(0);
		setMaximum(100);
		this.pos = 0;

		this.listeners = new ArrayList<>();

		addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}

			@Override
			public void mousePressed(MouseEvent e) {
				setBarPosition(e.getX());
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}
		});
	}

	public void setMinimum(int min) {
		this.min = min;
	}

	public void setMaximum(int max) {
		this.max = max;
		setPreferredSize(new Dimension(max, getHeight()));
	}

	public void setBarPosition(int newPos) {
		if (newPos > max) {
			this.pos = max;
		} else if (newPos < min) {
			this.pos = min;
		} else {
			this.pos = newPos;
		}

		for (BarListener listener : listeners) {
			listener.onBarMove(this.pos);
		}

		repaint();
	}

	public void addBarListener(BarListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(getForeground());
		g.fillRect(0, 0, (getWidth() * pos) / max, getHeight());

		// if we re-introduce a label text, then the label should be written here :)
	}

}
