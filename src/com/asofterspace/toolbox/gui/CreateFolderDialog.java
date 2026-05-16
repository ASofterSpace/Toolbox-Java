/**
 * Unlicensed code created by A Softer Space, 2026
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.gui;

import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.utils.Callback;
import com.asofterspace.toolbox.utils.CallbackWithStatus;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class CreateFolderDialog {

	// return values for showing the create folder dialog
	public static final int APPROVE_OPTION = JFileChooser.APPROVE_OPTION;
	public static final int CANCEL_OPTION = JFileChooser.CANCEL_OPTION;
	public static final int ERROR_OPTION = JFileChooser.ERROR_OPTION;

	private Directory currentDirectory;
	private Directory createdDirectory = null;

	private JFrame dialog = null;
	private JTextField newDirNameField = null;


	public CreateFolderDialog(Directory currentDirectory) {
		this.currentDirectory = currentDirectory;
	}

	public void showDialog(CallbackWithStatus callback) {

		if (currentDirectory == null) {
			currentDirectory = new Directory("/");
		}
		this.dialog = new JFrame("Create Directory in " + currentDirectory.getCanonicalDirname());

		GridBagLayout dialogLayout = new GridBagLayout();
		dialog.setLayout(dialogLayout);
		dialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		int rowNum = 0;

		newDirNameField = new JTextField();
		dialog.add(newDirNameField, new Arrangement(0, rowNum++, 1.0, 0.0));

		JPanel buttonRow = new JPanel();
		GridLayout buttonRowLayout = new GridLayout(1, 3);
		buttonRowLayout.setHgap(8);
		buttonRow.setLayout(buttonRowLayout);
		dialog.add(buttonRow, new Arrangement(0, rowNum++, 1.0, 0.0));

		JButton createButton = null;
		createButton = new JButton("Create");
		createButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				createdDirectory = new Directory(currentDirectory, newDirNameField.getText());
				createdDirectory.create();
				dialog.dispose();
				callback.call(APPROVE_OPTION);
			}
		});
		buttonRow.add(createButton);

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// cancel the dialog
				dialog.dispose();
				callback.call(CANCEL_OPTION);
			}
		});
		buttonRow.add(cancelButton);

		// Set the preferred size of the dialog
		int width = 500;
		int height = 200;
		dialog.setSize(width, height);
		dialog.setPreferredSize(new Dimension(width, height));

		GuiUtils.centerAndShowWindow(dialog);
	}

	public Directory getCreatedDirectory() {
		return createdDirectory;
	}
}
