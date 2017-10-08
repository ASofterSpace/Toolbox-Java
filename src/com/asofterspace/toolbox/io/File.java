package com.asofterspace.toolbox.io;

import java.util.ArrayList;
import java.util.List;

public class File {
	
	private String filename;
	
	private List<String> filecontents;

	
	/**
	 * Please do not construct a file without a name ;)
	 */
	@SuppressWarnings("unused")
	private File() {
	}
	
	/**
	 * Create a new file instance based on the fully qualified file name,
	 * using a slash as separator independent of the operating system,
	 * and automatically loading the file contents (if there are any)
	 * @param fullyQualifiedFileName
	 */
	public File(String fullyQualifiedFileName) {
	
		filename = fullyQualifiedFileName;
		
		loadContents();
	}
	
	/**
	 * Loads the file contents from the file system
	 * @return file contents
	 */
	public List<String> loadContents() {
		
		createParentDirectory();
		
		filecontents = new ArrayList<String>();
		
		// TODO
		
		return filecontents;
	}

	/**
	 * Gets the file contents from the last time they were read
	 * from the file system or set explicitly
	 * @return file contents
	 */
	public List<String> getContents() {
		
		return filecontents;
	}
	
	/**
	 * Explicitly sets the contents of this file instance
	 * (this does NOT automagically write them to the hard
	 * drive - if that is wanted, use saveContents()!)
	 * @param contents file contents to be set
	 */
	public void setContents(List<String> contents) {
		
		filecontents = contents;
	}
	
	/**
	 * Saves the current file contents of this instance
	 * to the file system
	 */
	public void save() {

		createParentDirectory();
		
		// TODO
	}
	
	/**
	 * Sets the file contents of this instance and writes
	 * them to the file system
	 * @param contents file contents
	 */
	public void saveContents(List<String> contents) {
		
		setContents(contents);
		
		save();
	}
	
	/**
	 * Creates the parent directory of this file on
	 * the file system
	 */
	private void createParentDirectory() {
		
		// TODO
	}
	
}
