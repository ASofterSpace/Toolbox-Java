/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.web;

import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.utils.ByteBuffer;
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

	protected Directory webRoot;

	protected BufferedReader input;

	protected BufferedOutputStream output;

	private boolean doNotSendBody;

	private int socketNum;

	private static int globalSocketNum = 0;

	private Thread ourThread;

	private List<Thread> threadList;


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

			} else {

				String[] lines = line.split(" ");

				if (lines.length == 3) {

					String requestKind = lines[0];
					String fileLocation = lines[1];
					String httpVersion = lines[2];

					if (httpVersion.startsWith("HTTP/1.1")) {

						switch (requestKind) {

							// a HEAD request is the same as a GET request, but it ONLY gives
							// the header and no content :)
							case "HEAD":
								doNotSendBody = true;
								// fall into GET... brilliant! :D

							case "GET":
								handleGet(fileLocation);
								break;

							case "PUT":
								handlePut(fileLocation);
								break;

							case "POST":
								handlePost(fileLocation);
								break;

							case "DELETE":
								handleDelete(fileLocation);
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
			}

		} catch (IOException e) {
			System.err.println("Something unexpected happened to the connection request handler!");
			System.err.println(e);
		}

		cleanup();
	}

	public void setThreadInfo(Thread thisThread, List<Thread> listContainingThisThread) {

		this.ourThread = thisThread;

		this.threadList = listContainingThisThread;
	}

	/**
	 * Clean up this worker
	 */
	protected void cleanup() {

		if (threadList == null) {
			return;
		}

		if (ourThread == null) {
			return;
		}

		synchronized(threadList) {
			threadList.remove(ourThread);
		}
	}

	protected void handleGet(String fileLocation) throws IOException {

		WebServerAnswer answer = answerGet(fileLocation);

		if (answer == null) {
			// only if the file was not found anyway...
			if ("/coffee".equals(fileLocation)) {
				// ... show an easteregg :)
				respond(418);
			} else {
				// TODO :: put in a 404 default file
				respond(404);
			}
		} else {
			respond(200, answer);
		}
	}

	/**
	 * Overwrite this to answer GETs without (necessarily) returning a file for them;
	 * if you return null, the fall-through is to look for a file to answer the GET request
	 */
	protected WebServerAnswer answerGet(String location, String[] arguments) {
		return null;
	}

	protected void handlePut(String fileLocation) throws IOException {

		respond(501);
	}

	protected void handlePost(String fileLocation) throws IOException {

		respond(501);
	}

	protected void handleDelete(String fileLocation) throws IOException {

		// sure we COULD delete the file in fileLocation...
		// but seriously, why would we? just because someone said we should? bushwah!
		respond(403);
	}

	// receive a whole line
	protected String receive() throws IOException {

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

	// receive the json content of the request, if json content has been sent along with the request
	// (and return null otherwise)
	protected String receiveJsonContent() throws IOException {

		WebRequestContent content = receiveArbitraryContent();

		if (content == null) {
			return null;
		}

		if (content.hasType("application/json")) {
			return content.getContentAsString();
		}

		return null;
	}

	protected WebRequestFormData receiveFormDataContent() throws IOException {

		WebRequestContent content = receiveArbitraryContent();

		if (content == null) {
			return null;
		}

		if (content.hasType("multipart/form-data")) {
			String strContent = content.getContentAsString();

			String boundary = content.getBoundary();

			String[] blocks = strContent.split(boundary);

			WebRequestFormData result = new WebRequestFormData();

			for (String block : blocks) {
				// we have blocks such as empty ones, and blocks containing just the string -- etc...
				// ignore those!
				if (block.length() > 4) {
					result.addBlock(block);
				}
			}

			return result;
		}

		return null;
	}

	protected WebRequestContent receiveArbitraryContent() throws IOException {

		WebRequestContent result = new WebRequestContent();

		int length = 0;

		String contentType = null;

		while (true) {

			String line = receive();

			if (line == null) {
				break;
			}

			if (line.toLowerCase().startsWith("content-length: ")) {
				try {
					result.setContentLength(Integer.parseInt(line.substring(16)));
				} catch (NumberFormatException e) {
				}
			}

			if (line.toLowerCase().startsWith("content-type: ")) {
				result.setContentType(line.substring(14));
			}

			if ("".equals(line)) {
				if (result.getContentLength() > 0) {
					ByteBuffer readData = new ByteBuffer();
					// StringBuilder readData = new StringBuilder();
					// StringBuilder output = new StringBuilder();
					for (int i = 0; i < result.getContentLength(); i++) {
						if (!input.ready()) {
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								break;
							}
							if (!input.ready()) {
								break;
							}
						}

						// ORIGINAL TODO :: this seems to have problem with non-ASCII nonsense currently...
						// readData.append((char) input.read());

						// TODO :: now with the current approach, basic UTF-8 is working
						// (everything with one and two bytes, as long as the second byte
						// is not too high or something like that), but more is not working,
						// and we cannot get it work either... e.g. take the word "россияне",
						// this comes in with some letters (e.g. the last two) fine,
						// while some others like the с which are JUST too high in unicode
						// come out mangled...
						// this particular example SHOULD look like:
						// р             о         с             с             и         я             н         е
						// -47,    -128, -48, -66, -47,   -127,  -47,   -127,  -48, -72, -47,   -113,  -48, -67, -48, -75  bytes actually expected
						// but instead, we get:
						// -47, 32, -84, -48, -66, -47, -1, -3,  -47, -1, -3,  -48, -72, -47, -1, -3,  -48, -67, -48, -75  bytes after conversion
						// 209, 32,172,  208, 190, 209, 255,253, 209, 255,253, 208, 184, 209, 255,253, 208, 189, 208, 181  ints after splitting above 255
						// 209,    8364, 208, 190, 209,   65533, 209,   65533, 208, 184, 209,   65533, 208, 189, 208, 181  ints incoming
						// as the ints incoming for с and я are exactly the same, we believe that there is already a problem
						// with the data coming into this function...
						int iread = input.read();

						if (iread > 255) {
							readData.append((byte) (iread / 256));
							readData.append((byte) (iread % 256));
							/*
							output.append(iread / 256);
							output.append(",");
							output.append(iread % 256);
							output.append(", ");
							*/
						} else {

							readData.append((byte) iread);
							/*
							output.append(iread);
							output.append(", ");
							*/
						}


					}

					/*
					System.out.println("raw: " + output.toString());
					System.out.println("bytes: " + readData.showBytes());

					String specialChar = "россияне";
					byte[] tmp = specialChar.getBytes("UTF-8");
					String s = new String(tmp, "UTF-8");
					for (byte tb : tmp) {
						System.out.println(tb);
					}
					*/

					String strContent = readData.toString(StandardCharsets.UTF_8);
					// String strContent = StandardCharsets.UTF_8.encode(buffer.toByteArray());
					result.setContent(strContent);
					// result.setContent(readData);
					return result;
				}
				break;
			}
		}

		return null;
	}

	private void send(String line) throws IOException {

		line = line + "\r\n";

		output.write(line.getBytes(StandardCharsets.UTF_8));
	}

	protected void respond(String status, WebServerAnswer answer) throws IOException {

		// System.out.println("Sending a " + status + " response for request #" + socketNum + "...");

		send("HTTP/1.1 " + status);

		send("Server: A Softer Space Java Server version " + Utils.TOOLBOX_VERSION_NUMBER);

		send("Access-Control-Allow-Origin: *");
		send("Access-Control-Allow-Methods: \"POST, GET\"");

		if (answer == null) {

			// never keep these (as there is no big file involved anyway...)
			send("Cache-Control: no-store");

			send("");

		} else {

			long length = answer.getContentLength();

			send("Content-Control: " + answer.getPreferredCacheParadigm());

			send("Content-Type: " + answer.getContentType());

			send("Content-Length: " + length);

			send("");

			if (!doNotSendBody) {

				byte[] binaryContent = answer.getBinaryContent();

				output.write(binaryContent, 0, (int) length);
			}
		}

		output.flush();
	}

	protected void respond(int status, WebServerAnswer answer) throws IOException {

		switch (status) {
			case 200:
				respond("200 OK", answer);
				break;
			case 400:
				respond("400 Bad Request", answer);
				break;
			case 403:
				respond("403 Forbidden", answer);
				break;
			case 404:
				respond("404 Not Found", answer);
				break;
			case 418:
				respond("418 I'm a teapot", answer);
				break;
			case 501:
				respond("501 Not Implemented", answer);
				break;
			case 505:
				respond("505 HTTP Version Not Supported", answer);
				break;
			default:
				respond("500 Internal Server Error", answer);
		}
	}

	protected void respond(String status) throws IOException {
		respond(status, null);
	}

	protected void respond(int status) throws IOException {
		respond(status, null);
	}

	private WebServerAnswer answerGet(String location) {

		if (location == null) {
			location = "";
		}

		String[] arguments = new String[0];

		if (location.contains("?")) {
			String[] locations = location.split("\\?");
			location = locations[0];
			arguments = locations[1].split("&");
		}

		if (location.equals("/")) {
			location = "/index";
		}

		// check if our behavior was overwritten
		WebServerAnswer answer = answerGet(location, arguments);
		if (answer != null) {
			return answer;
		}

		// at first, try the file itself
		File result = getFileFromLocation(location, arguments);

		if (result != null) {
			return new WebServerAnswerBasedOnFile(result);
		}

		// in case of a location like www.asofterspace.com/blubb/, actually add "index" to the end automagically...
		if (location.endsWith("/")) {
			location += "index";

			result = getFileFromLocation(location, arguments);

			if (result != null) {
				return new WebServerAnswerBasedOnFile(result);
			}
		}

		// then try serving a .php file that was navigated to without extension
		result = getFileFromLocation(location + ".php", arguments);

		if (result != null) {
			return new WebServerAnswerBasedOnFile(result);
		}

		// finally, if no .php file was found either, try to navigate to an .htm file...
		result = getFileFromLocation(location + ".htm", arguments);

		if (result != null) {
			return new WebServerAnswerBasedOnFile(result);
		}

		// ... or a .html file instead
		result = getFileFromLocation(location + ".html", arguments);

		if (result != null) {
			return new WebServerAnswerBasedOnFile(result);
		}

		return null;
	}

	protected String getWhitelistedLocationEquivalent(String location) {

		List<String> whitelist = server.getFileLocationWhitelist();

		if (location == null) {
			return null;
		}

		if (!location.startsWith("/")) {
			location = "/" + location;
		}

		for (String entry : whitelist) {

			if (location.equals(entry) || location.equals("/" + entry)) {

				return entry;
			}
		}

		return null;
	}

	/**
	 * For a request like /bla.htm?foo=bar&thoom=floom,
	 * location is /bla.htm
	 * arguments is ["foo=bar", "thoom=floom"]
	 */
	protected File getFileFromLocation(String location, String[] arguments) {

		String locEquiv = getWhitelistedLocationEquivalent(location);

		// if no root is specified, then we are just not serving any files at all
		// and if no location equivalent is found on the whitelist, we are not serving this request
		if ((webRoot != null) && (locEquiv != null)) {

			// actually get the file
			return webRoot.getFile(locEquiv);
		}

		// if the file was not found on the whitelist, do not return it
		// - even if it exists on the server!
		return null;
	}

	/**
	 * Passes in arguments like
	 * ["foo=bar", "blubb=blobb"], "foo"
	 *
	 * Returns
	 * "bar"
	 */
	public static String getArgumentValueByKey(String[] arguments, String key) {
		for (String arg : arguments) {
			if (arg.startsWith(key + "=")) {
				return arg.substring(key.length() + 1);
			}
		}
		return null;
	}

}
