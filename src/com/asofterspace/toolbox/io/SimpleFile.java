/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.io;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple file object describes a single line-oriented text file and enables simple access to
 * its contents.
 */
public class SimpleFile extends File {

	protected List<String> filecontents;

	protected boolean usingUtf8Bom = false;


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

	public boolean isUsingUTF8BOM() {
		return usingUtf8Bom;
	}

	/**
	 * Determines whether the UTF8 BOM should be written or not,
	 * but does NOT immediately write the file - so after changing
	 * this, if you also want to change the file on the disk, call
	 * save() afterwards!
	 */
	public void useUTF8BOM(boolean useItOrNot) {
		usingUtf8Bom = useItOrNot;
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

		createParentDirectory();

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

		try {
			byte[] binaryContent = Files.readAllBytes(this.getJavaPath());

			usingUtf8Bom = false;

			if (binaryContent.length > 2) {
				if ((binaryContent[0] == (byte) 239) &&
					(binaryContent[1] == (byte) 187) &&
					(binaryContent[2] == (byte) 191)) {
					usingUtf8Bom = true;
				}
			}

			String newContent = new String(binaryContent, StandardCharsets.UTF_8);

			if (usingUtf8Bom) {
				newContent = newContent.substring(1);
			}

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

		filecontents.add(line);
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

		java.io.File targetFile = initSave();

		if (usingUtf8Bom) {
			// initialize the file with the BOM
			try (FileOutputStream stream = new FileOutputStream(targetFile, false)) {

				// 0xEF 0xBB 0xBF
				byte[] utf8Bom = {(byte) 239, (byte) 187, (byte) 191};

				stream.write(utf8Bom);

			} catch (IOException e) {
				System.err.println("[ERROR] An IOException occurred when trying to write the UTF8 BOM to the file " + filename + " - inconceivable!");
			}
		}

		// if we are using a UTF8 BOM, append now (to the BOM we just wrote)
		// if we are not using a UTF8 BOM, do not append (but just overwrite), as we have not yet written
		// to the file (as we have not written any BOM or anything else)
		boolean append = usingUtf8Bom;

		// fill file with the actual data
		try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(targetFile, append), StandardCharsets.UTF_8)) {

			for (String line : filecontents) {
				writer.write(line);
				writer.write(lineEnding);
			}

		} catch (IOException e) {
			System.err.println("[ERROR] An IOException occurred when trying to write to the file " + filename + " - inconceivable!");
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
