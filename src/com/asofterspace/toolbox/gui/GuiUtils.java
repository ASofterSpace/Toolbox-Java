/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;


public class GuiUtils {

	// on the left hand side, we add this string to indicate that the file has changed
	public final static String CHANGE_INDICATOR = " *";

	public final static String LIGHT_SCHEME = "light";
	public final static String DARK_SCHEME = "dark";


	/**
	 * Maximize the window
	 */
	public static void maximizeWindow(JFrame window) {

		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();

		Rectangle bounds = env.getMaximumWindowBounds();
		window.setMaximizedBounds(bounds);

		window.setSize((int) bounds.getWidth(), (int) bounds.getHeight());

		window.setPreferredSize(new Dimension((int) bounds.getWidth(), (int) bounds.getHeight()));

		window.setLocation(new Point(0, 0));

		// This should actually maximize the window, but for some reason does not work (reliably),
		// so instead we do it manually in the lines above...
		// window.setExtendedState(window.getExtendedState() | JFrame.MAXIMIZED_BOTH);
	}

	/**
	 * Takes a created window - e.g. a JFrame or a JDialog - and shows it in the middle of the screen
	 */
	public static void centerAndShowWindow(final Window window) {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// Stage everything to be shown
				window.pack();

				// Actually display the whole jazz
				window.setVisible(true);

				// Center the window
				window.setLocationRelativeTo(null);
			}
		});
	}

	/**
	 * Extends the width of a component in a vertical boxlayout to the maximum width of the container
	 * Attention: This should only be called after all the content has been added to whatToMakeWide,
	 * as it will set the current vertical size as maximum size, so if you add content to it later,
	 * it will not be considered!
	 */
	public static void makeWide(JComponent whatToMakeWide) {

		whatToMakeWide.setAlignmentX(Component.CENTER_ALIGNMENT);

		Dimension makeWide = whatToMakeWide.getPreferredSize();
		makeWide.width = Integer.MAX_VALUE;
		whatToMakeWide.setMaximumSize(makeWide);
	}

}
