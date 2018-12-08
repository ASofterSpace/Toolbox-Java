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
	
		contents = contents.trim();
		
		if (contents.startsWith("<<")) {
			contents = contents.substring(3).trim();
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
		
		if (type.equals("Catalog")) {
			if (contents.contains("/Pages ")) {
				// pages = contents.substring
			}
		}
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String toString() {

		StringBuilder result = new StringBuilder();

		result.append(number);
		result.append(" ");
		result.append(generation);
		result.append(" ");
		result.append("obj");
		result.append("<<\r\n");
		
		if (type != null) {
			result.append("/Type /");
			result.append(type);
		}
		
		result.append(">>\r\n");
		result.append("endobj\r\n");
		
		return result.toString();
	}
}
