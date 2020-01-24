/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.codeeditor.utils;

public interface OpenFileCallback {

	/**
	 * Open a file with the given relative path and return true if you found such a file,
	 * or false if not
	 * (Here, the basePath is a string such as ../../ to get out of the current file's package
	 * back to the base, and the relativePath then is the path foo/bar/File.java of the file
	 * resolved from the base)
	 */
	boolean openFileRelativeToThis(String basePath, String relativePath, CodeLanguage language, String extraInfo);
}
