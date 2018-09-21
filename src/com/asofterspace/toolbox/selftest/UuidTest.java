package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.coders.ConversionException;
import com.asofterspace.toolbox.coders.UuidEncoderDecoder;
import com.asofterspace.toolbox.coders.UuidEncoderDecoder.UuidKind;
import com.asofterspace.toolbox.test.Test;
import com.asofterspace.toolbox.test.TestUtils;


public class UuidTest implements Test {

	@Override
	public void runAll() {

		generateUuidTest();

		prettifyUuidTest();

		ensureUuidTest();
	}

	public void generateUuidTest() {

		TestUtils.start("Generate UUID");

		String uuid = UuidEncoderDecoder.generateJavaUUID();

		UuidKind kind = UuidEncoderDecoder.detectUUIDkind(uuid);

		if (!UuidKind.JAVA.equals(kind)) {
			TestUtils.fail("We tried to generate a Java UUID, but the generated UUID was not detected as valid Java UUID!");
			return;
		}

		uuid = UuidEncoderDecoder.generateEcoreUUID();

		kind = UuidEncoderDecoder.detectUUIDkind(uuid);

		if (!UuidKind.ECORE.equals(kind)) {
			TestUtils.fail("We tried to generate an Ecore UUID, but the generated UUID was not detected as valid Ecore UUID!");
			return;
		}

		TestUtils.succeed();
	}

	public void prettifyUuidTest() {

		TestUtils.start("Prettify UUID");

		// when told that this is (supposed to be) a Java UUID, the prettifier should prettify even this hot mess...
		String prettyJavaUuid = UuidEncoderDecoder.prettifyJavaUUID("---1-23----4");

		if (!"00000000-0000-0000-0000-000000001234".equals(prettyJavaUuid)) {
			TestUtils.fail("We tried to prettify a very silly Java UUID - and it did not work!");
			return;
		}

		String prettyEcoreUuid = UuidEncoderDecoder.prettifyEcoreUUID("tv96sLc7Eeir2tT3jqTAfw==");

		if (!"_tv96sLc7Eeir2tT3jqTAfw".equals(prettyEcoreUuid)) {
			TestUtils.fail("We tried to prettify a silly Ecore UUID - and it did not work!");
			return;
		}

		prettyJavaUuid = UuidEncoderDecoder.prettifyJavaUUID("12345678-abcd-1234-abcd-123456789012");

		if (!"12345678-abcd-1234-abcd-123456789012".equals(prettyJavaUuid)) {
			TestUtils.fail("We tried to prettify a reasonable Java UUID - and it did not work!");
			return;
		}

		prettyEcoreUuid = UuidEncoderDecoder.prettifyEcoreUUID("_av96sLc7Eeir2tT3jqTAfw");

		if (!"_av96sLc7Eeir2tT3jqTAfw".equals(prettyEcoreUuid)) {
			TestUtils.fail("We tried to prettify a reasonable Ecore UUID - and it did not work!");
			return;
		}

		TestUtils.succeed();
	}

	public void ensureUuidTest() {

		TestUtils.start("Ensure UUID");

		try {
			String prettyJavaUuid = UuidEncoderDecoder.ensureUUIDisJava("12345678-abcd-1234-abcd-123456789012");

			if (!"12345678-abcd-1234-abcd-123456789012".equals(prettyJavaUuid)) {
				TestUtils.fail("We tried to ensure a reasonable Java UUID is Java - and it did not work!");
				return;
			}

			String prettyEcoreUuid = UuidEncoderDecoder.ensureUUIDisEcore("_av96sLc7Eeir2tT3jqTAfw");
			if (!"_av96sLc7Eeir2tT3jqTAfw".equals(prettyEcoreUuid)) {
				TestUtils.fail("We tried to ensure a reasonable Ecore UUID is Ecore - and it did not work!");
				return;
			}
		} catch (ConversionException e) {
			TestUtils.fail("There was a ConversionException while trying to ensure already correct UUIDs! Message: " + e.getMessage());
			return;
		}

		try {
			String prettyJavaUuid = UuidEncoderDecoder.ensureUUIDisJava("faiwojapwef");

			TestUtils.fail("We tried to ensure something is a Java UUID which really should not have worked, but were actually given a result!");
			return;
		} catch (ConversionException e) {
			// exception is wanted!
		}

		try {
			String prettyEcoreUuid = UuidEncoderDecoder.ensureUUIDisEcore("faiwojapwef");

			TestUtils.fail("We tried to ensure something is a Ecore UUID which really should not have worked, but were actually given a result!");
			return;
		} catch (ConversionException e) {
			// exception is wanted!
		}

		try {
			// use this example:
			// 00010010 00110100 01010110 01111000 10101011 11001101 00010010 00110100 10101011 11001101 00010010 00110100 01010110 01111000 10010000 00010010 bin
			//    1   2    3   4    5   6    7   8    a   b    c   d    1   2    3   4    a   b    c   d    1   2    3   4    5   6    7   8    9   0    1   2 hex
			//      E      j      R     W      e      K      v     N      E      j      S     r      z      R      I     0      V      n      i     Q      E g base 64
			String origEcoreUuid = "_EjRWeKvNEjSrzRI0VniQEg";

			String prettyJavaUuid = UuidEncoderDecoder.ensureUUIDisJava(origEcoreUuid);

			if (!"12345678-abcd-1234-abcd-123456789012".equals(prettyJavaUuid)) {
				TestUtils.fail("We tried to convert an Ecore UUID to Java using the ensure function... but it did not work! The returned Java UUID was: " + prettyJavaUuid);
				return;
			}

			String prettyEcoreUuid = UuidEncoderDecoder.ensureUUIDisEcore(prettyJavaUuid);

			if (!origEcoreUuid.equals(prettyEcoreUuid)) {
				TestUtils.fail("We tried to convert a Java UUID to Ecore using the ensure function... but it did not work! The returned Ecore UUID was: " + prettyEcoreUuid);
				return;
			}
		} catch (ConversionException e) {
			TestUtils.fail("There was a ConversionException while trying to convert UUIDs! Message: " + e.getMessage());
			return;
		}

		TestUtils.succeed();
	}

}
