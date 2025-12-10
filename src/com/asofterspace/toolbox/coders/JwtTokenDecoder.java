/**
 * Unlicensed code created by A Softer Space, 2025
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.coders;

import com.asofterspace.toolbox.io.JSON;
import com.asofterspace.toolbox.io.JsonParseException;
import com.asofterspace.toolbox.utils.Record;


/**
 * A class that can decode a jwt token (for now just very rudimentary though without security features)
 *
 * @author Moya (a softer space, 2025)
 */
public class JwtTokenDecoder {

	// decode an arbitrary string payload
	public static String decode(String token) {

		String header = "{\"alg\":\"none\"}";

		int index = token.indexOf(".");
		if (index >= 0) {
			String payloadPartOfToken = token.substring(index + 1);
			index = payloadPartOfToken.indexOf(".");
			if (index >= 0) {
				payloadPartOfToken = payloadPartOfToken.substring(0, index);
				return Base64Decoder.decode(payloadPartOfToken);
			}
		}

		return null;
	}

	// decode a token containing a JSON payload as Record
	public static Record decodeToRecord(String token) throws JsonParseException {

		String payload = decode(token);

		return new JSON(payload);
	}

}
