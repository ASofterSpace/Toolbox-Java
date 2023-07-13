/**
 * Unlicensed code created by A Softer Space, 2017
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.web;

import com.asofterspace.toolbox.coders.UrlEncoder;
import com.asofterspace.toolbox.io.BinaryFile;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.JSON;
import com.asofterspace.toolbox.io.JsonParseException;
import com.asofterspace.toolbox.utils.ByteBuffer;
import com.asofterspace.toolbox.utils.StrUtils;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;


/**
 * This (hopefully) simplifies access to the web, providing regular REST methods
 * such as GET, POST and PUT, as well as providing basic FTP methods such as
 * ftpDownload
 *
 * @author Moya (a softer space, 2017)
 */
public class WebAccessor {

	private static Directory cache = new Directory("cache");

	// keeps track of the last accessed URL for debug purposes
	private static String lastUrl;


	/**
	 * Get a web resource asynchronously
	 * @param url  The url of the web resource
	 * @param callback  The callback that is called once the content has been retrieved (or an error
	 *				  has occurred)
	 */
	public static void getAsynch(final String url, final WebAccessedCallback callback) {

		Thread t = new Thread(new Runnable() { public void run() {

			get(url, null, null, callback);
		}});

		t.start();
	}

	private static String bytesToString(byte[] byteArr) {
		return new String(byteArr, StandardCharsets.UTF_8);
	}

	/**
	 * Get a web resource synchronously, assuming that it is in UTF-8
	 * @param url  The url of the web resource
	 */
	public static String get(String url) {
		return bytesToString(getBytes(url));
	}

	/**
	 * Get a web resource in bytes synchronously (not assuming any encoding therefore!)
	 * @param url  The url of the web resource
	 */
	public static byte[] getBytes(String url) {
		return getPutPost(url, "", "GET", null, null);
	}

	/**
	 * Get a web resource in bytes synchronously (not assuming any encoding therefore!)
	 * @param url  The url of the web resource
	 */
	public static byte[] getBytes(String url, WebAccessedCallback callback) {
		return getPutPost(url, "", "GET", null, callback);
	}

	/**
	 * Get a web resource synchronously, assuming that it is in UTF-8
	 * @param url  The base url of the web resource (without parameters)
	 * @param parameters  The parameters that should be appended to the url
	 */
	public static String get(String url, Map<String, String> parameters) {
		return bytesToString(getPutPost(url + mapToUrlSuffix(parameters), "", "GET", null, null));
	}

	/**
	 * Get a web resource synchronously, assuming that it is in UTF-8
	 * @param url  The base url of the web resource (without parameters)
	 * @param parameters  The parameters that should be appended to the url
	 * @param extraHeaders  Extra header fields (and their values) that should be sent with the request
	 */
	public static String get(String url, Map<String, String> parameters, Map<String, String> extraHeaders) {
		return bytesToString(getPutPost(url + mapToUrlSuffix(parameters), "", "GET", extraHeaders, null));
	}

	/**
	 * Get a web resource synchronously, assuming that it is in UTF-8
	 * @param url  The base url of the web resource (without parameters)
	 * @param parameters  The parameters that should be appended to the url
	 * @param extraHeaders  Extra header fields (and their values) that should be sent with the request
	 * @param callback  A callback which will receive information about the result of the call
	 */
	public static String get(String url, Map<String, String> parameters, Map<String, String> extraHeaders,
		WebAccessedCallback callback) {

		String content = bytesToString(getPutPost(url + mapToUrlSuffix(parameters), "", "GET", extraHeaders, callback));

		if (callback != null) {
			if ((content == null) || (content.length() < 1)) {
				callback.gotError();
			} else {
				callback.gotContent(content);
			}
		}

		return content;
	}

	/**
	 * Put a web resource synchronously, assuming that the result is in UTF-8
	 * @param url  The url of the web resource
	 * @param messageBody The message body to be sent
	 */
	public static String put(String url, String messageBody) {
		return bytesToString(getPutPost(url, messageBody, "PUT", null, null));
	}

	/**
	 * Put a web resource synchronously, assuming that the result is in UTF-8
	 * @param url  The url of the web resource
	 * @param parameters The parameters to be sent as message body
	 */
	public static String put(String url, Map<String, String> parameters) {
		return bytesToString(getPutPost(url, mapToMessageBody(parameters), "PUT", null, null));
	}

	/**
	 * Post a web resource synchronously, assuming that the result is in UTF-8
	 * @param url  The url of the web resource
	 * @param messageBody The message body to be sent
	 */
	public static String post(String url, String messageBody) {
		return bytesToString(getPutPost(url, messageBody, "POST", null, null));
	}

	/**
	 * Post a web resource synchronously, assuming that the result is in UTF-8
	 * @param url  The url of the web resource
	 * @param parameters  The parameters to be sent as message body
	 */
	public static String post(String url, Map<String, String> parameters) {
		return bytesToString(getPutPost(url, mapToMessageBody(parameters), "POST", null, null));
	}

	public static String postJson(String url, String messageBody) {
		Map<String, String> extraHeaders = new HashMap<>();
		extraHeaders.put("Content-Type", "application/json; charset=utf-8");
		return bytesToString(getPutPost(url, messageBody, "POST", extraHeaders, null));
	}

	/**
	 * Post a web resource synchronously, assuming that the result is in UTF-8
	 * @param url  The url of the web resource
	 * @param messageBody  The message body to be sent
	 * @param extraHeaders  Further headers and their data to be sent
	 */
	public static String post(String url, String messageBody, Map<String, String> extraHeaders) {
		return bytesToString(getPutPost(url, messageBody, "POST", extraHeaders, null));
	}

	/**
	 * Download a file from a server with given URL and port, authenticating using a plaintext password,
	 * from a given path on the server to a local target file (which will be overwritten if it already
	 * exists before calling this method)
	 *
	 * Returns true if this succeeded, false otherwise
	 */
	public static boolean downloadFtp(String url, int port, String username, String password,
		String pathOnServer, File localTarget) {

		try {
			String fullUrlForFtpTransaction =
				"ftp://" + username + ":" + password + "@" + url + ":" + port + pathOnServer;

			URL urlAsURL = new URL(fullUrlForFtpTransaction);

			URLConnection connection = urlAsURL.openConnection();
			InputStream inputStream = connection.getInputStream();
			Files.copy(inputStream, localTarget.getJavaPath());
			inputStream.close();

			return localTarget.exists();

		} catch (IOException e) {
			return false;
		}
	}

	private static String mapToUrlSuffix(Map<String, String> parameters) {

		String result = mapToMessageBody(parameters);

		if (result.length() > 0) {
			return "?" + result;
		}

		return "";
	}

	private static String mapToMessageBody(Map<String, String> parameters) {

		if (parameters == null) {
			return "";
		}

		StringBuilder result = new StringBuilder();

		String separator = "";

		for (Map.Entry<String, String> parameter : parameters.entrySet()) {
			result.append(separator);
			separator = "&";
			result.append(UrlEncoder.encodeFormData(parameter.getKey()));
			result.append("=");
			result.append(UrlEncoder.encodeFormData(parameter.getValue()));
		}

		return result.toString();
	}

	private static byte[] getPutPost(String url, String messageBody, String requestKind, Map<String, String> extraHeaders,
		WebAccessedCallback callback) {

		try {
			WebAccessor.lastUrl = url;

			if (url.startsWith("//")) {
				url = "https://" + url.substring(2);
			}

			if ((!url.startsWith("http://")) && (!url.startsWith("https://"))) {
				url = "http://" + url;
			}

			URL urlAsURL = new URL(url);

			HttpURLConnection connection = (HttpURLConnection) urlAsURL.openConnection();

			connection.setRequestMethod(requestKind);

			// pretend to be a reasonable browser; in this case: firefox; see:
			// https://stackoverflow.com/questions/13670692/403-forbidden-with-java-but-not-web-browser
			// https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/User-Agent/Firefox
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:10.0) Gecko/20100101 Firefox/10.0");

			if (extraHeaders != null) {
				for (Map.Entry<String, String> extraHeader : extraHeaders.entrySet()) {
					connection.setRequestProperty(extraHeader.getKey(), extraHeader.getValue());
				}
			}

			if (!("GET".equals(requestKind) || "HEAD".equals(requestKind))) {

				connection.setDoOutput(true);
				/*
				// we could url-encode the message body, but in many cases - e.g. content type
				// application/json; charset=utf-8 - we do not want to url-encode it, we just
				// send the plain UTF-8...
				String postData = URLEncoder.encode(messageBody, "UTF-8");
				byte[] postDataBytes = postData.toString().getBytes("UTF-8");
				// connection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
				connection.getOutputStream().write(postDataBytes);
				*/

				/*
				OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
				out.write(messageBody);
				out.close();
				*/

				DataOutputStream out = new DataOutputStream(connection.getOutputStream());
				out.write(messageBody.getBytes(StandardCharsets.UTF_8));
				out.close();
			}

			connection.connect();

			if (callback != null) {
				callback.gotResponseCode(connection.getResponseCode());
			}

			BufferedInputStream reader = null;
			int resCode = connection.getResponseCode();
			if ((resCode >= 100) && (resCode < 400)) {
				reader = new BufferedInputStream(connection.getInputStream());
			} else {
				// TODO :: throw an exception with the content of this? or in some other way notify that this is not cool!
				reader = new BufferedInputStream(connection.getErrorStream());
			}

			// we could here do readLine() on an actual BufferedReader, and read for each line
			// in that case we would need to manually append line endings such as \n or \r\n though
			// sadly, the buffered reader does not discriminate, so we cannot reconstruct whether
			// a particular line ended in \n or in \r\n (and YES, there are files which must contain
			// both in different locations, such as JPEG image files!)
			ByteBuffer buffer = new ByteBuffer();

			while (true) {

				// nextByte is an int between 0 and 255 usually,
				// but will be set to -1 to indicate the stream
				// being fully consumed
				int ap = reader.read();

				if (ap < 0) {
					break;
				}

				buffer.append((byte) ap);
			}

			reader.close();

			return buffer.toArray();

		} catch (IOException e) {
			System.out.println("There was an IOException in get for " + url + "\n" + e);
		}

		return new byte[0];
	}

	/**
	 * Clears the cache that can be built up e.g. through calling getFile()
	 */
	public static void clearCache() {

		cache.delete();
	}

	/**
	 * Get a web resource directly as File object
	 * (this is a virtual file only though - its name will not resolve to a path on the disk,
	 * and saving it will do nothing, but you can read its contents and convert it to a
	 * different one if need be)
	 * @param url  The URL that hopefully returns a file
	 * @return The file that has been found on the web
	 */
	public static File getFile(String url) {

		String resultName = url;

		if (resultName.contains("?")) {
			resultName = resultName.substring(0, resultName.indexOf("?"));
		}

		if (resultName.contains("&")) {
			resultName = resultName.substring(0, resultName.indexOf("&"));
		}

		if (resultName.contains("/")) {
			resultName = resultName.substring(resultName.lastIndexOf("/") + 1);
		}

		String resultExt = "";

		if (resultName.contains(".")) {
			resultExt = resultName.substring(resultName.lastIndexOf(".") + 1);
			resultName = resultName.substring(0, resultName.lastIndexOf("."));
		}

		resultName += "_" + StrUtils.getRandomString(8);
		resultName += "." + resultExt;

		BinaryFile result = new BinaryFile(cache, resultName);

		result.saveContent(getBytes(url));

		return result;
	}

	/**
	 * Get a local or web resource directly as File object
	 * @param pathOrUrl  The path or URL that hopefully returns a file
	 * @return The file that has been found on the web
	 */
	public static File getLocalOrWebFile(String pathOrUrl) {

		if (pathOrUrl.contains("://") || pathOrUrl.startsWith("localhost:") || pathOrUrl.startsWith("localhost/")) {
			return getFile(pathOrUrl);
		}

		return new File(pathOrUrl);
	}

	/**
	 * Get a web resource directly as JSON object
	 * @param url  The URL that hopefully returns a JSON resource
	 * @return The JSON resource that has been found on the web
	 */
	public static JSON getJSON(String url) throws JsonParseException {
		return new JSON(get(url));
	}

	/**
	 * Gets the last accessed URL (intended for debug usage to include the last accessed URL in error messages
	 * and such; as this whole class is static, if some other thread uses the WebAccessor in between, you will
	 * get the URL of that access, and not of "your" last call!)
	 */
	public static String getLastUrl() {
		return lastUrl;
	}

}
