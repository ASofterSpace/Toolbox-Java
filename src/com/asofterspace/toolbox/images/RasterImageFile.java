/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.images;

import com.asofterspace.toolbox.io.File;


/**
 * Any raster-based image file can extend this and get a lot of functionality for free...
 *
 * @author Moya (a softer space), 2019
 */
public abstract class RasterImageFile extends ImageFile {

	protected Image img;


	/**
	 * You can construct a RasterImageFile instance by directly from a path name.
	 */
	public RasterImageFile(String fullyQualifiedFileName) {

		super(fullyQualifiedFileName);
	}

	/**
	 * You can construct a RasterImageFile instance by basing it on an existing file object.
	 */
	public RasterImageFile(File regularFile) {

		super(regularFile);
	}

	protected abstract void loadImageContents();

	public void assign(Image img) {

		this.img = img;
	}

	public Image getImage() {

		if (img == null) {
			loadImageContents();
		}

		return img;
	}

	public int getWidth() {

		if (img == null) {
			loadImageContents();
		}

		return img.getWidth();
	}

	public int getHeight() {

		if (img == null) {
			loadImageContents();
		}

		return img.getHeight();
	}

	public ColorRGB getPixel(int x, int y) {

		if (img == null) {
			loadImageContents();
		}

		return img.getPixel(x, y);
	}

	public void setPixel(int x, int y, ColorRGB pix) {

		if (img == null) {
			loadImageContents();
		}

		img.setPixel(x, y, pix);
	}

}
