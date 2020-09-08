/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;


/**
 * Utility class for sorting and in general re-ordering lists
 *
 * In general, functions in here do NOT change the lists passed in (different from the behavior
 * of the Java Collections API), but are returning new lists containing the results
 */
public class SortUtils  {

	private static Random rand;


	public static <T> List<T> shuffle(List<T> listToShuffle) {
		if (rand == null) {
			rand = new Random();
		}
		List<T> newList = new ArrayList<>();
		if (listToShuffle == null) {
			return newList;
		}
		List<T> removeList = new ArrayList<>();
		removeList.addAll(listToShuffle);
		while (removeList.size() > 0) {
			int randIndex = rand.nextInt(removeList.size());
			newList.add(removeList.get(randIndex));
			removeList.remove(randIndex);
		}
		return newList;
	}

	public static <T> List<T> reverse(List<T> listToReverse) {
		List<T> newList = new ArrayList<>();
		if (listToReverse == null) {
			return newList;
		}
		newList.addAll(listToReverse);
		Collections.reverse(newList);
		return newList;
	}

	public static <T> List<T> sort(List<T> listToSort, SortOrder sortOrder) {
		return sort(listToSort, sortOrder, null);
	}

	public static <T> List<T> sort(List<T> listToSort, SortOrder sortOrder, Stringifier<T> stringifier) {
		if (sortOrder == null) {
			sortOrder = SortOrder.ALPHABETICAL;
		}
		switch (sortOrder) {
			case NUMERICAL:
				return sortNumerically(listToSort, stringifier);
			default:
				return sortAlphabetically(listToSort, stringifier);
		}
	}

	public static <T> List<T> sortAlphabetically(List<T> listToSort) {
		return sortAlphabetically(listToSort, null);
	}

	public static <T> List<T> sortAlphabetically(List<T> listToSort, Stringifier<T> stringifier) {

		List<T> newList = new ArrayList<>();

		if (listToSort == null) {
			return newList;
		}

		newList.addAll(listToSort);

		if (stringifier == null) {
			stringifier = new StringifierLowCase<T>();
		}

		final Stringifier<T> finalifier = stringifier;

		Collections.sort(newList, new Comparator<T>() {

			public int compare(T a, T b) {
				String bLow = finalifier.getString(b);
				String aLow = finalifier.getString(a);
				if (aLow == null) {
					return -1;
				}
				if (bLow == null) {
					return 1;
				}
				return aLow.compareTo(bLow);
			}
		});

		return newList;
	}

	public static <T> List<T> sortNumerically(List<T> listToSort) {
		return sortNumerically(listToSort, null);
	}

	public static <T> List<T> sortNumerically(List<T> listToSort, Stringifier<T> stringifier) {

		List<T> newList = new ArrayList<>();

		if (listToSort == null) {
			return newList;
		}

		newList.addAll(listToSort);

		if (stringifier == null) {
			stringifier = new StringifierLowCase<T>();
		}

		final Stringifier<T> finalifier = stringifier;

		Collections.sort(newList, new Comparator<T>() {

			public int compare(T a, T b) {
				// we want to sort such that ISSUE-2 comes before ISSUE-11, which comes before ISSUE-300...
				// soooo let's see first of all if the first character that is different is a digit -
				// if no, then we can just sort regularly as the letters are sorted alphabetically, fine...
				// if yes, then we carry on gobbling digits, and when there are no more, then we want to
				// add zeroes and THEN compare...
				String bLow = finalifier.getString(b);
				String aLow = finalifier.getString(a);
				for (int i = 0; i < bLow.length(); i++) {
					if (i >= aLow.length()) {
						return -1;
					}
					char ca = aLow.charAt(i);
					char cb = bLow.charAt(i);
					if (Character.isDigit(ca) && Character.isDigit(cb)) {
						bLow = bLow.substring(i);
						aLow = aLow.substring(i);
						int aj = 0;
						int bj = 0;
						for (; bj < bLow.length(); bj++) {
							if (!Character.isDigit(bLow.charAt(bj))) {
								break;
							}
						}
						for (; aj < aLow.length(); aj++) {
							if (!Character.isDigit(aLow.charAt(aj))) {
								break;
							}
						}
						while (aj < bj) {
							aLow = "0" + aLow;
							aj++;
						}
						while (bj < aj) {
							bLow = "0" + bLow;
							bj++;
						}
						for (int j = 0; j < aj; j++) {
							if (aLow.charAt(j) != bLow.charAt(j)) {
								return aLow.charAt(j) - bLow.charAt(j);
							}
						}
						aLow = aLow.substring(aj);
						bLow = bLow.substring(bj);
						i = -1;
					} else {
						if (ca == cb) {
							continue;
						}
						return ca - cb;
					}
				}

				return 1;
			}
		});

		return newList;
	}
}
