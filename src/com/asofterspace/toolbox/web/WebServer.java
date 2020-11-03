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
 * Usually, you want to extend this generic WebServer with your own Server object
 * which overrides the getHandler function to return your own handler that extends
 * WebServerRequestHandler and adds functionality to it.
 *
 * @author Moya (a softer space, 2019)
 */
public class WebServer implements Runnable {

	private boolean serverRunning;

	private int port;

	private String address;

	protected Directory webRoot;

	private List<String> fileLocationWhitelist;

	// a separate thread for the server in case it is started asynchronously
	// (otherwise it just runs on the main thread)
	private Thread serverThread;

	// a list of all handler threads that have been created so far for handling requests
	private List<Thread> currentHandlerThreads;


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
			port = 8080;
		}
		this.port = port;

		serverRunning = false;

		this.webRoot = webRoot;

		fileLocationWhitelist = new ArrayList<>();

		// seems like a good default assumption ;)
		address = "localhost";

		currentHandlerThreads = new ArrayList<>();
	}

	/**
	 * Serve data with this server synchronously
	 * (by starting one handler after another)
	 */
	public void serve() {

		serverRunning = true;

		ServerSocket socket = null;

		try {
			socket = new ServerSocket(port);

			// while we keep serving...
			while (serverRunning) {

				// ... get the next incoming connection request (waiting until there is one) ...
				Socket request = socket.accept();

				// ... and handle it expertly through one of our handlers :)
				synchronized(currentHandlerThreads) {
					WebServerRequestHandler handler = getHandler(request);
					Thread currentHandlerThread = new Thread(handler);
					currentHandlerThread.start();
					currentHandlerThreads.add(currentHandlerThread);
					handler.setThreadInfo(currentHandlerThread, currentHandlerThreads);
				}
			}

		} catch (IOException e) {
			System.err.println("Something unexpected happened to the server!");
			System.err.println(e);
		} finally {
			try {
				if (socket != null) {
					socket.close();
				}
			} catch (IOException e2) {
				System.err.println("Something unexpected happened while closing the server!");
				System.err.println(e2);
			}
		}
	}

	/**
	 * Serve data with this server asynchronously
	 * (by starting one handler after another - so we here start a thread which then starts other threads)
	 */
	public void serveAsync() {
		serverThread = new Thread(this);
		serverThread.start();
	}

	/**
	 * Serve data, either synchronously or asynchronously
	 */
	public void serve(boolean async) {
		if (async) {
			serveAsync();
		} else {
			serve();
		}
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

	public int getPort() {
		return port;
	}

	public String getAddress() {
		return address;
	}

	public void stop() {

		serverRunning = false;

		synchronized(currentHandlerThreads) {
			for (Thread currentHandlerThread : currentHandlerThreads) {
				try {
					currentHandlerThread.interrupt();
				} catch (Exception e) {
					System.err.println("We tried stopping a web server handler thread, but got an exception: " + e);
				}
			}
		}

		if (serverThread != null) {
			serverThread.interrupt();
		}
	}
}
