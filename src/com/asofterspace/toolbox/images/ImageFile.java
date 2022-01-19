/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.images;

import com.asofterspace.toolbox.io.BinaryFile;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;


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

	/**
	 * Create a new file instance based on a Directory and the name of
	 * the file inside the directory
	 * @param directory The directory in which the file is located
	 * @param filename The (local) name of the actual file
	 */
	public ImageFile(Directory directory, String filename) {

		super(directory, filename);
	}


	public abstract void assign(Image img);

	public abstract void setImage(Image img);

	public abstract Image getImage();

	public abstract int getWidth();

	public abstract int getHeight();

	public abstract ColorRGBA getPixel(int x, int y);

	public abstract void setPixel(int x, int y, ColorRGBA pix);

	public abstract void save();

	public abstract void saveTransparently();

}
