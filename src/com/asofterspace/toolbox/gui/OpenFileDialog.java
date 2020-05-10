/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.gui;

import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;


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
		this.currentDirectory = currentDirectory;
	}

	/**
	 * Shows the open dialog and synchronously returns only after the user picked a file
	 * or closed the dialog in some other way, with the return value indicating what
	 * actually happened
	 */
	public int showOpenDialog(Component parent) {

		selectedFiles = new ArrayList<>();
		selectedFolders = new ArrayList<>();

		// TODO
		return ERROR_OPTION;
	}

	public List<File> getSelectedFiles() {
		return selectedFiles;
	}

	public List<Directory> getSelectedFolders() {
		return selectedFolders;
	}

}
