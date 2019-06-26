/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.web;


/**
 * This represents the content of a request to our web server
 *
 * @author Moya (a softer space, 2019)
 */
public class WebRequestContent {

	private String contentString;

	private int contentLength;

	private String contentType;


	public WebRequestContent() {

		contentLength = 0;
	}

	public void setContent(String content) {
		contentString = content;
	}

	public void setContent(StringBuilder content) {
		contentString = content.toString();
	}

	public void setContentLength(Integer length) {

		if (length == null) {
			contentLength = 0;
		} else {
			contentLength = length;
		}
	}

	public void setContentType(String type) {
		contentType = type;
	}

	public boolean hasType(String type) {

		if ((contentType == null) || (type == null)) {
			return false;
		}

		if (contentType.equals(type)) {
			return true;
		}

		// in the case of multipart form data, the content type field contains something like:
		// multipart/form-data; boundary=---------------------------18467633426500
		if (type.equals("multipart/form-data")) {
			if (contentType.startsWith(type)) {
				return true;
			}
		}

		return false;
	}

	public String getBoundary() {

		if (contentType == null) {
			return null;
		}

		int index = contentType.indexOf("boundary=");

		if (index > -1) {
			return contentType.substring(index + 9);
		}

		return null;
	}

	public String getContentAsString() {
		return contentString;
	}

	public int getContentLength() {
		return contentLength;
	}

}
