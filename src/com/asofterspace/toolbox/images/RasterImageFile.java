/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.images;

import com.asofterspace.toolbox.io.Directory;
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

	/**
	 * Create a new file instance based on a Directory and the name of
	 * the file inside the directory
	 * @param directory The directory in which the file is located
	 * @param filename The (local) name of the actual file
	 */
	public RasterImageFile(Directory directory, String filename) {

		super(directory, filename);
	}

	protected abstract void loadImageContents();

	public void assign(Image img) {

		setImage(img);
	}

	public void setImage(Image img) {

		this.img = img;
	}

	public void assign(ImageMultiLayered img) {

		setImage(img);
	}

	public void setImage(ImageMultiLayered img) {

		this.img = img.bake();
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

	public ColorRGBA getPixel(int x, int y) {

		if (img == null) {
			loadImageContents();
		}

		return img.getPixel(x, y);
	}

	public void setPixel(int x, int y, ColorRGBA pix) {

		if (img == null) {
			loadImageContents();
		}

		img.setPixel(x, y, pix);
	}

}
