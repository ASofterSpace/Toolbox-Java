/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.io;

import com.asofterspace.toolbox.utils.Image;


/**
 * A loader lets us implement new image loading and saving behavior for
 * the ImageFileCtrl outside of the core io part
 *
 * @author Tom Moya Schiller, moya@asofterspace.com
 */
public interface ImageFileHandler {

	public boolean canLoad(File imageFile);

	public Image loadImageFromFile(File imageFile);

	public boolean canSave(File targetFile);

	public ImageFile createFile(File targetFile);

}
