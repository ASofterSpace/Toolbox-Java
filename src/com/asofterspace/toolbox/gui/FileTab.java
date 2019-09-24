/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.gui;


public interface FileTab {

	public String getFilePath();

	public String getDirectoryName();

	public boolean isMissing();

	public boolean hasBeenChanged();
}
