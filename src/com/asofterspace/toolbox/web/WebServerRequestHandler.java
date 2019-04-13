/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.web;

import com.asofterspace.toolbox.io.BinaryFile;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.Utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;


/**
 * This handles one particular incoming connection request to the server
 *
 * @author Moya (a softer space, 2019)
 */
public class WebServerRequestHandler implements Runnable {

	private WebServer server;

	private Socket request;

	private Directory webRoot;

	private BufferedReader input;

	private BufferedOutputStream output;

	private boolean doNotSendBody;

	private int socketNum;

	private static int globalSocketNum = 0;


	public WebServerRequestHandler(WebServer server, Socket request, Directory webRoot) {

		socketNum = globalSocketNum++;

		// System.out.println("Request #" + socketNum + " incoming!");

		this.server = server;

		this.request = request;

		this.webRoot = webRoot;

		doNotSendBody = false;
	}

	@Override
	@SuppressWarnings("fallthrough")
	public void run() {

		// System.out.println("Handler for request #" + socketNum + " starting up...");

		try (
			BufferedReader inputReader = new BufferedReader(new InputStreamReader(request.getInputStream()));
			BufferedOutputStream outputWriter = new BufferedOutputStream(request.getOutputStream())
		) {
			// System.out.println("Starting to read request #" + socketNum + "...");

			this.input = inputReader;
			this.output = outputWriter;

			String line = receive();

			// System.out.println("First line of request #" + socketNum + ": '" + line + "'");

			// well if there is no request...
			if (line == null) {
				// ... then it is pretty clearly bad xD
				respond(400);
				return;
			}

			String[] lines = line.split(" ");

			if (lines.length == 3) {

				String requestKind = lines[0];
				String fileLocation = lines[1];
				String httpVersion = lines[2];

				if (httpVersion.startsWith("HTTP/1.1")) {

					File requestedFile = getFile(fileLocation);

					switch (requestKind) {

						// a HEAD request is the same as a GET request, but it ONLY gives
						// the header and no content :)
						case "HEAD":
							doNotSendBody = true;
							// fall into GET... brilliant! :D

						case "GET":
							if (requestedFile == null) {
								// only if the file was not found anyway...
								if ("/coffee".equals(fileLocation)) {
									// ... show an easteregg :)
									respond(418);
								} else {
									// TODO :: put in a 404 default file
									respond(404);
								}
							} else {
								respond(200, requestedFile);
							}
							break;

						case "DELETE":
							// sure we COULD delete the requestedFile...
							// but seriously, why would we? just because someone said we should? bushwah!
							respond(403);
							break;

						default:
							respond(501);
					}
				} else {
					respond(505);
				}
			} else {
				respond(400);
			}

		} catch (IOException e) {
			System.err.println("Something unexpected happened to the connection request handler!");
			System.err.println(e);
		}
	}

	private String receive() throws IOException {

		try {
			// one minute (60 seconds) long, check every 100 ms if data became available...
			for (int i = 0; i < 600; i++) {

				// ... and if yes, return!
				if (input.ready()) {
					return input.readLine();
				}

				Thread.sleep(100);
			}
		} catch (InterruptedException e) {
			// our sleep was interrupted, so no more sleeping...
		}

		// check one last time...
		if (input.ready()) {
			return input.readLine();
		}

		// and if not, well, then tough luck...
		// System.out.println("Input of request #" + socketNum + " not ready to be read!");

		return null;
	}

	private void send(String line) throws IOException {

		line = line + "\r\n";

		output.write(line.getBytes(StandardCharsets.UTF_8));
	}

	private void respond(String status, File fileToSend) throws IOException {

		// System.out.println("Sending a " + status + " response for request #" + socketNum + "...");

		send("HTTP/1.1 " + status);

		send("Server: A Softer Space Java Server version " + Utils.TOOLBOX_VERSION_NUMBER);

		if (fileToSend == null) {

			// never keep these (as there is no big file involved anyway...)
			send("Cache-Control: no-store");

			send("");

		} else {

			String fn = fileToSend.getFilename();

			if (fn.endsWith(".png") || fn.endsWith(".js")) {
				// keep these a week long
				// (if we want to invalidate, we should add a ?v=version to the request)
				send("Cache-Control: max-age=604800");
			} else {
				// never keep these
				send("Cache-Control: no-store");
			}

			long length = fileToSend.getContentLength();

			send("Content-type: " + fileToSend.getContentType());

			send("Content-length: " + length);

			send("");

			if (!doNotSendBody) {

				BinaryFile binaryFile = new BinaryFile(fileToSend);

				byte[] binaryFileContent = binaryFile.loadContent();

				output.write(binaryFileContent, 0, (int) length);
			}
		}

		output.flush();
	}

	private void respond(int status, File fileToSend) throws IOException {

		switch (status) {
			case 200:
				respond("200 OK", fileToSend);
				break;
			case 400:
				respond("400 Bad Request", fileToSend);
				break;
			case 403:
				respond("403 Forbidden", fileToSend);
				break;
			case 404:
				respond("404 Not Found", fileToSend);
				break;
			case 418:
				respond("418 I'm a teapot", fileToSend);
				break;
			case 501:
				respond("501 Not Implemented", fileToSend);
				break;
			case 505:
				respond("505 HTTP Version Not Supported", fileToSend);
				break;
			default:
				respond("500 Internal Server Error", fileToSend);
		}
	}

	private void respond(String status) throws IOException {
		respond(status, null);
	}

	private void respond(int status) throws IOException {
		respond(status, null);
	}

	private File getFile(String location) {

		String[] arguments = new String[0];

		if (location.contains("?")) {
			String[] locations = location.split("\\?");
			location = locations[0];
			arguments = locations[1].split("&");
		}

		if (location.equals("/")) {
			location = "/index.htm";
		}

		List<String> whitelist = server.getFileLocationWhitelist();

		for (String entry : whitelist) {

			if (location.equals("/" + entry)) {

				// actually get the file
				return webRoot.getFile(entry);
			}
		}

		// if the file was not found on the whitelist, do not return it
		// - even if it exists on the server!
		return null;
	}

}
