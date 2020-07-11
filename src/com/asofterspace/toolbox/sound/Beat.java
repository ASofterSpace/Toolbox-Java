/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.sound;


public class Beat {

	// where in the sound data is the beat located?
	private int position;

	// how long is this beat?
	private int length;

	// who loud is the entire beat overall?
	private long loudness;

	// how jiggly is the beat (that is, how many up/down and down/up reversals are there within it?)
	private long jigglieness;

	// how intense is the beat (that is, how much louder are the first 20% compared to the last 20%?)
	private long intensity;

	// is this beat aligned with some other beat somewhere else? ^^
	private boolean isAligned;

	// has the beat been changed in some way?
	private boolean changed;


	public Beat(int position) {
		this.position = position;
		this.isAligned = false;
		this.changed = false;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public long getLoudness() {
		return loudness;
	}

	public void setLoudness(long loudness) {
		this.loudness = loudness;
	}

	public long getJigglieness() {
		return jigglieness;
	}

	public void setJigglieness(long jigglieness) {
		this.jigglieness = jigglieness;
	}

	public long getIntensity() {
		return intensity;
	}

	public void setIntensity(long intensity) {
		this.intensity = intensity;
	}

	public boolean getIsAligned() {
		return isAligned;
	}

	public void setIsAligned(boolean isAligned) {
		this.isAligned = isAligned;
	}

	public boolean getChanged() {
		return changed;
	}

	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	public String toString() {
		return "Beat at " + position + " [length: " + length + ", loudness: " +
			loudness + ", jigglieness: " + jigglieness + "]";
	}
}
