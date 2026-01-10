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
 * @author Moya Schiller, moya@asofterspace.com
 */
public class WebServerAnswerWithText extends WebServerAnswerBase {

	protected String textKind = "plain";


	public WebServerAnswerWithText(String text) {
		if (text == null) {
			text = "";
		}
		this.data = text.getBytes(StandardCharsets.UTF_8);
	}

	public WebServerAnswerWithText(int statusCode, String text) {
		if (text == null) {
			text = "";
		}
		this.data = text.getBytes(StandardCharsets.UTF_8);
		this.status = statusCode;
	}

	@Override
	public String getContentType() {

		if (textKind == null) {
			textKind = "plain";
		}

		return "text/" + textKind.toLowerCase();
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
