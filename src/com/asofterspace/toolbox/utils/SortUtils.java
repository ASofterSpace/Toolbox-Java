/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;


/**
 * Utility class for sorting and in general re-ordering lists
 *
 * In general, functions in here do NOT change the lists passed in (different from the behavior
 * of the Java Collections API), but are returning new lists containing the results
 */
public class SortUtils  {

	private static Random rand;


	public static <T> List<T> shuffle(Collection<T> listToShuffle) {
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

	public static <T> List<T> reverse(Collection<T> listToReverse) {
		List<T> newList = new ArrayList<>();
		if (listToReverse == null) {
			return newList;
		}
		newList.addAll(listToReverse);
		Collections.reverse(newList);
		return newList;
	}

	public static <T> List<T> randomize(Collection<T> listToReverse) {
		List<T> newList = new ArrayList<>();
		if (listToReverse == null) {
			return newList;
		}
		newList.addAll(listToReverse);
		Collections.shuffle(newList);
		return newList;
	}

	public static <T> List<T> sort(Collection<T> listToSort) {
		return sort(listToSort, SortOrder.ALPHABETICAL, null);
	}

	public static <T> List<T> sort(Collection<T> listToSort, SortOrder sortOrder) {
		return sort(listToSort, sortOrder, null);
	}

	public static <T> List<T> sort(Collection<T> listToSort, SortOrder sortOrder, Stringifier<T> stringifier) {
		if (sortOrder == null) {
			sortOrder = SortOrder.ALPHABETICAL;
		}
		switch (sortOrder) {
			case NUMERICAL:
				return sortNumerically(listToSort, stringifier);
			case REVERSE:
				return reverse(listToSort);
			case RANDOM:
				return randomize(listToSort);
			case ALPHABETICAL_IGNORE_UMLAUTS:
				return sortAlphabetically(listToSort, stringifier, true, false);
			case ALPHABETICAL_IGNORE_ARTICLES:
				return sortAlphabetically(listToSort, stringifier, false, true);
			case ALPHABETICAL_IGNORE_ALL:
				return sortAlphabetically(listToSort, stringifier, true, true);
			default:
				return sortAlphabetically(listToSort, stringifier, false, false);
		}
	}

	public static List<Integer> sortIntegers(Collection<Integer> listToSort) {

		List<Integer> newList = new ArrayList<>();

		if (listToSort == null) {
			return newList;
		}

		newList.addAll(listToSort);

		Collections.sort(newList, new Comparator<Integer>() {

			public int compare(Integer a, Integer b) {
				if (a == null) {
					return -1;
				}
				if (b == null) {
					return 1;
				}
				return a - b;
			}
		});

		return newList;
	}

	public static <T> List<T> sortAlphabetically(Collection<T> listToSort) {
		return sortAlphabetically(listToSort, null);
	}

	public static <T> List<T> sortAlphabetically(Collection<T> listToSort, Stringifier<T> stringifier) {
		return sortAlphabetically(listToSort, stringifier, false, false);
	}

	public static <T> List<T> sortAlphabetically(Collection<T> listToSort, Stringifier<T> stringifier,
		final boolean ignoreUmlauts, final boolean ignoreArticles) {

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

				if (ignoreArticles) {
					aLow = ignoreArticles(aLow);
					bLow = ignoreArticles(bLow);
				}

				if (ignoreUmlauts) {
					aLow = ignoreUmlauts(aLow);
					bLow = ignoreUmlauts(bLow);
				}

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

	private static String ignoreArticles(String result) {
		String resLow = result.toLowerCase();
		if (resLow.startsWith("the ")) {
			return result.substring(4);
		}
		if (resLow.startsWith("a ")) {
			return result.substring(2);
		}
		return result;
	}

	private static String ignoreUmlauts(String result) {
		result = StrUtils.replaceAll(result, "ä", "a");
		result = StrUtils.replaceAll(result, "Ä", "A");
		result = StrUtils.replaceAll(result, "ö", "o");
		result = StrUtils.replaceAll(result, "Ö", "O");
		result = StrUtils.replaceAll(result, "ü", "u");
		result = StrUtils.replaceAll(result, "Ü", "U");
		result = StrUtils.replaceAll(result, "é", "e");
		result = StrUtils.replaceAll(result, "É", "E");
		result = StrUtils.replaceAll(result, "è", "e");
		result = StrUtils.replaceAll(result, "È", "E");
		result = StrUtils.replaceAll(result, "ê", "e");
		result = StrUtils.replaceAll(result, "Ê", "E");
		return result;
	}

	public static <T> List<T> sortNumerically(Collection<T> listToSort) {
		return sortNumerically(listToSort, null);
	}

	public static <T> List<T> sortNumerically(Collection<T> listToSort, Stringifier<T> stringifier) {

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

	public static <T> Set<T> getElementsOnlyOnceInCollection(Collection<T> collection) {

		Set<T> result = new HashSet<>();

		Set<T> encounteredMoreOften = getElementsMoreThanOnceInCollection(collection);

		result.addAll(collection);
		result.removeAll(encounteredMoreOften);

		return result;
	}

	public static <T> Set<T> getElementsMoreThanOnceInCollection(Collection<T> collection) {

		Set<T> encounteredOnce = new HashSet<>();
		Set<T> encounteredMoreOften = new HashSet<>();

		for (T elem : collection) {
			if (encounteredOnce.contains(elem)) {
				encounteredMoreOften.add(elem);
			} else {
				encounteredOnce.add(elem);
			}
		}

		return encounteredMoreOften;
	}

	/**
	 * Takes in a Map<T, Integer> and sorts the entries of the map into a list,
	 * ordered by the value increasingly
	 * So e.g. a Map<String, Integer> containing {"test", 3}, {"foo", 1}, {"bar", 5} is sorted
	 * into a List<Pair<String, Integer>> containing [{"foo", 1}, {"test", 3}, {"bar", 5}].
	 */
	public static <T> List<Pair<T, Integer>> sortMapByValues(Map<T, Integer> map) {

		List<Pair<T, Integer>> newList = new ArrayList<>();

		if (map == null) {
			return newList;
		}

		for (Map.Entry<T, Integer> entry : map.entrySet()) {
			T key = entry.getKey();
			Integer value = entry.getValue();
			newList.add(new Pair<T, Integer>(key, value));
		}

		Collections.sort(newList, new Comparator<Pair<T, Integer>>() {

			public int compare(Pair<T, Integer> a, Pair<T, Integer> b) {
				if (a.getValue() == null) {
					if (b.getValue() == null) {
						return 0;
					} else {
						return 1;
					}
				} else {
					if (b.getValue() == null) {
						return -1;
					}
				}
				return a.getValue() - b.getValue();
			}
		});

		return newList;
	}
}
