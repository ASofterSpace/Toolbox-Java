/**
 * Unlicensed code created by A Softer Space, 2024
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.images;


/**
 * A single text layer in a multi-layered image
 *
 * @author Moya, 2024
 */
public class ImageLayerBasedOnText extends ImageLayer  {

	private String text;


	public ImageLayerBasedOnText(int offsetX, int offsetY, String text) {
		super(offsetX, offsetY);
		this.text = text;
	}

	public void drawOnto(Image ontoImage) {
		ontoImage.drawText(text, offsetY, null, null, offsetX);
	}

	public String getText() {
		return text;
	}

	public ImageLayerBasedOnText copy() {
		return new ImageLayerBasedOnText(offsetX, offsetY, text);
	}

}
