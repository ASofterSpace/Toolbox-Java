/**
 * Unlicensed code created by A Softer Space, 2026
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.pdf;

import com.asofterspace.toolbox.utils.StrUtils;

import java.awt.Canvas;
import java.awt.Font;
import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.List;


public class PdfBuilderText {

	String text;

	String fontName;

	int x;

	int y;

	int size;

	private FontMetrics savedMetrics = null;

	private static final Canvas METRICS_CANVAS = new Canvas();


	public PdfBuilderText(String text, String fontName, int x, int y, int size) {
		this.text = text;
		this.fontName = fontName;
		this.x = x;
		this.y = y;
		this.size = size;
	}

	public String getText() {
		return text;
	}

	public String getFontName() {
		return fontName;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getSize() {
		return size;
	}

	public List<PdfBuilderText> splitIntoLines() {
		List<PdfBuilderText> result = new ArrayList<>();
		List<String> splitText = StrUtils.split(text, "\n");
		int offset = 0;
		for (String line : splitText) {
			result.add(new PdfBuilderText(line, fontName, x, y + offset, size));
			offset += size;
		}
		return result;
	}

	int getWidth(String line) {
		if (line == null) {
			return 0;
		}
		// silly approximation
		// return line.length() * size;

		// works, but will create metrics again and again
		// return Image.getTextDimensionsWidth(line, fontName, size);

		if (savedMetrics == null) {
			Font font = new Font(fontName, Font.PLAIN, size);
			savedMetrics = METRICS_CANVAS.getFontMetrics(font);
		}

		return savedMetrics.stringWidth(line);
	}

}
