/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.io;

import java.util.ArrayList;
import java.util.List;


/**
 * A csv file object describes a single csv file and enables simple access to
 * its contents.
 */
public class CsvFile extends SimpleFile {

	int currentLine = 1;

	private char entrySeparator = ',';


	/**
	 * You can construct a CsvFile instance by directly from a path name.
	 */
	public CsvFile(String fullyQualifiedFileName) {

		super(fullyQualifiedFileName);
	}

	/**
	 * You can construct a CsvFile instance by basing it on an existing file object.
	 */
	public CsvFile(File regularFile) {

		super();

		regularFile.copyToFileObject(this);
	}

	/**
	 * Create a new file instance based on a Directory and the name of
	 * the file inside the directory
	 * @param directory The directory in which the file is located
	 * @param filename The (local) name of the actual file
	 */
	public CsvFile(Directory directory, String filename) {

		super(directory, filename);
	}

	public int getCurrentLineNum() {
		return currentLine;
	}

	/**
	 * 0 means that getContentLine() will next return the very first line (which would normally be the headline)
	 */
	public void setCurrentLineNum(int currentLine) {
		this.currentLine = currentLine;
	}

	public String getHeadLine() {

		// if the content has not yet been fetched... fetch it!
		ensureContents(true);

		currentLine = 1;

		return filecontents.get(0);
	}

	public String getContentLine() {

		// if the content has not yet been fetched... fetch it!
		ensureContents(true);

		// if we got too far, return nothing!
		if (currentLine >= filecontents.size()) {
			return null;
		}

		String result = filecontents.get(currentLine);

		// get the next line
		currentLine++;

		return result;
	}

	public List<String> getContentLineInColumns() {

		return getLineInColumns(getContentLine());
	}

	public List<String> getHeadLineInColumns() {

		return getLineInColumns(getHeadLine());
	}

	private List<String> getLineInColumns(String line) {

		if (line == null) {
			return null;
		}

		List<String> result = new ArrayList<>();

		int len = line.length();
		String nextStr = "";
		boolean inQuotedStr = false;

		for (int i = 0; i < len; i++) {

			if (line.charAt(i) == '\"') {
				inQuotedStr = !inQuotedStr;
			} else if ((!inQuotedStr) && (line.charAt(i) == entrySeparator)) {
				result.add(nextStr);
				nextStr = "";
			} else {
				nextStr += line.charAt(i);
			}
		}

		result.add(nextStr);

		return result;
	}

	public void setHeadLine(String headline) {

		this.clearContent();

		this.appendContent(headline);
	}

	public void setHeadLine(List<String> headlineColumns) {

		this.setHeadLine(joinColumns(headlineColumns));
	}

	public void appendContent(List<String> lineColumns) {

		this.appendContent(joinColumns(lineColumns));
	}

	public void setEntrySeparator(char entrySeparator) {
		this.entrySeparator = entrySeparator;
	}

	private String joinColumns(List<String> columns) {

		if (columns == null) {
			return "";
		}

		StringBuilder result = new StringBuilder();

		String sep = "";

		for (String col : columns) {
			result.append(sep);
			if ((col != null) && col.contains(sep)) {
				result.append("\"");
				result.append(col);
				result.append("\"");
			} else {
				result.append(col);
			}
			sep = ""+entrySeparator;
		}

		return result.toString();
	}

	public static String sanitizeForCsv(Object val) {
		if (val == null) {
			return "";
		}
		if ((val instanceof Double) || (val instanceof Float)) {
			return val.toString().replace(",", ".");
		}
		String strVal = val.toString();
		strVal = strVal.trim();
		strVal = strVal.replaceAll(",", "");
		strVal = strVal.replaceAll("\n", "");
		strVal = strVal.replaceAll("\r", "");
		strVal = strVal.replaceAll("\t", " ");
		return strVal;
	}

	/**
	 * Gives back a string representation of the csv file object
	 */
	@Override
	public String toString() {
		return "com.asofterspace.toolbox.io.CsvFile: " + filename + " with head line:\n" + getHeadLine();
	}

}
