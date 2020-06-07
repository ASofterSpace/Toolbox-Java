/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.music;



/**
 * This represents some SoundData, e.g. an entire song contained in a WAV file
 */
public class SoundData {

	private int[] leftData;

	private int[] rightData;


	public SoundData(int[] leftData, int[] rightData) {
		this.leftData = leftData;
		this.rightData = rightData;
	}

	public int[] getLeftData() {
		return leftData;
	}

	public void setLeftData(int[] leftData) {
		this.leftData = leftData;
	}

	public int[] getRightData() {
		return rightData;
	}

	public void setRightData(int[] rightData) {
		this.rightData = rightData;
	}

	public void normalize() {

		int max = 0;
		for (int i = 0; i < leftData.length; i++) {
			if (leftData[i] > max) {
				max = leftData[i];
			}
			if (-leftData[i] > max) {
				max = -leftData[i];
			}
			if (rightData[i] > max) {
				max = rightData[i];
			}
			if (-rightData[i] > max) {
				max = -rightData[i];
			}
		}
		for (int i = 0; i < leftData.length; i++) {
			leftData[i] = (int) ((leftData[i] * (long) 8*16*16*16) / max);
			rightData[i] = (int) ((rightData[i] * (long) 8*16*16*16) / max);
		}
	}

	public void scale(double scaleFactor) {
		for (int i = 0; i < leftData.length; i++) {
			leftData[i] = (int) (scaleFactor * leftData[i]);
			rightData[i] = (int) (scaleFactor * rightData[i]);
		}
	}

	public void trim() {
		int max = 0;
		int min = 0;
		for (int i = 0; i < leftData.length; i++) {
			if (leftData[i] > max) {
				max = leftData[i];
			}
			if (leftData[i] < min) {
				min = leftData[i];
			}
			if (rightData[i] > max) {
				max = rightData[i];
			}
			if (rightData[i] < min) {
				min = rightData[i];
			}
		}
		if (- min > max) {
			max = - min;
		}
		max = max / 3276;
		int noiseStart = 0;
		int noiseLength = 0;
		for (int i = 0; i < leftData.length; i++) {
			int val = leftData[i];
			if (val < 0) {
				val = - val;
			}
			if (val > max) {
				noiseStart = i;
				break;
			}
			val = rightData[i];
			if (val < 0) {
				val = - val;
			}
			if (val > max) {
				noiseStart = i;
				break;
			}
		}
		for (int i = noiseStart; i < leftData.length; i++) {
			int val = leftData[i];
			if (val < 0) {
				val = - val;
			}
			if (val > max) {
				noiseLength = i - noiseStart;
			}
			val = rightData[i];
			if (val < 0) {
				val = - val;
			}
			if (val > max) {
				noiseLength = i - noiseStart;
			}
		}
		int[] newLeft = new int[noiseLength];
		int[] newRight = new int[noiseLength];
		for (int i = 0; i < noiseLength; i++) {
			newLeft[i] = leftData[i + noiseStart];
			newRight[i] = rightData[i + noiseStart];
		}
		leftData = newLeft;
		rightData = newRight;
	}
}
