/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.images.DefaultImageFile;
import com.asofterspace.toolbox.images.PpmFile;
import com.asofterspace.toolbox.test.Test;
import com.asofterspace.toolbox.test.TestUtils;


public class ImagePpmTest implements Test {

	@Override
	public void runAll() {

		readPpmTest();

		savePpmTest();

		convertBitmapToPpmTest();
	}

	public void readPpmTest() {

		TestUtils.start("Reading a PPM File");

		PpmFile imgFile = new PpmFile(AllTests.IMAGE_TEST_DATA_PATH + "/qrcode.ppm");

		boolean success = true;

		if (!imgFile.getPixel(0, 0).is(0, 0, 0)) {
			success = false;
		}

		if (!imgFile.getPixel(1, 1).is(236, 236, 236)) {
			success = false;
		}

		if (!imgFile.getPixel(9, 1).is(208, 208, 208)) {
			success = false;
		}

		if (!imgFile.getPixel(0, 15).is(207, 207, 207)) {
			success = false;
		}

		if (success) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We tried reading a well-known PPM File - but the contents that we read were incorrect!");
	}

	public void savePpmTest() {

		TestUtils.start("Saving a PPM File");

		PpmFile inputFile = new PpmFile(AllTests.IMAGE_TEST_DATA_PATH + "/qrcode.ppm");

		String outFileName = AllTests.TEST_PATH + "/qrcode_saved.ppm";

		PpmFile outputFile = new PpmFile(outFileName);

		outputFile.assign(inputFile.getImage());

		outputFile.save();

		PpmFile resultFile = new PpmFile(outFileName);

		if (outputFile.getImage().equals(resultFile.getImage())) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We tried saving a PPM File, reading it again, and checking if the result was the same - but it was not!");
	}

	public void convertBitmapToPpmTest() {

		TestUtils.start("Converting a Bitmap to PPM");

		DefaultImageFile bmpFile = new DefaultImageFile(AllTests.IMAGE_TEST_DATA_PATH + "/qrcode.bmp");

		String outFileName = AllTests.TEST_PATH + "/qrcode_saved_again.ppm";

		PpmFile ppmFile = new PpmFile(outFileName);

		ppmFile.assign(bmpFile.getImage());

		ppmFile.save();

		PpmFile resultFile = new PpmFile(outFileName);

		if (bmpFile.getImage().equals(resultFile.getImage())) {
			TestUtils.succeed();
			return;
		}

		TestUtils.fail("We tried loading a Bitmap File, saving it as PPM File, reading it again, and checking if the result was the same as the original Bitmap - but it was not!");
	}
}
