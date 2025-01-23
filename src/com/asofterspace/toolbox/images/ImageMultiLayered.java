/**
 * Unlicensed code created by A Softer Space, 2024
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.images;

import java.util.ArrayList;
import java.util.List;


/**
 * A multi-layered image
 *
 * @author Moya, 2024
 */
public class ImageMultiLayered {

	private int width;

	private int height;

	private List<ImageLayer> layers = new ArrayList<>();


	public ImageMultiLayered(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public ImageMultiLayered(Image img) {
		this(img.getWidth(), img.getHeight());
		layers = new ArrayList<>();
		layers.add(new ImageLayerBasedOnImage(0, 0, img, null));
	}

	public void clear() {
		layers = new ArrayList<>();
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void addLayer(ImageLayer layer) {
		if (layer != null) {
			layers.add(layer);
		}
	}

	public void replaceLayer(int index, ImageLayer layer) {
		if ((index >= 0) && (index < layers.size())) {
			layers.set(index, layer);
		}
	}

	public void deleteLayer(int index) {
		if ((index >= 0) && (index < layers.size())) {
			layers.remove(index);
		}
	}

	public ImageLayer getLayer(int index) {
		if ((index >= 0) && (index < layers.size())) {
			return layers.get(index);
		}
		return null;
	}

	public int getLayerAmount() {
		return layers.size();
	}

	public void moveLayerFullyUp(int index) {
		if (index >= layers.size() - 1) {
			return;
		}
		List<ImageLayer> others = new ArrayList<>();
		for (int i = 0; i < layers.size(); i++) {
			if (i != index) {
				others.add(layers.get(i));
			}
		}
		others.add(layers.get(index));
		layers = others;
	}

	public void moveLayerOneUp(int index) {
		if (index >= layers.size() - 1) {
			return;
		}
		List<ImageLayer> others = new ArrayList<>();
		for (int i = 0; i < layers.size(); i++) {
			if (i != index) {
				others.add(layers.get(i));
			}
			if (i == index + 1) {
				others.add(layers.get(index));
			}
		}
		layers = others;
	}

	public void moveLayerOneDown(int index) {
		if (index < 1) {
			return;
		}
		List<ImageLayer> others = new ArrayList<>();
		for (int i = 0; i < layers.size(); i++) {
			if (i == index - 1) {
				others.add(layers.get(index));
			}
			if (i != index) {
				others.add(layers.get(i));
			}
		}
		layers = others;
	}

	public void moveLayerFullyDown(int index) {
		if (index < 1) {
			return;
		}
		List<ImageLayer> others = new ArrayList<>();
		others.add(layers.get(index));
		for (int i = 0; i < layers.size(); i++) {
			if (i != index) {
				others.add(layers.get(i));
			}
		}
		layers = others;
	}

	public ImageMultiLayered copy() {
		ImageMultiLayered result = new ImageMultiLayered(width, height);
		for (ImageLayer layer : layers) {
			result.layers.add(layer.copy());
		}
		return result;
	}

	public Image bake() {

		if (layers.size() == 0) {
			ColorRGBA backgroundColor = new ColorRGBA(0, 0, 0, 0);
			return new Image(width, height, backgroundColor);
		}

		if (layers.size() == 1) {
			ImageLayer layer = layers.get(0);
			if ((layer.getOffsetX() == 0) && (layer.getOffsetY() == 0)) {
				if (layer instanceof ImageLayerBasedOnImage) {
					ImageLayerBasedOnImage imgLayer = (ImageLayerBasedOnImage) layer;
					Image image = imgLayer.getImage();
					if ((image.getWidth() == width) && (image.getHeight() == height)) {
						return image.copy();
					}
				}
			}
		}

		// special case: base layer is already an image, no drawing necessary
		ImageLayer baseLayer = layers.get(0);
		if (baseLayer instanceof ImageLayerBasedOnImage) {
			ImageLayerBasedOnImage baseLayerImg = (ImageLayerBasedOnImage) baseLayer;
			if ((baseLayerImg.getOffsetX() == 0) && (baseLayerImg.getOffsetY() == 0) && (baseLayerImg.getWidth() == width) && (baseLayerImg.getHeight() == height)) {
				Image base = baseLayerImg.getImage().copy();
				for (int i = 1; i < layers.size(); i++) {
					layers.get(i).drawOnto(base);
				}
				return base;
			}
		}

		ColorRGBA backgroundColor = new ColorRGBA(0, 0, 0, 0);
		Image base = new Image(width, height, backgroundColor);
		for (ImageLayer layer : layers) {
			layer.drawOnto(base);
		}
		return base;
	}

}
