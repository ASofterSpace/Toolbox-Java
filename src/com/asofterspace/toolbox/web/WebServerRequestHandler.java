/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.web;

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

			System.out.println("First line of request #" + socketNum + ": " + line);

			System.out.println("Sending a response for request #" + socketNum + "...");
			send("HTTP/1.1 501 Not Implemented");
			send("Server: A Softer Space Java Server version " + Utils.TOOLBOX_VERSION_NUMBER);

		} catch (IOException e) {
			System.err.println("Something unexpected happened to the connection request handler!");
			System.err.println(e);
		}

		System.out.println("Request #" + socketNum + " has been expertly handled. ;)");
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

}
