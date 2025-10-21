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
		initFromJavaDir(javaDirectory);
	}

	/**
	 * Create a new directory instance based on a parent Directory and the name of
	 * the directory inside the parent directory
	 * @param parentDirectory The directory in which the directory is located
	 * @param dirname The (local) name of the actual directory
	 */
	public Directory(Directory parentDirectory, String dirname) {

		if ((dirname == null) || "".equals(dirname)) {
			this.dirname = parentDirectory.dirname;
			return;
		}

		this.dirname = parentDirectory.getJavaPath().resolve(dirname).toAbsolutePath().toString();
	}

	private void initFromJavaDir(java.io.File javaDirectory) {
		this.dirname = javaDirectory.getAbsolutePath();
	}

	/**
	 * Actually copy this directory and all its contents to a new location on the disk,
	 * the location being given as a Directory object containing the destination
	 * Returns a Directory object representing the target directory location
	 */
	public Directory copyToDisk(Directory destination) {

		java.io.File entryPoint = getJavaFile();

		if (entryPoint.isDirectory()) {
			copyToDiskInternally(entryPoint, destination.getJavaFile());
		}

		return destination;
	}

	private void copyToDiskInternally(java.io.File entryPoint, java.io.File targetPoint) {

		targetPoint.mkdir();

		Path targetPointPath = targetPoint.toPath();

		java.io.File[] children = entryPoint.listFiles();
		if (children != null) {
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
	 * Gets the parent directory without ensuring that it exists (which it might not, in case
	 * that we are already at the top-level)
	 */
	public Directory getParentDirectory() {

		return new Directory(dirname + "/..");
	}

	/**
	 * Get the path of the directory
	 */
	public String getDirname() {
		return dirname;
	}

	/**
	 * Get only the local part of the dirname associated with this directory,
	 * so just the name itself instead of the full path
	 */
	public String getLocalDirname() {

		return File.toLocalName(dirname);
	}

	/**
	 * Get the absolute dirname associated with this directory object
	 */
	public String getAbsoluteDirname() {

		Path basePath = getJavaPath();

		// there is a weird bug where absolute paths of "C:" do not work
		// and anyway, cannot do shorter / absolute-er than just "C:" ^^
		String directString = basePath.toString();
		if (directString.length() == 2) {
			return directString;
		}

		try {
			basePath = basePath.toAbsolutePath();
		} catch (Throwable e) {
			System.err.println("Cannot convert " + basePath + " to absolute path: " + e);
		}

		return basePath.toString();
	}

	/**
	 * Get the canonical filename associated with this file object,
	 * or if it is unavailable, at lest the absolute filename
	 */
	public String getCanonicalDirname() {

		Path basePath = getJavaPath();

		// there is a weird bug where canonical paths of "C:" do not work
		// and anyway, cannot do shorter / canonical-er than just "C:" ^^
		String directString = basePath.toString();
		if (directString.length() == 2) {
			return directString;
		}

		try {
			return basePath.toRealPath().toString();

		} catch (IOException | SecurityException ei) {

			try {
				return basePath.toAbsolutePath().toString();
			} catch (Throwable e) {
				System.err.println("Cannot convert " + basePath + " to absolute path: " + e);
			}

			return basePath.toString();
		}
	}

	/**
	 * Get a Java File object representing this directory
	 */
	public java.io.File getJavaFile() {

		java.io.File result = new java.io.File(dirname);
		if (dirname.length() == 2) {
			result = new java.io.File(dirname + "/");
		}
		return result;
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

		java.io.File dir = getJavaFile();

		dir.mkdirs();
	}

	/**
	 * Clears this directory - that is, ensures that it exists and is empty
	 * (this is the same as first deleting it and then recreating it)
	 */
	public void clear() {

		create();

		boolean recursively = false;
		List<File> delFiles = getAllFiles(recursively);
		List<Directory> delDirs = getAllDirectories(recursively);

		for (File delFile : delFiles) {
			delFile.delete();
		}
		for (Directory delDir : delDirs) {
			delDir.delete();
		}
	}

	public Boolean isEmpty() {

		java.io.File entryPoint = getJavaFile();

		// a directory is empty if it has no children
		if (entryPoint.isDirectory()) {
			java.io.File[] children = entryPoint.listFiles();

			if (children == null) {
				return true;
			}

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

		java.io.File entryPoint = getJavaFile();

		return entryPoint.exists() && entryPoint.isDirectory();
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

		return getAllFilesInternally(getJavaFile(), null, null, recursively);
	}

	/**
	 * Get all the files contained in the directory (and, if recursively
	 * is set to true, in its sub-directories) whose names end with the
	 * given string
	 */
	public List<File> getAllFilesEndingWith(String endStr, boolean recursively) {

		return getAllFilesInternally(getJavaFile(), null, endStr, recursively);
	}

	/**
	 * Get all the files contained in the directory (and, if recursively
	 * is set to true, in its sub-directories) whose names start with the
	 * given string
	 */
	public List<File> getAllFilesStartingWith(String startStr, boolean recursively) {

		return getAllFilesInternally(getJavaFile(), startStr, null, recursively);
	}

	/**
	 * Get all the files contained in the directory (and, if recursively
	 * is set to true, in its sub-directories) whose names start and end
	 * with the given strings
	 */
	public List<File> getAllFilesStartingAndEndingWith(String startStr, String endStr, boolean recursively) {

		return getAllFilesInternally(getJavaFile(), startStr, endStr, recursively);
	}

	private List<File> getAllFilesInternally(java.io.File entryPoint, String startStr, String endStr, boolean recursively) {

		List<File> result = new ArrayList<>();

		if (entryPoint.isDirectory()) {
			java.io.File[] children = entryPoint.listFiles();
			if (children != null) {
				for (java.io.File curChild : children) {
					if (curChild.isDirectory()) {
						if (recursively) {
							result.addAll(getAllFilesInternally(curChild, startStr, endStr, true));
						}
					} else {
						if ((endStr == null) || curChild.getAbsolutePath().endsWith(endStr)) {
							if ((startStr == null) || curChild.getName().startsWith(startStr)) {
								result.add(new File(curChild));
							}
						}
					}
				}
			}
		}

		return result;
	}

	/**
	 * Get all the directories contained in the directory (and, if recursively
	 * is set to true, in its sub-directories)
	 */
	public List<Directory> getAllDirectories(boolean recursively) {
		return getAllDirectoriesInternally(getJavaFile(), recursively);
	}

	private List<Directory> getAllDirectoriesInternally(java.io.File entryPoint, boolean recursively) {

		List<Directory> result = new ArrayList<>();

		if (entryPoint.isDirectory()) {
			java.io.File[] children = entryPoint.listFiles();
			if (children != null) {
				for (java.io.File curChild : children) {
					if (curChild.isDirectory()) {
						result.add(new Directory(curChild));
						if (recursively) {
							result.addAll(getAllDirectoriesInternally(curChild, true));
						}
					}
				}
			}
		}

		return result;
	}

	/**
	 * Finds a file based on its local name
	 */
	public File findFile(String localFilename) {

		return findFileInternally(getJavaFile(), localFilename);
	}

	/**
	 * Finds a file based on a possible list of local names
	 */
	public File findFileFromList(List<String> localFilenames) {

		for (String localFilename : localFilenames) {
			File result = findFileInternally(getJavaFile(), localFilename);
			if (result != null) {
				return result;
			}
		}

		return null;
	}

	private File findFileInternally(java.io.File entryPoint, String localFilename) {

		if (entryPoint.isDirectory()) {
			java.io.File[] children = entryPoint.listFiles();
			if (children != null) {
				for (java.io.File curChild : children) {
					if (curChild.isDirectory()) {
						File result = findFileInternally(curChild, localFilename);
						if (result != null) {
							return result;
						}
					} else {
						if (localFilename.equals(curChild.getName())) {
							return new File(curChild);
						}
					}
				}
			}
		}

		return null;
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

		Path filePath = file.getJavaPath().toAbsolutePath();
		Path dirPath = this.getJavaPath().toAbsolutePath();
		return dirPath.relativize(filePath).toString();
	}

	/**
	 * Renames this directory by giving it a new name (but keeping it in the same path)
	 */
	public void rename(String newName) {
		try {
			if (newName.equals(getLocalDirname())) {
				// nothing to do here
				return;
			}
			if (newName.toLowerCase().equals(getLocalDirname().toLowerCase())) {
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
				initFromJavaDir(newPath.toFile());
			} else {
				Path newPath = getJavaPath().resolveSibling(newName);
				Files.move(getJavaPath(), newPath, StandardCopyOption.REPLACE_EXISTING);
				initFromJavaDir(newPath.toFile());
			}
		} catch (IOException e) {
			System.err.println("[ERROR] An IOException occurred when trying to rename the directory " + dirname + " to " + newName + " - inconceivable!");
		}
	}

	/**
	 * Deletes this directory and all files and folders inside,
	 * recursively
	 */
	public void delete() {
		deleteDir(getJavaFile());
	}

	/**
	 * Deletes the indicated file or directory recursively,
	 * if it exists (does not throw an exception otherwise)
	 */
	private static void deleteDir(java.io.File dir) {

		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				deleteDir(new java.io.File(dir, children[i]));
			}
		}
		dir.delete();
	}

	@Override
	public String toString() {
		return "Directory: " + dirname;
	}

	// Implements equals based on the exact dirname only; there may be several dirnames corresponding to the same directory on the disk though
	// (e.g. absolute vs. relative paths)
	@Override
	public boolean equals(Object other) {

		// If the other one does not even exist, we are not the same - because we exist!
		if (other == null) {
			return false;
		}

		if (other instanceof Directory) {
			Directory otherDirectory = (Directory) other;

			// If our values for dirname are different...
			if (this.dirname == null) {
				if (otherDirectory.dirname != null) {
					// ... then we are not the same!
					return false;
				}
			} else if (!this.dirname.equals(otherDirectory.dirname)) {
				// ... then we are not the same!
				return false;
			}

			// We have no reason to assume that we are not the same
			return true;
		}

		// If the other one cannot even be cast to us, then we are not the same!
		return false;
	}

	@Override
	public int hashCode() {
		int result = 0;
		if (this.dirname != null) {
			result += this.dirname.hashCode();
		}
		return result;
	}

}
