/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.utils;

import java.util.ArrayList;
import java.util.List;


public class SearchUtils {

	public static <T extends Searchable> List<T> filterList(List<T> entries, String searchfor) {

		List<T> foundEntries = new ArrayList<>();

		if ((searchfor != null) && (!"".equals(searchfor))) {
			searchfor = searchfor.trim();
			List<String> searchfors = new ArrayList<>();
			if (searchfor.startsWith("\"") && searchfor.endsWith("\"")) {
				// "foo bar"   searches for foo[SPACE]bar exactly
				if (searchfor.length() > 1) {
					searchfor = searchfor.substring(1, searchfor.length() - 1);
				}
				searchfors.add(searchfor);
			} else {
				// foo bar   searches for entries containing foo and bar
				String[] searchforArr = searchfor.split(" ");
				for (String cur : searchforArr) {
					searchfors.add(cur);
				}
			}
			foundEntries = new ArrayList<>();
			outerLoop:
			for (T entry : entries) {
				for (String cur : searchfors) {
					if (!entry.contains(cur)) {
						continue outerLoop;
					}
				}
				foundEntries.add(entry);
			}
		} else {
			foundEntries.addAll(entries);
		}

		return foundEntries;
	}

}
