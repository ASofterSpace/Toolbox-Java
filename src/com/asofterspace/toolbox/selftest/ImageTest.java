/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.images.ColorRGBA;
import com.asofterspace.toolbox.images.Image;
import com.asofterspace.toolbox.test.Test;
import com.asofterspace.toolbox.test.TestUtils;


public class ImageTest implements Test {

	@Override
	public void runAll() {

		testColorRGBAComparison();

		testColorRGBAMixing();

		testColorRGBADarknessDetection();

		testColorRGBAstringification();

		testImageClearing();

		testImageComparison();

		testImageRemoveColors();
	}

	public void testColorRGBAComparison() {

		TestUtils.start("Color RGB Comparison");

		ColorRGBA blackOne = new ColorRGBA(0, 0, 0);

		ColorRGBA blackTwo = new ColorRGBA(0, 0, 0);

		ColorRGBA white = new ColorRGBA(255, 255, 255);

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

	public void testColorRGBAMixing() {

		TestUtils.start("Color RGB Mixing");

		ColorRGBA black = new ColorRGBA(0, 0, 0);
		ColorRGBA red = new ColorRGBA(254, 0, 0);
		ColorRGBA white = new ColorRGBA(254, 252, 252);

		ColorRGBA blackred = new ColorRGBA(127, 0, 0);
		ColorRGBA blackredMix = ColorRGBA.intermix(black, red, 0.5);

		if (!blackred.equals(blackredMix)) {
			TestUtils.fail("Black mixed with red does not give the correct result!");
			return;
		}

		ColorRGBA redredredwhite = new ColorRGBA(254, 63, 63);
		ColorRGBA redredredwhiteMix = ColorRGBA.intermix(red, white, 0.75);

		if (!redredredwhite.equals(redredredwhiteMix)) {
			TestUtils.fail("Lots of red mixed with white does not give the correct result!");
			return;
		}

		TestUtils.succeed();
	}

	public void testColorRGBADarknessDetection() {

		TestUtils.start("Color RGB Darkness Comparison");

		ColorRGBA black = new ColorRGBA(0, 0, 0);

		ColorRGBA red = new ColorRGBA(255, 0, 0);

		ColorRGBA yellow = new ColorRGBA(255, 255, 0);

		ColorRGBA white = new ColorRGBA(255, 255, 255);

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

	public void testColorRGBAstringification() {

		TestUtils.start("Color RGB Stringification");

		ColorRGBA colOne = new ColorRGBA(1, 2, 3);
		ColorRGBA colTwo = new ColorRGBA(10, 20, 30);
		ColorRGBA colThree = new ColorRGBA(240, 31, 197);

		if (!colOne.equals(ColorRGBA.fromString(colOne.toString()))) {
			TestUtils.fail("Color one does not equal itself when being transformed into a string and back!");
			return;
		}

		if (!colTwo.equals(ColorRGBA.fromString(colTwo.toString()))) {
			TestUtils.fail("Color two does not equal itself when being transformed into a string and back!");
			return;
		}

		if (!colThree.equals(ColorRGBA.fromString(colThree.toString()))) {
			TestUtils.fail("Color three does not equal itself when being transformed into a string and back!");
			return;
		}

		if (!colOne.equals(ColorRGBA.fromString("rgba ( 1 , 2 , 3 )"))) {
			TestUtils.fail("Color one does not equal to rgba ( 1 , 2 , 3 )!");
			return;
		}

		if (!colThree.equals(ColorRGBA.fromString("#F01FC5"))) {
			TestUtils.fail("Color three does not equal to #F01FC5!");
			return;
		}

		if (!colThree.toHexString().equals("#F01FC5")) {
			TestUtils.fail("Color three toHexString() is " + colThree.toHexString() + " which does not equal to #F01FC5!");
			return;
		}

		TestUtils.succeed();
	}

	public void testImageClearing() {

		TestUtils.start("Image Clearing");

		Image img = new Image(100, 100);
		img.setPixel(10, 10, new ColorRGBA(128, 0, 176));
		ColorRGBA gotPix = img.getPixel(10, 10);

		if (gotPix.equals(new ColorRGBA())) {
			TestUtils.fail("A pixel that was set seems to have been cleared before calling clear()!");
			return;
		}

		img.clear();

		gotPix = img.getPixel(10, 10);

		if (!gotPix.equals(new ColorRGBA())) {
			TestUtils.fail("Calling clear() on a pixel that was previously set does not seem to have worked!");
			return;
		}

		gotPix = img.getPixel(8, 8);

		if (!gotPix.equals(new ColorRGBA())) {
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
		plainAndDot.setPixel(10, 10, new ColorRGBA(0, 0, 0));

		Image plainAndDot2 = new Image(100, 100);
		plainAndDot2.setPixel(10, 10, new ColorRGBA(0, 0, 0));

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
		before.setPixel(6, 7, new ColorRGBA(33, 0, 0));
		before.setPixel(6, 8, new ColorRGBA(0, 66, 0));
		before.setPixel(6, 9, new ColorRGBA(0, 0, 99));
		before.setPixel(6, 10, new ColorRGBA(255, 255, 255));
		before.setPixel(6, 13, new ColorRGBA(0, 0, 0));

		Image after = new Image(100, 100);
		after.setPixel(6, 7, new ColorRGBA(11, 11, 11));
		after.setPixel(6, 8, new ColorRGBA(22, 22, 22));
		after.setPixel(6, 9, new ColorRGBA(33, 33, 33));
		after.setPixel(6, 10, new ColorRGBA(255, 255, 255));
		after.setPixel(6, 13, new ColorRGBA(0, 0, 0));

		before.removeColors();

		if (!after.equals(before)) {
			TestUtils.fail("We attempted to remove the colors from an image, but it did not go as expected...");
			return;
		}

		TestUtils.succeed();
	}
}
