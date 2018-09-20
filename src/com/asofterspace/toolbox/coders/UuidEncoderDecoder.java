package com.asofterspace.toolbox.coders;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.UUID;


/**
 * A class that can decode UUIDs
 *
 * @author Moya (a softer space, 2018)
 */
public class UuidEncoderDecoder {

    private static final char[] ECORE_UUID_LETTERS = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'};

	public static String generateJavaUUID() {

		// start with a real UUID!
		UUID sourceUUID = UUID.randomUUID();

		// ... aaand that's it already xD
		return sourceUUID.toString();
	}
	
	public static String generateEcoreUUID() {

		// start with a real UUID!
		UUID sourceUUID = UUID.randomUUID();

		// ... aaand convert it to Ecore
		return convertJavaUUIDtoEcore(sourceUUID);
	}

	public static String convertJavaUUIDtoEcore(String sourceUUID) {

		return convertJavaUUIDtoEcore(UUID.fromString(sourceUUID));
	}

	public static String convertJavaUUIDtoEcore(UUID sourceUUID) {

		// convert the UUID to base 64
		ByteBuffer sourceByteBuf = ByteBuffer.wrap(new byte[16]);
		sourceByteBuf.putLong(sourceUUID.getMostSignificantBits());
		sourceByteBuf.putLong(sourceUUID.getLeastSignificantBits());

		byte[] sourceBytes = sourceByteBuf.array();

		/*
		// if we ever switch to Java 8, we can speed things up by using the built-in java.util.Base64:
		String base64UUID = Base64.getUrlEncoder().withoutPadding().encodeToString(sourceBytes);
		*/
		
		String base64UUID = Base64Encoder.encodeIntoBase64(sourceBytes, ECORE_UUID_LETTERS);

		// add starting underscore and remove trailing == if it was there (shouldn't be though)
		String ecoreUUID = "_" + base64UUID.substring(0, 22);

		return ecoreUUID;
	}

	public static String convertEcoreUUIDtoJava(String sourceUUID) {

		// remove starting underscrore - should be there for an ecore UUID
		if (sourceUUID.startsWith("_")) {
			sourceUUID = sourceUUID.substring(1);
		}

		/*
		// if we ever switch to Java 8, we can speed things up by using the built-in java.util.Base64:
		byte[] sourceBytes = Base64.getDecoder().decode(sourceUUID);

		StringBuilder resBuilder = new StringBuilder();

		for (byte sourceByte : sourceBytes) {
			resBuilder.append(String.format("%02x", sourceByte));
		}

		String result = resBuilder.toString();
		*/
		
		String result = Base64Decoder.decodeFromBase64(sourceUUID, ECORE_UUID_LETTERS);

		while (result.length() < 32) {
			result = "0" + result;
		}

		// transform from XXXXXXXXXXXXX...XXXXX
		// to XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX
		return result.substring(0, 8) + "-" +
		       result.substring(8, 12) + "-" +
		       result.substring(12, 16) + "-" +
		       result.substring(16, 20) + "-" +
		       result.substring(20, 32);
	}
	
	/**
	 * Takes in a link of the forms:
	 * file#id ,  #id  or  id
	 * And returns in each case:
	 * id
	 */
	public static String getIdFromEcoreLink(String link) {

		if (link == null) {
			return null;
		}
		
		if (link.contains("#")) {
			return link.substring(link.lastIndexOf("#") + 1);
		}
		
		return link;
	}

}
