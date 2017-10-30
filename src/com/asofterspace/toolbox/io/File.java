package com.asofterspace.toolbox.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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
		
		try {
			byte[] encoded = Files.readAllBytes(Paths.get(filename));

			String newContent = new String(encoded, StandardCharsets.UTF_8);
			  
			setContent(newContent);
			
		} catch (IOException e) {
			System.err.println("[ERROR] Trying to load the file " + filename + ", an I/O Exception occurred - inconceivable!");
		}
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
	 * Explicitly sets the contents of this file instance
	 * as text consisting of several \n-separated lines
	 * (this does NOT automagically write them to the hard
	 * drive - if that is wanted, use saveContents()!)
	 * @param contents file contents to be set
	 */
	public void setContent(String content) {
		
		filecontents = new ArrayList<String>();
		
		String[] lines = content.split("\n");
		
		for (String line : lines) {
			filecontents.add(line);
		}
	}
	
	/**
	 * Saves the current file contents of this instance
	 * to the file system
	 */
	public void save() {

		java.io.File thisFile = new java.io.File(filename);
		
		// create parent directories
		thisFile.getParentFile().mkdirs();
		
		// create file
		try {
			
			thisFile.createNewFile();
			
		} catch (IOException e) {
			System.err.println("[ERROR] An IOException occurred when trying to create the file - inconceivable!");
		}
		
		// fill file with data
		try (PrintWriter writer = new PrintWriter(thisFile)) {
			
			for (String line : filecontents) {
				writer.println(line);
			}
			
		} catch (FileNotFoundException e) {
			System.err.println("[ERROR] We attempted to save a file, but a FileNotFoundException was raised - inconceivable!");
		}
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
	 * Sets the file contents of this instance and writes
	 * them to the file system
	 * @param content file content
	 */
	public void saveContent(String content) {
		
		setContent(content);
		
		save();
	}
	
}
