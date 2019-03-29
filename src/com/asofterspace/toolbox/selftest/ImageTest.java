/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.test.Test;
import com.asofterspace.toolbox.test.TestUtils;
import com.asofterspace.toolbox.utils.ColorRGB;
import com.asofterspace.toolbox.utils.Image;


public class ImageTest implements Test {

	@Override
	public void runAll() {

		testColorRGBComparison();

		testColorRGBDarknessDetection();

		testImageComparison();
	}

	public void testColorRGBComparison() {

		TestUtils.start("Color RGB Comparison");

		ColorRGB blackOne = new ColorRGB(0, 0, 0);

		ColorRGB blackTwo = new ColorRGB(0, 0, 0);

		ColorRGB white = new ColorRGB(255, 255, 255);

		if (!blackOne.equals(blackTwo)) {
			TestUtils.fail("Black does not equal black!");
			return;
		}

		if (!blackOne.fastEquals(blackTwo)) {
			TestUtils.fail("Black does not fast equal black!");
			return;
		}

		if (white.equals(blackTwo)) {
			TestUtils.fail("White equals black!");
			return;
		}

		if (white.fastEquals(blackTwo)) {
			TestUtils.fail("White fast equals black!");
			return;
		}

		TestUtils.succeed();
	}

	public void testColorRGBDarknessDetection() {

		TestUtils.start("Color RGB Darkness Comparison");

		ColorRGB black = new ColorRGB(0, 0, 0);

		ColorRGB red = new ColorRGB(255, 0, 0);

		ColorRGB yellow = new ColorRGB(255, 255, 0);

		ColorRGB white = new ColorRGB(255, 255, 255);

		if (!black.isDark()) {
			TestUtils.fail("Black is not dark!");
			return;
		}

		if (!red.isDark()) {
			TestUtils.fail("Red is not dark!");
			return;
		}

		if (yellow.isDark()) {
			TestUtils.fail("Yellow is dark!");
			return;
		}

		if (white.isDark()) {
			TestUtils.fail("White is dark!");
			return;
		}

		TestUtils.succeed();
	}

	public void testImageComparison() {

		TestUtils.start("Image Comparison");

		Image plain = new Image(100, 100);

		Image plainBig = new Image(200, 200);

		Image plainAndDot = new Image(100, 100);
		plainAndDot.setPixel(10, 10, new ColorRGB(0, 0, 0));

		Image plainAndDot2 = new Image(100, 100);
		plainAndDot2.setPixel(10, 10, new ColorRGB(0, 0, 0));

		if (plain.equals(plainBig)) {
			TestUtils.fail("Different sized images are reported as equals!");
			return;
		}

		if (plain.equals(plainAndDot)) {
			TestUtils.fail("Empty image and image with dot are reported as equals!");
			return;
		}

		if (!plainAndDot2.equals(plainAndDot)) {
			TestUtils.fail("Two images with the same contents are reported as different!");
			return;
		}

		TestUtils.succeed();
	}
}
