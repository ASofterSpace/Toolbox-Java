package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.coders.Base64Decoder;
import com.asofterspace.toolbox.coders.Base64Encoder;
import com.asofterspace.toolbox.coders.HexEncoder;
import com.asofterspace.toolbox.test.Test;
import com.asofterspace.toolbox.test.TestUtils;


public class ConverterTest implements Test {

	@Override
	public void runAll() {

		// do the same test three times to ensure that all three ways of base64 cutoff are handled correctly
		base64HexCrossTest(1);
		base64HexCrossTest(2);
		base64HexCrossTest(3);
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

}
