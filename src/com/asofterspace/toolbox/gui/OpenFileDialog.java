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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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

	// GUI parts
	private JTextField currentDirPathField;
	private JList<String> fileView;

	// data for the GUI
	private List<String> visibleFiles;
	private List<String> visibleFolders;


	public OpenFileDialog() {
	}

	public OpenFileDialog(Directory currentDirectory) {
		this();
		this.currentDirectory = currentDirectory;
	}

	public void showOpenDialog(CallbackWithStatus callback) {

		this.selectedFiles = new ArrayList<>();
		this.selectedFolders = new ArrayList<>();

		// Create the window
		final JFrame dialog = new JFrame(dialogTitle);
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
		JScrollPane fileViewScroller = new JScrollPane(fileView);
		fileViewScroller.setPreferredSize(new Dimension(8, 8));
		dialog.add(fileViewScroller, new Arrangement(0, 1, 1.0, 1.0));

		fileView.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int index = fileView.locationToIndex(e.getPoint());
					if (index < visibleFolders.size()) {
						// enter this folder
						enterFolder(visibleFolders.get(index));
					} else {
						// open this file
						selectedFiles = new ArrayList<>();
						selectedFolders = new ArrayList<>();
						selectedFiles.add(new File(currentDirectory, visibleFiles.get(index - visibleFolders.size())));
						dialog.dispose();
						callback.call(APPROVE_OPTION);
					}
				}
			}
		});

		JPanel buttonRow = new JPanel();
		GridLayout buttonRowLayout = new GridLayout(1, 3);
		buttonRowLayout.setHgap(8);
		buttonRow.setLayout(buttonRowLayout);
		dialog.add(buttonRow, new Arrangement(0, 2, 1.0, 0.0));

		JButton openButton = new JButton("Open");
		openButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// open the currently selected file(s)
				selectedFiles = new ArrayList<>();
				selectedFolders = new ArrayList<>();
				List<String> highlightedEntries = fileView.getSelectedValuesList();
				for (String highlightedEntry : highlightedEntries) {
					Directory newDir = new Directory(currentDirectory, highlightedEntry);
					if (newDir.exists()) {
						selectedFolders.add(newDir);
					} else {
						selectedFiles.add(new File(currentDirectory, highlightedEntry));
					}
				}
				dialog.dispose();
				callback.call(APPROVE_OPTION);
			}
		});
		buttonRow.add(openButton);

		JButton enterButton = new JButton("Enter");
		enterButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// enter into the currently selected folder
				List<String> highlightedEntries = fileView.getSelectedValuesList();
				if (highlightedEntries.size() > 0) {
					enterFolder(highlightedEntries.get(0));
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
	 * Refreshes the folders and files shown in the file view based on the current directory
	 */
	private void refreshFileView() {

		currentDirPathField.setText(currentDirectory.getCanonicalDirname());

		boolean recursively = false;

		visibleFolders = new ArrayList<>();

		for (Directory cur : currentDirectory.getAllDirectories(recursively)) {
			visibleFolders.add(cur.getLocalDirname());
		}

		Collections.sort(visibleFolders);

		visibleFiles = new ArrayList<>();

		for (File cur : currentDirectory.getAllFiles(recursively)) {
			visibleFiles.add(cur.getLocalFilename());
		}

		Collections.sort(visibleFiles);

		String[] fileViewData = new String[visibleFolders.size() + visibleFiles.size()];

		for (int i = 0; i < visibleFolders.size(); i++) {
			fileViewData[i] = visibleFolders.get(i);
		}
		for (int i = 0; i < visibleFiles.size(); i++) {
			fileViewData[i + visibleFolders.size()] = visibleFiles.get(i);
		}

		fileView.setListData(fileViewData);
	}

	private void enterFolder(String folderName) {
		Directory newDirectory = new Directory(currentDirectory, folderName);
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
	}

	public List<File> getSelectedFiles() {
		return selectedFiles;
	}

	public List<Directory> getSelectedFolders() {
		return selectedFolders;
	}

}
