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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;


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

	private List<Directory> selectedDirectories;

	// GUI parts
	private JFrame dialog;
	private JTextField currentDirPathField;
	private JList<String> fileView;

	// data for the GUI
	private List<String> visibleFiles;
	private List<String> visibleDirectories;

	private CallbackWithStatus callback;


	public OpenFileDialog() {
	}

	public OpenFileDialog(Directory currentDirectory) {
		this();
		this.currentDirectory = currentDirectory;
	}

	public void showOpenDialog(CallbackWithStatus callback) {

		this.callback = callback;
		this.selectedFiles = new ArrayList<>();
		this.selectedDirectories = new ArrayList<>();

		// Create the window
		this.dialog = new JFrame(dialogTitle);
		GridBagLayout dialogLayout = new GridBagLayout();
		dialog.setLayout(dialogLayout);
		dialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		// Populate the window
		JPanel topPanel = new JPanel();
		GridBagLayout topPanelLayout = new GridBagLayout();
		topPanel.setLayout(topPanelLayout);

		currentDirPathField = new JTextField();
		topPanel.add(currentDirPathField, new Arrangement(0, 0, 1.0, 0.0));

		// listen to the enter key being pressed
		currentDirPathField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Directory newDir = new Directory(currentDirPathField.getText());
				if (newDir.exists()) {
					currentDirectory = newDir;
					refreshFileView();
				}
			}
		});

		JButton upBtn = new JButton("Up");
		upBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Directory newDir = currentDirectory.getParentDirectory();
				if (newDir.exists()) {
					currentDirectory = newDir;
					refreshFileView();
				}
			}
		});
		topPanel.add(upBtn, new Arrangement(1, 0, 0.0, 0.0));

		dialog.add(topPanel, new Arrangement(0, 0, 1.0, 0.0));

		fileView = new JList<>();
		setMultiSelectionEnabled(multiSelectionEnabled);
		JScrollPane fileViewScroller = new JScrollPane(fileView);
		fileViewScroller.setPreferredSize(new Dimension(8, 8));
		dialog.add(fileViewScroller, new Arrangement(0, 1, 1.0, 1.0));

		// enter directories / open files when double clicking
		fileView.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					// one selected (as a double click always targets just one row)
					// - enter directory (if it is one) or open file (if it is one)
					int index = fileView.locationToIndex(e.getPoint());
					performFileViewActionBasedOnIndex(index);
				}
			}
		});

		// enter directories / open files when pressing [Enter] key
		fileView.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {

					int[] indices = fileView.getSelectedIndices();

					// none selected - do nothing
					if (indices.length < 1) {
						return;
					}

					// one selected - enter directory (if it is one) or open file (if it is one)
					if (indices.length == 1) {
						performFileViewActionBasedOnIndex(indices[0]);
						return;
					}

					// several selected - open them all
					openCurrentlySelectedFiles();
				}
			}
		});

		JPanel buttonRow = new JPanel();
		GridLayout buttonRowLayout = new GridLayout(1, 3);
		buttonRowLayout.setHgap(8);
		buttonRow.setLayout(buttonRowLayout);
		dialog.add(buttonRow, new Arrangement(0, 2, 1.0, 0.0));

		JButton openButton = new JButton("Select These");
		openButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openCurrentlySelectedFiles();
			}
		});
		buttonRow.add(openButton);

		JButton enterButton = new JButton("Descend Into Folder");
		enterButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// enter into the currently selected directory
				List<String> highlightedEntries = fileView.getSelectedValuesList();
				if (highlightedEntries.size() > 0) {
					enterDirectory(highlightedEntries.get(0));
				}
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

		refreshFileView();
	}

	/**
	 * open the currently selected file(s) and folder(s)
	 */
	private void openCurrentlySelectedFiles() {

		selectedFiles = new ArrayList<>();
		selectedDirectories = new ArrayList<>();

		List<String> highlightedEntries = fileView.getSelectedValuesList();

		for (String highlightedEntry : highlightedEntries) {
			Directory newDir = new Directory(currentDirectory, highlightedEntry);
			if (newDir.exists()) {
				if (fileSelectionMode != FILES_ONLY) {
					selectedDirectories.add(newDir);
				}
			} else {
				if (fileSelectionMode != DIRECTORIES_ONLY) {
					selectedFiles.add(new File(currentDirectory, highlightedEntry));
				}
			}
		}
		dialog.dispose();
		callback.call(APPROVE_OPTION);
	}

	private void performFileViewActionBasedOnIndex(int index) {

		if (index < visibleDirectories.size()) {
			// enter this directory
			enterDirectory(visibleDirectories.get(index));
		} else {
			if (fileSelectionMode == DIRECTORIES_ONLY) {
				return;
			}
			// open this file
			selectedFiles = new ArrayList<>();
			selectedDirectories = new ArrayList<>();
			selectedFiles.add(new File(currentDirectory, visibleFiles.get(index - visibleDirectories.size())));
			dialog.dispose();
			callback.call(APPROVE_OPTION);
		}
	}

	/**
	 * Refreshes the directorys and files shown in the file view based on the current directory
	 */
	private void refreshFileView() {

		currentDirPathField.setText(currentDirectory.getCanonicalDirname());

		boolean recursively = false;

		visibleDirectories = new ArrayList<>();

		for (Directory cur : currentDirectory.getAllDirectories(recursively)) {
			visibleDirectories.add(cur.getLocalDirname());
		}

		Collections.sort(visibleDirectories, new Comparator<String>() {
			public int compare(String a, String b) {
				return a.toLowerCase().compareTo(b.toLowerCase());
			}
		});

		visibleFiles = new ArrayList<>();

		for (File cur : currentDirectory.getAllFiles(recursively)) {
			visibleFiles.add(cur.getLocalFilename());
		}

		Collections.sort(visibleFiles, new Comparator<String>() {
			public int compare(String a, String b) {
				return a.toLowerCase().compareTo(b.toLowerCase());
			}
		});

		String[] fileViewData = new String[visibleDirectories.size() + visibleFiles.size()];

		for (int i = 0; i < visibleDirectories.size(); i++) {
			fileViewData[i] = visibleDirectories.get(i);
		}
		for (int i = 0; i < visibleFiles.size(); i++) {
			fileViewData[i + visibleDirectories.size()] = visibleFiles.get(i);
		}

		fileView.setListData(fileViewData);
	}

	private void enterDirectory(String directoryName) {
		Directory newDirectory = new Directory(currentDirectory, directoryName);

		// automatically descend into folders that only contain exactly one other folder
		while (true) {
			boolean recursively = false;
			List<Directory> dirs = newDirectory.getAllDirectories(recursively);
			List<File> files = newDirectory.getAllFiles(recursively);
			if ((files.size() == 0) && (dirs.size() == 1)) {
				newDirectory = dirs.get(0);
			} else {
				break;
			}
		}

		if (newDirectory.exists()) {
			currentDirectory = newDirectory;
			refreshFileView();
		}
	}

	public Directory getCurrentDirectory() {
		return currentDirectory;
	}

	public void setCurrentDirectory(Directory currentDirectory) {
		if (currentDirectory != null) {
			this.currentDirectory = currentDirectory;
		}
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

		if (fileView != null) {
			if (multiSelectionEnabled) {
				fileView.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			} else {
				fileView.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			}
		}
	}

	public List<File> getSelectedFiles() {
		return selectedFiles;
	}

	public List<Directory> getSelectedDirectories() {
		return selectedDirectories;
	}

}
