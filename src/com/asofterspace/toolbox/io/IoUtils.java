package com.asofterspace.toolbox.io;

import java.util.UUID;


public class IoUtils {

	public static final Directory WORKDIR = new Directory("workdir");


	/**
	 * Cleanup the local workdir (which is used e.g. for temporarily keeping unzipped files...
	 * but there is no need to keep unzipped files from the last run of this program, in case
	 * somehow the files were not properly closed and deleted back then
	 */
	public static void cleanAllWorkDirs() {

		WORKDIR.delete();
	}
	
	public static Directory createDedicatedWorkDir() {
	
		UUID dedicatedId = UUID.randomUUID();

		return IoUtils.WORKDIR.createChildDir(dedicatedId.toString());
	}

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