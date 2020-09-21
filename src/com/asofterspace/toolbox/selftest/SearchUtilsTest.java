/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.selftest;

import com.asofterspace.toolbox.test.Test;
import com.asofterspace.toolbox.test.TestUtils;
import com.asofterspace.toolbox.utils.Searchable;
import com.asofterspace.toolbox.utils.SearchUtils;

import java.util.ArrayList;
import java.util.List;


public class SearchUtilsTest implements Test {

	@Override
	public void runAll() {

		simpleSearchTest();

		severalSearchTest();

		exactSearchTest();
	}

	class Entry implements Searchable {

		private int id;
		private String text;

		public Entry(int id, String text) {
			this.id = id;
			this.text = text;
		}

		public int getId() {
			return id;
		}

		@Override
		public boolean contains(String searchfor) {
			return text.contains(searchfor);
		}
	}

	private String getIds(List<Entry> list) {
		String result = "";
		for (Entry e : list) {
			result += e.getId();
		}
		return result;
	}

	public void simpleSearchTest() {

		TestUtils.start("Simple Search");

		List<Entry> entries = new ArrayList<>();
		entries.add(new Entry(1, "foo bar blubb"));
		entries.add(new Entry(2, "foo foo"));
		entries.add(new Entry(3, "blubbblubbb blubbelubb"));
		entries.add(new Entry(4, "bar foo"));

		List<Entry> filtered = SearchUtils.filterList(entries, "foo");
		if (!"124".equals(getIds(filtered))) {
			TestUtils.fail("We searched for \"foo\" and did not get 124, but instead " + getIds(filtered) + "!");
			return;
		}

		filtered = SearchUtils.filterList(entries, "bar");
		if (!"14".equals(getIds(filtered))) {
			TestUtils.fail("We searched for \"bar\" and did not get 14, but instead " + getIds(filtered) + "!");
			return;
		}

		TestUtils.succeed();
	}

	public void severalSearchTest() {

		TestUtils.start("Several Search");

		List<Entry> entries = new ArrayList<>();
		entries.add(new Entry(1, "foo bar blubb"));
		entries.add(new Entry(2, "foo foo"));
		entries.add(new Entry(3, "blubbblubbb blubbelubb"));
		entries.add(new Entry(4, "bar foo"));

		List<Entry> filtered = SearchUtils.filterList(entries, "foo bar");
		if (!"14".equals(getIds(filtered))) {
			TestUtils.fail("We searched for \"foo bar\" and did not get 14, but instead " + getIds(filtered) + "!");
			return;
		}

		TestUtils.succeed();
	}

	public void exactSearchTest() {

		TestUtils.start("Exact Search");

		List<Entry> entries = new ArrayList<>();
		entries.add(new Entry(1, "foo bar blubb"));
		entries.add(new Entry(2, "foo foo"));
		entries.add(new Entry(3, "blubbblubbb blubbelubb"));
		entries.add(new Entry(4, "bar foo"));

		List<Entry> filtered = SearchUtils.filterList(entries, "\"foo bar\"");
		if (!"1".equals(getIds(filtered))) {
			TestUtils.fail("We searched for \"foo bar\" and did not get 1, but instead " + getIds(filtered) + "!");
			return;
		}

		TestUtils.succeed();
	}
}
