/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.io;

import com.asofterspace.toolbox.coders.HexDecoder;

import java.io.ByteArrayOutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;


/**
 * This class represents a single object inside a PDF file
 */
public class PdfObject {

	private int number;

	private int generation;

	// plain-text content for whatever is not contained in the rest...
	// e.g. for objects that only contain [1 2 3] - yes, just an array, outside of any dictionary! -
	//   or for objects that only contain (1 2 3),
	//   or for objects that only contain 123 - all three of these we encountered "in the wild" :)
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

	public int getNumber() {
		return number;
	}

	public String getDictValue(String key) {
		ensureDictContent();
		return this.dictContent.getAsString(key);
	}

	/**
	 * Gets the stream exactly as it is internally
	 */
	public String getStreamContent() {

		return streamContent;
	}

	/**
	 * Gets the stream, possibly unzipping it if it is zipped
	 * in a way that we are aware of - but if not, then just
	 * getting it as is
	 */
	public String getPlainStreamContent() {

		if (streamContent == null) {
			return null;
		}

		if (this.dictContent == null) {
			this.dictContent = new PdfDictionary();
		}

		if ("/FlateDecode".equals(this.dictContent.getAsString("/Filter"))) {

			Inflater inflater = new Inflater();

			inflater.setInput(streamContent.getBytes(PdfFile.PDF_CHARSET), 0, streamContent.length());

			// we create a buffer into which we unzip the stream - however, the stream might be zipped really really really
			// well, and might exceed any specific buffer...
			int bufferSize = streamContent.length();
			if (bufferSize < 1024) {
				bufferSize = 1024;
			}
			byte[] buffer = new byte[bufferSize];

			// ... therefore we then copy the buffer into an (unbound) output stream, which we will convert back once we are
			// done (and know how big the output in the end actually is)
			ByteArrayOutputStream output = new ByteArrayOutputStream();

			while (!inflater.finished()) {
				try {
					int bufferSizeFilled = inflater.inflate(buffer);
					if (bufferSizeFilled < 1) {
						// aaand we are done! no more data incoming!
						break;
					}
					output.write(buffer, 0, bufferSizeFilled);
				} catch (DataFormatException e) {
					System.err.println("[ERROR] Unzipping a PDF object stream failed - oh well!");
					inflater.end();
					// return streamContent;
					throw new RuntimeException(e);
				}
			}

			inflater.end();
			return new String(output.toByteArray(), PdfFile.PDF_CHARSET);
		}

		if ("/ASCIIHexDecode".equals(this.dictContent.getAsString("/Filter"))) {

			byte[] decodedData = HexDecoder.decodeBytesFromHex(streamContent);

			return new String(decodedData, PdfFile.PDF_CHARSET);
		}

		return streamContent;
	}

	/**
	 * Uncompresses the contents (mostly the stream content) of this object.
	 * If the object is not compressed, this changes nothing.
	 * If the object is compressed in a way we do not understand, this also changes nothing. ^^
	 */
	public void uncompress() {
		// TODO :: if this worksed, remove /Filter entry from the dictionary?
		this.streamContent = getPlainStreamContent();
	}

	public Integer getStreamLength() {

		if (streamContent == null) {
			return null;
		}

		return streamContent.length();
	}

	/**
	 * Reads the length property of this object in case we have a stream
	 * This method is used to read the stream and therefore needs to produce
	 * results even before the object is fully read!
	 * (Which is possible, because the length is always declared before the
	 * stream contents themselves.)
	 */
	public Integer preGetStreamLength() {

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

	private void ensureDictContent() {
		if (this.dictContent == null) {
			this.dictContent = new PdfDictionary();
		}
	}

	public void setDictValue(String key, String value) {
		ensureDictContent();
		this.dictContent.set(key, value);
	}

	public void setDictValue(String key, PdfDictionary value) {
		ensureDictContent();
		this.dictContent.set(key, value);
	}

	public void removeDictValue(String key) {
		ensureDictContent();
		this.dictContent.remove(key);
	}

	public void setStreamContent(String streamContent) {
		this.streamContent = streamContent;

		setDictValue("/Length", ""+streamContent.length());
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
