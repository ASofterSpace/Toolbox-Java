/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.io;

import com.asofterspace.toolbox.utils.ColorRGB;
import com.asofterspace.toolbox.utils.Image;
import com.asofterspace.toolbox.Utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


/**
 * A PGM image file object describes an uncompressed grayscale image file,
 * often with .pgm extension, and starting with P5 as magic bytes
 * (or P2 for ASCII instead of raw format)
 *
 * TODO :: add ASCII variant
 * TODO :: allow comments inside the file
 *
 * @author Moya (a softer space), 2019
 */
public class PgmFile extends RasterImageFile {

	/**
	 * You can construct a PbmFile instance by directly from a path name.
	 */
	public PgmFile(String fullyQualifiedFileName) {

		super(fullyQualifiedFileName);
	}

	/**
	 * You can construct a PbmFile instance by basing it on an existing file object.
	 */
	public PgmFile(File regularFile) {

		super(regularFile);
	}

	protected void loadImageContents() {

		try {

			int width = 0;
			int height = 0;
			int maxColorValue = 255;

			byte[] binaryContent = Files.readAllBytes(Paths.get(this.filename));
			int len = binaryContent.length;

			if (binaryContent[0] != 'P') {
				System.err.println("[ERROR] Trying to load the PGM file " + filename + ", but its header is not that of a PGM file!");
			}

			if (binaryContent[1] != '5') {
				System.err.println("[ERROR] Trying to load the PGM file " + filename + ", but its header is not that of a raw PGM file!");
			}

			if (!isWhitespace(binaryContent[2])) {
				System.err.println("[ERROR] Trying to load the PGM file " + filename + ", but its header is weird!");
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
			ColorRGB[][] uncompressedData = new ColorRGB[height][width];

			// the scale is already exactly the RGB scale (0 .. 255)
			if (maxColorValue == 255){
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						byte g = binaryContent[cur++];

						uncompressedData[y][x] = new ColorRGB(g, g, g);
					}
				}
			} else {
				// ah well, we have to scale the data...
				for (int y = 0; y < height; y++) {
					for (int x = 0; x < width; x++) {
						byte g = (byte) (((int) binaryContent[cur++] * 255) / maxColorValue);

						uncompressedData[y][x] = new ColorRGB(g, g, g);
					}
				}
			}

			img = new Image(uncompressedData);

		} catch (ArrayIndexOutOfBoundsException | IOException e) {
			System.err.println("[ERROR] Trying to load the PGM file " + filename + ", but there was an exception - inconceivable!\n" + e);
		}
	}

	private boolean isWhitespace(byte curByte) {
		return curByte == ' ' || curByte == '\n' || curByte == '\r' || curByte == '\t';
	}

	@Override
	public void save() {

		if (img == null) {
			loadImageContents();
		}

		StringBuilder out = new StringBuilder();

		out.append("P5");
		out.append("\n");
		out.append(getWidth());
		out.append("\n");
		out.append(getHeight());
		out.append("\n");
		// we want to actually save using the maxColorValue,
		// as we internally have the data uncompressed anyway
		out.append("255");
		out.append("\n");

		// fill file with data
		try (FileOutputStream stream = new FileOutputStream(initSave())) {

			stream.write(out.toString().getBytes());

			for (int y = 0; y < getHeight(); y++) {
				for (int x = 0; x < getWidth(); x++) {
					ColorRGB px = img.getPixel(x, y);
					stream.write(px.getGrayByte());
					stream.write(px.getGrayByte());
					stream.write(px.getGrayByte());
				}
			}

		} catch (IOException e) {
			System.err.println("[ERROR] An IOException occurred when trying to write to the file " + filename + " - inconceivable!");
		}
	}

	/**
	 * Gives back a string representation of the pgm file object
	 */
	@Override
	public String toString() {
		return "com.asofterspace.toolbox.io.PgmFile: " + filename;
	}

}
