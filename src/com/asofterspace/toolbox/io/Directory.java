package com.asofterspace.toolbox.io;

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
