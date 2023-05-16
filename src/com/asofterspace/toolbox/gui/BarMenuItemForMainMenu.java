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

import javax.swing.SwingUtilities;


/**
 * This is a bar item right inside the main menu with a bar that
 * can be moved by the user
 */
public class BarMenuItemForMainMenu extends MenuItemForMainMenu {

	public static final long serialVersionUID = 3458397457249723l;

	private static int BORDER_WIDTH = 1;

	private int min;
	private int max;
	private Integer pos;
	private Integer setPos;
	private Integer prevPos;

	private boolean mouseDown;
	private boolean sendUpdateOnMousePress = false;

	private List<BarListener> listeners;


	public BarMenuItemForMainMenu() {
		super("");

		setMinimum(0);
		setMaximum(100);
		this.pos = 0;
		this.setPos = 0;
		this.prevPos = null;

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
				if (SwingUtilities.isRightMouseButton(e)) {
					if (setPos == null) {
						displayBarAtPosition(prevPos, true);
					} else {
						displayBarAtPosition(null, true);
					}
				} else {
					if (sendUpdateOnMousePress) {
						setBarPosition(e.getX(), true);
					} else {
						displayBarAtPosition(e.getX(), true);
					}
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				mouseDown = false;
				if (SwingUtilities.isRightMouseButton(e)) {
					if (setPos == null) {
						setBarPosition(prevPos, true);
					} else {
						prevPos = setPos;
						setBarPosition(null, true);
					}
				} else {
					setBarPosition(e.getX(), true);
				}
			}
		});

		addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseDragged(MouseEvent e) {
				if (mouseDown) {
					displayBarAtPosition(e.getX(), true);
				}
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				if (mouseDown) {
					displayBarAtPosition(e.getX(), true);
				}
			}
		});
	}

	public void setMinimum(int min) {
		this.min = min;
	}

	public void setMaximum(int max) {
		this.max = max;
		setSize(new Dimension(max + 2 * BORDER_WIDTH, getHeight()));
		setPreferredSize(new Dimension(max + 2 * BORDER_WIDTH, (int) getPreferredSize().getHeight()));
		setMinimumSize(new Dimension(max + 2 * BORDER_WIDTH, (int) getMinimumSize().getHeight()));
		setMaximumSize(new Dimension(max + 2 * BORDER_WIDTH, (int) getMaximumSize().getHeight()));
	}

	public void setBarPosition(Integer newPos, boolean notifyListeners) {

		// re-display but do not send update as this will be done in a second anyway
		displayBarAtPosition(newPos, false);

		this.setPos = this.pos;

		if (notifyListeners) {
			for (BarListener listener : listeners) {
				listener.onBarMove(this.pos);
			}
		}
	}

	private void displayBarAtPosition(Integer newPos, boolean notifyListeners) {

		if (newPos == null) {
			this.pos = newPos;
		} else {
			if (newPos > max) {
				this.pos = max;
			} else if (newPos < min) {
				this.pos = min;
			} else {
				this.pos = newPos;
			}
		}

		if (notifyListeners) {
			for (BarListener listener : listeners) {
				listener.onBarDisplay(this.pos);
			}
		}

		repaint();
	}

	public void addBarListener(BarListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		int outerWidth = getWidth();
		int outerHeight = getHeight();
		int innerWidth = outerWidth - 2 * BORDER_WIDTH;
		int innerHeight = outerHeight - 2 * BORDER_WIDTH;

		g.setColor(getForeground());
		g.drawLine(0, 0, outerWidth-1, 0);
		g.drawLine(0, outerHeight-1, outerWidth-1, outerHeight-1);
		g.drawLine(0, 0, 0, outerHeight-1);
		g.drawLine(outerWidth-1, 0, outerWidth-1, outerHeight-1);
		g.setColor(getBackground());
		g.fillRect(BORDER_WIDTH, BORDER_WIDTH, innerWidth, innerHeight);
		g.setColor(getForeground());
		if (pos == null) {
			int quarterWidth = innerWidth / 4;
			int halfHeight = innerHeight / 2;
			g.fillRect(BORDER_WIDTH, BORDER_WIDTH, quarterWidth, halfHeight);
			g.fillRect(BORDER_WIDTH + quarterWidth, BORDER_WIDTH + halfHeight, quarterWidth, innerHeight-halfHeight);
			g.fillRect(BORDER_WIDTH + (2 * quarterWidth), BORDER_WIDTH, quarterWidth, halfHeight);
			g.fillRect(BORDER_WIDTH + (3 * quarterWidth), BORDER_WIDTH + halfHeight, quarterWidth, innerHeight-halfHeight);
		} else {
			g.fillRect(BORDER_WIDTH, BORDER_WIDTH, (innerWidth * pos) / max, innerHeight);
		}

		// if we re-introduce a label text, then the label should be written here :)
	}

	public boolean getSendUpdateOnMousePress() {
		return sendUpdateOnMousePress;
	}

	public void setSendUpdateOnMousePress(boolean sendUpdateOnMousePress) {
		this.sendUpdateOnMousePress = sendUpdateOnMousePress;
	}

}
