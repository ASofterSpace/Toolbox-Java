package com.asofterspace.toolbox;

import java.util.List;

public class Utils {

	public static String strListToString(List<String> stringList) {

		StringBuilder sb = new StringBuilder();
		
		for (String jsonStr : stringList) {
			sb.append(jsonStr);
		}
		
		return sb.toString();
	}
}
