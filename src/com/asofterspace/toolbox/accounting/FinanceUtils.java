/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.accounting;


/**
 * Utility functions related to finances
 */
public class FinanceUtils {

	public static Integer calcPostTax(Integer amount, Integer taxationPercent) {
		if ((amount == null) || (taxationPercent == null)) {
			return null;
		}
		return (int) Math.round((amount * (100 + taxationPercent)) / 100.0);
	}

	public static Integer calcPreTax(Integer postTaxAmount, Integer taxationPercent) {
		if ((postTaxAmount == null) || (taxationPercent == null)) {
			return null;
		}
		return (int) Math.round((postTaxAmount * 100.00) / (100 + taxationPercent));
	}

	public static String formatMoney(Integer amount) {

		if (amount == null) {
			return "N/A";
		}

		// let the main part of the function work only on positive values, and just add the minus sign
		// in the very end again
		boolean isNegative = amount < 0;
		if (isNegative) {
			amount = - amount;
		}

		String result = "" + amount;

		// 1 to 001
		while (result.length() < 3) {
			result = "0" + result;
		}

		// 001 to 0.01
		result = result.substring(0, result.length() - 2) + "." + result.substring(result.length() - 2);

		// 2739.80 to 2,739.80
		if (result.length() > 6) {
			result = result.substring(0, result.length() - 6) + "," + result.substring(result.length() - 6);
		}
		// 2739,800.00 to 2,739,800.00
		if (result.length() > 10) {
			result = result.substring(0, result.length() - 10) + "," + result.substring(result.length() - 10);
		}

		if (isNegative) {
			result = "- " + result;
		}

		return result;
	}

	public static String formatMoney(Integer amount, Currency currency) {

		if (amount == null) {
			return formatMoney(amount);
		}

		// 0.01 to 0.01 EUR
		return formatMoney(amount) + " " + currency;
	}

}
