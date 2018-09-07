package com.asofterspace.toolbox.io;

import java.nio.file.Path;
import java.nio.file.Paths;
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
	 * Get the path of the directory
	 */
	public String getDirname() {
		return dirname;
	}
	
	/**
	 * Get a Java File object representing this directory
	 */
	public java.io.File getJavaFile() {

		return new java.io.File(dirname);
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
		
		if (entryPoint.isDirectory()) {
			java.io.File[] children = entryPoint.listFiles();
			
			return children.length <= 0;
		}
		
		return null;
	}
	
	/**
	 * Returns true if something exists under this name (which does NOT need to be a directory, btw.!)
	 */
	public boolean exists() {
	
		java.io.File entryPoint = new java.io.File(dirname);
		
		return entryPoint.exists();
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
        Path oldFilePath = Paths.get(oldFile.getFilename());
        Path oldDirPath = Paths.get(oldDir.dirname);
        Path filePathRelative = oldDirPath.relativize(oldFilePath);

		// append relative file path to new dir
        Path newDirPath = Paths.get(newDir.dirname);
		java.io.File newJavaFile = newDirPath.resolve(filePathRelative).toFile();
		
		return new File(newJavaFile);
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
