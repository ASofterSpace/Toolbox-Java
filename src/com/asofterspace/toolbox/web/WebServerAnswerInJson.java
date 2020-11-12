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


	public WebServerAnswerInJson(String jsonData) {
		init(jsonData);
	}

	public WebServerAnswerInJson(Record jsonData) {
		if (jsonData instanceof JSON) {
			init(jsonData.toString());
		} else {
			init(new JSON(jsonData).toString());
		}
	}

	private void init(String jsonData) {
		this.data = jsonData.getBytes(StandardCharsets.UTF_8);
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
}
