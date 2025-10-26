/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.codeeditor.base.Code;
import com.asofterspace.toolbox.coders.UrlDecoder;
import com.asofterspace.toolbox.coders.UrlEncoder;
import com.asofterspace.toolbox.io.JsonParseException;
import com.asofterspace.toolbox.test.Test;
import com.asofterspace.toolbox.test.TestUtils;


public class CoderTest implements Test {

	@Override
	public void runAll() {

		urlEncoderDecoderTest();
	}

	public void urlEncoderDecoderTest() throws JsonParseException {

		TestUtils.start("URL Encoder / Decoder Test");

		String input = "http://www.foo.org/sections/bar bob";
		String encoded = UrlEncoder.encode(input);
		String encodedPath = UrlEncoder.encodePath(input);
		String encodedFormData = UrlEncoder.encodeFormData(input);
		String decoded = UrlDecoder.decode(encoded);
		String decodedPath = UrlDecoder.decode(encodedPath);
		String decodedFormData = UrlDecoder.decode(encodedFormData);

		if (!"http%3A%2F%2Fwww.foo.org%2Fsections%2Fbar%20bob".equals(encoded) {
			TestUtils.fail("URL encoding unexpected: " + input " encoded to: " + encoded);
			return;
		}
		if (!"http://www.foo.org/sections/bar%20bob".equals(encodedPath) {
			TestUtils.fail("URL path encoding unexpected: " + input " encoded to: " + encodedPath);
			return;
		}
		if (!"http%3A%2F%2Fwww.foo.org%2Fsections%2Fbar+bob".equals(encodedFormData) {
			TestUtils.fail("URL form data encoding unexpected: " + input " encoded to: " + encodedFormData);
			return;
		}
		if (!input.equals(decoded) {
			TestUtils.fail("URL decoding unexpected: " + input " decoded to: " + decoded);
			return;
		}
		if (!input.equals(decodedPath) {
			TestUtils.fail("URL path decoding unexpected: " + input " decoded to: " + decodedPath);
			return;
		}
		if (!input.equals(decodedFormData) {
			TestUtils.fail("URL form data decoding unexpected: " + input " decoded to: " + decodedFormData);
			return;
		}
		TestUtils.succeed();
	}

}
