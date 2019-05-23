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
public class WebServer implements Runnable {

	private boolean serverRunning;

	private int port;

	protected Directory webRoot;

	private List<String> fileLocationWhitelist;

	private Thread currentHandlerThread;


	public WebServer() {
		init(null, null);
	}

	public WebServer(Directory webRoot) {
		init(webRoot, null);
	}

	public WebServer(Directory webRoot, Integer port) {
		init(webRoot, port);
	}

	private void init(Directory webRoot, Integer port) {

		if (port == null) {
			this.port = 8080;
		} else {
			this.port = port;
		}

		serverRunning = false;

		this.webRoot = webRoot;

		fileLocationWhitelist = new ArrayList<>();
	}

	/**
	 * Serve data with this server synchronously
	 * (by starting one handler after another)
	 */
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
				currentHandlerThread = new Thread(handler);
				currentHandlerThread.start();
				currentHandlerThread = null;
			}

		} catch (IOException e) {
			System.err.println("Something unexpected happened to the server!");
			System.err.println(e);
		}
	}

	/**
	 * Serve data with this server asynchronously
	 * (by starting one handler after another - so we here start a thread which then starts other threads)
	 */
	public void serveAsync() {
		Thread serverThread = new Thread(this);
		serverThread.start();
	}

	@Override
	public void run() {
		serve();
	}

	protected WebServerRequestHandler getHandler(Socket request) {
		return new WebServerRequestHandler(this, request, webRoot);
	}

	/**
	 * Add a file to the file location whitelist, with its path relative to the web root
	 * (by default, NOTHING is served, and ONLY files on the whitelist are getting served!)
	 */
	public void addToWhitelist(String filename) {
		this.fileLocationWhitelist.add(filename);
	}

	/**
	 * Set the file location whitelist
	 * (by default, NOTHING is served, and ONLY files on the whitelist are getting served!)
	 */
	public void setWhitelist(List<String> whitelist) {
		this.fileLocationWhitelist = whitelist;
	}

	public List<String> getFileLocationWhitelist() {
		return fileLocationWhitelist;
	}

	public void stop() {

		serverRunning = false;

		if (currentHandlerThread != null) {
			currentHandlerThread.interrupt();
		}
	}
}
