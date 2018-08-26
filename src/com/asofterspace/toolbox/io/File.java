package com.asofterspace.toolbox.io;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
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
	}

	/**
	 * Create a new file instance based on a Java File
	 * @param javaFile
	 */
	public File(java.io.File javaFile) {

		filename = javaFile.getName();
	}
	
	/**
	 * Get the filename associated with this file object
	 */
	public String getFilename() {

		return filename;
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
		
		if (filecontents == null) {
			loadContents();
		}
		
		return filecontents;
	}

	/**
	 * Gets the file content from the last time it was read
	 * from the file system or set explicitly
	 * @return file content as \n-separated lines in one string
	 */
	public String getContent() {

		// if the content has not yet been fetched... fetch it!
		if (filecontents == null) {
			loadContents();
		}

		// if the content still is not available... meh!
		if (filecontents == null) {
			return "";
		}

		StringBuilder result = new StringBuilder();

		String separator = "";

		for (String line : filecontents) {

			result.append(separator);
			separator = "\n";

			result.append(line);
		}

		return result.toString();
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
			if (line.endsWith("\r")) {
				line = line.substring(0, line.length() - 1);
			}
			if (line.startsWith("\r")) {
				line = line.substring(1);
			}
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
			System.err.println("[ERROR] An IOException occurred when trying to create the file " + thisFile + " - inconceivable!");
		}
		
		// fill file with data
		try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(thisFile), StandardCharsets.UTF_8)) {
			
			for (String line : filecontents) {
				writer.write(line + "\n");
			}
			
		} catch (IOException e) {
			System.err.println("[ERROR] An IOException occurred when trying to write to the file " + thisFile + " - inconceivable!");
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
	
	/**
	 * Gives back a string representation of the file object
	 */
	@Override
	public String toString() {
		return "com.asofterspace.toolbox.io.File: " + filename;
	}

}
