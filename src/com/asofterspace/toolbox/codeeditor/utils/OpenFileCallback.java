/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.codeeditor.utils;

import java.util.List;


public interface OpenFileCallback {

	/**
	 * Open a file with one of the given relative paths and return true if you found such a file,
	 * or false if not
	 * (Here, the basePath is a string such as ../../ to get out of the current file's package
	 * back to the base, and the relativePaths then are paths such as foo/bar/File.java of the
	 * file resolved from the base)
	 */
	boolean openFileRelativeToThis(String basePath, List<String> relativePaths, CodeLanguage language, String extraInfo);

	/**
	 * Get the contents of other open files whose names match one of the file endings
	 */
	List<String> getOtherFileContents(List<String> fileEndings);
}
