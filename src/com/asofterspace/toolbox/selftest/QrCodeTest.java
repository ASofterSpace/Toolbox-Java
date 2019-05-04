/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.barcodes.QrCode;
import com.asofterspace.toolbox.barcodes.QrCodeFactory;
import com.asofterspace.toolbox.barcodes.QrCodeQualityLevel;
import com.asofterspace.toolbox.barcodes.QrCodeUtils;
import com.asofterspace.toolbox.io.DefaultImageFile;
import com.asofterspace.toolbox.io.PpmFile;
import com.asofterspace.toolbox.test.Test;
import com.asofterspace.toolbox.test.TestUtils;
import com.asofterspace.toolbox.utils.Image;


public class QrCodeTest implements Test {

	@Override
	public void runAll() {

		getBitsFromImageTest();

		readSmallQrCodeTest();

		readMediumQrCodeTest();

		generateQrErrorCodeTest();

		writeSimpleQrCodeTest();

		writeAndReadQrCodeTest();

		writeQrCodeWithWhitespaceTest();
	}

	public void getBitsFromImageTest() {

		TestUtils.start("Getting the individual bits from a QR Code");

		PpmFile imgFile = new PpmFile(AllTests.IMAGE_TEST_DATA_PATH + "/qrcode.ppm");

		QrCode qrCode = QrCodeFactory.readFromQrImage(imgFile.getImage());

		PpmFile shouldBeFile = new PpmFile(AllTests.IMAGE_TEST_DATA_PATH + "/qrcode_bits.ppm");

		if (shouldBeFile.getImage().equals(qrCode.toImage())) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We tried reading a well-known QR Code - but the ones and zeroes could not even be detected!");
	}

	public void readSmallQrCodeTest() {

		TestUtils.start("Reading a Small QR Code (Version 2)");

		PpmFile imgFile = new PpmFile(AllTests.IMAGE_TEST_DATA_PATH + "/qrcode.ppm");

		QrCode qrCode = QrCodeFactory.readFromQrImage(imgFile.getImage());

		if ("http://asofterspace.com".equals(qrCode.getContent())) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We tried reading a well-known QR Code - but the contents that we read were incorrect!");
	}

	public void readMediumQrCodeTest() {

		TestUtils.start("Reading a Medium QR Code (Version 3)");

		DefaultImageFile imgFile = new DefaultImageFile(AllTests.IMAGE_TEST_DATA_PATH + "/qrcode_2.bmp");

		QrCode qrCode = QrCodeFactory.readFromQrImage(imgFile.getImage(), 3);

		if ("We are coming for you, Mars! :)".equals(qrCode.getContent().trim())) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We tried reading a well-known QR Code - but the contents that we read were incorrect!");
	}

	public void generateQrErrorCodeTest() {

		TestUtils.start("Generating some QR Error Codes");

		if (25 != QrCodeUtils.addAlpha(0, 1)) {
			TestUtils.fail("We tried generating some error codes for a QR code but already adding some alpha values did not work out!");
		}

		int[] genPoly = QrCodeUtils.getGeneratorPolynomialAlphas(3);

		if ((genPoly.length != 4) || (genPoly[0] != 0) || (genPoly[1] != 198) || (genPoly[2] != 199) || (genPoly[3] != 3)) {
			TestUtils.fail("We tried generating some error codes for a QR code but already generating a generator polynomial failed!");
		}

		int[] messagePolynomial = {64, 116, 214, 86, 247, 114, 3, 162, 144, 236, 17, 236, 17, 236, 17, 236};

		int[] result = QrCodeUtils.getErrorCorrectionCodewords(messagePolynomial, 1, QrCodeQualityLevel.MEDIUM_QUALITY);

		if ((result[0] == 215) && (result[1] == 212) && (result[2] == 220) && (result[3] ==  27) &&
			(result[4] == 104) && (result[5] == 235) && (result[6] ==  76) && (result[7] == 122) &&
			(result[8] == 104) && (result[9] ==   8)) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We tried generating some error codes for a QR code but did not get the correct result!");
	}

	public void writeSimpleQrCodeTest() {

		TestUtils.start("Writing a simple QR Code");

		QrCode qrCode = QrCodeFactory.createFromString("Hello, we are A Softer Space! :)", null, QrCodeMaskPattern.PATTERN_0);
		Image generatedQrImg = qrCode.toImage();

		PpmFile imgFile = new PpmFile(AllTests.IMAGE_TEST_DATA_PATH + "/qrtest.ppm");
		Image loadedQrImg = imgFile.getImage();

		if (generatedQrImg.equals(loadedQrImg)) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We tried creating a simple QR code, but we did not get the correct result!");
	}

	public void writeAndReadQrCodeTest() {

		TestUtils.start("Writing and reading a QR Code");

		QrCode qrCode = QrCodeFactory.createFromString("We do like space, and science in general...");

		if ("We do like space, and science in general...".equals(qrCode.getContent())) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We tried creating a QR code and reading it again, but we did not get the correct result!");
	}

	public void writeQrCodeWithWhitespaceTest() {

		TestUtils.start("Writing a QR Code with whitespace");

		Image generatedQrImg = QrCodeFactory.createWhitespacedImageFromString("Hi, it is us again! ^^", null, QrCodeMaskPattern.PATTERN_0);

		PpmFile imgFile = new PpmFile(AllTests.IMAGE_TEST_DATA_PATH + "/qrtest_whitespace.ppm");
		Image loadedQrImg = imgFile.getImage();

		if (generatedQrImg.equals(loadedQrImg)) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We tried creating a QR code surrounded by whitespace, but we did not get the correct result!");
	}

}
