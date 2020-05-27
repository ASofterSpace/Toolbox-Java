/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.io;

import com.asofterspace.toolbox.io.BinaryFile;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.utils.BitUtils;


/**
 * This represents a Wave Audio File
 */
public class WavFile extends BinaryFile {

	private int[] data;


	/**
	 * You can construct a WavFile instance directly from a path name.
	 */
	public WavFile(String fullyQualifiedFileName) {

		super(fullyQualifiedFileName);
	}

	/**
	 * You can construct a WavFile instance by basing it on an existing file object.
	 */
	public WavFile(File regularFile) {

		super(regularFile);
	}

	private void initialize() {

		if (data == null) {
			loadWavContents();
		}
	}

	private void loadWavContents() {

		byte[] bytes = loadContent();

		int subchunk1Start = 12;
		int subchunk1SizeOffset = 8;
		int subchunk1Size = BitUtils.bytesToInt(bytes, 16);

		int subchunk2Start = subchunk1Start + subchunk1SizeOffset + subchunk1Size;
		int subchunk2Size = BitUtils.bytesToInt(bytes, subchunk2Start + 4);
		int dataStart = subchunk2Start + 8;
		int dataEnd = subchunk2Start + 8 + subchunk2Size;

		if (dataEnd > bytes.length) {
			System.err.println("It looks like the wave file you are trying to load has been cut off!");
			dataEnd = bytes.length;
		}

		int[] data = new int[dataEnd - dataStart];

		for (int i = dataStart; i < dataEnd; i++) {
			// TODO: actually load the data
		}
	}

	public int[] getData() {

		initialize();

		return data;
	}

	public void save() {

		initialize();

		byte[] bytes = new byte[0];

		// TODO create bytes based on data

		super.saveContent(bytes);
	}

	@Override
	public String toString() {
		return "com.asofterspace.toolbox.io.WavFile: " + filename;
	}

}
