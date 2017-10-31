package com.asofterspace.toolbox.io;

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
			for (int i=0; i<children.length; i++) {
				deleteDir(new java.io.File(dir, children[i]));
			}
		}
		dir.delete();
	}

}
