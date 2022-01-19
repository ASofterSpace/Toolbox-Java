/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.images;

import java.util.Random;


/**
 * A factory for auto-generating new 2D images
 *
 * @author Tom Moya Schiller, moya@asofterspace.com
 */
public class Image2DFactory {

	public static Image createRandomImage() {

		Random rand = new Random();

		while (true) {
			int width = 8 + rand.nextInt(16);
			int height = 8 + rand.nextInt(16);
			Image first = new Image(width, height);
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					first.setPixel(x, y,
						new ColorRGBA(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)));
				}
			}

			double beauty = evaluateBeauty(first);
			System.out.println("Beauty: " + beauty);
			if (beauty > -40) {
				return first;
			}
		}
	}

	/**
	 * Evaluates how beautiful an image is, based on various criteria
	 * A positive result is beautiful (the more beautiful, the higher the result)
	 * A negative result is not beautiful
	 */
	private static double evaluateBeauty(Image img) {

		// start without any opinion
		int result = 0;
		int width = img.getWidth();
		int height = img.getHeight();

		// discard one point for each pixel which has no neighbouring pixel that has a similar color
		for (int x = 1; x < width-1; x++) {
			for (int y = 1; y < height-1; y++) {
				ColorRGBA cur = img.getPixel(x, y);
				if (cur.fastSimilar(img.getPixel(x+1, y))) {
					continue;
				}
				if (cur.fastSimilar(img.getPixel(x+1, y+1))) {
					continue;
				}
				if (cur.fastSimilar(img.getPixel(x, y+1))) {
					continue;
				}
				if (cur.fastSimilar(img.getPixel(x-1, y+1))) {
					continue;
				}
				if (cur.fastSimilar(img.getPixel(x-1, y))) {
					continue;
				}
				if (cur.fastSimilar(img.getPixel(x-1, y-1))) {
					continue;
				}
				if (cur.fastSimilar(img.getPixel(x, y-1))) {
					continue;
				}
				if (cur.fastSimilar(img.getPixel(x+1, y-1))) {
					continue;
				}
				result--;
			}
		}

		// gain points if there are clearly just a few color that dominate the picture...
		// TODO

		return (double) result;
	}
}
