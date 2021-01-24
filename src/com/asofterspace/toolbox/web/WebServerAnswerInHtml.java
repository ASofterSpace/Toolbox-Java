/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.web;


/**
 * This represents the html data that a web server might send as answer
 * to a request
 *
 * @author Tom Moya Schau, moya@asofterspace.com
 */
public class WebServerAnswerInHtml extends WebServerAnswerWithText {

	public WebServerAnswerInHtml(String html) {
		super(html);
		setTextKind("html");
	}
}
