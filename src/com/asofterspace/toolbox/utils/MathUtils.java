/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.utils;


/**
 * A utility class for mathematical fun
 *
 * @author Moya (a softer space), 2019
 */
public class MathUtils {

	public static int[] findMaxima(double[] data, int amount) {

		double[] invertedData = new double[data.length];

		// finding maxima is like finding minima...
		for (int i = 0; i < data.length; i++) {
			// ... just the other way around!
			invertedData[i] = - data[i];
		}

		return findMinima(invertedData, amount);
	}

	/**
	 * This finds a given amount of minima in a real-life dataset,
	 * returning the indices of the minimal values.
	 * Here, areas of white noise are ignored even if they are lower
	 * than the minima that we are trying to find - so we are not
	 * trying to find the overall absolute minimal values of the data
	 * (for that we could just report to lowest values in the array,
	 * and be done) - but instead we are reporting which values, to
	 * a human, look like they are minima somewhere within the graph
	 */
	public static int[] findMinima(double[] data, int amount) {

		// how large of a neighbourhood are we looking at, surrounding a minimum / maximum?
		final int NEIGHBOURHOOD_SIZE = 9;

		int[] result = new int[amount];
		double[] resultDistances = new double[amount];

		for (int i = 0; i < amount; i++) {
			result[i] = -1;
			resultDistances[i] = 0;
		}

		for (int i = NEIGHBOURHOOD_SIZE; i < data.length - NEIGHBOURHOOD_SIZE; i++) {

			// do we have a local minimum (e.g. a value that is lower than its neighbourhood)?
			boolean localMinimum = true;
			for (int j = i - NEIGHBOURHOOD_SIZE; j < i + NEIGHBOURHOOD_SIZE + 1; j++) {
				if (data[i] > data[j]) {
					localMinimum = false;
					break;
				}
			}

			if (localMinimum) {
				// we have a local minimum!
				// now calculate how much of a minimum it is - for this, look at the neighbourhood
				// again, but this time actually calculate the distances and add them...
				double distance = 0;
				for (int j = i - NEIGHBOURHOOD_SIZE; j < i + NEIGHBOURHOOD_SIZE + 1; j++) {
					distance += data[j] - data[i];
				}

				// check if this distance is better (larger) than the ones previously recorded
				for (int a = 0; a < amount; a++) {
					if ((result[a] == -1) || (resultDistances[a] < distance)) {

						// our result is better than the one at position a!
						// so move the others...
						for (int b = amount-1; b > a; b--) {
							result[b] = result[b-1];
							resultDistances[b] = resultDistances[b-1];
						}

						// ... and insert this one...
						result[a] = i;
						resultDistances[a] = distance;

						// ... and, finally, step further right as we want to ignore
						// additional minima at the same height as the one we already
						// found around the neighbourhood (there can be none that is
						// even lower, as otherwise we would not have had a local
						// minimum, but there can possibly be one at the exact same
						// height which might then be reported as second hit, although
						// it is basically the same minimum)
						i += NEIGHBOURHOOD_SIZE;

						break;
					}
				}
			}
		}

		return result;
	}

	/**
	 * Calculates the result of dividend / divisor - that is, this divides one int by
	 * another int and does so cheaply (so, quickly) but still kind-of-correctly -
	 * that is, instead of just throwing away the remainder, we do check for it and
	 * round up if it is high enough
	 */
	public static int divideInts(int dividend, int divisor) {
		int result = dividend / divisor;
		if (dividend % divisor > divisor / 2) {
			result++;
		}
		return result;
	}

}
