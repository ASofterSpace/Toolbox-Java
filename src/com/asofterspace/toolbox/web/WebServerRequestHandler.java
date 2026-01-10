/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.web;

import com.asofterspace.toolbox.coders.UrlDecoder;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.utils.ByteBuffer;
import com.asofterspace.toolbox.Utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This handles one particular incoming connection request to the server
 *
 * @author Moya (a softer space, 2019)
 */
public class WebServerRequestHandler implements Runnable {

	public static final String HEADER_KEY_HOST = "Host: ";
	public static final String HEADER_KEY_AUTHORIZATION = "Authorization: ";
	public static final String HEADER_KEY_COOKIE = "Cookie: ";
	public static final String HEADER_KEY_SET_COOKIE = "Set-Cookie: ";
	public static final String HEADER_KEY_CACHE_CONTROL = "Cache-Control: ";
	public static final String HEADER_KEY_CONTENT_LENGTH = "Content-Length: ";
	public static final String HEADER_KEY_CONTENT_TYPE = "Content-Type: ";

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

	private boolean responded = false;

	// the auth token string that was received
	// will be set to the content of the Authorization: header
	// after a call to receiveArbitraryContent
	// (which is also called by receiveJsonContent or receiveFormDataContent)
	private String receivedAuthTokenStr = null;
	private String receivedCookieStr = null;
	private String receivedHostStr = null;


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
			// we here use ISO_8859_1, so that we can just get one byte after the other without anything funny going on
			BufferedReader inputReader = new BufferedReader(new InputStreamReader(request.getInputStream(), StandardCharsets.ISO_8859_1));
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

							case "OPTIONS":
								handleOptions(fileLocation);
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

	protected void handleOptions(String fileLocation) throws IOException {

		respond(200);
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
			respond(answer.getStatus(), answer);
		}
	}

	/**
	 * Overwrite this to answer GETs without (necessarily) returning a file for them;
	 * if you return null, the fall-through is to look for a file to answer the GET request
	 * (Here, foobar?arg=val&foo=bar would be passed to you as Map{"arg": "val", "foo": "bar"})
	 */
	protected WebServerAnswer answerGet(String location, Map<String, String> arguments) {
		return null;
	}

	/**
	 * Overwrite this to answer GETs without (necessarily) returning a file for them;
	 * if you return null, the fall-through is to look for a file to answer the GET request
	 * (Here, foobar?arg=val&foo=bar would be passed to you as ["arg=val", "foo=bar"])
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

		// System.out.println("DEBUG START receive()");

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
		return receiveFormDataContent(StandardCharsets.UTF_8);
	}

	protected WebRequestFormData receiveFormDataContent(Charset charset) throws IOException {

		WebRequestContent content = receiveArbitraryContent(charset);

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
		return receiveArbitraryContent(StandardCharsets.UTF_8);
	}

	protected WebRequestContent receiveArbitraryContent(Charset charset) throws IOException {

		int receivedContentLength = 0;
		String receivedContentType = null;

		while (true) {

			String line = receive();

			if (line == null) {
				break;
			}

			// System.out.println(line);

			if (line.startsWith(HEADER_KEY_AUTHORIZATION)) {
				this.receivedAuthTokenStr = line.substring(HEADER_KEY_AUTHORIZATION.length());
				continue;
			}

			if (line.startsWith(HEADER_KEY_COOKIE)) {
				this.receivedCookieStr = line.substring(HEADER_KEY_COOKIE.length());
				continue;
			}

			if (line.startsWith(HEADER_KEY_HOST)) {
				this.receivedHostStr = line.substring(HEADER_KEY_HOST.length());
				continue;
			}

			if (line.startsWith(HEADER_KEY_CONTENT_LENGTH)) {
				try {
					receivedContentLength = Integer.parseInt(line.substring(HEADER_KEY_CONTENT_LENGTH.length()));
				} catch (NumberFormatException e) {
				}
				continue;
			}

			if (line.startsWith(HEADER_KEY_CONTENT_TYPE)) {
				receivedContentType = line.substring(HEADER_KEY_CONTENT_TYPE.length());
				continue;
			}

			if ("".equals(line)) {
				if (receivedContentLength > 0) {
					ByteBuffer readData = new ByteBuffer();
					for (int i = 0; i < receivedContentLength; i++) {
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

						// we get one byte after the other (still all in fake ISO_8859_1, even though it is
						// actually containing UTF-8)
						int iread = input.read();
						readData.append((byte) iread);
					}

					// and now that all the bytes have been gathered, we can (once) interpret this as UTF-8,
					// or whatever it should be interpreted as!
					String receivedContent = readData.toString(charset);

					WebRequestContent result = new WebRequestContent();
					result.setContentLength(receivedContentLength);
					result.setContentType(receivedContentType);
					result.setContent(receivedContent);
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

	protected String getAccessControlAllowOrigin() {
		return "*";
	}

	protected void respond(int status) throws IOException {
		respond(status, null);
	}

	protected void respond(String status) throws IOException {
		respond(status, null);
	}

	protected void respond(WebServerAnswer answer) throws IOException {
		respond(answer.getStatus(), answer);
	}

	protected void respond(int status, WebServerAnswer answer) throws IOException {

		switch (status) {
			case 200:
				respond("200 OK", answer);
				break;
			case 204:
				respond("204 No Content", answer);
				break;
			case 301:
				respond("301 Moved Permanently", answer);
				break;
			case 302:
				respond("302 Moved Temporarily", answer);
				break;
			case 400:
				respond("400 Bad Request", answer);
				break;
			case 401:
				respond("401 Unauthorized", answer);
				break;
			case 403:
				respond("403 Forbidden", answer);
				break;
			case 404:
				respond("404 Not Found", answer);
				break;
			case 405:
				respond("405 Method Not Allowed", answer);
				break;
			case 406:
				respond("406 Not Acceptable", answer);
				break;
			case 418:
				respond("418 I'm a teapot", answer);
				break;
			case 419:
				respond("419 Page Expired", answer);
				break;
			case 429:
				respond("429 Too Many Requests", answer);
				break;
			case 501:
				respond("501 Not Implemented", answer);
				break;
			case 505:
				respond("505 HTTP Version Not Supported", answer);
				break;
			default:
				if (status != 500) {
					System.err.println("WebServerRequestHandler responding with HTTP 500, as HTTP " +
									   status + " is unknown!");
				}
				respond("500 Internal Server Error", answer);
		}
	}

	protected void respond(String status, WebServerAnswer answer) throws IOException {

		if (responded) {
			return;
		}

		responded = true;

		// System.out.println("Sending a " + status + " response for request #" + socketNum + "...");

		send("HTTP/1.1 " + status);

		send("Server: A Softer Space Java Server version " + Utils.TOOLBOX_VERSION_NUMBER);

		send("Access-Control-Allow-Origin: " + getAccessControlAllowOrigin());
		send("Access-Control-Allow-Methods: POST, GET, OPTIONS");
		send("Access-Control-Allow-Headers: X-PINGOTHER, Content-Type");
		send("Access-Control-Max-Age: 86400");

		if (answer == null) {

			// never keep these (as there is no big file involved anyway...)
			send(HEADER_KEY_CACHE_CONTROL + "no-store");
			send("");

		} else {

			long length = answer.getContentLength();

			send(HEADER_KEY_CACHE_CONTROL + answer.getPreferredCacheParadigm());
			send(HEADER_KEY_CONTENT_TYPE + answer.getContentType());
			send(HEADER_KEY_CONTENT_LENGTH + length);
			List<String> extraHeaderLines = answer.getExtraHeaderLines();
			if (extraHeaderLines != null) {
				for (String extraHeaderLine : extraHeaderLines) {
					if ((extraHeaderLine != null) && (!"".equals(extraHeaderLine))) {
						send(extraHeaderLine);
					}
				}
			}
			send("");

			if (!doNotSendBody) {

				byte[] binaryContent = answer.getBinaryContent();

				output.write(binaryContent, 0, (int) length);
			}
		}

		output.flush();
	}

	private WebServerAnswer answerGet(String location) {

		if (location == null) {
			location = "";
		}

		String[] arguments = new String[0];
		Map<String, String> argumentMap = new HashMap<>();

		if (location.contains("?")) {
			String[] locations = location.split("\\?");
			location = locations[0];
			if (locations.length > 1) {
				arguments = locations[1].split("&");
				for (String arg : arguments) {
					if (arg.contains("=")) {
						String key = arg.substring(0, arg.indexOf("="));
						String value = arg.substring(arg.indexOf("=") + 1);
						key = UrlDecoder.decode(key);
						value = UrlDecoder.decode(value);
						argumentMap.put(key, value);
					}
				}
			}
		}

		if (location.equals("/")) {
			location = "/index";
		}

		// check if our behavior was overwritten
		WebServerAnswer answer = answerGet(location, argumentMap);
		if (answer != null) {
			return answer;
		}

		answer = answerGet(location, arguments);
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
				return UrlDecoder.decode(arg.substring(key.length() + 1));
			}
		}
		return null;
	}

	public String getReceivedAuthTokenStr() {
		return receivedAuthTokenStr;
	}

	public String getReceivedCookieStr() {
		return receivedCookieStr;
	}

	public String getReceivedHostStr() {
		return receivedHostStr;
	}

}
