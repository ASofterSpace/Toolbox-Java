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
						// and insert this one...
						result[a] = i;
						resultDistances[a] = distance;
						break;
					}
				}
			}
		}

		return result;
	}

}
