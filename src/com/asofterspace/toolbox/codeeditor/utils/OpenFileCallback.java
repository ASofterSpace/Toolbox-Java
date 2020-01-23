/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.codeeditor.utils;

public interface OpenFileCallback {

	void openFileRelativeToThis(String relativePath, CodeLanguage language, String extraInfo);
}
