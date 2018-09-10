package com.asofterspace.toolbox.io;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * A file object describes a single file and enables simple access to
 * its contents.
 */
public class File {

	String filename;

	List<String> filecontents;


	/**
	 * Please do not construct a file without a name ;)
	 */
	protected File() {
	}

	/**
	 * Create a new file instance based on the fully qualified file name,
	 * using a slash as separator independent of the operating system,
	 * and automatically loading the file contents (if there are any)
	 * @param fullyQualifiedFileName
	 */
	public File(String fullyQualifiedFileName) {
	
		this.filename = fullyQualifiedFileName;
	}

	/**
	 * Create a new file instance based on a Java File
	 * @param javaFile
	 */
	public File(java.io.File javaFile) {

		this.filename = javaFile.getAbsolutePath();
	}

	/**
	 * Create a new file instance based on a Directory and the name of
	 * the file inside the directory
	 * @param directory The directory in which the file is located
	 * @param filename The (local) name of the actual file
	 */
	public File(Directory directory, String filename) {

		this.filename = Paths.get(directory.getDirname()).resolve(filename).toAbsolutePath().toString();
	}
	
	/**
	 * Get the filename associated with this file object
	 */
	public String getFilename() {

		return filename;
	}
	
	/**
	 * Get only the local part of the filename associated with this file object,
	 * so just the name itself instead of the full path
	 */
	public String getLocalFilename() {

		if (filename == null) {
			return null;
		}
		
		String[] firstFilenameParts = filename.split("/");
		String firstResult = firstFilenameParts[firstFilenameParts.length - 1];
		
		String[] secondFilenameParts = filename.split("\\\\");
		String secondResult = secondFilenameParts[secondFilenameParts.length - 1];
		
		if (firstResult.length() > secondResult.length()) {
			return secondResult;
		} else {
			return firstResult;
		}
	}
	
	/**
	 * Get a Java File object representing this file
	 */
	public java.io.File getJavaFile() {

		return new java.io.File(filename);
	}
	
	/**
	 * Get a URI object representing this file
	 */
	public java.net.URI getURI() {

		return getJavaFile().toURI();
	}
	
	/**
	 * Gets the directory containing this file
	 */
	public Directory getParentDirectory() {

		return new Directory(getJavaFile().getParent());
	}

	/**
	 * Returns true if something exists under this name (which does NOT need to be a file, btw.!)
	 */
	public boolean exists() {

		return getJavaFile().exists();
	}
	
	/**
	 * Loads the file contents from the file system
	 * @return file contents
	 */
	public List<String> loadContents(boolean complainIfMissing) {
		
		try {
			byte[] binaryContent = Files.readAllBytes(Paths.get(filename));

			String newContent = new String(binaryContent, StandardCharsets.UTF_8);
			  
			setContent(newContent);
			
		} catch (IOException e) {
			if (complainIfMissing) {
				System.err.println("[ERROR] Trying to load the file " + filename + ", an I/O Exception occurred - inconceivable!");
			}
		}
		return filecontents;
	}
	
	/**
	 * Ensure that the contents have been loaded
	 * @param complainIfMissing Complain on sys err if the file is missing
	 */
	protected void ensureContents(boolean complainIfMissing) {
	
		if (filecontents == null) {
			loadContents(complainIfMissing);
		}
	}

	/**
	 * Gets the file contents from the last time they were read
	 * from the file system or set explicitly
	 * @return file contents
	 */
	public List<String> getContents() {
		
		return getContents(true);
	}

	/**
	 * Gets the file contents from the last time they were read
	 * from the file system or set explicitly
	 * @param complainIfMissing Complain on sys err if the file is missing
	 * @return file contents
	 */
	public List<String> getContents(boolean complainIfMissing) {
		
		// if the content has not yet been fetched... fetch it!
		ensureContents(complainIfMissing);
		
		return filecontents;
	}

	/**
	 * Gets the file content from the last time it was read
	 * from the file system or set explicitly
	 * @return file content as \n-separated lines in one string
	 */
	public String getContent() {
		return getContent(true);
	}
	
	/**
	 * Gets the file content from the last time it was read
	 * from the file system or set explicitly
	 * @param complainIfMissing Complain on sys err if the file is missing
	 * @return file content as \n-separated lines in one string
	 */
	public String getContent(boolean complainIfMissing) {

		// if the content has not yet been fetched... fetch it!
		ensureContents(complainIfMissing);

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
	public void setContent(StringBuilder content) {
		setContent(content.toString());
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
		if (thisFile.getParentFile() != null) {
			thisFile.getParentFile().mkdirs();
		}
		
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
	 * Re-set the location of this particular CDM file (such that when we call save() later on, the new location is used,
	 * but until save() is called, nothing changes)
	 */
	public void setFilelocation(File newLocation) {
	
		filename = newLocation.getFilename();
	}
	
	/**
	 * Copy this file to another another file instance
	 */
	public void copyTo(File other) {

		other.filename = this.filename;
		
		if (this.filecontents == null) {
			other.filecontents = null;
		} else {
			other.filecontents = new ArrayList<>(this.filecontents);
		}
	}
	
	/**
	 * Delete this file from disk
	 */
	public void delete() {
		getJavaFile().delete();
	}

	/**
	 * Gives back a string representation of the file object
	 */
	@Override
	public String toString() {
		return "com.asofterspace.toolbox.io.File: " + filename;
	}
	
}
