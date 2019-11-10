/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.gui;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
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

	private boolean mouseDown;

	private List<BarListener> listeners;


	public BarMenuItemForMainMenu() {
		super("");

		setMinimum(0);
		setMaximum(100);
		this.pos = 0;

		this.listeners = new ArrayList<>();

		mouseDown = false;

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
				mouseDown = true;
				displayBarAtPosition(e.getX());
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				mouseDown = false;
				setBarPosition(e.getX());
			}
		});

		addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseDragged(MouseEvent e) {
				if (mouseDown) {
					displayBarAtPosition(e.getX());
				}
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				if (mouseDown) {
					displayBarAtPosition(e.getX());
				}
			}
		});
	}

	public void setMinimum(int min) {
		this.min = min;
	}

	public void setMaximum(int max) {
		this.max = max;
		setSize(new Dimension(max, getHeight()));
		setPreferredSize(new Dimension(max, (int) getPreferredSize().getHeight()));
		setMinimumSize(new Dimension(max, (int) getMinimumSize().getHeight()));
		setMaximumSize(new Dimension(max, (int) getMaximumSize().getHeight()));
	}

	public void setBarPosition(Integer newPos) {

		displayBarAtPosition(newPos);

		notifyBarListeners();
	}

	private void displayBarAtPosition(Integer newPos) {

		if (newPos == null) {
			newPos = 0;
		}

		if (newPos > max) {
			this.pos = max;
		} else if (newPos < min) {
			this.pos = min;
		} else {
			this.pos = newPos;
		}

		repaint();
	}

	private void notifyBarListeners() {

		for (BarListener listener : listeners) {
			listener.onBarMove(this.pos);
		}
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
