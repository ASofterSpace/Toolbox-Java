/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.io;

import com.asofterspace.toolbox.utils.ColorRGB;
import com.asofterspace.toolbox.utils.Image;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;


/**
 * A default image file can be used to load an image file with the
 * default Java ImageIO approach (which usually can load Bitmaps
 * and JPEGs, but can also be augmented by adding further image
 * extension libraries.)
 *
 * @author Moya (a softer space), 2019
 */
public class DefaultImageFile extends RasterImageFile {

	/**
	 * You can construct a DefaultImageFile instance by directly from a path name.
	 */
	public DefaultImageFile(String fullyQualifiedFileName) {

		super(fullyQualifiedFileName);
	}

	/**
	 * You can construct a DefaultImageFile instance by basing it on an existing file object.
	 */
	public DefaultImageFile(File regularFile) {

		super(regularFile);
	}

	protected void loadImageContents() {

		try {

			File file = new File(this.filename);
			BufferedImage javaImg = ImageIO.read(file.getJavaFile());

			int width = javaImg.getWidth();
			int height = javaImg.getHeight();

			ColorRGB[][] uncompressedData = new ColorRGB[height][width];

			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int rgb = javaImg.getRGB(x, y);
					int r = (rgb >> 16) & 0xFF;
					int g = (rgb >> 8) & 0xFF;
					int b = (rgb) & 0xFF;

					uncompressedData[y][x] = new ColorRGB(r, g, b);
				}
			}

			img = new Image(uncompressedData);

		} catch (IOException e) {
			System.err.println("[ERROR] Trying to load the default image file " + filename + ", but there was an exception - inconceivable!\n" + e);
		}
	}

	@Override
	public void save() {
		System.err.println("[ERROR] No saving mechanism for default image files has been implemented, so " + filename + " cannot be created!");
	}

	/**
	 * Gives back a string representation of the default image file object
	 */
	@Override
	public String toString() {
		return "com.asofterspace.toolbox.io.DefaultImageFile: " + filename;
	}

}
