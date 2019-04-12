/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.web;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;


/**
 * This handles one particular incoming connection request to the server
 *
 * @author Moya (a softer space, 2019)
 */
public class WebServerRequestHandler implements Runnable {

	private Socket request;

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
			BufferedReader inputReader = new BufferedReader(new InputStreamReader(request.getInputStream()))
		) {

			System.out.println("Starting to read request #" + socketNum + "...");

			if (inputReader.ready()) {
				String input = inputReader.readLine();

				System.out.println("First line of request #" + socketNum + ": " + input);
			} else {
				System.out.println("Input of request #" + socketNum + " not ready to be read!");
				// TODO :: wait for a while, then check again...
			}

		} catch (IOException e) {
			System.err.println("Something unexpected happened to the connection request handler!");
			System.err.println(e);
		}

		System.out.println("Request #" + socketNum + " has been expertly handled. ;)");
	}

}
