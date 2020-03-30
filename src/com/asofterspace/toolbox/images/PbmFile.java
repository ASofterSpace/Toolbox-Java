/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.images;

import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.utils.BitUtils;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


/**
 * A PBM image file object describes an uncompressed monochrome image file,
 * often with .pbm extension, and starting with P4 as magic bytes
 * (or P1 for ASCII instead of raw format)
 *
 * TODO :: add ASCII variant
 * TODO :: allow comments inside the file
 *
 * @author Moya (a softer space), 2019
 */
public class PbmFile extends RasterImageFile {

	/**
	 * You can construct a PbmFile instance by directly from a path name.
	 */
	public PbmFile(String fullyQualifiedFileName) {

		super(fullyQualifiedFileName);
	}

	/**
	 * You can construct a PbmFile instance by basing it on an existing file object.
	 */
	public PbmFile(File regularFile) {

		super(regularFile);
	}

	protected void loadImageContents() {

		try {

			int width = 0;
			int height = 0;

			byte[] binaryContent = Files.readAllBytes(Paths.get(this.filename));
			int len = binaryContent.length;

			if (binaryContent[0] != 'P') {
				System.err.println("[ERROR] Trying to load the PBM file " + filename + ", but its header is not that of a PBM file!");
			}

			if (binaryContent[1] != '4') {
				System.err.println("[ERROR] Trying to load the PBM file " + filename + ", but its header is not that of a raw PBM file!");
			}

			if (!isWhitespace(binaryContent[2])) {
				System.err.println("[ERROR] Trying to load the PBM file " + filename + ", but its header is weird!");
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

			// content starts exactly one newline after the height
			ColorRGB[][] uncompressedData = new ColorRGB[height][width];

			ColorRGB black = new ColorRGB(0, 0, 0);
			ColorRGB white = new ColorRGB(255, 255, 255);
			boolean[] buffer = new boolean[8];
			int curInBuffer = 8;

			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					if (curInBuffer >= 8) {
						buffer = BitUtils.byteToBits(binaryContent[cur++]);
						curInBuffer = 0;
					}
					if (buffer[curInBuffer]) {
						uncompressedData[y][x] = black;
					} else {
						uncompressedData[y][x] = white;
					}
					curInBuffer++;
				}
			}

			img = new Image(uncompressedData);

		} catch (ArrayIndexOutOfBoundsException | IOException e) {
			System.err.println("[ERROR] Trying to load the PBM file " + filename + ", but there was an exception - inconceivable!\n" + e);
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

		out.append("P4");
		out.append("\n");
		out.append(getWidth());
		out.append("\n");
		out.append(getHeight());
		out.append("\n");

		// fill file with data
		try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(initSave()))) {

			stream.write(out.toString().getBytes());

			boolean[] buffer = new boolean[8];
			int cur = 0;

			for (int y = 0; y < getHeight(); y++) {
				for (int x = 0; x < getWidth(); x++) {
					buffer[cur] = img.getPixel(x, y).isDark();
					cur++;
					if (cur >= 8) {
						stream.write(BitUtils.bitsToByte(buffer));
						cur = 0;
					}
				}
			}

			if (cur > 0) {
				for (; cur < 8; cur++) {
					buffer[cur] = false;
				}
				stream.write(BitUtils.bitsToByte(buffer));
			}

		} catch (IOException e) {
			System.err.println("[ERROR] An IOException occurred when trying to write to the file " + filename + " - inconceivable!");
		}
	}

	/**
	 * Gives back a string representation of the pbm file object
	 */
	@Override
	public String toString() {
		return "com.asofterspace.toolbox.io.PbmFile: " + filename;
	}

}
