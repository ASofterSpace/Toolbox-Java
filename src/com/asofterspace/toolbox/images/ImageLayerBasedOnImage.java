/**
 * Unlicensed code created by A Softer Space, 2024
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.images;


/**
 * A single image layer in a multi-layered image
 *
 * @author Moya, 2024
 */
public class ImageLayerBasedOnImage extends ImageLayer  {

	private Image image;


	public ImageLayerBasedOnImage(int offsetX, int offsetY, Image image) {
		super(offsetX, offsetY);
		this.image = image;
	}

	public void drawOnto(Image ontoImage) {
		ontoImage.drawTransparently(image, offsetX, offsetY);
	}

	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
	}

	public int getWidth() {
		return this.image.getWidth();
	}

	public int getHeight() {
		return this.image.getHeight();
	}

	public ImageLayerBasedOnImage copy() {
		return new ImageLayerBasedOnImage(offsetX, offsetY, image.copy());
	}

	public ImageLayerBasedOnImage convertToImageLayerBasedOnImage() {
		return copy();
	}

}
