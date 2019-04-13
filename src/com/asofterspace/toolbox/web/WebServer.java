/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.web;

import com.asofterspace.toolbox.io.Directory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


/**
 * This (hopefully) simplifies serving content on the web
 *
 * @author Moya (a softer space, 2019)
 */
public class WebServer {

	private boolean serverRunning;

	private int port;

	protected Directory webRoot;

	private List<String> fileLocationWhitelist;


	public WebServer(Directory webRoot) {

		port = 8080;

		serverRunning = false;

		this.webRoot = webRoot;

		fileLocationWhitelist = new ArrayList<>();
	}

	public void serve() {

		serverRunning = true;

		try {
			ServerSocket socket = new ServerSocket(port);

			// while we keep serving...
			while (serverRunning) {

				// ... get the next incoming connection request (waiting until there is one) ...
				Socket request = socket.accept();

				// ... and handle it expertly through one of our handlers :)
				WebServerRequestHandler handler = getHandler(request);
				Thread handlerThread = new Thread(handler);
				handlerThread.start();
			}

		} catch (IOException e) {
			System.err.println("Something unexpected happened to the server!");
			System.err.println(e);
		}
	}

	protected WebServerRequestHandler getHandler(Socket request) {
		return new WebServerRequestHandler(this, request, webRoot);
	}

	public void setFileLocationWhitelist(List<String> whitelist) {
		this.fileLocationWhitelist = whitelist;
	}

	public List<String> getFileLocationWhitelist() {
		return fileLocationWhitelist;
	}
}
