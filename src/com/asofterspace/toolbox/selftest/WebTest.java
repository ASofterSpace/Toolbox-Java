/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.test.Test;
import com.asofterspace.toolbox.test.TestUtils;
import com.asofterspace.toolbox.web.WebAccessor;
import com.asofterspace.toolbox.web.WebServer;


public class WebTest implements Test {

	private WebServer server;


	@Override
	public void runAll() {

		startServer();

		communicateWithServerTest();

		stopServer();
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

	public void stopServer() {

		server.stop();
	}

}
