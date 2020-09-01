/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.JSON;
import com.asofterspace.toolbox.io.JsonParseException;
import com.asofterspace.toolbox.web.WebServer;
import com.asofterspace.toolbox.web.WebServerAnswer;
import com.asofterspace.toolbox.web.WebServerAnswerInJson;
import com.asofterspace.toolbox.web.WebServerRequestHandler;

import java.io.IOException;
import java.net.Socket;


public class WebTestServerRequestHandler extends WebServerRequestHandler {

	public static String lastPostContentStr;

	public static JSON lastPostContentJSON;


	public WebTestServerRequestHandler(WebServer server, Socket request, Directory webRoot) {

		super(server, request, webRoot);
	}

	@Override
	protected void handlePost(String fileLocation) throws IOException {

		WebTestServerRequestHandler.lastPostContentStr = null;
		WebTestServerRequestHandler.lastPostContentJSON = null;

		String jsonData = receiveJsonContent();

		WebTestServerRequestHandler.lastPostContentStr = jsonData;

		if (jsonData == null) {
			respond(400);
			return;
		}

		JSON json;
		try {
			json = new JSON(jsonData);
		} catch (JsonParseException e) {
			respond(400);
			return;
		}

		WebTestServerRequestHandler.lastPostContentJSON = json;

		WebServerAnswer answer = new WebServerAnswerInJson("{\"success\": true}");
		respond(200, answer);
	}

}
