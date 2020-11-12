/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.web;

import java.nio.charset.StandardCharsets;


/**
 * This represents the text that a web server might send as answer
 * to a request
 *
 * @author Tom Moya Schau, moya@asofterspace.com
 */
public class WebServerAnswerWithText implements WebServerAnswer {

	private byte[] data;

	private String textKind = "plain";


	public WebServerAnswerWithText(String text) {
		this.data = text.getBytes(StandardCharsets.UTF_8);
	}

	@Override
	public long getContentLength() {

		return data.length;
	}

	@Override
	public String getPreferredCacheParadigm() {

		// never keep text data as it might change all the time
		return "no-store";
	}

	@Override
	public String getContentType() {

		if (textKind == null) {
			textKind = "plain";
		}

		return "text/" + textKind.toLowerCase();
	}

	@Override
	public byte[] getBinaryContent() {

		return data;
	}

	public String getTextKind() {
		return textKind;
	}

	/**
	 * Possibilities: plain, html
	 */
	public void setTextKind(String textKind) {
		this.textKind = textKind;
	}

}
