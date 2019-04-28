/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.barcodes.QrCode;
import com.asofterspace.toolbox.barcodes.QrCodeFactory;
import com.asofterspace.toolbox.io.DefaultImageFile;
import com.asofterspace.toolbox.io.PpmFile;
import com.asofterspace.toolbox.test.Test;
import com.asofterspace.toolbox.test.TestUtils;


public class QrCodeTest implements Test {

	@Override
	public void runAll() {

		getBitsFromImageTest();

		readSmallQrCodeTest();

		readMediumQrCodeTest();

		writeSimpleQrCodeTest();
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

	public void writeSimpleQrCodeTest() {

		TestUtils.start("Writing a Simple QR Code");

		QrCode qrCode = QrCodeFactory.constructFromString("Hello from A Softer Space! :)");

		if ("Hello from A Softer Space! :)".equals(qrCode.getContent().trim())) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We tried reading the QR Code that we just created - but the contents that we read were incorrect!");
	}

}
