/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.io;

import com.asofterspace.toolbox.utils.ColorRGB;


/**
 * Any image file needs to be able to offer us some functions, which are listed here...
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

	public abstract int getWidth();

	public abstract int getHeight();

	public abstract ColorRGB getPixel(int x, int y);

}
