/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.test.Test;
import com.asofterspace.toolbox.test.TestUtils;
import com.asofterspace.toolbox.utils.SortUtils;

import java.util.ArrayList;
import java.util.List;


public class SortUtilsTest implements Test {

	@Override
	public void runAll() {

		shuffleTest();

		reverseTest();

		sortNumericallyTest();
	}

	private void shuffleTest() {

		TestUtils.start("Shuffle");

		List<String> list = new ArrayList<>();
		list.add("foo");
		list.add("bar");
		list.add("zoink");
		list.add("ploink");
		list.add("woink");

		int maxNum = 100;

		while (maxNum > 0) {
			maxNum--;

			List<String> resList = SortUtils.shuffle(list);

			if (!resList.get(0).equals(list.get(0)) ||
				!resList.get(1).equals(list.get(1)) ||
				!resList.get(2).equals(list.get(2)) ||
				!resList.get(3).equals(list.get(3)) ||
				!resList.get(4).equals(list.get(4))) {
				TestUtils.succeed();
				return;
			}
		}

		TestUtils.fail("After 100 runs, SortUtils.shuffle(list) never produced a result different from list!");
	}

	private void reverseTest() {

		TestUtils.start("Reverse");

		List<String> list = new ArrayList<>();
		list.add("foo");
		list.add("bar");
		list.add("zoink");

		List<String> resList = SortUtils.reverse(list);

		if (!list.get(0).equals("foo")) {
			TestUtils.fail("SortUtils.reverse(list) changed the contents of the list we passed in!");
		}

		int i = 0;

		if (!resList.get(i).equals("zoink")) {
			TestUtils.fail("Result #" + i + " is wrong!");
		}
		i++;

		if (!resList.get(i).equals("bar")) {
			TestUtils.fail("Result #" + i + " is wrong!");
		}
		i++;

		if (!resList.get(i).equals("foo")) {
			TestUtils.fail("Result #" + i + " is wrong!");
		}
		i++;

		TestUtils.succeed();
	}

	private void sortNumericallyTest() {

		TestUtils.start("Sort Numerically");

		List<String> list = new ArrayList<>();
		list.add("FOOBAR-1");
		list.add("FOOBAR-11");
		list.add("FOOBAR-2");
		list.add("FOOBAR-47");
		list.add("FOOBAR-5");
		list.add("FOOBAR-16");
		list.add("anloInk");
		list.add("ASLUAR");
		list.add("Xentan");
		list.add("FOOBAR-124");
		list.add("FOOBAR-3");
		list.add("xoro");

		List<String> resList = SortUtils.sortNumerically(list);

		if (!list.get(0).equals("FOOBAR-1")) {
			TestUtils.fail("SortUtils.sortNumerically(list) changed the contents of the list we passed in!");
		}

		int i = 0;

		if (!resList.get(i).equals("anloInk")) {
			TestUtils.fail("Result #" + i + " is wrong!");
		}
		i++;

		if (!resList.get(i).equals("ASLUAR")) {
			TestUtils.fail("Result #" + i + " is wrong!");
		}
		i++;

		if (!resList.get(i).equals("FOOBAR-1")) {
			TestUtils.fail("Result #" + i + " is wrong!");
		}
		i++;

		if (!resList.get(i).equals("FOOBAR-2")) {
			TestUtils.fail("Result #" + i + " is wrong!");
		}
		i++;

		if (!resList.get(i).equals("FOOBAR-3")) {
			TestUtils.fail("Result #" + i + " is wrong!");
		}
		i++;

		if (!resList.get(i).equals("FOOBAR-5")) {
			TestUtils.fail("Result #" + i + " is wrong!");
		}
		i++;

		if (!resList.get(i).equals("FOOBAR-11")) {
			TestUtils.fail("Result #" + i + " is wrong!");
		}
		i++;

		if (!resList.get(i).equals("FOOBAR-16")) {
			TestUtils.fail("Result #" + i + " is wrong!");
		}
		i++;

		if (!resList.get(i).equals("FOOBAR-47")) {
			TestUtils.fail("Result #" + i + " is wrong!");
		}
		i++;

		if (!resList.get(i).equals("FOOBAR-124")) {
			TestUtils.fail("Result #" + i + " is wrong!");
		}
		i++;

		if (!resList.get(i).equals("Xentan")) {
			TestUtils.fail("Result #" + i + " is wrong!");
		}
		i++;

		if (!resList.get(i).equals("xoro")) {
			TestUtils.fail("Result #" + i + " is wrong!");
		}
		i++;

		TestUtils.succeed();
	}

}
