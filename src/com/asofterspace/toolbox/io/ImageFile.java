/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.io;

import com.asofterspace.toolbox.utils.ColorRGB;
import com.asofterspace.toolbox.utils.Image;

import java.util.List;


/**
 * Any image file needs to be able to offer us some functions, which are listed here...
 *
 * @author Moya (a softer space), 2019
 */
public abstract class ImageFile extends BinaryFile {

	/**
	 * You can construct an ImageFile instance by directly from a path name.
	 */
	public ImageFile(String fullyQualifiedFileName) {

		super(fullyQualifiedFileName);
	}

	/**
	 * You can construct an ImageFile instance by basing it on an existing file object.
	 */
	public ImageFile(File regularFile) {

		super(regularFile);
	}


	public abstract void assign(Image img);

	public abstract Image getImage();

	public abstract int getWidth();

	public abstract int getHeight();

	public abstract ColorRGB getPixel(int x, int y);

	public abstract void setPixel(int x, int y, ColorRGB pix);

	public abstract void save();


	public static Image readImageFromFile(File imageFile) {

		if (imageFile.getFilename().toLowerCase().endsWith(".ppm")) {
			PpmFile ppmFile = new PpmFile(imageFile);
			return ppmFile.getImage();
		}

		if (imageFile.getFilename().toLowerCase().endsWith(".pgm")) {
			PgmFile pgmFile = new PgmFile(imageFile);
			return pgmFile.getImage();
		}

		if (imageFile.getFilename().toLowerCase().endsWith(".pbm")) {
			PbmFile pbmFile = new PbmFile(imageFile);
			return pbmFile.getImage();
		}

		if (imageFile.getFilename().toLowerCase().endsWith(".pdf")) {
			PdfFile pdfFile = new PdfFile(imageFile);
			List<Image> images = pdfFile.getPictures();
			if (images.size() > 0) {
				return images.get(0);
			}
		}

		DefaultImageFile defaultImageFile = new DefaultImageFile(imageFile);
		return defaultImageFile.getImage();
	}

	public static void saveImageToFile(Image image, File targetFile) {

		ImageFile imageFile = null;

		if (targetFile.getFilename().toLowerCase().endsWith(".ppm")) {
			imageFile = new PpmFile(targetFile);
		}

		if (targetFile.getFilename().toLowerCase().endsWith(".pgm")) {
			imageFile = new PgmFile(targetFile);
		}

		if (targetFile.getFilename().toLowerCase().endsWith(".pbm")) {
			imageFile = new PbmFile(targetFile);
		}

		if (imageFile == null) {
			imageFile = new DefaultImageFile(targetFile);
		}

		imageFile.assign(image);
		imageFile.save();
	}

}
