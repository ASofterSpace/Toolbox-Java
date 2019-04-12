/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.web;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * This (hopefully) simplifies serving content on the web
 *
 * @author Moya (a softer space, 2019)
 */
public class WebServer {

	private boolean serverRunning;

	private int port;


	public WebServer() {

		port = 8080;

		serverRunning = false;
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
				WebServerRequestHandler handler = new WebServerRequestHandler(request);
				Thread handlerThread = new Thread(handler);
				handlerThread.start();
			}

		} catch (IOException e) {
			System.err.println("Something unexpected happened to the server!");
			System.err.println(e);
		}
	}
}
