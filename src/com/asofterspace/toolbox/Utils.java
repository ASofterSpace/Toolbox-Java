package com.asofterspace.toolbox;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class Utils {

	public final static int TOOLBOX_VERSION_NUMBER = 8;

    private static final char[] ECORE_UUID_LETTERS = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'};


	public static String generateEcoreUUID() {

		// start with a real UUID!
        UUID sourceUUID = UUID.randomUUID();
		
		// convert the UUID to base 64
		ByteBuffer sourceByteBuf = ByteBuffer.wrap(new byte[16]);
		sourceByteBuf.putLong(sourceUUID.getMostSignificantBits());
		sourceByteBuf.putLong(sourceUUID.getLeastSignificantBits());

        byte[] sourceBytes = sourceByteBuf.array();
		
		String base64UUID = Base64.getUrlEncoder().encodeToString(sourceBytes);
		
		// add starting underscore and remove trailing ==
		String ecoreUUID = "_" + base64UUID.substring(0, 22);

        return ecoreUUID;
	}

	public static String strListToString(List<String> stringList) {

		StringBuilder sb = new StringBuilder();

		if (stringList != null) {
			for (String jsonStr : stringList) {
				sb.append(jsonStr);
			}
		}

		return sb.toString();
	}
}
