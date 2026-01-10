/**
 * Unlicensed code created by A Softer Space, 2026
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.web;

import java.util.ArrayList;


/**
 * This returns a redirect answer
 * Any status from 300 to 399 is acceptable, although only a few are standard ^^'
 */
public class WebServerAnswerRedirect extends WebServerAnswerWithText {

	public WebServerAnswerRedirect(String targetURL) {
		this(302, targetURL);
	}

	public WebServerAnswerRedirect(int status, String targetURL) {
		this(status, targetURL, "redirect");
	}

	public WebServerAnswerRedirect(int status, String targetURL, String textContent) {
		super(status, textContent);

		this.extraHeaderLines = new ArrayList<>();
		this.extraHeaderLines.add("Location: " + targetURL);
	}

}
