/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.io;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PdfDictionary {

	private List<String> ordering = new ArrayList<>();

	private Map<String, String> dictStrings = new HashMap<>();
	
	private Map<String, PdfDictionary> dictInnerDicts = new HashMap<>();


	public PdfDictionary() {

	}

	public String loadFromString(String content) {

		// we assume we get something like << /Key1 /Value1 /Key2 2 1 4 /Key3 << /Key4 /Value4 >> >>
		// we want to create a map like:
		// Key1: Value1
		// Key2: 2 1 4
		// Key3: (Key4: Value4)

		int startIndex = content.indexOf("<<");

		// no clue what this is supposed to be, but surely not a valid dictionary!
		if (startIndex < 0) {
			return "";
		}

		while (true) {

			int keyIndex = content.indexOf("/", startIndex + 1);
			int endIndex = content.indexOf(">>", startIndex + 1);

			// we get no nothing... we are confused!
			if ((keyIndex < 0) && (endIndex < 0)) {
				return "";
			}

			// at least one of them is found... if no key is found, then end is found and is closest...;
			// if key is found, but end is also found and is closest...
			if ((keyIndex < 0) || ((endIndex > -1) && (endIndex < keyIndex))) {
				// ... then the end is near(er than the next key)!
				content = content.substring(endIndex + 2).trim();
				return content;
			}

			int spaceIndex = content.indexOf(" ", keyIndex + 1);
			int slashIndex = content.indexOf("/", keyIndex + 1);
			int lAngleIndex = content.indexOf("<", keyIndex + 1);
			int rAngleIndex = content.indexOf(">", keyIndex + 1);
			int rIndex = content.indexOf("\r", keyIndex + 1);
			int nIndex = content.indexOf("\n", keyIndex + 1);
			int roundBracketIndex = content.indexOf("(", keyIndex + 1);
			int squareBracketIndex = content.indexOf("[", keyIndex + 1);

			int nextIndex = spaceIndex;
			String nextChar = " ";
			
			if (nextIndex < 0) {
				nextIndex = content.length();
			}
			
			if ((slashIndex > -1) && (slashIndex < nextIndex)) {
				nextIndex = slashIndex;
				nextChar = "/";
			}
			if ((lAngleIndex > -1) && (lAngleIndex < nextIndex)) {
				nextIndex = lAngleIndex;
				nextChar = "<";
			}
			if ((rAngleIndex > -1) && (rAngleIndex < nextIndex)) {
				nextIndex = rAngleIndex;
				nextChar = ">";
			}
			if ((rIndex > -1) && (rIndex < nextIndex)) {
				nextIndex = rIndex;
				nextChar = "\r";
			}
			if ((nIndex > -1) && (nIndex < nextIndex)) {
				nextIndex = nIndex;
				nextChar = "\n";
			}
			if ((roundBracketIndex > -1) && (roundBracketIndex < nextIndex)) {
				nextIndex = roundBracketIndex;
				nextChar = "(";
			}
			if ((squareBracketIndex > -1) && (squareBracketIndex < nextIndex)) {
				nextIndex = squareBracketIndex;
				nextChar = "[";
			}

			String key = content.substring(keyIndex, nextIndex);
			content = content.substring(nextIndex).trim();
			if (content.startsWith("<")) {
				// let's recursively create inner dictionaries! :)
				PdfDictionary innerDict = new PdfDictionary();
				content = innerDict.loadFromString(content);
				set(key, innerDict);
			} else {
				int searchAfterIndex = 1;
			
				if (content.startsWith("(")) {
					searchAfterIndex = content.indexOf(")") + 1;
				}
				
				if (content.startsWith("[")) {
					searchAfterIndex = content.indexOf("]") + 1;
				}
			
				rAngleIndex = content.indexOf(">", searchAfterIndex);
				nextIndex = content.indexOf("/", searchAfterIndex);
				nextChar = "/";
				
				if (nextIndex < 0) {
					nextIndex = content.length();
				}
				
				if ((rAngleIndex > -1) && (rAngleIndex < nextIndex)) {
					nextIndex = rAngleIndex;
					nextChar = ">";
				}

				String value = content.substring(0, nextIndex).trim();
				set(key, value);

				content = content.substring(nextIndex);
			}

			startIndex = -1;
		}
	}
	
	public void set(String key, String value) {
		dictStrings.put(key, value);
		ordering.add(key);
	}
	
	public void set(String key, PdfDictionary value) {
		dictInnerDicts.put(key, value);
		ordering.add(key);
	}
	
	/**
	 * Append the content of this PDF dictionary to a PDF file that is in the process of being created
	 */
	public void appendToPdfFile(StringBuilder result, String outputLineSeparator) {
	
		result.append("<<");
		
		for (String key : ordering) {

			for (Map.Entry<String, String> entry : dictStrings.entrySet()) {
				if (key.equals(entry.getKey())) {
					result.append(outputLineSeparator);
					result.append(entry.getKey());
					result.append(" ");
					result.append(entry.getValue());
				}
			}

			for (Map.Entry<String, PdfDictionary> entry : dictInnerDicts.entrySet()) {
				if (key.equals(entry.getKey())) {
					result.append(outputLineSeparator);
					result.append(entry.getKey());
					result.append(" ");
					entry.getValue().appendToPdfFile(result, " ");
				}
			}

		}

		result.append(outputLineSeparator);
		result.append(">>");
	}

	/**
	 * Convert this PDF dictionary to a string for diagnostic output
	 */
	public String toString() {
	
		StringBuilder result = new StringBuilder();
		
		String sep = "";
	
		for (Map.Entry<String, String> entry : dictStrings.entrySet()) {
			result.append(sep);
			sep = "\n";
			result.append(entry.getKey() + ": " + entry.getValue());
		}
		
		for (Map.Entry<String, PdfDictionary> entry : dictInnerDicts.entrySet()) {
			result.append(sep);
			sep = "\n";
			result.append(entry.getKey() + ": " + entry.getValue().toString().replace("\n", "\n    "));
		}
		
		return result.toString();
	}
	
}
