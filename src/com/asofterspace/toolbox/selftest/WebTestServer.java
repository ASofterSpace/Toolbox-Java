/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.web.WebServer;
import com.asofterspace.toolbox.web.WebServerRequestHandler;

import java.net.Socket;


public class WebTestServer extends WebServer {

	public WebTestServer(Directory webRoot, int port) {

		super(webRoot, port);
	}

	protected WebServerRequestHandler getHandler(Socket request) {
		return new WebTestServerRequestHandler(this, request, webRoot);
	}

}
