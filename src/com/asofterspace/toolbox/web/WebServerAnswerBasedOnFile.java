/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.web;

import com.asofterspace.toolbox.io.BinaryFile;
import com.asofterspace.toolbox.io.File;


/**
 * This represents the file data that a web server might send as answer
 * to a request
 *
 * @author Moya (a softer space, 2019)
 */
public class WebServerAnswerBasedOnFile implements WebServerAnswer {

	private String filename;

	private String contentType;

	private byte[] data;


	public WebServerAnswerBasedOnFile(File fileContainingData) {

		this.filename = fileContainingData.getFilename();

		this.contentType = fileContainingData.getContentType();

		BinaryFile binaryFile = new BinaryFile(fileContainingData);

		this.data = binaryFile.loadContent();
	}

	public long getContentLength() {

		return data.length;
	}

	public String getPreferredCacheParadigm() {

		String lowFilename = filename.toLowerCase();

		// keep these a week long
		// (if we want to invalidate, we should increase the ?v=version argument of the request)
		if (lowFilename.endsWith(".jpg") ||
			lowFilename.endsWith(".png") ||
			lowFilename.endsWith(".pdf") ||
			lowFilename.endsWith(".js")) {
			return "public, max-age=604800";
		}

		// never keep the others
		return "no-store";
	}

	public String getContentType() {

		return contentType;
	}

	public byte[] getBinaryContent() {

		return data;
	}
}
