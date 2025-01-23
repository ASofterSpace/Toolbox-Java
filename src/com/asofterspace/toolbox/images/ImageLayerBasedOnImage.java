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

	private String caption;


	public ImageLayerBasedOnImage(int offsetX, int offsetY, Image image, String caption) {
		super(offsetX, offsetY);
		this.image = image;

		// we do not want image to be null so instead we create a flaming red default
		if (this.image == null) {
			this.image = new Image(64, 64, new ColorRGBA(255, 0, 0, 255));
		}

		this.caption = caption;
	}

	public void drawOnto(Image ontoImage) {
		ontoImage.drawTransparently(image, offsetX, offsetY);
	}

	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		if (image != null) {
			this.image = image;
		}
	}

	public int getWidth() {
		return this.image.getWidth();
	}

	public int getHeight() {
		return this.image.getHeight();
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public ImageLayerBasedOnImage copy() {
		return new ImageLayerBasedOnImage(offsetX, offsetY, image.copy(), caption);
	}

	public ImageLayerBasedOnImage convertToImageLayerBasedOnImage() {
		return copy();
	}

}
