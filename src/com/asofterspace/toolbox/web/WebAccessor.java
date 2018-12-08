/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.web;

import com.asofterspace.toolbox.coders.UrlDecoder;
import com.asofterspace.toolbox.coders.UrlEncoder;
import com.asofterspace.toolbox.io.JSON;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

	/**
	 * Get a web resource asynchronously
	 * @param url  The url of the web resource
	 * @param callback  The callback that is called once the content has been retrieved (or an error
	 *                  has occurred)
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
		return getPutPost(url, "", "GET");
	}

	/**
	 * Get a web resource synchronously
	 * @param url  The base url of the web resource (without parameters)
	 * @param parameters  The parameters that should be appended to the url
	 */
	public static String get(String url, Map<String, String> parameters) {
		return getPutPost(url + mapToUrlSuffix(parameters), "", "GET");
	}

	/**
	 * Put a web resource synchronously
	 * @param url  The url of the web resource
	 * @param messageBody The message body to be sent
	 */
	public static String put(String url, String messageBody) {
		return getPutPost(url, messageBody, "PUT");
	}

	/**
	 * Put a web resource synchronously
	 * @param url  The url of the web resource
	 * @param parameters The parameters to be sent as message body
	 */
	public static String put(String url, Map<String, String> parameters) {
		return getPutPost(url, mapToMessageBody(parameters), "PUT");
	}

	/**
	 * Post a web resource synchronously
	 * @param url  The url of the web resource
	 * @param messageBody The message body to be sent
	 */
	public static String post(String url, String messageBody) {
		return getPutPost(url, messageBody, "POST");
	}

	/**
	 * Post a web resource synchronously
	 * @param url  The url of the web resource
	 * @param parameters The parameters to be sent as message body
	 */
	public static String post(String url, Map<String, String> parameters) {
		return getPutPost(url, mapToMessageBody(parameters), "POST");
	}
	
	private static String mapToUrlSuffix(Map<String, String> parameters) {
		
		String result = mapToMessageBody(parameters);
		
		if (result.length() > 0) {
			return "?" + result;
		}
		
		return "";
	}
	
	private static String mapToMessageBody(Map<String, String> parameters) {
	
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

	private static String getPutPost(String url, String messageBody, String requestKind) {

		try {
			URL urlAsURL = new URL(url);

			HttpURLConnection connection = (HttpURLConnection) urlAsURL.openConnection();

			connection.setRequestMethod(requestKind);

			// pretend to be a reasonable browser; in this case: firefox; see:
			// https://stackoverflow.com/questions/13670692/403-forbidden-with-java-but-not-web-browser
			// https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/User-Agent/Firefox
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:10.0) Gecko/20100101 Firefox/10.0");

			if ("GET".equals(requestKind)) {
			} else {
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

			InputStreamReader ireader = new InputStreamReader(connection.getInputStream());
			BufferedReader reader = new BufferedReader(ireader);

			String nextLine = reader.readLine();
			StringBuilder data = new StringBuilder();

			while (nextLine != null) {
				data.append(nextLine);
				nextLine = reader.readLine();
			}

			reader.close();

			return data.toString();

		} catch (IOException e) {
			System.out.println("There was an IOException in get for " + url + "\n" + e);
			return "";
		}
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
