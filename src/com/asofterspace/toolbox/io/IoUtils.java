package com.asofterspace.toolbox.io;


public class IoUtils {

	/**
	 * Takes a path like C:\a/b\c and transforms it into C:/a/b/c if we are under Windows,
	 * or keeps it as C:\a/b\c if we are under Linux (with the assumption that backslashes
	 * under Windows are separators which we want to convert to Linux separators)
	 */
	public static String osPathStrToLinuxPathStr(String path) {
	
		String sep = System.getProperty("file.separator");
		
		return path.replace(sep, "/");
	}

}