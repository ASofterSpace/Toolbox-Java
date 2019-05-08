/**
 * Unlicensed code created by A Softer Space, 2017
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.web;

import com.asofterspace.toolbox.coders.UrlDecoder;
import com.asofterspace.toolbox.coders.UrlEncoder;
import com.asofterspace.toolbox.io.BinaryFile;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.JSON;
import com.asofterspace.toolbox.utils.ByteBuffer;
import com.asofterspace.toolbox.Utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;


/**
 * This (hopefully) simplifies access to the web
 *
 * @author Moya (a softer space, 2017)
 */
public class WebAccessor {

	private static Directory cache = new Directory("cache");


	/**
	 * Get a web resource asynchronously
	 * @param url  The url of the web resource
	 * @param callback  The callback that is called once the content has been retrieved (or an error
	 *				  has occurred)
	 */
	public static void getAsynch(final String url, final WebAccessedCallback callback) {

		Thread t = new Thread(new Runnable() { public void run() {

			String content = get(url);

			if ((content == null) || (content.length() < 1)) {
				callback.gotError();
			} else {
				callback.gotContent(content);
			}
		}});

		t.start();
	}

	/**
	 * Get a web resource synchronously
	 * @param url  The url of the web resource
	 */
	public static String get(String url) {
		return getPutPost(url, "", "GET", null).toString();
	}

	/**
	 * Get a web resource in bytes synchronously
	 * @param url  The url of the web resource
	 */
	public static byte[] getBytes(String url) {
		return getPutPost(url, "", "GET", null).toArray();
	}

	/**
	 * Get a web resource synchronously
	 * @param url  The base url of the web resource (without parameters)
	 * @param parameters  The parameters that should be appended to the url
	 */
	public static String get(String url, Map<String, String> parameters) {
		return getPutPost(url + mapToUrlSuffix(parameters), "", "GET", null).toString();
	}

	/**
	 * Get a web resource synchronously
	 * @param url  The base url of the web resource (without parameters)
	 * @param parameters  The parameters that should be appended to the url
	 * @param extraHeaders  Extra header fields (and their values) that should be sent with the request
	 */
	public static String get(String url, Map<String, String> parameters, Map<String, String> extraHeaders) {
		return getPutPost(url + mapToUrlSuffix(parameters), "", "GET", extraHeaders).toString();
	}

	/**
	 * Put a web resource synchronously
	 * @param url  The url of the web resource
	 * @param messageBody The message body to be sent
	 */
	public static String put(String url, String messageBody) {
		return getPutPost(url, messageBody, "PUT", null).toString();
	}

	/**
	 * Put a web resource synchronously
	 * @param url  The url of the web resource
	 * @param parameters The parameters to be sent as message body
	 */
	public static String put(String url, Map<String, String> parameters) {
		return getPutPost(url, mapToMessageBody(parameters), "PUT", null).toString();
	}

	/**
	 * Post a web resource synchronously
	 * @param url  The url of the web resource
	 * @param messageBody The message body to be sent
	 */
	public static String post(String url, String messageBody) {
		return getPutPost(url, messageBody, "POST", null).toString();
	}

	/**
	 * Post a web resource synchronously
	 * @param url  The url of the web resource
	 * @param parameters The parameters to be sent as message body
	 */
	public static String post(String url, Map<String, String> parameters) {
		return getPutPost(url, mapToMessageBody(parameters), "POST", null).toString();
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

	private static ByteBuffer getPutPost(String url, String messageBody, String requestKind, Map<String, String> extraHeaders) {

		try {
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
				String postData = URLEncoder.encode(messageBody, "UTF-8");
				byte[] postDataBytes = postData.toString().getBytes("UTF-8");

				connection.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
				connection.getOutputStream().write(postDataBytes);
				*/
				OutputStreamWriter out = new OutputStreamWriter(
						connection.getOutputStream());
				out.write(messageBody);
				out.close();
			}

			connection.connect();

			BufferedInputStream reader = new BufferedInputStream(connection.getInputStream());

			// we could here do readLine() on an actual BufferedReader, and read for each line
			// in that case we would need to manually append line endings such as \n or \r\n though
			// sadly, the buffered reader does not discrimiate, so we cannot reconstruct whether
			// a particular line ended in \n or in \r\n (and YES, there are files which must contain
			// most in different locations, such as JPEG image files!)
			ByteBuffer buffer = new ByteBuffer();

			while (reader.available() > 0) {
				int ap = reader.read();
				buffer.append((byte) ap);
			}

			reader.close();

			return buffer;

		} catch (IOException e) {
			System.out.println("There was an IOException in get for " + url + "\n" + e);
			return new ByteBuffer();
		}
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

		resultName += "_" + Utils.getRandomString(8);
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
	public static JSON getJSON(String url) {
		return new JSON(get(url));
	}

}
