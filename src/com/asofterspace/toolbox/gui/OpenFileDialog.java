/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.gui;

import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.utils.Callback;
import com.asofterspace.toolbox.utils.CallbackWithStatus;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;


public class OpenFileDialog {

	// possible file selection modes
	public static final int FILES_ONLY = JFileChooser.FILES_ONLY;
	public static final int DIRECTORIES_ONLY = JFileChooser.DIRECTORIES_ONLY;
	public static final int FILES_AND_DIRECTORIES = JFileChooser.FILES_AND_DIRECTORIES;

	// return values for showing the open dialog
	public static final int APPROVE_OPTION = JFileChooser.APPROVE_OPTION;
	public static final int CANCEL_OPTION = JFileChooser.CANCEL_OPTION;
	public static final int ERROR_OPTION = JFileChooser.ERROR_OPTION;

	private Directory currentDirectory = new Directory(".");

	private String dialogTitle = "Open File";

	private int fileSelectionMode = FILES_ONLY;

	private boolean multiSelectionEnabled = false;

	private List<File> selectedFiles;

	private List<Directory> selectedFolders;


	public OpenFileDialog() {
	}

	public OpenFileDialog(Directory currentDirectory) {
		this();
		this.currentDirectory = currentDirectory;
	}

	public void showOpenDialog(Frame parent, CallbackWithStatus callback) {

		this.selectedFiles = new ArrayList<>();
		this.selectedFolders = new ArrayList<>();

		// Create the window
		final JDialog dialog = new JDialog(parent, dialogTitle, true);
		GridLayout dialogLayout = new GridLayout(7, 1);
		dialogLayout.setVgap(8);
		dialog.setLayout(dialogLayout);
		dialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		// Populate the window
		final JTextField currentDirPathField = new JTextField();
		dialog.add(currentDirPathField);

		final JList<String> fileView = new JList<>();
		dialog.add(fileView);

		JPanel buttonRow = new JPanel();
		GridLayout buttonRowLayout = new GridLayout(1, 3);
		buttonRowLayout.setHgap(8);
		buttonRow.setLayout(buttonRowLayout);
		dialog.add(buttonRow);

		JButton openButton = new JButton("Open");
		openButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// open the currently selected file(s)
				// TODO
			}
		});
		buttonRow.add(openButton);

		JButton enterButton = new JButton("Enter");
		enterButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// enter into the currently selected folder
				// TODO
			}
		});
		buttonRow.add(enterButton);

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
		int width = 600;
		int height = 320;
		dialog.setSize(width, height);
		dialog.setPreferredSize(new Dimension(width, height));

		GuiUtils.centerAndShowWindow(dialog);
	}

	public Directory getCurrentDirectory() {
		return currentDirectory;
	}

	public void setCurrentDirectory(Directory currentDirectory) {
		this.currentDirectory = currentDirectory;
	}

	public String getDialogTitle() {
		return dialogTitle;
	}

	public void setDialogTitle(String dialogTitle) {
		this.dialogTitle = dialogTitle;
	}

	public int getFileSelectionMode() {
		return fileSelectionMode;
	}

	public void setFileSelectionMode(int fileSelectionMode) {
		this.fileSelectionMode = fileSelectionMode;
	}

	public boolean getMultiSelectionEnabled() {
		return multiSelectionEnabled;
	}

	public void setMultiSelectionEnabled(boolean multiSelectionEnabled) {
		this.multiSelectionEnabled = multiSelectionEnabled;
	}

	public List<File> getSelectedFiles() {
		return selectedFiles;
	}

	public List<Directory> getSelectedFolders() {
		return selectedFolders;
	}

}
