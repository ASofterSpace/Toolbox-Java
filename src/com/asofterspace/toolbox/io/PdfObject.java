/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.io;


/**
 * This class represents a single object inside a PDF file
 */
public class PdfObject {

	private int number;
	
	private int generation;
	
	private String type;
	
	// plain-text content for whatever is not contained in the rest... Übergangslösung or proper solution forever? hmmm...
	private String content = "";
	
	private StringBuilder contentReader = new StringBuilder();
	

	/**
	 * Create a pdf object based on the line defining it in a pdf file
	 * (usually there is one line defining the "metadata" of an object,
	 * followed by a block containing its contents... this constructor
	 * just uses the line with the metadata and expects the contents to
	 * be set later or kept empty)
	 */
	public PdfObject(String pdfLine) throws NumberFormatException, NullPointerException, ArrayIndexOutOfBoundsException {
		
		String[] lineArr = pdfLine.split(" ");
		
		number = Integer.valueOf(lineArr[0]);
		
		generation = Integer.valueOf(lineArr[1]);
	}
	
	public PdfObject(int number, int generation) {
		this.number = number;
		this.generation = generation;
	}
	
	public void readContents(String contents) {

		contentReader.append(contents + "\r\n");
	}
	
	public void doneReadingContents() {
	
		String contents = contentReader.toString();
	
	/*
		contents = contents.trim();
		
		if (contents.startsWith("<<")) {
			contents = contents.substring(3).trim();
		}
		if (contents.endsWith(">>")) {
			contents = contents.substring(0, contents.length()-2).trim();
		}
	
		// we could now have something like /Type /Catalog, or /Type/Catalog/Pages
		if (contents.startsWith("/Type")) {
			contents = contents.substring(5).trim();
			if (contents.startsWith("/Catalog")) {
				type = "Catalog";
				contents = contents.substring(8).trim();
			} else if (contents.startsWith("/Pages")) {
				type = "Pages";
				contents = contents.substring(6).trim();
			} else if (contents.startsWith("/Page")) {
				type = "Page";
				contents = contents.substring(5).trim();
			} else if (contents.startsWith("/Range")) {
				type = "Range";
				contents = contents.substring(6).trim();
			} else if (contents.startsWith("/Font")) {
				type = "Font";
				contents = contents.substring(5).trim();
			}
		}
		
		if (type != null) {
			if (type.equals("Catalog")) {
				if (contents.contains("/Pages ")) {
					// pages = contents.substring
				}
			}
		}
		*/
		this.content = contents;
	}

	/**
	 * Reads the length property of this object in case we have a stream
	 * This method is used to read the stream and therefore needs to produce
	 * results even before the object is fully read!
	 * (Which is possible, because the length is always declared before the
	 * stream contents themselves.)
	 */
	public Integer getStreamLength() {
		
		// do NOT rely on contentReader already being done with its thing!
		String contents = contentReader.toString();

		int index = contents.indexOf("/Length ");

		if (index < 0) {
			return null;
		}

		String lenStr = contents.substring(index + 8);

		StringBuilder result = new StringBuilder();

		for (int i = 0; i < lenStr.length(); i++) {
			char curChar = lenStr.charAt(i);
			if (Character.isDigit(curChar)) {
				result.append(curChar);
			} else {
				break;
			}
		}

		try {
			return Integer.valueOf(result.toString());
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	public String toString() {

		StringBuilder result = new StringBuilder();

		result.append(number);
		result.append(" ");
		result.append(generation);
		result.append(" ");
		result.append("obj");
		result.append("\r\n");
		
		if (type == null) {
			result.append(content);
		} else {
			result.append("<<\r\n");
			result.append("/Type /");
			result.append(type);
			result.append("\r\n");
			result.append(content);
			result.append("\r\n");
			result.append(">>\r\n");
		}
		
		result.append("endobj\r\n");
		
		return result.toString();
	}
}
