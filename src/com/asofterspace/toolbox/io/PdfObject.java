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
	
	// plain-text content for whatever is not contained in the rest...
	// e.g. for objects that only contain [1 2 3] - yes, just an array, outside of any dictionary!
	// null if the object only contains dictionary and/or stream content
	private String content = null;

	// dictionary content, null if the object does not contain a dictionary
	private PdfDictionary dictContent = null;

	// plain-text stream content, null if the object does not contain a stream, NOT ending on \r\n
	// as we want to have just the stream
	private String streamContent = null;

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

		// stream reading already done? nothing more to do...
		if (contentReader == null) {
			return;
		}

		contentReader.append(contents + "\r\n");
	}

	public void readStreamContents(String streamContents) {

		this.streamContent = streamContents;

		// remove the word "stream" from the content
		contentReader.setLength(contentReader.length() - "stream\r\n".length());

		// aaand finalize everything already - nothing interesting happens after a stream anyway ;)
		doneReadingContents();
	}
	
	public void doneReadingContents() {

		// stream reading already done? nothing more to do...
		if (contentReader == null) {
			return;
		}

		String contents = contentReader.toString().trim();

		contentReader = null;

		// oho, we have a dictionary! we actually know what to do with this! :)
		if (contents.startsWith("<<")) {
			dictContent = new PdfDictionary();
			dictContent.loadFromString(contents);
		} else {
			this.content = contents;
		}
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
	
	public void setContent(String content) {
		this.content = content;
	}

	public void setDictValue(String key, String value) {

		if (this.dictContent == null) {
			this.dictContent = new PdfDictionary();
		}
		
		this.dictContent.set(key, value);
	}
	
	public void setDictValue(String key, PdfDictionary value) {

		if (this.dictContent == null) {
			this.dictContent = new PdfDictionary();
		}
		
		this.dictContent.set(key, value);
	}
	
	public void setStreamContent(String streamContent) {
		this.streamContent = streamContent;
	}
	
	/**
	 * Append the content of this PDF object to a PDF file that is in the process of being created
	 */
	public void appendToPdfFile(StringBuilder result) {

		result.append(number);
		result.append(" ");
		result.append(generation);
		result.append(" ");
		result.append("obj");
		result.append("\r\n");

		if (content != null) {
			result.append(content);
			result.append("\r\n");
		}

		if (dictContent != null) {
			dictContent.appendToPdfFile(result, "\r\n");
			result.append("\r\n");
		}

		if (streamContent != null) {
			result.append("stream\r\n");
			result.append(streamContent);
			result.append("\r\nendstream\r\n");
		}
		
		result.append("endobj\r\n");
	}
}
