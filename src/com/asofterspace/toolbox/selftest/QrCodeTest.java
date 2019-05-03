/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.barcodes.QrCode;
import com.asofterspace.toolbox.barcodes.QrCodeFactory;
import com.asofterspace.toolbox.barcodes.QrCodeQualityLevel;
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

		calculateQrErrorCodeTest();

		writeSimpleQrCodeTest();

		writeQrCodeWithWhitespaceTest();
	}

	public void getBitsFromImageTest() {

		TestUtils.start("Getting the individual bits from a QR Code");

		PpmFile imgFile = new PpmFile(AllTests.IMAGE_TEST_DATA_PATH + "/qrcode.ppm");

		QrCode qrCode = new QrCode(imgFile.getImage());

		PpmFile shouldBeFile = new PpmFile(AllTests.IMAGE_TEST_DATA_PATH + "/qrcode_bits.ppm");

		if (shouldBeFile.getImage().equals(qrCode.getDatapointsAsImage())) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We tried reading a well-known QR Code - but the ones and zeroes could not even be detected!");
	}

	public void readSmallQrCodeTest() {

		TestUtils.start("Reading a Small QR Code (Version 2)");

		PpmFile imgFile = new PpmFile(AllTests.IMAGE_TEST_DATA_PATH + "/qrcode.ppm");

		QrCode qrCode = new QrCode(imgFile.getImage());

		if ("http://asofterspace.com".equals(qrCode.getContent())) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We tried reading a well-known QR Code - but the contents that we read were incorrect!");
	}

	public void readMediumQrCodeTest() {

		TestUtils.start("Reading a Medium QR Code (Version 3)");

		DefaultImageFile imgFile = new DefaultImageFile(AllTests.IMAGE_TEST_DATA_PATH + "/qrcode_2.bmp");

		QrCode qrCode = new QrCode(imgFile.getImage(), 3);

		if ("We are coming for you, Mars! :)".equals(qrCode.getContent().trim())) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We tried reading a well-known QR Code - but the contents that we read were incorrect!");
	}

	public void calculateQrErrorCodeTest() {

		TestUtils.start("Calculating some QR Error Codes");

		QrCode qrCode = new QrCode(1);

		System.out.println("addAlpha(0, 1): " + qrCode.addAlpha(0, 1));

		qrCode.setEdcLevel(QrCodeQualityLevel.MEDIUM_QUALITY);

		TestUtils.fail("We tried generating some error codes for a QR code but did not get the correct result!");
	}

	public void writeSimpleQrCodeTest() {

		TestUtils.start("Writing a simple QR Code");

		QrCode qrCode = QrCodeFactory.constructFromString("Hello, we are A Softer Space! :)");

		// DEBUG START
		PpmFile imgFile = new PpmFile(AllTests.TEST_PATH + "/../qrtest.ppm");

		imgFile.assign(qrCode.getDatapointsAsImage());

		imgFile.save();
		// DEBUG END

		if ("Hello, we are A Softer Space! :)".equals(qrCode.getContent())) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We tried creating a QR code surrounded by whitespace, but we did not get the correct result!");
	}

	public void writeQrCodeWithWhitespaceTest() {

		TestUtils.start("Writing a QR Code with whitespace");

		Image qrImage = QrCodeFactory.createWhitespacedImageFromString("Hi, it is us again! ^^");

		// DEBUG START
		PpmFile imgFile = new PpmFile(AllTests.TEST_PATH + "/../qrtest_whitespace.ppm");

		imgFile.assign(qrImage);

		imgFile.save();
		// DEBUG END

		// TODO :: add success possibility, otherwise the test is a bit harsh ^^

		TestUtils.fail("We tried creating a QR code surrounded by whitespace, but we did not get the correct result!");
	}

}
