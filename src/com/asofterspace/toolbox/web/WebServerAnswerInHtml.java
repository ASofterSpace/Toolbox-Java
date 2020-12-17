/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.web;

import java.nio.charset.StandardCharsets;


/**
 * This represents the html data that a web server might send as answer
 * to a request
 *
 * @author Tom Moya Schau, moya@asofterspace.com
 */
public class WebServerAnswerInHtml implements WebServerAnswer {

	private byte[] data;


	public WebServerAnswerInHtml(String html) {
		this.data = html.getBytes(StandardCharsets.UTF_8);
	}

	@Override
	public long getContentLength() {

		return data.length;
	}

	@Override
	public String getPreferredCacheParadigm() {

		// never keep html data as it might change all the time
		return "no-store";
	}

	@Override
	public String getContentType() {

		return "text/html";
	}

	@Override
	public byte[] getBinaryContent() {

		return data;
	}
}
