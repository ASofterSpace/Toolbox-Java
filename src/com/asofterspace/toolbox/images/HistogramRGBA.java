/**
 * Unlicensed code created by A Softer Space, 2024
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.images;



/**
 * A histogram of color channels which can be created by the Image class
 * and which can be displayed as image in turn
 *
 * @author Moya, 2024
 */
public class HistogramRGBA {

	private int bucketNum;

	private int[] bucketsR;
	private int[] bucketsG;
	private int[] bucketsB;
	private int[] bucketsA;


	public HistogramRGBA(int bucketAmount) {
		if (bucketAmount < 1) {
			bucketAmount = 1;
		}
		this.bucketNum = bucketAmount;
		this.bucketsR = new int[bucketAmount];
		this.bucketsG = new int[bucketAmount];
		this.bucketsB = new int[bucketAmount];
		this.bucketsA = new int[bucketAmount];
		for (int i = 0; i < bucketAmount; i++) {
			bucketsR[i] = 0;
			bucketsG[i] = 0;
			bucketsB[i] = 0;
			bucketsA[i] = 0;
		}
	}

	public void encounterPixel(ColorRGBA col) {
		this.bucketsR[(col.getR() * bucketNum) / 256]++;
		this.bucketsG[(col.getG() * bucketNum) / 256]++;
		this.bucketsB[(col.getB() * bucketNum) / 256]++;
		this.bucketsA[(col.getA() * bucketNum) / 256]++;
	}

	public int getBucketAmount() {
		return bucketNum;
	}

	public int[] getBucketsR() {
		return bucketsR;
	}

	public int[] getBucketsG() {
		return bucketsG;
	}

	public int[] getBucketsB() {
		return bucketsB;
	}

	public int[] getBucketsA() {
		return bucketsA;
	}

}
