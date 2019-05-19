/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.web;

import java.util.HashMap;
import java.util.Map;


/**
 * This represents the content of a single block of a request to our web server
 * in the case of form data being contained in the request
 *
 * @author Moya (a softer space, 2019)
 */
public class WebRequestFormDataBlock {

	private String content;

	private String contentType;

	private String contentDisposition;


	/**
	 * A block looks like
	 *
	 * Content-Disposition: form-data; name="height"
	 *
	 * 40
	 *
	 * So it has some Content-* fields, then an empty line, and then the actual data, followed by a newline
	 */
	public WebRequestFormDataBlock(String data) {

		int cur = jumpOverNewline(data, 0);

		while (cur < data.length()) {

			data = data.substring(cur);

			int nindex = data.indexOf('\n');
			int rindex = data.indexOf('\r');
			int end;

			if (nindex > -1) {
				if ((rindex > -1) && (rindex < nindex)) {
					end = rindex;
				} else {
					end = nindex;
				}
			} else {
				if (rindex > -1) {
					end = rindex;
				} else {
					end = data.length();
				}
			}

			String line = data.substring(0, end).toLowerCase();

			if (line.startsWith("content-type: ")) {
				contentType = line.substring(14);
			}

			if (line.startsWith("content-disposition: ")) {
				contentDisposition = line.substring(21);
			}

			data = data.substring(end);

			if ("".equals(line)) {
				break;
			}
		}

		cur = jumpOverNewline(data, cur);

		content = data.substring(cur);

		if (content.endsWith("--")) {
			content = content.substring(0, content.length() - 2);
		}
	}

	private int jumpOverNewline(String data, int cur) {

		if (data.charAt(cur) == '\n') {
			return cur + 1;
		}

		if (cur+1 < data.length()) {
			if ((data.charAt(cur) == '\r') && (data.charAt(cur+1) == '\n')) {
				return cur + 2;
			}
		}

		return cur;
	}

	private boolean isNewline(char curChar) {

		return (curChar == '\n') || (curChar == '\r');
	}

	public String getName() {

		if (contentDisposition == null) {
			return null;
		}

		int pos = contentDisposition.indexOf("name=\"");

		if (pos < 0) {
			return null;
		}

		String name = contentDisposition.substring(pos + 6);

		name = name.substring(0, name.indexOf("\""));

		return name;
	}

	public String getContent() {

		return content;
	}

}
