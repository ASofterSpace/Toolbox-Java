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

	private Integer outlineSize;

	private ColorRGBA outlineColor;


	public ImageLayerBasedOnText(int offsetX, int offsetY, String text, String fontName, Integer fontSize,
		ColorRGBA textColor, Integer outlineSize, ColorRGBA outlineColor) {

		super(offsetX, offsetY);
		this.text = text;
		this.fontName = fontName;
		this.fontSize = fontSize;
		this.textColor = textColor;
		this.outlineSize = outlineSize;
		this.outlineColor = outlineColor;
	}

	public void drawOnto(Image ontoImage) {

		if ((text != null) && (!"".equals(text))) {

			Integer s = outlineSize;
			if ((s != null) && (outlineColor != null)) {
				if (s > 0) {
					drawOutline(ontoImage, -s, 0);
					drawOutline(ontoImage, 0, s);
					drawOutline(ontoImage, s, 0);
					drawOutline(ontoImage, 0, -s);
					if (s > 1) {
						int sH = (s+1)/2;
						drawOutline(ontoImage, -sH, -sH);
						drawOutline(ontoImage, -sH, sH);
						drawOutline(ontoImage, sH, -sH);
						drawOutline(ontoImage, sH, sH);
						if (s > 2) {
							drawOutline(ontoImage, -sH, 0);
							drawOutline(ontoImage, 0, sH);
							drawOutline(ontoImage, sH, 0);
							drawOutline(ontoImage, 0, -sH);
							if (s > 3) {
								int sT = (s+1)/3;
								int s2 = ((3*s)+1)/4;
								drawOutline(ontoImage, -sT, -s2);
								drawOutline(ontoImage, -sT, s2);
								drawOutline(ontoImage, sT, -s2);
								drawOutline(ontoImage, sT, s2);
								drawOutline(ontoImage, -s2, -sT);
								drawOutline(ontoImage, -s2, sT);
								drawOutline(ontoImage, s2, -sT);
								drawOutline(ontoImage, s2, sT);
								if (s > 4) {
									int s5 = (s+1)/5;
									int s8 = ((8*s)+1)/9;
									drawOutline(ontoImage, -s5, -s8);
									drawOutline(ontoImage, -s5, s8);
									drawOutline(ontoImage, s5, -s8);
									drawOutline(ontoImage, s5, s8);
									drawOutline(ontoImage, -s8, -s5);
									drawOutline(ontoImage, -s8, s5);
									drawOutline(ontoImage, s8, -s5);
									drawOutline(ontoImage, s8, s5);
									if (s > 5) {
										drawOutline(ontoImage, -s2, 0);
										drawOutline(ontoImage, 0, s2);
										drawOutline(ontoImage, s2, 0);
										drawOutline(ontoImage, 0, -s2);
										if (s > 6) {
											drawOutline(ontoImage, -sT, -sT);
											drawOutline(ontoImage, -sT, sT);
											drawOutline(ontoImage, sT, -sT);
											drawOutline(ontoImage, sT, sT);
										}
									}
								}
							}
						}
					}
				}
			}

			// not currently supported with transparent drawing!
			Boolean useAntiAliasing = false;

			ontoImage.drawTextTransparently(text, offsetY, null, null, offsetX,
				fontName, fontSize, useAntiAliasing, textColor);
		}
	}

	private void drawOutline(Image ontoImage, int atX, int atY) {

		Boolean useAntiAliasing = false;
		ontoImage.drawTextTransparently(text, offsetY+atY, null, null, offsetX+atX,
			fontName, fontSize, useAntiAliasing, outlineColor);
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

	public Integer getOutlineSize() {
		return outlineSize;
	}

	public void setOutlineSize(Integer outlineSize) {
		this.outlineSize = outlineSize;
	}

	public ColorRGBA getOutlineColor() {
		return outlineColor;
	}

	public void setOutlineColor(ColorRGBA outlineColor) {
		this.outlineColor = outlineColor;
	}

	public int getWidth() {
		return Image.getTextDimensions(text, fontName, fontSize).getX();
	}

	public int getHeight() {
		return Image.getTextDimensions(text, fontName, fontSize).getY();
	}

	public ImageLayerBasedOnText copy() {
		return new ImageLayerBasedOnText(
			offsetX, offsetY, text, fontName, fontSize, textColor, outlineSize, outlineColor
		);
	}

	public ImageLayerBasedOnImage convertToImageLayerBasedOnImage() {
		Image baseImg = new Image(getWidth(), getHeight(), new ColorRGBA(0, 0, 0, 0));
		drawOnto(baseImg);
		return new ImageLayerBasedOnImage(offsetX, offsetY, baseImg, text);
	}

}
