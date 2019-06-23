/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;


public class Directory {

	private String dirname;


	/**
	 * Please do not construct a directory without a name ;)
	 */
	@SuppressWarnings("unused")
	private Directory() {
	}

	/**
	 * Create a new directory instance based on the fully qualified dir name,
	 * using a slash as separator independent of the operating system
	 * @param fullyQualifiedDirName
	 */
	public Directory(String fullyQualifiedDirName) {

		dirname = fullyQualifiedDirName;
	}

	/**
	 * Create a new directory instance based on a Java File
	 * @param javaDirectory
	 */
	public Directory(java.io.File javaDirectory) {

		dirname = javaDirectory.getAbsolutePath();
	}

	/**
	 * Create a new directory instance based on a parent Directory and the name of
	 * the directory inside the parent directory
	 * @param parentDirectory The directory in which the directory is located
	 * @param dirname The (local) name of the actual directory
	 */
	public Directory(Directory parentDirectory, String dirname) {

		this.dirname = parentDirectory.getJavaPath().resolve(dirname).toAbsolutePath().toString();
	}

	/**
	 * Actually copy this directory and all its contents to a new location on the disk,
	 * the location being given as a Directory object containing the destination
	 * Returns a Directory object representing the target directory location
	 */
	public Directory copyToDisk(Directory destination) {

		java.io.File entryPoint = new java.io.File(dirname);

		if (entryPoint.isDirectory()) {
			copyToDiskInternally(entryPoint, destination.getJavaFile());
		}

		return destination;
	}

	private void copyToDiskInternally(java.io.File entryPoint, java.io.File targetPoint) {

		targetPoint.mkdir();

		Path targetPointPath = targetPoint.toPath();

		java.io.File[] children = entryPoint.listFiles();
		for (java.io.File curChild : children) {
			java.io.File targetFile = targetPointPath.resolve(curChild.getName()).toFile();
			if (curChild.isDirectory()) {
				copyToDiskInternally(curChild, targetFile);
			} else {
				try {
					Files.copy(curChild.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					System.err.println("[ERROR] The file " + curChild.toPath() + " could not be copied to " + targetFile.toPath() + "!");
				}
			}
		}
	}

	/**
	 * Gets a child directory inside this directory without ensuring that it exists on disk,
	 * and returns that instance
	 */
	public Directory getChildDir(String name) {

		return new Directory(dirname + "/" + name);
	}

	/**
	 * Creates a child directory inside this directory, ensures it exists on disk,
	 * and returns that instance
	 */
	public Directory createChildDir(String name) {

		Directory child = new Directory(dirname + "/" + name);

		child.create();

		return child;
	}

	/**
	 * Get the path of the directory
	 */
	public String getDirname() {
		return dirname;
	}

	/**
	 * Get the absolute dirname associated with this directory object
	 */
	public String getAbsoluteDirname() {

		Path basePath = getJavaPath();

		return basePath.toAbsolutePath().toString();
	}

	/**
	 * Get a Java File object representing this directory
	 */
	public java.io.File getJavaFile() {

		return new java.io.File(dirname);
	}

	/**
	 * Get a Java Path object representing this directory
	 */
	public Path getJavaPath() {

		return Paths.get(dirname);
	}

	/**
	 * Creates this directory on the underlying file system
	 */
	public void create() {

		java.io.File dir = new java.io.File(dirname);

		dir.mkdirs();
	}

	/**
	 * Clears this directory - that is, ensures that it exists and is empty
	 * (this is the same as first deleting it and then recreating it)
	 */
	public void clear() {

		delete();

		create();
	}

	public Boolean isEmpty() {

		java.io.File entryPoint = new java.io.File(dirname);

		// a directory is empty if it has no children
		if (entryPoint.isDirectory()) {
			java.io.File[] children = entryPoint.listFiles();

			return children.length <= 0;
		}

		// a file is never empty - it is always something blocking us from writing a subfile into this path
		if (entryPoint.isFile()) {
			return false;
		}

		// neither a file nor a directory? this is a directory not yet existing - and by definition empty! ;)
		return true;
	}

	/**
	 * Returns true if something exists under this name (which does NOT need to be a directory, btw.!)
	 */
	public boolean exists() {

		java.io.File entryPoint = new java.io.File(dirname);

		return entryPoint.exists();
	}

	/**
	 * Get a file inside this directory
	 */
	public File getFile(String filename) {

		return new File(this, filename);
	}

	/**
	 * Get all the files contained in the directory (and, if recursively
	 * is set to true, in its sub-directories)
	 */
	public List<File> getAllFiles(boolean recursively) {

		return getAllFilesInternally(new java.io.File(dirname), recursively);
	}

	private List<File> getAllFilesInternally(java.io.File entryPoint, boolean recursively) {

		List<File> result = new ArrayList<>();

		if (entryPoint.isDirectory()) {
			java.io.File[] children = entryPoint.listFiles();
			for (java.io.File curChild : children) {
				if (curChild.isDirectory()) {
					if (recursively) {
						result.addAll(getAllFilesInternally(curChild, true));
					}
				} else {
					result.add(new File(curChild));
				}
			}
		}

		return result;
	}

	/**
	 * Take an old file that is in this directory or a subdirectory, and return a new file pointing to the same
	 * relative path underneath the new directory
	 * E.g. if this is /usr/bin and oldFile is /usr/bar/foo/bar.txt, with newDir being /var, then this function
	 * returns a file pointing towards /var/foo/bar.txt (but no actual moving or copying is being done on disk)
	 */
	public File traverseFileTo(File oldFile, Directory newDir) {

		Directory oldDir = this;

		// express new file relative to old dir
		Path oldFilePath = oldFile.getJavaPath().toAbsolutePath();
		Path oldDirPath = oldDir.getJavaPath().toAbsolutePath();
		Path filePathRelative = oldDirPath.relativize(oldFilePath);

		// append relative file path to new dir
		Path newDirPath = newDir.getJavaPath();

		java.io.File newJavaFile = newDirPath.resolve(filePathRelative).toFile();
		return new File(newJavaFile);
	}

	/**
	 * Take a file that is in this directory or a subdirectory, and return a path as string pointing to the same
	 * file as relative path seen from this directory
	 * E.g. if this is /usr/bin and file is /usr/bar/foo/bar.txt, then this function returns a string containing
	 * foo/bar.txt (but no actual moving or copying is being done on disk)
	 */
	public String getRelativePath(File file) {

		Path filePath = file.getJavaPath();
		Path dirPath = this.getJavaPath();
		return dirPath.relativize(filePath).toString();
	}

	/**
	 * Deletes this directory and all files and folders inside,
	 * recursively
	 */
	public void delete() {

		deleteDir(new java.io.File(dirname));
	}

	private static void deleteDir(java.io.File dir)
	{
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				deleteDir(new java.io.File(dir, children[i]));
			}
		}
		dir.delete();
	}

}
