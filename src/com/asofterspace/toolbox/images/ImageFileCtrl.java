/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.images;

import com.asofterspace.toolbox.io.File;

import java.util.ArrayList;
import java.util.List;


/**
 * A controller that allows us to interact with images and files
 *
 * @author Moya Schiller, moya@asofterspace.com
 */
public class ImageFileCtrl {

	private List<ImageFileHandler> extraFileHandlers;


	public ImageFileCtrl() {

		this.extraFileHandlers = new ArrayList<>();
	}

	public void addHandler(ImageFileHandler handler) {

		extraFileHandlers.add(handler);
	}

	public Image loadImageFromFile(File imageFile) {

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

		for (ImageFileHandler handler : extraFileHandlers) {
			if (handler.canLoad(imageFile)) {
				Image result = handler.loadImageFromFile(imageFile);
				if (result != null) {
					return result;
				}
			}
		}

		DefaultImageFile defaultImageFile = new DefaultImageFile(imageFile);
		return defaultImageFile.getImage();
	}

	public void saveImageToFile(Image image, File targetFile) {

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

		for (ImageFileHandler handler : extraFileHandlers) {
			if (handler.canSave(targetFile)) {
				imageFile = handler.createFile(targetFile);
			}
		}

		if (imageFile == null) {
			imageFile = new DefaultImageFile(targetFile);
		}

		imageFile.assign(image);
		imageFile.saveTransparently();
	}

}
