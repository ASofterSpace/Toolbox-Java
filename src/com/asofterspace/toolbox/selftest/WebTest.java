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
import com.asofterspace.toolbox.utils.Record;
import com.asofterspace.toolbox.web.WebAccessor;
import com.asofterspace.toolbox.web.WebExtractor;
import com.asofterspace.toolbox.web.WebServer;

import java.util.ArrayList;
import java.util.List;


public class WebTest implements Test {

	private WebServer server;


	@Override
	public void runAll() {

		clearEverything();

		startServer();

		communicateWithServerTest();

		getAdvancedJsonFromServerTest();

		getUtf8JsonFromServerTest();

		sendJsonToServerTest();

		sendUtf8JsonToServerTest();

		getFileTest();

		stopServer();


		extractDataTest();

		getNumberFromHtmlTest();

		getHighestNumberFromHtmlTest();

		getHighestNumberFromHtmlByListTest();

		extractJsonDictTest();

		removeHtmlTagsFromTextTest();
	}

	public void clearEverything() {

		WebAccessor.clearCache();
	}

	public void startServer() {

		this.server = new WebTestServer(new Directory(AllTests.TEST_DATA_PATH), 8081);

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

	public void getUtf8JsonFromServerTest() {

		TestUtils.start("Get UTF8 JSON from Server");

		server.addToWhitelist("json/utf8.json");

		String result = WebAccessor.get("http://localhost:8081/json/utf8.json");

		try {
			JSON jsonContent = new JSON(result);

			if (!jsonContent.getString("foo").equals("россияне")) {
				TestUtils.fail("We stored {\"foo\": \"россияне\"} in a JSON file, then read the file - and did not get россияне when querying for foo!");
				return;
			}
		} catch (JsonParseException e) {
			TestUtils.fail("We stored stuff in a JSON file, then read it over the web, but it could not be parsed: " + e);
			return;
		}

		TestUtils.succeed();
	}

	public void sendJsonToServerTest() {

		TestUtils.start("Send JSON to Server");

		String result = WebAccessor.postJson("http://localhost:8081/post", "{\"foo\": \"bar\"}");

		if (!WebTestServerRequestHandler.lastPostContentStr.equals("{\"foo\": \"bar\"}")) {
			TestUtils.fail("We sent a POST and the POST data did not arrive, instead it was: '" +
				WebTestServerRequestHandler.lastPostContentStr + "'");
		}

		if (!WebTestServerRequestHandler.lastPostContentJSON.getString("foo").equals("bar")) {
			TestUtils.fail("We sent a POST and the POST data did not arrive, instead it was: '" +
				WebTestServerRequestHandler.lastPostContentJSON + "'");
		}

		TestUtils.succeed();
	}

	public void sendUtf8JsonToServerTest() {

		TestUtils.start("Send UTF8 JSON to Server");

		String result = WebAccessor.postJson("http://localhost:8081/post", "{\"foo\": \"россияне\", \"others\": \"’\"}");

		if (!WebTestServerRequestHandler.lastPostContentStr.equals("{\"foo\": \"россияне\", \"others\": \"’\"}")) {
			TestUtils.fail("We sent a POST and the POST data did not arrive, instead it was: '" +
				WebTestServerRequestHandler.lastPostContentStr + "'");
		}

		if (!WebTestServerRequestHandler.lastPostContentJSON.getString("foo").equals("россияне")) {
			TestUtils.fail("We sent a POST and the POST data did not arrive, instead it was: '" +
				WebTestServerRequestHandler.lastPostContentJSON + "'");
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

	public void extractDataTest() {

		TestUtils.start("Extract Data");

		String html = "<html>\nbla blubb <a href='boink'>\n</html>";

		String result = WebExtractor.extract(html, "<a href='", "'");

		if (!"boink".equals(result)) {
			TestUtils.fail("We tried to extract 'boink' from an html string but got '" + result + "' instead!");
			return;
		}

		TestUtils.succeed();
	}

	public void getNumberFromHtmlTest() {

		TestUtils.start("Get Number from HTML");

		String html = "<html>\nbla blubb <a href='boink'>\n" +
			"<div class=\"bla1\">bla</div>\n" +
			"<div class=\"bla2\">bli</div>\n" +
			"<div class=\"bla4\">blö</div>\n" +
			"<div class=\"bla3\">blu</div>\n" +
			"</html>";

		Integer result = WebExtractor.getNumberFromHtml(html, "<div class=\"bla", "\"");

		if ((result == null) || !result.equals(1)) {
			TestUtils.fail("We tried to extract the first number '1' from an html string " +
				"but got '" + result + "' instead!");
			return;
		}

		TestUtils.succeed();
	}

	public void getHighestNumberFromHtmlTest() {

		TestUtils.start("Get Highest Number from HTML");

		String html = "<html>\nbla blubb <a href='boink'>\n" +
			"<div class=\"bla1\">bla</div>\n" +
			"<div class=\"bla2\">bli</div>\n" +
			"<div class=\"bla4\">blö</div>\n" +
			"<div class=\"blakeks\">bly</div>\n" +
			"<div class=\"bla3\">blu</div>\n" +
			"</html>";

		Integer result = WebExtractor.getHighestNumberFromHtml(html, "<div class=\"bla", "\"");

		if ((result == null) || !result.equals(4)) {
			TestUtils.fail("We tried to extract the hightest number '4' from an html string " +
				"but got '" + result + "' instead!");
			return;
		}

		TestUtils.succeed();
	}

	public void getHighestNumberFromHtmlByListTest() {

		TestUtils.start("Get Highest Number from HTML by List");

		String html = "<html>\nbla blubb <a href='boink'>\n" +
			"<div class=\"bla1\">bla</div>\n" +
			"<div class=\"bla2\">bli</div>\n" +
			"<div class=\'bla4\'>blö</div>\n" +
			"<div class=\"bla3\">blu</div>\n" +
			"</html>";

		List<String> before = new ArrayList<>();
		before.add("<div class=\"bla");
		before.add("<div class='bla");
		List<String> after = new ArrayList<>();
		after.add("\"");
		after.add("'");
		Integer result = WebExtractor.getHighestNumberFromHtml(html, before, after);

		if ((result == null) || !result.equals(4)) {
			TestUtils.fail("We tried to extract the hightest number '4' from an html string " +
				"but got '" + result + "' instead!");
			return;
		}

		html = "<html>\nbla blubb <a href='boink'>\n" +
			"<div class=\"bla5\">bla</div>\n" +
			"<div class=\"bla2\">bli</div>\n" +
			"<div class=\'bla4\'>blö</div>\n" +
			"<div class=\"bla3\">blu</div>\n" +
			"</html>";

		result = WebExtractor.getHighestNumberFromHtml(html, before, after);

		if ((result == null) || !result.equals(5)) {
			TestUtils.fail("We tried to extract the hightest number '5' from an html string " +
				"but got '" + result + "' instead!");
			return;
		}

		TestUtils.succeed();
	}

	public void extractJsonDictTest() {

		TestUtils.start("Extract JSON Dict");

		String html = "<html>\nbla blubb <a href='boink'>\n" +
			"<script>\n" +
			"var bla = {\"foo\": \"b&auml;r\"}\n" +
			"</script>\n" +
			"</html>";

		Record rec = WebExtractor.extractJsonDict(html, "var bla = {", "}", true, null);

		if (!"bär".equals(rec.getString("foo"))) {
			TestUtils.fail("We tried to extract the entry 'bär' for key 'foo' from a JSON object in an html string " +
				"but got '" + rec.getString("foo") + "' instead!");
			return;
		}

		TestUtils.succeed();
	}

	public void removeHtmlTagsFromTextTest() {

		TestUtils.start("Remove HTML Tags from Text");

		String html = "<html>\nbla blubb <a href='boink'>\n" +
			"<script>\n" +
			"var bla = {\"foo\": \"b&auml;r\"}\n" +
			"</script>\n" +
			"</html>";

		String expectedResult = "\nbla blubb \n" +
			"\n" +
			"var bla = {\"foo\": \"b&auml;r\"}\n" +
			"\n";

		String result = WebExtractor.removeHtmlTagsFromText(html);

		if (!expectedResult.equals(result)) {
			TestUtils.fail("We tried to remove HTML tags from the first given string but got:\n" + result);
			return;
		}

		html = "<fine>blubbel<broken";

		expectedResult = "blubbel";

		result = WebExtractor.removeHtmlTagsFromText(html);

		if (!expectedResult.equals(result)) {
			TestUtils.fail("We tried to remove HTML tags from the second given string but got:\n" + result);
			return;
		}

		TestUtils.succeed();
	}

}
