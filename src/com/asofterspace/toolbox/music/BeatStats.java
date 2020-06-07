/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.music;

import java.util.List;


public class BeatStats {

	private long averageLength = 0;
	private long averageLoudness = 0;
	private long averageJigglieness = 0;
	private long maxLength = 0;
	private long maxLoudness = 0;
	private long maxJigglieness = 0;


	public BeatStats(List<Beat> beats) {

		averageLength = 0;
		averageLoudness = 0;
		averageJigglieness = 0;
		maxLength = 0;
		maxLoudness = 0;
		maxJigglieness = 0;

		for (Beat beat : beats) {
			averageLength += beat.getLength();
			averageLoudness += beat.getLoudness();
			averageJigglieness += beat.getJigglieness();
			if (beat.getLength() > maxLength) {
				maxLength = beat.getLength();
			}
			if (beat.getLoudness() > maxLoudness) {
				maxLoudness = beat.getLoudness();
			}
			if (beat.getJigglieness() > maxJigglieness) {
				maxJigglieness = beat.getJigglieness();
			}
		}

		averageLength = averageLength / beats.size();
		averageLoudness = averageLoudness / beats.size();
		averageJigglieness = averageJigglieness / beats.size();
	}

	public long getAverageLength() {
		return averageLength;
	}

	public long getAverageLoudness() {
		return averageLoudness;
	}

	public long getAverageJigglieness() {
		return averageJigglieness;
	}

	public long getMaxLength() {
		return maxLength;
	}

	public long getMaxLoudness() {
		return maxLoudness;
	}

	public long getMaxJigglieness() {
		return maxJigglieness;
	}

}
