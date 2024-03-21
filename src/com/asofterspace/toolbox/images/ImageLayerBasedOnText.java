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

	private String fontName;

	private Integer fontSize;

	private ColorRGBA textColor;


	public ImageLayerBasedOnText(int offsetX, int offsetY, String text, String fontName, Integer fontSize, ColorRGBA textColor) {
		super(offsetX, offsetY);
		this.text = text;
		this.fontName = fontName;
		this.fontSize = fontSize;
		this.textColor = textColor;
	}

	public void drawOnto(Image ontoImage) {

		if ((text != null) && (!"".equals(text))) {

			// not currently supported with transparent drawing!
			Boolean useAntiAliasing = false;

			ontoImage.drawTextTransparently(text, offsetY, null, null, offsetX,
				fontName, fontSize, useAntiAliasing, textColor);
		}
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getFontName() {
		return fontName;
	}

	public void setFontName(String fontName) {
		this.fontName = fontName;
	}

	public Integer getFontSize() {
		return fontSize;
	}

	public void setFontSize(Integer fontSize) {
		this.fontSize = fontSize;
	}

	public ColorRGBA getTextColor() {
		return textColor;
	}

	public void setTextColor(ColorRGBA textColor) {
		this.textColor = textColor;
	}

	public int getWidth() {
		return Image.getTextDimensions(text, fontName, fontSize).getX();
	}

	public int getHeight() {
		return Image.getTextDimensions(text, fontName, fontSize).getY();
	}

	public ImageLayerBasedOnText copy() {
		return new ImageLayerBasedOnText(offsetX, offsetY, text, fontName, fontSize, textColor);
	}

}
