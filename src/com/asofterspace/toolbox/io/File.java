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
 * A file object describes literally any file - but just a file, not a directory!
 */
public class File {

	protected String filename;


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
	 * Create a new file instance based on another File
	 * @param File
	 */
	public File(File regularFile) {

		regularFile.copyToFileObject(this);
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

		this.filename = directory.getJavaPath().resolve(filename).toAbsolutePath().toString();
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
	 * TODO :: this here might have problems if the filename legitimately contains
	 * slashes or backslashes! if that ever happens, be more clever! :)
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
	 * Get the absolute filename associated with this file object
	 */
	public String getAbsoluteFilename() {

		Path basePath = getJavaPath();

		return basePath.toAbsolutePath().toString();
	}

	/**
	 * Get the canonical filename associated with this file object,
	 * or if it is unavailable, at lest the absolute filename
	 */
	public String getCanonicalFilename() {

		try {
			return getJavaFile().getCanonicalPath();

		} catch (IOException | SecurityException e) {

			return getAbsoluteFilename();
		}
	}

	/**
	 * Get a Java File object representing this file
	 */
	public java.io.File getJavaFile() {

		return new java.io.File(filename);
	}

	/**
	 * Get a Java Path object representing this file
	 */
	public Path getJavaPath() {

		return Paths.get(filename);
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
	 * Gets the directory containing this file and ensures that it actually exists
	 */
	public Directory createParentDirectory() {

		Directory parentDir = getParentDirectory();

		parentDir.create();

		return parentDir;
	}

	/**
	 * Returns true if something exists under this name (which does NOT need to be a file, btw.!)
	 */
	public boolean exists() {

		return getJavaFile().exists();
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
	public void copyToFileObject(File other) {

		other.filename = this.filename;
	}

	/**
	 * Actually copy this file's contents to a new location on the disk,
	 * the location being given as a string filename
	 * Returns a File object representing the target file location
	 */
	public File copyToDisk(String destination) {

		File result = new File(destination);

		this.copyToDisk(result);

		return result;
	}

	/**
	 * Actually copy this file's contents to a new location on the disk,
	 * the location being given as a File object containing a filename
	 * Returns a File object representing the target file location
	 * (mostly for symmetry reasons with the other copyToDisk functions -
	 * in this case, the returned File object is just the File object that
	 * was given as argument... ^^)
	 */
	public File copyToDisk(File destination) {

		java.io.File destinationFile = destination.getJavaFile();

		// create parent directories
		if (destinationFile.getParentFile() != null) {
			destinationFile.getParentFile().mkdirs();
		}

		try {

			Files.copy(this.getJavaPath(), destination.getJavaPath(), StandardCopyOption.REPLACE_EXISTING);

		} catch (IOException e) {
			System.err.println("[ERROR] The file " + filename + " could not be copied to " + destination + "!");
		}

		return destination;
	}

	/**
	 * Actually copy this file's contents to a new location on the disk,
	 * the location being given as a Directory object into which the File
	 * is to be copied
	 * Returns a File object representing the target file location
	 */
	public File copyToDisk(Directory destination) {

		File destinationFile = destination.getFile(getLocalFilename());

		return copyToDisk(destinationFile);
	}

	/**
	 * Creates a java file instance and ensures its parents actually exist,
	 * such that it can immediately be used to actually save the file contents
	 * in classes that handle saving (such as SimpleFile and BinaryFile)
	 */
	protected java.io.File initSave() {

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

		return thisFile;
	}

	/**
	 * Gets the length of the file content in bytes (from the disk, so if you have written content
	 * but not saved it, this will not return updated information)
	 */
	public long getContentLength() {
		return getJavaFile().length();
	}

	/**
	 * Gets the content type - which you might want to override when extending this class ;)
	 */
	public String getContentType() {

		if (filename.endsWith(".htm") || filename.endsWith(".html")) {
			return "text/html";
		}

		if (filename.endsWith(".css")) {
			return "text/css";
		}

		if (filename.endsWith(".js")) {
			return "text/javascript";
		}

		if (filename.endsWith(".json")) {
			return "application/json";
		}

		if (filename.endsWith(".png")) {
			return "image/png";
		}

		if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
			return "image/jpeg";
		}

		return "text/plain";
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
