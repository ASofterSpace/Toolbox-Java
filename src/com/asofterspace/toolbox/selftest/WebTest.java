/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.images.DefaultImageFile;
import com.asofterspace.toolbox.images.Image;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.JSON;
import com.asofterspace.toolbox.io.JsonParseException;
import com.asofterspace.toolbox.test.Test;
import com.asofterspace.toolbox.test.TestUtils;
import com.asofterspace.toolbox.web.WebAccessor;
import com.asofterspace.toolbox.web.WebServer;


public class WebTest implements Test {

	private WebServer server;


	@Override
	public void runAll() {

		clearEverything();

		startServer();

		communicateWithServerTest();

		getAdvancedJsonFromServerTest();

		getFileTest();

		stopServer();
	}

	public void clearEverything() {

		WebAccessor.clearCache();
	}

	public void startServer() {

		this.server = new WebServer(new Directory(AllTests.TEST_DATA_PATH), 8081);

		server.serveAsync();
	}

	public void communicateWithServerTest() {

		TestUtils.start("Communicate with Server");

		server.addToWhitelist("json/simple.json");

		String result = WebAccessor.get("http://localhost:8081/json/simple.json");

		if (!"{\"foo\": \"bar\"}".equals(result)) {
			TestUtils.fail("We tried to request a simple JSON file from our own server... and did not get it!");
			return;
		}

		TestUtils.succeed();
	}

	public void getAdvancedJsonFromServerTest() {

		TestUtils.start("Get Advanced JSON from Server");

		server.addToWhitelist("json/advanced.json");

		String result = WebAccessor.get("http://localhost:8081/json/advanced.json");

		try {
			JSON jsonContent = new JSON(result);

			if (!jsonContent.getString("foo").equals("")) {
				TestUtils.fail("We stored {\"foo\": \"\"} in an advanced JSON file, then read the file - and did not get an empty string when querying for foo!");
				return;
			}

			if (!jsonContent.getString("bar").equals("\"")) {
				TestUtils.fail("We stored {\"bar\": \"\\\"\"} in a JSON file, then read the file - and did not get \" when querying for bar!");
				return;
			}

			if (!jsonContent.get("blu").asString().equals("blubb")) {
				TestUtils.fail("We stored {'blu': \"blubb\"} in a JSON file, then read the file - and did not get blubb when querying for blu!");
				return;
			}

			if (!jsonContent.getString("newline").equals("\n")) {
				TestUtils.fail("We stored {\"newline\": \"\\n\"} in an advanced JSON file, then read the file - and did not get \\n when querying for newline!");
				return;
			}

			if (!jsonContent.getString("leftout").equals("the Gänsefüßchen")) {
				TestUtils.fail("We stored {leftout: 'the Gänsefüßchen'} in a JSON file, then read the file - and did not get the Gänsefüßchen when querying for leftout!");
				return;
			}

			if (!jsonContent.getBoolean("otherbool").equals(false)) {
				TestUtils.fail("We stored {\"otherbool\": false} in a JSON file, then read the file - and did not get false when querying for otherbool!");
				return;
			}

			if (!jsonContent.getString("possible").equals("right?")) {
				TestUtils.fail("We stored {\"possible\": \"right?\"} in an advanced JSON file, then read the file - and did not get right? when querying for possible!");
				return;
			}
		} catch (JsonParseException e) {
			TestUtils.fail("We stored stuff in a JSON file, then read it over the web, but it could not be parsed: " + e);
			return;
		}

		TestUtils.succeed();
	}

	public void getFileTest() {

		TestUtils.start("Get File from Server");

		DefaultImageFile origImageFile = new DefaultImageFile(AllTests.IMAGE_TEST_DATA_PATH + "/qrtest_large_automask.png");

		Image origImage = origImageFile.getImage();

		server.addToWhitelist("images/qrtest_large_automask.png");

		// ensure that extra nonsense is stripped when the file is retrieved
		File gotFile = WebAccessor.getFile("http://localhost:8081/images/qrtest_large_automask.png?bla=foo&bar=blobb");

		DefaultImageFile gotImageFile = new DefaultImageFile(gotFile);

		Image gotImage = gotImageFile.getImage();

		if (!origImage.equals(gotImage)) {
			TestUtils.fail("We tried to request an image file from our own server... and did not get it!");
			return;
		}

		TestUtils.succeed();
	}

	public void stopServer() {

		server.stop();
	}

}
