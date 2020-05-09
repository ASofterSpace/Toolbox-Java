/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.accounting;


public enum Currency {

	EUR,
	USD,
	GBP,
	CHF,
	SEK;


	public static Currency fromString(String str) {

		if (str == null) {
			return null;
		}

		str = str.trim().toUpperCase();

		switch (str) {
			case "EUR":
				return EUR;
			case "USD":
				return USD;
		}

		return null;
	}
}
