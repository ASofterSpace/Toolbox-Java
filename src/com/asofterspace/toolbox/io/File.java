/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.io;

import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
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
		initFromJavaFile(javaFile);
	}

	private void initFromJavaFile(java.io.File javaFile) {
		// we try to actually use the canoncial path, if possible - as this prevents
		// weirdness when getting very long paths in Windows (in which case absolute
		// paths might still contain SIXLET~1 directory names in between etc.)
		try {
			this.filename = javaFile.getCanonicalPath();
		} catch (IOException e) {
			this.filename = javaFile.getAbsolutePath();
		}
	}

	/**
	 * Create a new file instance based on a parent Directory and the name of
	 * the file inside the parent directory
	 * @param parentDirectory The directory in which the file is located
	 * @param filename The (local) name of the actual file
	 */
	public File(Directory parentDirectory, String filename) {

		// complicated, and should work fine?
		// this.filename = parentDirectory.getJavaPath().resolve(filename).toAbsolutePath().toString();

		// much less complicated... and actually works better (in case of filename containing slashes)
		this.filename = parentDirectory.getAbsoluteDirname() + "/" + filename;
	}

	/**
	 * Create a list of file instances based on several Java Files
	 * @param javaFiles
	 */
	public static List<File> fromJavaFiles(java.io.File[] javaFiles) {
		List<File> result = new ArrayList<>();
		for (java.io.File javaFile : javaFiles) {
			result.add(new File(javaFile));
		}
		return result;
	}

	/**
	 * Get the filename associated with this file object
	 */
	public String getFilename() {

		return filename;
	}

	/**
	 * Gets the type of this file, purely based on the filename (so basically just
	 * whatever happens to follow the last dot in the filename)
	 */
	public String getFiletype() {
		int lastDotIndex = filename.lastIndexOf(".");
		if (lastDotIndex >= 0) {
			return filename.substring(lastDotIndex + 1);
		}
		return filename;
	}

	/**
	 * Get only the local part of the filename associated with this file object,
	 * so just the name itself instead of the full path
	 */
	public String getLocalFilename() {

		return toLocalName(filename);
	}

	/**
	 * Get only the local part of the filename, without the type ending, so instead
	 * of foobar.txt, get just foobar
	 */
	public String getLocalFilenameWithoutType() {

		String result = getLocalFilename();

		if (result.contains(".")) {
			result = result.substring(0, result.lastIndexOf("."));
		}

		return result;
	}

	static String toLocalName(String path) {

		// TODO :: this here might have problems if the filename legitimately contains
		// slashes or backslashes! if that ever happens, be more clever! :)

		if (path == null) {
			return null;
		}

		String[] firstFilenameParts = path.split("/");
		String firstResult = firstFilenameParts[firstFilenameParts.length - 1];

		String[] secondFilenameParts = path.split("\\\\");
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

		return new java.io.File(Paths.get(filename).toAbsolutePath().toString());
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

		java.io.File javaFile = getJavaFile();

		return javaFile.exists() && !javaFile.isDirectory();
	}

	public Date getCreationDate() {
		try {
			BasicFileAttributes fileAttributes = Files.readAttributes(getJavaPath(), BasicFileAttributes.class);
			FileTime fileTime = fileAttributes.creationTime();
			return new Date(fileTime.toMillis());
		} catch (IOException e) {
			return null;
		}
	}

	public Date getChangeDate() {
		try {
			BasicFileAttributes fileAttributes = Files.readAttributes(getJavaPath(), BasicFileAttributes.class);
			FileTime fileTime = fileAttributes.lastModifiedTime();
			return new Date(fileTime.toMillis());
		} catch (IOException e) {
			return null;
		}
	}

	public Long getSize() {
		return getJavaFile().length();
	}

	/**
	 * Re-set the location of this particular file (such that when we call save() later on, the new location is used,
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

		return this.copyToDisk(result);
	}

	/**
	 * Actually copy this file's contents to a new location on the disk,
	 * the location being given as a File object containing a filename
	 * Returns a File object representing the target file location
	 * (mostly for symmetry reasons with the other copyToDisk functions -
	 * in this case, the returned File object is just the File object that
	 * was given as argument... or null, if the operation failed! ^^)
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
			System.err.println("[ERROR] The file " + filename + " could not be copied to " + destination.getFilename() + "!");
			return null;
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

		switch (getFiletype().toLowerCase()) {
			case "htm":
			case "html":
				return "text/html";
			case "md":
				return "text/markdown";
			case "csv":
				return "text/csv";
			case "xml":
				return "application/xml";
			case "css":
				return "text/css";
			case "js":
				return "text/javascript";
			case "json":
				return "application/json";
			case "pdf":
				return "application/pdf";
			case "png":
				return "image/png";
			case "jpg":
			case "jpeg":
				return "image/jpeg";
			case "bmp":
				return "image/bmp";
			case "gif":
				return "image/gif";
			case "webp":
				return "image/webp";
			case "avif":
				return "image/avif";
			case "svg":
				return "image/svg+xml";
			case "mp4":
				return "video/mp4";
			case "webm":
				return "video/webm";
			case "mpg":
			case "mpeg":
				return "video/mpeg";
			case "wmv":
				return "video/x-ms-wmv";
			case "avi":
				return "video/x-msvideo";
			case "ts":
				return "video/mp2t";
			case "mp3":
				return "audio/mpeg";
			case "wav":
				return "audio/wav";
			case "mid":
			case "midi":
				return "audio/midi";
			case "weba":
				return "audio/webm";
			case "ogg":
				return "audio/ogg";
			case "aac":
				return "audio/aac";
			case "odt":
				return "application/vnd.oasis.opendocument.text";
			case "ods":
				return "application/vnd.oasis.opendocument.spreadsheet";
			case "odp":
				return "application/vnd.oasis.opendocument.presentation";
			case "doc":
				return "application/msword";
			case "xls":
				return "application/vnd.ms-excel";
			case "docx":
				return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
			case "xlsx":
				return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
			case "zip":
				return "application/zip";
			case "bin":
			case "qzp":
				return "application/octet-stream";
		}

		return "text/plain";
	}

	/**
	 * Renames this file by giving it a new name (but keeping it in the same path)
	 */
	public void rename(String newName) {
		try {
			if (newName.equals(getLocalFilename())) {
				// nothing to do here
				return;
			}
			if (newName.toLowerCase().equals(getLocalFilename().toLowerCase())) {
				// if the new and old name are basically the same, and we are under
				// Windows, then a straight renaming might be problematic... so just
				// to be completely sure, we rename to something else, and then
				// rename back!
				String tempNewName = newName + ".tmp";
				Directory dir = getParentDirectory();
				while (true) {
					Directory curTmpDir = dir.getChildDir(tempNewName);
					File curTmpFile = dir.getFile(tempNewName);
					if (!curTmpFile.exists() && !curTmpDir.exists()) {
						break;
					}
					tempNewName += ".tmp";
				}
				Path tempNewPath = getJavaPath().resolveSibling(tempNewName);
				Files.move(getJavaPath(), tempNewPath, StandardCopyOption.REPLACE_EXISTING);
				Path newPath = tempNewPath.resolveSibling(newName);
				Files.move(tempNewPath, newPath, StandardCopyOption.REPLACE_EXISTING);
				initFromJavaFile(newPath.toFile());
			} else {
				Path newPath = getJavaPath().resolveSibling(newName);
				Files.move(getJavaPath(), newPath, StandardCopyOption.REPLACE_EXISTING);
				initFromJavaFile(newPath.toFile());
			}
		} catch (IOException e) {
			System.err.println("[ERROR] An IOException occurred when trying to rename the file " + filename + " to " + newName + " - inconceivable!");
		}
	}

	/**
	 * Move this file to a new directory, returns true if it worked
	 */
	public boolean moveTo(Directory newParentDir) {
		try {
			Path newPath = newParentDir.getFile(getLocalFilename()).getJavaPath();
			Files.move(getJavaPath(), newPath, StandardCopyOption.REPLACE_EXISTING);
			initFromJavaFile(newPath.toFile());
			return true;
		} catch (IOException e) {
			System.err.println("[ERROR] An IOException occurred when trying to move the file " + filename + " to " + newParentDir.getAbsoluteDirname() + " - inconceivable!");
		}
		return false;
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
		return "File: " + filename;
	}

	// Implements equals based on the exact filename only; there may be several filenames corresponding to the same file on the disk though
	// (e.g. absolute vs. relative paths)
	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (other instanceof File) {
			File otherFile = (File) other;
			if (otherFile.filename == null) {
				return filename == null;
			}
			return otherFile.filename.equals(filename);
		}
		return false;
	}

	@Override
	public int hashCode() {
		if (filename == null) {
			return 0;
		}
		return filename.hashCode();
	}

}
