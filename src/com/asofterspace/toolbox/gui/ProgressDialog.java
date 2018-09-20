package com.asofterspace.toolbox.gui;

import com.asofterspace.toolbox.utils.ProgressIndicator;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;


/**
 * This class takes care of displaying a modal window containing a progress bar which can be updated programmatically.
 * Whoop whoop! :)
 */
public class ProgressDialog implements ProgressIndicator {

	private JFrame progressDialog;
	
	private JProgressBar progressBar;
	
	private final static int RESOLUTION = 32768;

	
	public ProgressDialog(String explanation) {

		// Create the window
		progressDialog = new JFrame("Progress");
		GridLayout progressDialogLayout = new GridLayout(2, 1);
		progressDialogLayout.setVgap(8);
		progressDialog.setLayout(progressDialogLayout);
		progressDialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		// Populate the window
		JLabel explanationLabel = new JLabel();
		explanationLabel.setText(explanation);
		progressDialog.add(explanationLabel);

		progressBar = new JProgressBar(0, RESOLUTION);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		progressDialog.add(progressBar);

		// Set the preferred size of the dialog
		int width = 500;
		int height = 120;
		progressDialog.setSize(width, height);
		progressDialog.setPreferredSize(new Dimension(width, height));

		GuiUtils.centerAndShowWindow(progressDialog);
	}
	
	public void setProgress(final double currentProgress) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				progressBar.setValue((int) (RESOLUTION * currentProgress));
				progressBar.update(progressBar.getGraphics());
			}
		});
	}
	
	public void done() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				progressDialog.dispose();
			}
		});
	}

}