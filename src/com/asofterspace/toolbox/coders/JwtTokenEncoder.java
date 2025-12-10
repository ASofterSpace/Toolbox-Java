/**
 * Unlicensed code created by A Softer Space, 2025
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.coders;

import com.asofterspace.toolbox.io.JSON;
import com.asofterspace.toolbox.utils.Record;
import com.asofterspace.toolbox.utils.StrUtils;


/**
 * A class that can encode a jwt token (for now just very rudimentary though without security features)
 *
 * @author Moya (a softer space, 2025)
 */
public class JwtTokenEncoder {

	// encode an arbitrary string payload
	public static String encode(String payload) {

		String header = "{\"alg\":\"none\"}";

		return base64encCompressed(header) + '.' + base64encCompressed(payload) + '.';
	}

	// encode a Record as JSON payload
	public static String encodeRecord(Record payload) {

		boolean compressed = true;
		String linePrefix = "";
		JSON jsonHelper = new JSON();
		String payloadStr = jsonHelper.toString(payload, compressed, linePrefix);

		return encode(payloadStr);
	}

	private static String base64encCompressed(String str) {
		str = Base64Encoder.encode(str);
		str = StrUtils.replaceAll(str, "\r", "");
		str = StrUtils.replaceAll(str, "\n", "");
		str = StrUtils.replaceAll(str, "=", "");
		return str;
	}

}
