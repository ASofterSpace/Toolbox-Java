package com.asofterspace.toolbox.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;


public class GuiUtils {

	/**
	 * Takes a created window - e.g. a JFrame or a JDialog - and shows it in the middle of the screen
	 */
	public static void centerAndShowWindow(Window window) {

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// Center the window
				window.setLocationRelativeTo(null);

				// Stage everything to be shown
				window.pack();

				// Actually display the whole jazz
				window.setVisible(true);
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