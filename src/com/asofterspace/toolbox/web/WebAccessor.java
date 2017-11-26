package com.asofterspace.toolbox.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebAccessor {

	public static String get(String url) {
		
		try {
	
			URL urlAsURL = new URL(url);
			
			HttpURLConnection connection = (HttpURLConnection) urlAsURL.openConnection();
			
			connection.setRequestMethod("GET");
			
			// pretend to be a reasonable browser; in this case: firefox; see:
			// https://stackoverflow.com/questions/13670692/403-forbidden-with-java-but-not-web-browser
			// https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/User-Agent/Firefox
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:10.0) Gecko/20100101 Firefox/10.0");

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

	public static JSON getJSON(String url) {
		return new JSON(get(url));
	}
	
	/**
	 * Takes http%3A%2F%2Fwww.foo.org%2Fsections%2Fbar
	 * and converts to http://www.foo.org/sections/bar
	 * @param url
	 * @return
	 */
	public static String urldecode(String url) {

		// TODO :: improve!

		url = url.replace("&amp;", "&");
		url = url.replace("%3A", ":");
		url = url.replace("%3a", ":");
		url = url.replace("%2F", "/");
		url = url.replace("%2f", "/");
		url = url.replace("%3F", "?");
		url = url.replace("%3f", "?");
		url = url.replace("%3D", "=");
		url = url.replace("%3d", "=");
		url = url.replace("%26", "&");

		return url;
	}

}
