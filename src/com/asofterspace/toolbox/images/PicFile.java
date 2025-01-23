/**
 * Unlicensed code created by A Softer Space, 2024
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.images;

import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.io.JSON;
import com.asofterspace.toolbox.io.JsonFile;
import com.asofterspace.toolbox.io.JsonParseException;
import com.asofterspace.toolbox.utils.Record;
import com.asofterspace.toolbox.utils.StrUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * A PIC image file object describes an image containing multiple layers, text etc.
 *
 * @author Moya, 2024
 */
public class PicFile extends JsonFile {

	private ImageMultiLayered img;


	/**
	 * You can construct a PicFile instance by directly from a path name.
	 */
	public PicFile(String fullyQualifiedFileName) {

		super(fullyQualifiedFileName);
	}

	/**
	 * You can construct a PicFile instance by basing it on an existing file object.
	 */
	public PicFile(File regularFile) {

		super(regularFile);
	}

	public void assign(ImageMultiLayered img) {
		this.img = img;
	}

	public void assign(Image img) {
		this.img = new ImageMultiLayered(img);
	}

	public ImageMultiLayered getImageMultiLayered() {

		if (img == null) {
			loadImageContents();
		}

		return img;
	}

	protected void loadImageContents() {

		img = null;

		try {
			Record coreInfo = getAllContents();

			img = new ImageMultiLayered(coreInfo.getInteger("width"), coreInfo.getInteger("height"));

			List<Record> layerRecs = coreInfo.getArray("layers");
			for (Record layerRec : layerRecs) {
				int offsetX = layerRec.getInteger("offsetX");
				int offsetY = layerRec.getInteger("offsetY");
				String kind = layerRec.getString("kind");
				switch (kind) {
					case "image":
						DefaultImageFile innerImageFile = new DefaultImageFile(
							getAbsoluteFilename() + layerRec.getString("path"));
						Image innerImg = innerImageFile.getImage();
						if (innerImg != null) {
							ImageLayerBasedOnImage imgLayer = new ImageLayerBasedOnImage(
								offsetX, offsetY, innerImageFile.getImage(), layerRec.getString("caption", "")
							);
							img.addLayer(imgLayer);
						} else {
							System.out.println("Unable to load layer file '" + innerImageFile.getAbsoluteFilename() +
								"' - will be ignored!");
						}
						break;
					case "text":
						ImageLayerBasedOnText txtLayer = new ImageLayerBasedOnText(
							offsetX, offsetY, layerRec.getString("text"), layerRec.getString("fontName"),
							layerRec.getInteger("fontSize"), ColorRGBA.fromString(layerRec.getString("color"))
						);
						img.addLayer(txtLayer);
						break;
					default:
						System.out.println("Unknown layer kind '" + kind + "' encountered - will be ignored!");
						break;
				}
			}

		} catch (JsonParseException e) {
			System.out.println(e);
		}
	}

	@Override
	public void save() {

		if (img == null) {
			loadImageContents();
		}

		if (img != null) {
			jsonContent = new JSON();
			jsonContent.set("width", img.getWidth());
			jsonContent.set("height", img.getHeight());

			Record layerRecArr = Record.emptyArray();
			List<Record> layers = new ArrayList<>();
			int i = 0;
			while (true) {
				ImageLayer layer = img.getLayer(i);
				if (layer == null) {
					break;
				}
				Record layerRec = new Record();
				layerRec.set("offsetX", layer.getOffsetX());
				layerRec.set("offsetY", layer.getOffsetY());
				layerRec.set("kind", "unknown");
				if (layer instanceof ImageLayerBasedOnImage) {
					layerRec.set("kind", "image");
					ImageLayerBasedOnImage imgLayer = (ImageLayerBasedOnImage) layer;
					layerRec.set("caption", imgLayer.getCaption());
					String relPath = "_" + StrUtils.leftPad0(""+i, 3) + ".png";
					layerRec.set("path", relPath);
					DefaultImageFile innerImageFile = new DefaultImageFile(
						getAbsoluteFilename() + relPath
					);
					innerImageFile.assign(imgLayer.getImage());
					innerImageFile.saveTransparently();
				}
				if (layer instanceof ImageLayerBasedOnText) {
					layerRec.set("kind", "text");
					ImageLayerBasedOnText textLayer = (ImageLayerBasedOnText) layer;
					layerRec.set("text", textLayer.getText());
					layerRec.set("fontName", textLayer.getFontName());
					layerRec.set("fontSize", textLayer.getFontSize());
					layerRec.set("color", textLayer.getTextColor().toString());
				}
				layerRecArr.append(layerRec);
				i++;
			}
			jsonContent.set("layers", layerRecArr);
			super.save();
		}
	}

	/**
	 * Gives back a string representation of the pic file object
	 */
	@Override
	public String toString() {
		return "com.asofterspace.toolbox.io.PicFile: " + filename;
	}

}
