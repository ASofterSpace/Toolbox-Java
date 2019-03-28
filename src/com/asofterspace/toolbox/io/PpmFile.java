/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.io;

import com.asofterspace.toolbox.utils.ColorRGB;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


/**
 * A PPM image file object describes a certain type of file, often with .ppm extension,
 * and starting with P6 as magic bytes (or P3 for ASCII instead of raw format)
 */
public class PpmFile extends ImageFile {

	// the data in uncompressed form, with first index vertical, second index horizontal
	private ColorRGB[][] uncompressedData;

	private int width;

	private int height;

	private int maxColorValue;


	/**
	 * You can construct a PpmFile instance by directly from a path name.
	 */
	public PpmFile(String fullyQualifiedFileName) {

		super(fullyQualifiedFileName);
	}

	/**
	 * You can construct a PpmFile instance by basing it on an existing file object.
	 */
	public PpmFile(File regularFile) {

		super(regularFile);
	}

	protected void loadPpmContents() {

		try {

			byte[] binaryContent = Files.readAllBytes(Paths.get(this.filename));
			int len = binaryContent.length;

			if (binaryContent[0] != 'P') {
				System.err.println("[ERROR] Trying to load the PPM file " + filename + ", but its header is not that of a PPM file!");
			}

			if (binaryContent[1] != '6') {
				System.err.println("[ERROR] Trying to load the PPM file " + filename + ", but its header is not that of a raw PPM file!");
			}

			if (binaryContent[2] != '\n') {
				System.err.println("[ERROR] Trying to load the PPM file " + filename + ", but its header is weird!");
			}

			int cur = 3;

			StringBuilder buf = new StringBuilder();

			while (cur < len) {

				byte curByte = binaryContent[cur];
				cur++;

				if (isWhitespace(curByte)) {
					width = Integer.parseInt(buf.toString());
					break;
				} else {
					buf.append((char) curByte);
				}
			}

			while (cur < len) {

				byte curByte = binaryContent[cur];
				cur++;

				if (!isWhitespace(curByte)) {
					cur--;
					break;
				}
			}

			buf = new StringBuilder();

			while (cur < len) {

				byte curByte = binaryContent[cur];
				cur++;

				if (isWhitespace(curByte)) {
					height = Integer.parseInt(buf.toString());
					break;
				} else {
					buf.append((char) curByte);
				}
			}

			while (cur < len) {

				byte curByte = binaryContent[cur];
				cur++;

				if (!isWhitespace(curByte)) {
					cur--;
					break;
				}
			}

			buf = new StringBuilder();

			while (cur < len) {

				byte curByte = binaryContent[cur];
				cur++;

				if (isWhitespace(curByte)) {
					maxColorValue = Integer.parseInt(buf.toString());
					break;
				} else {
					buf.append((char) curByte);
				}
			}

			// content starts exactly one newline after the maxColorValue
			uncompressedData = new ColorRGB[height][width];

			// the scale is already exactly the RGB scale (0 .. 255)
			if (maxColorValue == 255){
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						byte r = binaryContent[cur++];
						byte g = binaryContent[cur++];
						byte b = binaryContent[cur++];

						uncompressedData[y][x] = new ColorRGB(r, g, b);
					}
					if (binaryContent[cur] == '\n') {
						cur++;
					}
				}
			} else {
				// ah well, we have to scale the data...
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						byte r = (byte) (((int) binaryContent[cur++] * 255) / maxColorValue);
						byte g = (byte) (((int) binaryContent[cur++] * 255) / maxColorValue);
						byte b = (byte) (((int) binaryContent[cur++] * 255) / maxColorValue);

						uncompressedData[y][x] = new ColorRGB(r, g, b);
					}
					if (binaryContent[cur] == '\n') {
						cur++;
					}
				}
			}

		} catch (ArrayIndexOutOfBoundsException e) {
			// we expect array index out of bounds exceptions and just catch them,
			// instead of having to check on every step if cur < length ourselves...
		} catch (IOException e) {
			System.err.println("[ERROR] Trying to load the PPM file " + filename + ", but there was an exception - inconceivable!\n" + e);
		}
	}

	private boolean isWhitespace(byte curByte) {
		return curByte == ' ' || curByte == '\n' || curByte == '\r' || curByte == '\t';
	}

	@Override
	public int getWidth() {

		if (uncompressedData == null) {
			loadPpmContents();
		}

		return width;
	}

	@Override
	public int getHeight() {

		if (uncompressedData == null) {
			loadPpmContents();
		}

		return height;
	}

	@Override
	public ColorRGB getPixel(int x, int y) {

		if (uncompressedData == null) {
			loadPpmContents();
		}

		return uncompressedData[y][x];
	}

	/**
	 * Gives back a string representation of the ppm file object
	 */
	@Override
	public String toString() {
		return "com.asofterspace.toolbox.io.PpmFile: " + filename;
	}

}
