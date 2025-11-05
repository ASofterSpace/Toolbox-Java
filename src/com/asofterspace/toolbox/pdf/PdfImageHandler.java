/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.pdf;

import com.asofterspace.toolbox.images.Image;
import com.asofterspace.toolbox.images.ImageFile;
import com.asofterspace.toolbox.images.ImageFileHandler;
import com.asofterspace.toolbox.io.File;

import java.util.List;


/**
 * This lets us implement PDF image loading behavior for
 * the ImageFileCtrl
 *
 * @author Moya Schiller, moya@asofterspace.com
 */
public class PdfImageHandler implements ImageFileHandler {

	public boolean canLoad(File imageFile) {

		return imageFile.getFilename().toLowerCase().endsWith(".pdf");
	}

	public Image loadImageFromFile(File imageFile) {

		// TODO :: actually return several images, maybe?
		PdfFile pdfFile = new PdfFile(imageFile);
		List<Image> images = pdfFile.getPictures();
		if (images.size() > 0) {
			return images.get(0);
		}
		return null;
	}

	public boolean canSave(File targetFile) {
		return false;
	}

	public ImageFile createFile(File targetFile) {
		// TODO
		return null;
	}

}
