/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.web;

import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.Utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;


/**
 * This handles one particular incoming connection request to the server
 *
 * @author Moya (a softer space, 2019)
 */
public class WebServerRequestHandler implements Runnable {

	private Socket request;

	private BufferedReader input;

	private BufferedOutputStream output;

	private int socketNum;

	private static int globalSocketNum = 0;


	public WebServerRequestHandler(Socket request) {

		socketNum = globalSocketNum++;

		System.out.println("Request #" + socketNum + " incoming!");

		this.request = request;
	}

	@Override
	public void run() {

		System.out.println("Handler for request #" + socketNum + " starting up...");

		try (
			BufferedReader inputReader = new BufferedReader(new InputStreamReader(request.getInputStream()));
			BufferedOutputStream outputWriter = new BufferedOutputStream(request.getOutputStream())
		) {
			System.out.println("Starting to read request #" + socketNum + "...");

			this.input = inputReader;
			this.output = outputWriter;

			String line = receive();

			System.out.println("First line of request #" + socketNum + ": '" + line + "'");

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

						// a HEAD request is the same as a GET request, but it ONLY gives
						// the header and no content :)
						case "HEAD":
							if (requestedFile == null) {
								respond(404);
							} else {
								respond(200);
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

		String result = null;

		if (input.ready()) {
			result = input.readLine();
		} else {
			System.out.println("Input of request #" + socketNum + " not ready to be read!");
			// TODO :: wait for a while, then check again...
		}

		return result;
	}

	private void send(String line) throws IOException {

		line = line + "\r\n";

		output.write(line.getBytes(StandardCharsets.UTF_8));
	}

	private void respond(String status, File fileToSend) throws IOException {

		System.out.println("Sending a " + status + " response for request #" + socketNum + "...");

		send("HTTP/1.1 " + status);

		send("Server: A Softer Space Java Server version " + Utils.TOOLBOX_VERSION_NUMBER);
	}

	private void respond(int status, File fileToSend) throws IOException {

		switch (status) {
			case 200:
				respond("200 OK");
				break;
			case 400:
				respond("400 Bad Request");
				break;
			case 403:
				respond("403 Forbidden");
				break;
			case 404:
				respond("404 Not Found");
				break;
			case 418:
				respond("418 I'm a teapot");
				break;
			case 501:
				respond("501 Not Implemented");
				break;
			case 505:
				respond("505 HTTP Version Not Supported");
				break;
			default:
				respond("500 Internal Server Error");
		}
	}

	private void respond(String status) throws IOException {
		respond(status, null);
	}

	private void respond(int status) throws IOException {
		respond(status, null);
	}

	private File getFile(String location) {
		// TODO :: actually get the file
		return null;
	}

}
