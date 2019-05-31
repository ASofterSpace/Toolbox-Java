/**
 * Unlicensed code created by A Softer Space, 2018
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.coders.Base64Decoder;
import com.asofterspace.toolbox.coders.Base64Encoder;
import com.asofterspace.toolbox.coders.HexDecoder;
import com.asofterspace.toolbox.coders.HexEncoder;
import com.asofterspace.toolbox.test.Test;
import com.asofterspace.toolbox.test.TestUtils;

import java.math.BigInteger;


public class ConverterTest implements Test {

	@Override
	public void runAll() {

		// do the same test three times to ensure that all three ways of base64 cutoff are handled correctly
		base64HexCrossTest(1);
		base64HexCrossTest(2);
		base64HexCrossTest(3);

		hexStrDecoderTest();

		hexNumberDecoderTest();
	}

	public void base64HexCrossTest(int testnum) {

		TestUtils.start("Base64 Hex Cross " + testnum);

		String origString = "This is our example string...";

		for (int i = 0; i < testnum; i++) {
			origString += ".";
		}

		String input = Base64Encoder.encodeIntoBase64(origString);

		String inTobase64ToHex = Base64Decoder.decodeFromBase64ToHexStr(input);

		String inToBase64 = Base64Decoder.decodeFromBase64(input);

		String base64ToHex = HexEncoder.encodeStringToHex(inToBase64);

		if (!inTobase64ToHex.equals(base64ToHex)) {
			TestUtils.fail("We tried to decode the same base64 text directly to hex and via base64 decoding to string and from there to hex, but the result was different!");
			return;
		}

		TestUtils.succeed();
	}

	public void hexStrDecoderTest() {

		TestUtils.start("Hex String Decoder");

		String input = "0x57 0x65 20 77 61 6E 74 20 74 6F 20 67 6F 20 74 6F204D61727321203A29";

		String output = HexDecoder.decodeStringFromHex(input);

		if (!output.equals("We want to go to Mars! :)")) {
			TestUtils.fail("We tried to decode a well-known hex string but got \"" + output + "\"!");
			return;
		}

		TestUtils.succeed();
	}

	public void hexNumberDecoderTest() {

		TestUtils.start("Hex Number Decoder");

		String input = "6B06";

		BigInteger output = HexDecoder.decodeNumberFromHex(input);

		int intOutput = output.intValue();

		if (intOutput != 27398) {
			TestUtils.fail("We tried to decode a well-known hex number but got " + output + "!");
			return;
		}

		TestUtils.succeed();
	}

}
