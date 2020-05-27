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

	private int[] leftData;
	private int[] rightData;

	private Integer numberOfChannels;
	private Integer sampleRate;
	private Integer byteRate;
	private Integer bitsPerSample;
	private Integer audioFormat;


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

		if (leftData == null) {
			loadWavContents();
		}
	}

	private void loadWavContents() {

		byte[] bytes = loadContent();

		// first step: process the entire file and search for the fmt and the data chunks
		// (they could be anywhere among a lot of other chunks!)
		int curChunkStart = 12;

		int dataStart = 0;
		int dataEnd = 0;

		while (curChunkStart < bytes.length) {
			int curChunkSize = BitUtils.bytesToInt(bytes, curChunkStart + 4);
			byte[] curChunkNameBytes = new byte[4];
			curChunkNameBytes[0] = bytes[curChunkStart];
			curChunkNameBytes[1] = bytes[curChunkStart+1];
			curChunkNameBytes[2] = bytes[curChunkStart+2];
			curChunkNameBytes[3] = bytes[curChunkStart+3];
			String curChunkName = new String(curChunkNameBytes);

			if ("fmt ".equals(curChunkName)) {
				audioFormat = BitUtils.bytesToInt(bytes, curChunkStart + 8, 2);
				numberOfChannels = BitUtils.bytesToInt(bytes, curChunkStart + 10, 2);
				sampleRate = BitUtils.bytesToInt(bytes, curChunkStart + 12, 4);
				byteRate = BitUtils.bytesToInt(bytes, curChunkStart + 16, 4);
				bitsPerSample = BitUtils.bytesToInt(bytes, curChunkStart + 22, 2);
			}

			if ("data".equals(curChunkName)) {
				dataStart = curChunkStart + 8;
				dataEnd = dataStart + curChunkSize;
			}

			curChunkStart += 8 + curChunkSize;
		}

		System.out.println("Audio Format: " + audioFormat);
		System.out.println("Number of Channels: " + numberOfChannels);
		System.out.println("Sample Rate: " + sampleRate);
		System.out.println("Byte Rate: " + byteRate);
		System.out.println("Bits per Sample: " + bitsPerSample);

		if (dataEnd > bytes.length) {
			System.err.println("It looks like the wave file you are trying to load has been cut off!");
			dataEnd = bytes.length;
		}

		leftData = new int[(8 * (dataEnd - dataStart)) / (bitsPerSample * numberOfChannels)];
		if (numberOfChannels > 1) {
			rightData = new int[leftData.length];
		}

		if (bitsPerSample == 16) {
			int j = 0;
			for (int i = dataStart; i < dataEnd; i += 2, j++) {
				leftData[j] = BitUtils.bytesToInt(bytes, i, 2);
				if (numberOfChannels > 1) {
					i += 2;
					rightData[j] = BitUtils.bytesToInt(bytes, i, 2);
				}
			}
		} else {
			System.err.println("Cannot read WAVs with " + bitsPerSample + " bits per sample!");
		}
	}

	public int[] getData() {
		return getLeftData();
	}

	public int[] getLeftData() {
		initialize();
		return leftData;
	}

	public void setLeftData(int[] leftData) {
		this.leftData = leftData;
	}

	public int[] getRightData() {
		initialize();
		if (numberOfChannels < 2) {
			return leftData;
		}
		return rightData;
	}

	public void setRightData(int[] rightData) {
		this.rightData = rightData;
	}

	public Integer getNumberOfChannels() {
		initialize();
		return numberOfChannels;
	}

	public void setNumberOfChannels(Integer numberOfChannels) {
		this.numberOfChannels = numberOfChannels;
	}

	public Integer getSampleRate() {
		initialize();
		return sampleRate;
	}

	public void setSampleRate(Integer sampleRate) {
		this.sampleRate = sampleRate;
	}

	public Integer getByteRate() {
		initialize();
		return byteRate;
	}

	public void setByteRate(Integer byteRate) {
		this.byteRate = byteRate;
	}

	public Integer getBitsPerSample() {
		initialize();
		return bitsPerSample;
	}

	public void setBitsPerSample(Integer bitsPerSample) {
		this.bitsPerSample = bitsPerSample;
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
