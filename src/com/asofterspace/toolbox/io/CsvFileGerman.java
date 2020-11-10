/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.io;

import com.asofterspace.toolbox.utils.TextEncoding;


/**
 * A csv file object describes a single csv file and enables simple access to
 * its contents.
 */
public class CsvFileGerman extends CsvFile {

	/**
	 * You can construct a CsvFile instance by directly from a path name.
	 */
	public CsvFileGerman(String fullyQualifiedFileName) {

		super(fullyQualifiedFileName);

		setEntrySeparator(';');
		setEncoding(TextEncoding.UTF8_WITH_BOM);
	}

	/**
	 * You can construct a CsvFile instance by basing it on an existing file object.
	 */
	public CsvFileGerman(File regularFile) {

		super(regularFile);

		setEntrySeparator(';');
		setEncoding(TextEncoding.UTF8_WITH_BOM);
	}

	/**
	 * Create a new file instance based on a Directory and the name of
	 * the file inside the directory
	 * @param directory The directory in which the file is located
	 * @param filename The (local) name of the actual file
	 */
	public CsvFileGerman(Directory directory, String filename) {

		super(directory, filename);

		setEntrySeparator(';');
		setEncoding(TextEncoding.UTF8_WITH_BOM);
	}

	public static String sanitizeForCsv(Object val) {
		if (val == null) {
			return "";
		}
		if ((val instanceof Double) || (val instanceof Float)) {
			return val.toString().replace(".", ",");
		}
		String strVal = val.toString();
		strVal = strVal.trim();
		strVal = strVal.replaceAll(";", "");
		strVal = strVal.replaceAll("\n", "");
		strVal = strVal.replaceAll("\r", "");
		strVal = strVal.replaceAll("\t", " ");
		return strVal;
	}

}
