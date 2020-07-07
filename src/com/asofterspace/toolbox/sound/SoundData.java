/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.sound;


/**
 * This represents some SoundData, e.g. an entire song contained in a WAV file
 */
public class SoundData {

	// this is the loudest sound that can be represented:
	// 16^4 (so 16*16*16*16) is available for all, but the volume is signed
	// so we actually have (16^4)/2, and because it is signed, the very largest
	// value has to be avoided, so it is (16^4)/2 - 1...
	private final static long SOUND_MAX = (8l*16*16*16) - 1;

	private int[] leftData;

	private int[] rightData;


	public SoundData(int[] leftData, int[] rightData) {
		this.leftData = leftData;
		this.rightData = rightData;
	}

	public SoundData copy() {
		int[] newLeftData = new int[leftData.length];
		System.arraycopy(leftData, 0, newLeftData, 0, leftData.length);
		int[] newRightData = new int[rightData.length];
		System.arraycopy(leftData, 0, newRightData, 0, rightData.length);
		return new SoundData(newLeftData, newRightData);
	}

	public int getLength() {
		if (leftData == null) {
			return 0;
		}
		return leftData.length;
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

	public int getMax() {

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
		return max;
	}

	public void normalize() {

		int max = getMax();
		for (int i = 0; i < leftData.length; i++) {
			leftData[i] = (int) ((leftData[i] * SOUND_MAX) / max);
			rightData[i] = (int) ((rightData[i] * SOUND_MAX) / max);
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

	public int[] getLeftFourier(int from, int to) {
		return getFourierForData(getLeftData(), from, to);
	}

	public int[] getRightFourier(int from, int to) {
		return getFourierForData(getRightData(), from, to);
	}

	public int[] getFourier(int from, int to) {
		int[] result = getLeftFourier(from, to);
		int[] resultRight = getRightFourier(from, to);
		for (int i = 0; i < resultRight.length; i++) {
			result[i] += resultRight[i];
		}
		return result;
	}

	// takes only left channel as input, and smooshes the output into 20% of its size,
	// and only outputs the lower half (as the upper half is a symmetrical copy anyway)
	public int[] getSmallFourier(int from, int to) {
		return getSmallFourierForData(getLeftData(), from, to);
	}

	private static int[] getFourierForData(int[] data, int from, int to) {

		int len = to - from;

		if (len < 1) {
			return new int[0];
		}

		int[] fourierInput = new int[len];

		System.arraycopy(data, from, fourierInput, 0, len);

		return getFourierForData(fourierInput);
	}

	/**
	 * Gets the slow, basic Fourier transform for some data
	 */
	private static int[] getFourierForData(int[] data) {
		int[] result = new int[data.length];
		double[] doubleData = new double[data.length];

		for (int i = 0; i < data.length; i++) {
			doubleData[i] = data[i];
		}

		for (int i = 0; i < result.length; i++) {
			double curReal = 0;
			double curImaginary = 0;
			for (int n = 0; n < doubleData.length; n++) {
				double expAngle = (2.0 * Math.PI * i * n) / doubleData.length;
				double realPart = Math.cos(expAngle) * doubleData[n];
				double imaginaryPart = Math.sin(expAngle) * doubleData[n];
				curReal += realPart;
				curImaginary += imaginaryPart;
			}
			curReal = 2 * curReal / doubleData.length;
			curImaginary = 2 * curImaginary / doubleData.length;
			result[i] = (int) (i * Math.sqrt(curReal*curReal + curImaginary*curImaginary));
		}

		return result;
	}

	private static int[] getSmallFourierForData(int[] data, int from, int to) {

		if (to > data.length) {
			to = data.length;
		}

		int len = to - from;
		int SMALLNESS_FACTOR = 5;

		// we divide by two additionally as the upper half is just a copy of the lower half of the output
		int[] result = new int[len / (2 * SMALLNESS_FACTOR)];
		double[] doubleData = new double[len];

		for (int i = from; i < to; i++) {
			doubleData[i - from] = data[i];
		}

		for (int i = 0; i < result.length; i++) {
			result[i] = 0;
		}

		for (int i = 0; i < len; i++) {
			if (i/SMALLNESS_FACTOR >= result.length) {
				break;
			}
			double curReal = 0;
			double curImaginary = 0;
			for (int n = 0; n < len; n++) {
				double expAngle = (2.0 * Math.PI * i * n) / len;
				double realPart = Math.cos(expAngle) * doubleData[n];
				double imaginaryPart = Math.sin(expAngle) * doubleData[n];
				curReal += realPart;
				curImaginary += imaginaryPart;
			}
			curReal = 2 * curReal / len;
			curImaginary = 2 * curImaginary / len;
			result[i/SMALLNESS_FACTOR] += (int) (i * Math.sqrt(curReal*curReal + curImaginary*curImaginary));
		}

		return result;
	}
}
