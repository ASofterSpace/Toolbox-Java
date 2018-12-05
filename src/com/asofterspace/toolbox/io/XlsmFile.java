package com.asofterspace.toolbox.io;

import java.io.IOException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


/**
 * An xlsm file object describes a single xlsm file (macro-enabled xlsx file)
 * and enables simple access to its contents.
 */
public class XlsmFile extends XlsxFile {

	/**
	 * You can construct a XlsmFile instance by directly from a path name.
	 */
	public XlsmFile(String fullyQualifiedFileName) {

		super(fullyQualifiedFileName);
	}

	/**
	 * You can construct an XlsmFile instance by basing it on an existing file object.
	 */
	public XlsmFile(File regularFile) {

		super(regularFile);
	}
	
	/**
	 * This is a macro-enabled XLSX file, so let's actually add some macros. ;)
	 */
	public void addMacro() {
	
		// TODO
	}
	
	/**
	 * Gives back a string representation of the xlsm file object
	 */
	@Override
	public String toString() {
		return "com.asofterspace.toolbox.io.XlsmFile: " + filename;
	}

}
