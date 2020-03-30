/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.images.DefaultImageFile;
import com.asofterspace.toolbox.test.Test;
import com.asofterspace.toolbox.test.TestUtils;


public class ImageDefaultTest implements Test {

	@Override
	public void runAll() {

		AllTests.clearTestDirectory();

		readBitmapTest();
	}

	public void readBitmapTest() {

		TestUtils.start("Reading a Bitmap via Default ImageIO");

		DefaultImageFile imgFile = new DefaultImageFile(AllTests.IMAGE_TEST_DATA_PATH + "/qrcode.bmp");

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

		TestUtils.fail("We tried reading a well-known bitmap - but the contents that we read were incorrect!");
	}

}
