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
public class WebServerAnswerInJson extends WebServerAnswerBase {

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
	public String getContentType() {

		return "application/json";
	}

}
