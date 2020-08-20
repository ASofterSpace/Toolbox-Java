/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.images.ColorRGB;
import com.asofterspace.toolbox.images.Image;
import com.asofterspace.toolbox.test.Test;
import com.asofterspace.toolbox.test.TestUtils;


public class ImageTest implements Test {

	@Override
	public void runAll() {

		testColorRGBComparison();

		testColorRGBMixing();

		testColorRGBDarknessDetection();

		testColorRGBstringification();

		testImageClearing();

		testImageComparison();

		testImageRemoveColors();
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

	public void testColorRGBMixing() {

		TestUtils.start("Color RGB Mixing");

		ColorRGB black = new ColorRGB(0, 0, 0);
		ColorRGB red = new ColorRGB(254, 0, 0);
		ColorRGB white = new ColorRGB(254, 252, 252);

		ColorRGB blackred = new ColorRGB(127, 0, 0);
		ColorRGB blackredMix = ColorRGB.intermix(black, red, 0.5);

		if (!blackred.equals(blackredMix)) {
			TestUtils.fail("Black mixed with red does not give the correct result!");
			return;
		}

		ColorRGB redredredwhite = new ColorRGB(254, 63, 63);
		ColorRGB redredredwhiteMix = ColorRGB.intermix(red, white, 0.75);

		if (!redredredwhite.equals(redredredwhiteMix)) {
			TestUtils.fail("Lots of red mixed with white does not give the correct result!");
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

	public void testColorRGBstringification() {

		TestUtils.start("Color RGB Stringification");

		ColorRGB colOne = new ColorRGB(1, 2, 3);
		ColorRGB colTwo = new ColorRGB(10, 20, 30);
		ColorRGB colThree = new ColorRGB(240, 31, 197);

		if (!colOne.equals(ColorRGB.fromString(colOne.toString()))) {
			TestUtils.fail("Color one does not equal itself when being transformed into a string and back!");
			return;
		}

		if (!colTwo.equals(ColorRGB.fromString(colTwo.toString()))) {
			TestUtils.fail("Color two does not equal itself when being transformed into a string and back!");
			return;
		}

		if (!colThree.equals(ColorRGB.fromString(colThree.toString()))) {
			TestUtils.fail("Color three does not equal itself when being transformed into a string and back!");
			return;
		}

		if (!colOne.equals(ColorRGB.fromString("rgba ( 1 , 2 , 3 )"))) {
			TestUtils.fail("Color one does not equal to rgba ( 1 , 2 , 3 )!");
			return;
		}

		if (!colThree.equals(ColorRGB.fromString("#F01FC5"))) {
			TestUtils.fail("Color three does not equal to #F01FC5!");
			return;
		}

		TestUtils.succeed();
	}

	public void testImageClearing() {

		TestUtils.start("Image Clearing");

		Image img = new Image(100, 100);
		img.setPixel(10, 10, new ColorRGB(128, 0, 176));
		ColorRGB gotPix = img.getPixel(10, 10);

		if (gotPix.equals(new ColorRGB())) {
			TestUtils.fail("A pixel that was set seems to have been cleared before calling clear()!");
			return;
		}

		img.clear();

		gotPix = img.getPixel(10, 10);

		if (!gotPix.equals(new ColorRGB())) {
			TestUtils.fail("Calling clear() on a pixel that was previously set does not seem to have worked!");
			return;
		}

		gotPix = img.getPixel(8, 8);

		if (!gotPix.equals(new ColorRGB())) {
			TestUtils.fail("Calling clear() on a pixel that was not explicitly set does not seem to have worked!");
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

	public void testImageRemoveColors() {

		TestUtils.start("Image Color Removal");

		Image before = new Image(100, 100);
		before.setPixel(6, 7, new ColorRGB(33, 0, 0));
		before.setPixel(6, 8, new ColorRGB(0, 66, 0));
		before.setPixel(6, 9, new ColorRGB(0, 0, 99));
		before.setPixel(6, 10, new ColorRGB(255, 255, 255));
		before.setPixel(6, 13, new ColorRGB(0, 0, 0));

		Image after = new Image(100, 100);
		after.setPixel(6, 7, new ColorRGB(11, 11, 11));
		after.setPixel(6, 8, new ColorRGB(22, 22, 22));
		after.setPixel(6, 9, new ColorRGB(33, 33, 33));
		after.setPixel(6, 10, new ColorRGB(255, 255, 255));
		after.setPixel(6, 13, new ColorRGB(0, 0, 0));

		before.removeColors();

		if (!after.equals(before)) {
			TestUtils.fail("We attempted to remove the colors from an image, but it did not go as expected...");
			return;
		}

		TestUtils.succeed();
	}
}
