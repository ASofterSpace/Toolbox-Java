/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.accounting;


public enum Currency {

	E,
	EUR,
	D,
	USD,
	GBP,
	CHF,
	SEK,
	DKK,
	NOK,
	CAD,
	PLN,
	TRY,
	CNY,
	JPY;


	public static Currency fromString(String str) {

		if (str == null) {
			return null;
		}

		str = str.trim().toUpperCase();

		switch (str) {
			case "€":
				return E;
			case "EUR":
				return EUR;
			case "$":
				return D;
			case "USD":
				return USD;
			case "GBP":
				return GBP;
			case "CHF":
				return CHF;
			case "SEK":
				return SEK;
			case "DKK":
				return DKK;
			case "NOK":
				return NOK;
			case "CAD":
				return CAD;
			case "PLN":
				return PLN;
			case "TRY":
				return TRY;
			case "CNY":
				return CNY;
			case "JPY":
				return JPY;
		}

		return null;
	}

	@Override
	public String toString() {
		switch (this) {
			case E:
				return "€";
			case D:
				return "$";
		}
		return this.name();
	}
}
