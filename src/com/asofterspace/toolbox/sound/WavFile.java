/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.sound;

import com.asofterspace.toolbox.io.BinaryFile;
import com.asofterspace.toolbox.io.Directory;
import com.asofterspace.toolbox.io.File;
import com.asofterspace.toolbox.utils.BitUtils;


/**
 * This represents a Wave Audio File
 */
public class WavFile extends BinaryFile {

	private int[] leftData;
	private int[] rightData;
	private int[] monoData;

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

	/**
	 * Create a new WavFile instance based on a parent Directory and the name of
	 * the file inside the parent directory
	 * @param parentDirectory The directory in which the file is located
	 * @param filename The (local) name of the actual file
	 */
	public WavFile(Directory parentDirectory, String filename) {

		super(parentDirectory, filename);
	}

	public void copySettingsOf(WavFile other) {
		this.numberOfChannels = other.numberOfChannels;
		this.sampleRate = other.sampleRate;
		this.byteRate = other.byteRate;
		this.bitsPerSample = other.bitsPerSample;
		this.audioFormat = other.audioFormat;
	}

	private void initialize() {

		if (leftData == null) {
			loadWavContents();
		}
	}

	private void loadWavContents() {

		byte[] bytes = loadContent();

		this.monoData = null;

		// first step: process the entire file and search for the fmt and the data chunks
		// (they could be anywhere among a lot of other chunks!)
		int curChunkStart = 12;

		int dataStart = 0;
		int dataEnd = 0;

		while (curChunkStart < bytes.length) {
			int curChunkSize = BitUtils.bytesToInt(bytes, curChunkStart + 4);
			if (curChunkSize < 0) {
				curChunkSize = Integer.MAX_VALUE - (Integer.MIN_VALUE - curChunkSize);
			}
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

		switch (bitsPerSample) {
			case 8:
				int j = 0;
				for (int i = dataStart; i < dataEnd; i++, j++) {
					leftData[j] = bytes[i];
					if (numberOfChannels > 1) {
						i++;
						rightData[j] = bytes[i];
					}
				}
				break;
			case 16:
				j = 0;
				for (int i = dataStart; i < dataEnd; i += 2, j++) {
					leftData[j] = BitUtils.bytesToInt(bytes, i, 2);
					if (numberOfChannels > 1) {
						i += 2;
						rightData[j] = BitUtils.bytesToInt(bytes, i, 2);
					}
				}
				break;
			case 24:
				j = 0;
				for (int i = dataStart; i < dataEnd; i += 3, j++) {
					leftData[j] = BitUtils.bytesToInt(bytes, i, 3);
					if (numberOfChannels > 1) {
						i += 3;
						rightData[j] = BitUtils.bytesToInt(bytes, i, 3);
					}
				}
				break;
			default:
				System.err.println("Cannot read WAVs with " + bitsPerSample + " bits per sample!");
				break;
		}
	}

	/**
	 * Normalize the volume to the volume offered by a 16 bit WAV file
	 */
	public void normalizeTo16Bits() {
		initialize();

		switch (bitsPerSample) {
			case 8:
				for (int i = 0; i < leftData.length; i++) {
					leftData[i] = leftData[i] << 8;
					if (numberOfChannels > 1) {
						rightData[i] = rightData[i] << 8;
					}
				}
				break;
			case 24:
				for (int i = 0; i < leftData.length; i++) {
					leftData[i] = leftData[i] >>> 8;
					if (leftData[i] > 8*16*16*16) {
						leftData[i] -= 16*16*16*16;
					}
					if (numberOfChannels > 1) {
						rightData[i] = rightData[i] >>> 8;
						if (rightData[i] > 8*16*16*16) {
							rightData[i] -= 16*16*16*16;
						}
					}
				}
				break;
		}

		// we renormalized to 16
		bitsPerSample = 16;
	}

	public SoundData getSoundData() {
		return new SoundData(getLeftData(), getRightData());
	}

	public void setSoundData(SoundData soundData) {
		setLeftData(soundData.getLeftData());
		setRightData(soundData.getRightData());
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
		monoData = null;
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
		monoData = null;
	}

	public int[] getMonoData() {
		initialize();
		if (monoData == null) {
			monoData = new int[leftData.length];
			for (int i = 0; i < leftData.length; i++) {
				if (numberOfChannels > 1) {
					monoData[i] = (leftData[i] + rightData[i]) / 2;
				} else {
					monoData[i] = leftData[i];
				}
			}
		}
		return monoData;
	}

	public void adjustVolume(double factor) {

		initialize();

		for (int i = 0; i < leftData.length; i++) {
			leftData[i] = (int) (leftData[i] * factor);
		}

		if (numberOfChannels < 2) {
			return;
		}

		for (int i = 0; i < rightData.length; i++) {
			rightData[i] = (int) (rightData[i] * factor);
		}
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

	/**
	 * Takes a position in milliseconds and returns the exact offset into the int array at which
	 * this time is occurring in the song data
	 */
	public int millisToChannelPos(long posInMillis) {
		int bytesPerSample = bitsPerSample / 8;
		return (int) ((posInMillis * byteRate) / (1000l * bytesPerSample * numberOfChannels));
	}

	/**
	 * Takes an offset into the int array and returns the time in milliseconds at which it gets
	 * playes
	 */
	public int channelPosToMillis(long channelPos) {
		int bytesPerSample = bitsPerSample / 8;
		return (int) ((channelPos * 1000l * bytesPerSample * numberOfChannels) / byteRate);
	}

	public void save() {

		initialize();

		// prepare settings that we actually know how to write
		bitsPerSample = 16;
		int dataSize = leftData.length * 2;
		if (numberOfChannels < 1) {
			numberOfChannels = 1;
		}
		if (numberOfChannels > 1) {
			numberOfChannels = 2;
			dataSize *= 2;
		}
		audioFormat = 1;

		// create bytes based on data
		byte[] bytes = new byte[44 + dataSize];

		// RIFF
		bytes[0] = 'R';
		bytes[1] = 'I';
		bytes[2] = 'F';
		bytes[3] = 'F';

		// overall size
		BitUtils.intToBytes(36 + dataSize, bytes, 4);

		// WAVE
		bytes[8] = 'W';
		bytes[9] = 'A';
		bytes[10] = 'V';
		bytes[11] = 'E';

		// fmt
		bytes[12] = 'f';
		bytes[13] = 'm';
		bytes[14] = 't';
		bytes[15] = ' ';

		// fmt subchunk size
		BitUtils.intToBytes(16, bytes, 16);

		// fmt info
		BitUtils.intToBytes(audioFormat, bytes, 20, 2);
		BitUtils.intToBytes(numberOfChannels, bytes, 22, 2);
		BitUtils.intToBytes(sampleRate, bytes, 24);
		BitUtils.intToBytes(byteRate, bytes, 28);
		BitUtils.intToBytes(4, bytes, 32, 2); // block align
		BitUtils.intToBytes(bitsPerSample, bytes, 34, 2);

		// data
		bytes[36] = 'd';
		bytes[37] = 'a';
		bytes[38] = 't';
		bytes[39] = 'a';

		BitUtils.intToBytes(dataSize, bytes, 40);

		if (numberOfChannels < 2) {
			for (int i = 0; i < leftData.length; i++) {
				BitUtils.intToBytes(leftData[i], bytes, 44+(i*2), 2);
			}
		} else {
			for (int i = 0; i < leftData.length; i++) {
				BitUtils.intToBytes(leftData[i], bytes, 44+(i*4), 2);
				BitUtils.intToBytes(rightData[i], bytes, 44+(i*4)+2, 2);
			}
		}

		super.saveContent(bytes);
	}

	@Override
	public String toString() {
		return "com.asofterspace.toolbox.io.WavFile: " + filename;
	}

}
