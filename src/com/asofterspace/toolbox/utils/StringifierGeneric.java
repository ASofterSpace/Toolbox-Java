/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.utils;

public class StringifierGeneric<T> implements Stringifier<T> {

	public String getString(T orig) {
		if (orig == null) {
			return "";
		}
		String res = orig.toString();
		if (res == null) {
			return "";
		}
		return res;
	}
}
