/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.gui;

import javax.swing.JDialog;
import javax.swing.JFrame;


/**
 * This can be used as the basis for any main window, yay!
 */
public abstract class MainWindow implements Runnable {

	protected JFrame mainFrame;


	@Override
	public void run() {
	
		create();
		
		show();
	}

	public void create() {

		JFrame.setDefaultLookAndFeelDecorated(false);
		JDialog.setDefaultLookAndFeelDecorated(false);

		// Create the window
		mainFrame = new JFrame();
		
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public void show() {

		GuiUtils.centerAndShowWindow(mainFrame);
	}

	public JFrame getMainFrame() {
		return mainFrame;
	}

}
