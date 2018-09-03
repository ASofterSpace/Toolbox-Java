package com.asofterspace.toolbox.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


/**
 * A csv file object describes a single csv file and enables simple access to
 * its contents.
 */
public class CsvFile extends File {

	int currentLine = 0;
	

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
		
		regularFile.copyTo(this);
	}
	
	public String getHeadLine() {
		
		// if the content has not yet been fetched... fetch it!
		ensureContents();

		return filecontents.get(0);
	}
	
	public String getContentLine() {
	
		// if the content has not yet been fetched... fetch it!
		ensureContents();

		// get the next line...
		currentLine++;
		
		// ... and if we got too far, return nothing!
		if (currentLine >= filecontents.size()) {
			return null;
		}
		
		return filecontents.get(currentLine);
	}
	
	public List<String> getContentLineInColumns() {
		
		String line = getContentLine();
		
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
			} else if ((!inQuotedStr) && (line.charAt(i) == ',')) {
				result.add(nextStr);
				nextStr = "";
			} else {
				nextStr += line.charAt(i);
			}
		}
		
		result.add(nextStr);
		
		return result;
	}
	
	/**
	 * Gives back a string representation of the csv file object
	 */
	@Override
	public String toString() {
		return "com.asofterspace.toolbox.io.CsvFile: " + filename + " with head line:\n" + getHeadLine();
	}
	
}
