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

    public final static char[] ECORE_UUID_LETTERS = new char[] {
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
		'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
		'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_'};

	public final static char[] JAVA_UUID_LETTERS = new char[] {
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'a', 'b', 'c', 'd', 'e', 'f'};

	public static enum UuidKind {JAVA, ECORE};

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

	public static UuidKind detectUUIDkind(String uuid) {

		// check for default / Java UUID - without hyphens, it should be 32 characters long...
		String javaUUID = uuid.replaceAll("-", "");
		if (javaUUID.length() == 32) {
			return UuidKind.JAVA;
		}

		// check for Ecore UUID - it should be 22 characters long, plus an underscore at the beginning
		// if we lower our requirements a bit, then the underscore in the beginning is optional,
		// and some equals signs could come in the end (it at all, then usually two)
		String ecoreUUID = uuid.replaceAll("=", "");
		if (ecoreUUID.length() == 22) {
			return UuidKind.ECORE;
		}
		if ((ecoreUUID.length() == 23) && (ecoreUUID.startsWith("_"))) {
			return UuidKind.ECORE;
		}

		// this "UUID" is not of any kind we've ever heard of!
		return null;
	}

	public static String prettifyAnyUUID(String uuid) {

		UuidKind kind = detectUUIDkind(uuid);

		// use ifs instead of a switch, as overall it is just as short in this case PLUS we don't have to explicitly check for null! :D
		if (kind == UuidKind.JAVA) {
			return prettifyJavaUUID(uuid);
		}
		if (kind == UuidKind.ECORE) {
			return prettifyEcoreUUID(uuid);
		}

		// if the UUID kind could not be detected, just return the original garbled mess that came in... =P
		return uuid;
	}

	public static String prettifyJavaUUID(String uuid) {

		// remove potentially wrong hyphens
		String result = uuid.replaceAll("-", "");

		// fill up missing digits - should not usually be many,
		// so editing String instead of using StringBuilder seems reasonable
		while (result.length() < 32) {
			result = "0" + result;
		}

		// Java UUIDs are in lower case
		result = result.toLowerCase();

		// transform from XXXXXXXXXXXXX...XXXXX
		// to XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX
		return result.substring(0, 8) + "-" +
		       result.substring(8, 12) + "-" +
		       result.substring(12, 16) + "-" +
		       result.substring(16, 20) + "-" +
		       result.substring(20, 32);
	}

	public static String prettifyEcoreUUID(String uuid) {

		// remove potential base64 conversion artifacts
		String result = uuid.replaceAll("=", "");

		// ensure the result starts with an underscore
		if (result.length() == 22) {
			result = "_" + result;
		}

		return result;
	}

	public static String convertJavaUUIDtoEcore(String sourceUUID) {

		if (sourceUUID == null) {
			return null;
		}

		return convertJavaUUIDtoEcore(UUID.fromString(sourceUUID));
	}

	public static String convertJavaUUIDtoEcore(UUID sourceUUID) {

		if (sourceUUID == null) {
			return null;
		}

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
		// we could instead also call prettifyEcoreUUID, but this line here is probably a tiny bit quicker :)
		String ecoreUUID = "_" + base64UUID.substring(0, 22);

		return ecoreUUID;
	}

	public static String convertEcoreUUIDtoJava(String sourceUUID) {
	
		if (sourceUUID == null) {
			return null;
		}

		// remove starting underscrore - should be there for an ecore UUID
		if ((sourceUUID.length() == 23) && (sourceUUID.startsWith("_"))) {
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

		String result = Base64Decoder.decodeFromBase64ToHexStr(sourceUUID, ECORE_UUID_LETTERS, JAVA_UUID_LETTERS);

		return prettifyJavaUUID(result);
	}

	/**
	 * Takes in any kind of UUID
	 * Returns the UUID converted to Java (if it was not Java before) and prettified
	 * Throws exception if it the hot garbled mess that came in could not be recognized ;)
	 */
	public static String ensureUUIDisJava(String uuid) throws ConversionException {

		UuidKind kind = detectUUIDkind(uuid);

		if (UuidKind.JAVA.equals(kind)) {
			return UuidEncoderDecoder.prettifyJavaUUID(uuid);
		}

		if (UuidKind.ECORE.equals(kind)) {
			return UuidEncoderDecoder.convertEcoreUUIDtoJava(uuid);
		}

		throw new ConversionException("The UUID '" + uuid + "' that you wanted me to convert to Java cannot be recognized, sorry.");
	}

	/**
	 * Takes in any kind of UUID
	 * Returns the UUID converted to Ecore (if it was not Ecore before) and prettified
	 * Throws exception if it the hot garbled mess that came in could not be recognized ;)
	 */
	public static String ensureUUIDisEcore(String uuid) throws ConversionException {

		UuidKind kind = detectUUIDkind(uuid);

		if (UuidKind.JAVA.equals(kind)) {
			return UuidEncoderDecoder.convertJavaUUIDtoEcore(uuid);
		}

		if (UuidKind.ECORE.equals(kind)) {
			return UuidEncoderDecoder.prettifyEcoreUUID(uuid);
		}

		throw new ConversionException("The UUID '" + uuid + "' that you wanted me to convert to Ecore cannot be recognized, sorry.");
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
