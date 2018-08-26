package com.asofterspace.toolbox;

import java.util.List;

public class Utils {

	public final static int TOOLBOX_VERSION_NUMBER = 1;

	public static String strListToString(List<String> stringList) {

		StringBuilder sb = new StringBuilder();
		
		if (stringList != null) {
			for (String jsonStr : stringList) {
				sb.append(jsonStr);
			}
		}

		return sb.toString();
	}
}
