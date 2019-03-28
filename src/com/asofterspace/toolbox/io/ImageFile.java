/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.io;

import com.asofterspace.toolbox.utils.ColorRGB;
import com.asofterspace.toolbox.utils.Image;


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

}
