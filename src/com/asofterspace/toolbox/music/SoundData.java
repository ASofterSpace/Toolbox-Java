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

	public int[] getLeftDataCopy() {
		int[] result = new int[leftData.length];
		for (int i = 0; i < leftData.length; i++) {
			result[i] = leftData[i];
		}
		return result;
	}

	public void setLeftData(int[] leftData) {
		this.leftData = leftData;
	}

	public int[] getRightData() {
		return rightData;
	}

	public int[] getRightDataCopy() {
		int[] result = new int[rightData.length];
		for (int i = 0; i < rightData.length; i++) {
			result[i] = rightData[i];
		}
		return result;
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

	/**
	 * Adds a fade out effect to the end of this sound
	 */
	public void fadeOut(int durationInPos) {
		fadeOut(durationInPos, leftData.length - durationInPos);
	}

	/**
	 * Adds a fade out effect to the position in this sound
	 */
	public void fadeOut(int durationInPos, int position) {

		for (int i = position; i < position + durationInPos; i++) {

			if (i < 0) {
				continue;
			}
			if (i >= leftData.length) {
				break;
			}

			double scaleFactor = (position + durationInPos - i) / (1.0 * durationInPos);
			leftData[i] = (int) (leftData[i] * scaleFactor);
			rightData[i] = (int) (rightData[i] * scaleFactor);
		}
	}

	/**
	 * Adds a fade in effect to the beginning of this sound
	 */
	public void fadeIn(int durationInPos) {
		fadeIn(durationInPos, 0);
	}

	/**
	 * Adds a fade in effect to the position in this sound
	 */
	public void fadeIn(int durationInPos, int position) {

		for (int i = position; i < position + durationInPos; i++) {

			if (i < 0) {
				continue;
			}
			if (i >= leftData.length) {
				break;
			}

			double scaleFactor = (i - position) / (1.0 * durationInPos);
			leftData[i] = (int) (leftData[i] * scaleFactor);
			rightData[i] = (int) (rightData[i] * scaleFactor);
		}
	}

	/**
	 * Trims the song by removing quiet parts at the beginning and end, then adds silence
	 * with the given duration to the end (in units of position in the leftData / rightData
	 * array, not seconds or milliseconds!), but the cool thing is - instead of just silence,
	 * if the trimming cut off a quiet part of the song, that part is reused so the data is
	 * not lost...
	 */
	public void trimAndAdd(int durationToAddInPos) {

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

		max = max / 1024;

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

		noiseLength += durationToAddInPos;

		int[] newLeft = new int[noiseLength];
		int[] newRight = new int[noiseLength];

		for (int i = 0; i < noiseLength; i++) {
			if (i + noiseStart >= leftData.length) {
				newLeft[i] = 0;
				newRight[i] = 0;
			} else {
				newLeft[i] = leftData[i + noiseStart];
				newRight[i] = rightData[i + noiseStart];
			}
		}

		leftData = newLeft;
		rightData = newRight;
	}

	/**
	 * Trims the song by removing quiet parts at the beginning and end
	 */
	public void trim() {
		trimAndAdd(0);
	}
}
