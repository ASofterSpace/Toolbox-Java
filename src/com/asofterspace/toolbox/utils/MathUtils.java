/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * A utility class for mathematical fun
 *
 * @author Moya (a softer space), 2019
 */
public class MathUtils {

	private static Random rand = null;


	public static int max(int a, int b) {
		if (a > b) {
			return a;
		} else {
			return b;
		}
	}

	public static int min(int a, int b) {
		if (a < b) {
			return a;
		} else {
			return b;
		}
	}

	// this gets the smaller value that is zero or above, and returns -1 if neither is zero or above -
	// which is especially helpful for getting the smallest of several indexOf() calls
	public static int getMinimumIndex(int a, int b) {
		if (a < 0) {
			if (b < 0) {
				return min(a, b); // both negative: smallest
			} else {
				return b; // only b positive or zero
			}
		} else {
			if (b < 0) {
				return a; // only a positive or zero
			} else {
				return min(a, b); // both positive or zero: smallest
			}
		}
	}

	public static int max(int a, int b, int c) {
		if (a > b) {
			if (a > c) {
				return a;
			} else {
				return c;
			}
		} else {
			if (b > c) {
				return b;
			} else {
				return c;
			}
		}
	}

	public static int min(int a, int b, int c) {
		if (a < b) {
			if (a < c) {
				return a;
			} else {
				return c;
			}
		} else {
			if (b < c) {
				return b;
			} else {
				return c;
			}
		}
	}

	// this gets the smaller value that is zero or above, and returns -1 if neither is zero or above -
	// which is especially helpful for getting the smallest of several indexOf() calls
	public static int getMinimumIndex(int a, int b, int c) {
		if (a < 0) {
			if (b < 0) {
				if (c < 0) {
					return min(a, b, c); // all three negative: smallest
				} else {
					return c; // only c positive or zero
				}
			} else {
				if (c < 0) {
					return b; // only b positive or zero
				} else {
					return min(b, c); // both b and c positive or zero: smallest
				}
			}
		} else {
			if (b < 0) {
				if (c < 0) {
					return a; // only a positive or zero
				} else {
					return min(a, c); // both a and c positive or zero: smallest
				}
			} else {
				if (c < 0) {
					return min(a, b); // both a and b positive or zero: smallest
				} else {
					return min(a, b, c); // all three positive or zero: smallest
				}
			}
		}
	}

	public static int max(int a, int b, int c, int d) {
		if (a > b) {
			if (a > c) {
				if (a > d) {
					return a;
				} else {
					return d;
				}
			} else {
				if (c > d) {
					return c;
				} else {
					return d;
				}
			}
		} else {
			if (b > c) {
				if (b > d) {
					return b;
				} else {
					return d;
				}
			} else {
				if (c > d) {
					return c;
				} else {
					return d;
				}
			}
		}
	}

	public static int min(int a, int b, int c, int d) {
		if (a < b) {
			if (a < c) {
				if (a < d) {
					return a;
				} else {
					return d;
				}
			} else {
				if (c < d) {
					return c;
				} else {
					return d;
				}
			}
		} else {
			if (b < c) {
				if (b < d) {
					return b;
				} else {
					return d;
				}
			} else {
				if (c < d) {
					return c;
				} else {
					return d;
				}
			}
		}
	}

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
		if ((dividend % divisor) * 2 > divisor) {
			result++;
		}
		return result;
	}

	public static long divideLongs(long dividend, long divisor) {
		long result = dividend / divisor;
		if ((dividend % divisor) * 2 > divisor) {
			result++;
		}
		return result;
	}

	/**
	 * Gets an average of a list of integers, assuming none of them are null (!)
	 * Returns 0 for an empty list
	 * Only assured to work if the entries of the input list stay within 25% of
	 * the int range - the further they escape (up or down), the more likely it
	 * is that overflows will occur
	 * (and even then - it will work-ish but can easily be off due to internal
	 * rounding errors)
	 */
	public static int averageFast(List<Integer> lotsOfInts) {
		return averageFastFromTo(lotsOfInts, 0, lotsOfInts.size() - 1);
	}

	private static int averageFastFromTo(List<Integer> lotsOfInts, int from, int to) {
		if (to - from < 4) {
			switch (to - from) {
				case 3:
					return (lotsOfInts.get(from) + lotsOfInts.get(from + 1) +
						lotsOfInts.get(from + 2) + lotsOfInts.get(to)) / 4;
				case 2:
					return (lotsOfInts.get(from) + lotsOfInts.get(from + 1) + lotsOfInts.get(to)) / 3;
				case 1:
					return (lotsOfInts.get(from) + lotsOfInts.get(to)) / 2;
				case 0:
					return lotsOfInts.get(from);
				default:
					return 0;
			}
		}
		int mid = (from + to) / 2;
		return (averageFastFromTo(lotsOfInts, from, mid) + averageFastFromTo(lotsOfInts, mid, to)) / 2;
	}

	/**
	 * should be slower than averageFast, but have less problems with overflows and rounding,
	 * as everything is done in doubles and rounding is only applied a single time in the end
	 */
	public static int averageSlow(List<Integer> lotsOfInts) {
		return (int) averageSlowFromTo(lotsOfInts, 0, lotsOfInts.size() - 1);
	}

	private static double averageSlowFromTo(List<Integer> lotsOfInts, int from, int to) {
		if (to - from < 4) {
			switch (to - from) {
				case 3:
					return (0.0 + lotsOfInts.get(from) + lotsOfInts.get(from + 1) +
						lotsOfInts.get(from + 2) + lotsOfInts.get(to)) / 4;
				case 2:
					return (0.0 + lotsOfInts.get(from) + lotsOfInts.get(from + 1) + lotsOfInts.get(to)) / 3;
				case 1:
					return (0.0 + lotsOfInts.get(from) + lotsOfInts.get(to)) / 2;
				case 0:
					return 0.0 + lotsOfInts.get(from);
				default:
					return 0.0;
			}
		}
		int mid = (from + to) / 2;
		return (averageSlowFromTo(lotsOfInts, from, mid) + averageSlowFromTo(lotsOfInts, mid, to)) / 2;
	}

	public static int zeroIfNull(Integer value) {
		if (value == null) {
			return 0;
		}
		return (int) value;
	}

	public static boolean equals(Integer left, Integer right) {
		if (left == null) {
			return right == null;
		}
		if (right == null) {
			return left == null;
		}
		return (int) left == (int) right;
	}

	/**
	 * Gets a random true or false value
	 */
	public static boolean randomBoolean() {
		return randomInteger(2) == 0;
	}

	/**
	 * Gets an integer between 0 (inclusive) and under (exclusive),
	 * so e.g. if 10 is put in, then the possibile results are:
	 * 0, 1, 2, 3, 4, 5, 6, 7, 8, 9
	 */
	public static int randomInteger(int under) {
		if (rand == null) {
			rand = new Random();
		}

		if (under < 1) {
			return 0;
		}

		return rand.nextInt(under);
	}

	/**
	 * Gets a double between 0 (inclusive) and 1 (exclusive)
	 */
	public static double randomDouble() {
		if (rand == null) {
			rand = new Random();
		}

		return rand.nextDouble();
	}

	/**
	 * Gets a double between 0 (inclusive) and under (exclusive)
	 */
	public static double randomDouble(double under) {
		if (rand == null) {
			rand = new Random();
		}

		return under * rand.nextDouble();
	}

	/**
	 * Actually perform the mathorg calculation by first checking if there are brackets present, and if so,
	 * recursively removing them by calculating inner parts, and if not, by splitting the input on math ops
	 * and applying one after the other
	 * (the input is called vars as this was directly translated from datacomx source code)
	 */
	public static String calculateMathStr(String str) {

		str = StrUtils.replaceAll(str, "pi", "(3,141592653589793238)");
		str = StrUtils.replaceAll(str, ".", ",");
		str = StrUtils.replaceAll(str, "•", "*");
		str = StrUtils.replaceAll(str, "·", "*");
		str = StrUtils.replaceAll(str, "x", "*");
		str = StrUtils.replaceAll(str, "×", "*");
		str = StrUtils.replaceAll(str, "-/", "\\");
		str = StrUtils.replaceAll(str, "/", ":");

		str = calculateMathStrInternal(str);

		str = StrUtils.replaceAll(str, ",", ".");

		if (str.endsWith(".0")) {
			str = str.substring(0, str.length() - 2);
		}

		return str;
	}

	private static String calculateMathStrInternal(String vars) {

		if (vars.contains("(")) {

			int lastStartPos = -1;
			for (int i = 0; i < vars.length(); i++) {
				char c = vars.charAt(i);
				switch (c) {
					case '(':
						lastStartPos = i;
						break;
					case ')':
						if (lastStartPos >= 0) {
							return calculateMathStr(
								vars.substring(0, lastStartPos) +
								calculateMathStr(vars.substring(lastStartPos + 1, i)) +
								vars.substring(i + 1)
							);
						}
						return "ERROR: Encountered unmatched ')'!";
				}
			}
			return "ERROR: Encountered unmatched '('!";
		}

		if (vars.contains(")")) {
			return "ERROR: Encountered unmatched ')'!";
		}

		vars = StrUtils.replaceAll(vars, " ", "");
		vars = StrUtils.replaceAll(vars, "\t", "");
		vars = StrUtils.replaceAll(vars, "/", ":");
		vars = StrUtils.replaceAll(vars, "-", "+-");
		vars = StrUtils.replaceAll(vars, "*+-", "*-");
		vars = StrUtils.replaceAll(vars, ":+-", ":-");
		while (vars.contains("++")) {
			vars = StrUtils.replaceAll(vars, "++", "+");
		}
		while (vars.startsWith("+")) {
			if (vars.length() > 1) {
				vars = vars.substring(1);
			} else {
				return "ERROR: No input!";
			}
		}

		// ensure the first term starts with a plus
		vars = "+" + vars;

		// we split the string that is operated on into terms, with each term starting with a math operation
		// followed by a number (where the number itself may contain the char '\' to indicate root, so e.g.
		// a term can be +1 (add one) or :4 (divide by four) or -3/9 (minus the third root of nine))
		List<String> terms = new ArrayList<>();
		String currentTerm = "";
		for (int i = 0; i < vars.length(); i++) {
			char c = vars.charAt(i);
			switch (c) {
				case '*':
				case ':':
				case '+':
				case '^':
					terms.add(currentTerm);
					currentTerm = "" + c;
					break;
				default:
					currentTerm += c;
					break;
			}
		}
		terms.add(currentTerm);

		// iterate over all terms and resolve potentially existing roots
		for (int i = 0; i < terms.size(); i++) {
			String curTerm = terms.get(i);
			int pos = curTerm.indexOf("\\");
			// \ cannot be the first letter, as that is the math op
			if (pos > 0) {
				// if it is the second letter, we have a square root
				Double whichRoot = 2.0;
				if (pos > 1) {
					// if it is the third letter or later, we have a special root
					String whichRootStr = curTerm.substring(2, pos);
					whichRoot = StrUtils.strToDouble(whichRootStr);
					if (whichRoot == null) {
						return "ERROR: '" + whichRootStr + "'-th root cannot be parsed!";
					}
				}
				String underRootStr = curTerm.substring(pos + 1);
				Double underRoot = StrUtils.strToDouble(underRootStr);
				if (underRoot == null) {
					return "ERROR: Term under root '" + underRootStr + "' cannot be parsed!";
				}
				terms.set(i, "" + curTerm.charAt(0) + Math.pow(underRoot, 1 / whichRoot));
			}
		}

		// perform the actual computation of the terms
		double result = 0.0;

		for (String term : terms) {

			// a term that is just an op without a number does not do anything
			if (term.length() < 2) {
				continue;
			}

			char ops = term.charAt(0);

			Double termNum = StrUtils.strToDouble(term.substring(1));
			if (termNum == null) {
				return "ERROR: The term '" + term.substring(1) + "' could not be parsed!";
			}

			switch (ops) {
				case '+':
					result += termNum;
					break;
				case '*':
					result *= termNum;
					break;
				case ':':
					result /= termNum;
					break;
				case '^':
					result = Math.pow(result, termNum);
					break;
			}
		}

		return StrUtils.doubleToStr(result);
	}

}
