/**
 * Unlicensed code created by A Softer Space, 2026
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.pdf;

import com.asofterspace.toolbox.utils.StrUtils;

import java.util.ArrayList;
import java.util.List;


public class PdfBuilderTextAcrossLines extends PdfBuilderText {

	int maxWidth;


	public PdfBuilderTextAcrossLines(String text, String fontName, int x, int y, int size, int maxWidth) {
		super(text, fontName, x, y, size);
		this.maxWidth = maxWidth;
	}

	public int getMaxWidth() {
		return maxWidth;
	}

	@Override
	public List<PdfBuilderText> splitIntoLines() {
		List<PdfBuilderText> result = new ArrayList<>();
		List<String> splitText = StrUtils.split(text, "\n");
		int offset = 0;
		for (String line : splitText) {
			int width = getWidth(line);
			while (width > maxWidth) {
				width = 0;
				int untilchar = 0;
				while (width < maxWidth) {
					untilchar++;
					width = getWidth(line.substring(0, untilchar));
				}
				result.add(new PdfBuilderText(line.substring(0, untilchar), fontName, x, y + offset, size));
				offset += size;
				line = line.substring(untilchar);
				// after wraparound newline do not start with a space character
				while (line.startsWith(" ")) {
					line = line.substring(1);
				}
				width = getWidth(line);
			}
			result.add(new PdfBuilderText(line, fontName, x, y + offset, size));
			offset += size;
		}
		return result;
	}

}
