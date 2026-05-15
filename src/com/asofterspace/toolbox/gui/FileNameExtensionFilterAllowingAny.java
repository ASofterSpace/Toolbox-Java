/**
 * Unlicensed code created by A Softer Space, 2026
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.gui;

import java.io.File;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;


/**
 * A FileNameExtensionFilter that explicitly allows filtering „any“ file (no filter at all)
 * by specifying extension "*" or just none
 */
public class FileNameExtensionFilterAllowingAny extends FileFilter {

	private String desc;
	private String[] exts;


	public FileNameExtensionFilterAllowingAny(String description, String... extensions) {
		this.desc = description;
		this.exts = extensions;
		for (int i = 0; i < exts.length; i++) {
			exts[i] = exts[i].toLowerCase();
		}
	}

	public String getDescription() {
		return desc;
	}

	public boolean accept(File f) {
		if ((exts.length < 1) || ("*".equals(exts[0]))) {
			return true;
		}
		for (String ext : exts) {
			if (f.getName().toLowerCase().endsWith(ext)) {
				return true;
			}
		}
		return false;
	}


}
