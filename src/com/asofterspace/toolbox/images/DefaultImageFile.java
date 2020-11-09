/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.images;

import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;


/**
 * A default image file can be used to load an image file with the
 * default Java ImageIO approach (which usually can load Bitmaps,
 * JPEGs and PNGs, but can also be augmented by adding further image
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

	/**
	 * Create a new file instance based on a Directory and the name of
	 * the file inside the directory
	 * @param directory The directory in which the file is located
	 * @param filename The (local) name of the actual file
	 */
	public DefaultImageFile(Directory directory, String filename) {

		super(directory, filename);
	}

	protected void loadImageContents() {

		try {

			File file = new File(this.filename);
			BufferedImage javaImg = ImageIO.read(file.getJavaFile());

			int width = javaImg.getWidth();
			int height = javaImg.getHeight();

			ColorRGB[][] uncompressedData = new ColorRGB[height][width];

			img = new Image(uncompressedData);

			img.drawAwtImage(javaImg, 0, 0);

		} catch (IOException e) {
			System.err.println("[ERROR] Trying to load the default image file " + filename + ", but there was an exception - inconceivable!\n" + e);
		}
	}

	@Override
	public void save() {
		try {
			if (img == null) {
				loadImageContents();
			}

			File file = new File(this.filename);

			BufferedImage javaImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_3BYTE_BGR);

			ColorRGB[][] data = img.getDataSafely();

			for (int y = 0; y < img.getHeight(); y++) {
				for (int x = 0; x < img.getWidth(); x++) {
					ColorRGB c = data[y][x];
					int rgb = c.getR() << 16;
					rgb |= c.getG() << 8;
					rgb |= c.getB();
					javaImg.setRGB(x, y, rgb);
				}
			}

			if (filename.toLowerCase().endsWith(".bmp")) {
				ImageIO.write(javaImg, "bmp", file.getJavaFile());
			} else if (filename.toLowerCase().endsWith(".png")) {
				ImageIO.write(javaImg, "png", file.getJavaFile());
			} else {
				ImageIO.write(javaImg, "jpeg", file.getJavaFile());
			}

		} catch (IOException e) {
			System.err.println("[ERROR] Trying to save the default image file " + filename + ", but there was an exception - inconceivable!\n" + e);
		}
	}

	public void saveTransparently() {
		try {
			if (img == null) {
				loadImageContents();
			}

			File file = new File(this.filename);

			BufferedImage javaImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);

			ColorRGB[][] data = img.getDataSafely();

			for (int y = 0; y < img.getHeight(); y++) {
				for (int x = 0; x < img.getWidth(); x++) {
					ColorRGB c = data[y][x];
					int rgb = c.getR() << 24;
					rgb |= c.getG() << 16;
					rgb |= c.getB() << 8;
					rgb |= c.getA();
					javaImg.setRGB(x, y, rgb);
				}
			}

			if (filename.toLowerCase().endsWith(".bmp")) {
				ImageIO.write(javaImg, "bmp", file.getJavaFile());
			} else if (filename.toLowerCase().endsWith(".png")) {
				ImageIO.write(javaImg, "png", file.getJavaFile());
			} else {
				ImageIO.write(javaImg, "jpeg", file.getJavaFile());
			}

		} catch (IOException e) {
			System.err.println("[ERROR] Trying to save the default image file " + filename + ", but there was an exception - inconceivable!\n" + e);
		}
	}

	/**
	 * Gives back a string representation of the default image file object
	 */
	@Override
	public String toString() {
		return "com.asofterspace.toolbox.io.DefaultImageFile: " + filename;
	}

}
