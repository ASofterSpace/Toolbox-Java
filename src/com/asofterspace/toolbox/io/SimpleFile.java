/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.io;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple file object describes a single-line-oriented text file and enables simple access to
 * its contents.
 */
public class SimpleFile extends TextFile {

	protected List<String> filecontents;


	/**
	 * Please do not construct a file without a name ;)
	 */
	protected SimpleFile() {
	}

	/**
	 * Create a new file instance based on the fully qualified file name,
	 * using a slash as separator independent of the operating system,
	 * and automatically loading the file contents (if there are any)
	 * @param fullyQualifiedFileName
	 */
	public SimpleFile(String fullyQualifiedFileName) {

		super(fullyQualifiedFileName);
	}

	/**
	 * Create a new file instance based on another File
	 * @param File
	 */
	public SimpleFile(File regularFile) {

		super(regularFile);
	}

	/**
	 * Create a new file instance based on a Java File
	 * @param javaFile
	 */
	public SimpleFile(java.io.File javaFile) {

		super(javaFile);
	}

	/**
	 * Create a new file instance based on a Directory and the name of
	 * the file inside the directory
	 * @param directory The directory in which the file is located
	 * @param filename The (local) name of the actual file
	 */
	public SimpleFile(Directory directory, String filename) {

		super(directory, filename);
	}

	/**
	 * Creates this file on the disk, which entails:
	 * - creating the parent directory
	 * - assigning an empty file content, if there is not already content
	 *   (if this file has already been assigned content, that content will
	 *   be kept!)
	 * - saving the file to disk
	 */
	public void create() {

		super.prepareToCreate();

		if (filecontents == null) {
			filecontents = new ArrayList<>();
		}

		save();
	}

	/**
	 * Loads the file contents from the file system
	 * @return file contents
	 */
	public List<String> loadContents(boolean complainIfMissing) {

		setContent(super.loadContent(complainIfMissing));

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
	 * Clear the content (without saving)
	 */
	public void clearContent() {

		filecontents = new ArrayList<>();
	}

	/**
	 * Explicitly sets the contents of this file instance
	 * (this does NOT automagically write them to the hard
	 * drive - if that is wanted, use saveContents()!)
	 * @param contents file contents to be set
	 */
	public void setContents(List<String> contents) {

		filecontents = new ArrayList<>(contents);
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

		if (content == null) {
			filecontents = null;
			return;
		}

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
	 * Append a single line to the contents (without saving)
	 */
	public void appendContent(String line) {

		ensureContents(true);

		filecontents.add(line);
	}

	/**
	 * Insert a single line into the contents (without saving)
	 */
	public void insertContent(String line, int lineNum) {

		ensureContents(true);

		filecontents.add(lineNum, line);
	}

	/**
	 * Saves the current file contents of this instance
	 * to the file system
	 */
	public void save() {
		saveWithLineEndings("\n");
	}

	public void saveWithSystemLineEndings() {
		saveWithLineEndings(System.lineSeparator());
	}

	public void saveWithLineEndings(String lineEnding) {

		StringBuilder contentBuilder = new StringBuilder();

		for (String line : filecontents) {
			contentBuilder.append(line);
			contentBuilder.append(lineEnding);
		}

		super.setContent(contentBuilder.toString());

		super.save();
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
	 * Sets the file contents of this instance and writes
	 * them to the file system
	 * @param content file content
	 */
	public void saveContent(StringBuilder content) {

		setContent(content.toString());

		save();
	}

	/**
	 * Copy this file to another another file instance
	 */
	@Override
	public void copyToFileObject(File other) {

		super.copyToFileObject(other);

		if (other instanceof SimpleFile) {

			SimpleFile simpleOther = (SimpleFile) other;

			if (this.filecontents == null) {
				simpleOther.filecontents = null;
			} else {
				simpleOther.filecontents = new ArrayList<>(this.filecontents);
			}

		} else if (other instanceof TextFile) {

			TextFile textOther = (TextFile) other;

			textOther.setContent(this.getContent());
		}
	}

	/**
	 * Gives back a string representation of the file object
	 */
	@Override
	public String toString() {
		return "com.asofterspace.toolbox.io.SimpleFile: " + filename;
	}

}
