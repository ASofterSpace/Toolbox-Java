/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.web;

import com.asofterspace.toolbox.io.JSON;
import com.asofterspace.toolbox.utils.Record;

import java.nio.charset.StandardCharsets;


/**
 * This represents the json data that a web server might send as answer
 * to a request
 *
 * @author Moya (a softer space, 2019)
 */
public class WebServerAnswerInJson implements WebServerAnswer {

	private byte[] data;

	private int status = WebServer.DEFAULT_STATUS;


	public WebServerAnswerInJson(String jsonData) {
		this(WebServer.DEFAULT_STATUS, jsonData);
	}

	public WebServerAnswerInJson(int statusCode, String jsonData) {
		this.data = jsonData.getBytes(StandardCharsets.UTF_8);
		this.status = statusCode;
	}

	public WebServerAnswerInJson(Record jsonData) {
		this(WebServer.DEFAULT_STATUS, jsonData);
	}

	public WebServerAnswerInJson(int statusCode, Record jsonData) {
		if (jsonData instanceof JSON) {
			this.data = jsonData.toString().getBytes(StandardCharsets.UTF_8);
		} else {
			this.data = (new JSON(jsonData)).toString().getBytes(StandardCharsets.UTF_8);
		}
		this.status = statusCode;
	}

	public WebServerAnswerInJson(int statusCode, String key, String value) {
		JSON jsonData = new JSON();
		jsonData.set(key, value);
		this.data = jsonData.toString().getBytes(StandardCharsets.UTF_8);
		this.status = statusCode;
	}

	@Override
	public long getContentLength() {

		return data.length;
	}

	@Override
	public String getPreferredCacheParadigm() {

		// never keep json data as it might change all the time
		return "no-store";
	}

	@Override
	public String getContentType() {

		return "application/json";
	}

	@Override
	public byte[] getBinaryContent() {

		return data;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

}
