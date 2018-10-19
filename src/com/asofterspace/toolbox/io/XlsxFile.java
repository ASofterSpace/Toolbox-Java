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
 * An xlsx file object describes a single xlsx file and enables simple access to
 * its contents.
 */
// TODO :: maybe let this extend something like ZipFile?
public class XlsxFile extends File {

	private List<XlsxSheet> xlsxSheets;


	/**
	 * You can construct a XlsxFile instance by directly from a path name.
	 */
	public XlsxFile(String fullyQualifiedFileName) {

		super(fullyQualifiedFileName);
	}

	/**
	 * You can construct an XlsxFile instance by basing it on an existing file object.
	 */
	public XlsxFile(File regularFile) {

		super(regularFile);
	}
	
	/**
	 * For an XLSX file, call getSheets() to get access to its contents,
	 * not getContents() / setContents() as for a regular File (the
	 * regular File-based stuff will work, technically, but will be
	 * much less efficient and if you use both all hell might break
	 * loose... so yeah, only use the getRoot() function as entry-
	 * point for XLSX files, kthxbye!)
	 */
	public List<XlsxSheet> getSheets() {

		if (xlsxSheets == null) {
			loadXlsxContents();
		}

		return xlsxSheets;
	}

	protected void loadXlsxContents() {

		// TODO :: load the file
		
		// TODO :: unzip it
		
		// TODO :: actually load the contained sheets etc.
	}
	
	public void save() {

		saveTo(this);
	}

	public void saveTo(File newLocation) {

		saveTo(newLocation.filename);
	}

	public void saveTo(String newLocation) {

		// TODO :: zip up the files again, be happy...
	}

	/**
	 * Gives back a string representation of the xlsx file object
	 */
	@Override
	public String toString() {
		return "com.asofterspace.toolbox.io.XlsxFile: " + filename;
	}

}
