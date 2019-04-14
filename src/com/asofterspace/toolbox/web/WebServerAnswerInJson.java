/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.web;

import com.asofterspace.toolbox.io.JSON;

import java.nio.charset.StandardCharsets;


/**
 * This represents the json data that a web server might send as answer
 * to a request
 *
 * @author Moya (a softer space, 2019)
 */
public class WebServerAnswerInJson implements WebServerAnswer {

	private byte[] data;


	public WebServerAnswerInJson(String jsonData) {

		this.data = jsonData.getBytes(StandardCharsets.UTF_8);
	}

	public WebServerAnswerInJson(JSON jsonData) {

		this(jsonData.toString());
	}

	public long getContentLength() {

		return data.length;
	}

	public String getPreferredCacheParadigm() {

		// never keep json data as it might change all the time
		return "no-store";
	}

	public String getContentType() {

		return "application/json";
	}

	public byte[] getBinaryContent() {

		return data;
	}
}
