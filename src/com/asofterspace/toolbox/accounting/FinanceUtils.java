/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.accounting;

import com.asofterspace.toolbox.utils.StrUtils;


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

	/**
	 * Takes in:        Gives out:
	 *      1                 100
	 *      2,5               250
	 *      2.50€             250
	 *   1,002.50€         100250
	 *   1.002,50 EUR      100250
	 *   1.002.50USD       100250
	 */
	public static Integer parseMoney(String amountStr) {

		if (amountStr == null) {
			return null;
		}

		try {
			amountStr = amountStr.replaceAll(" ", "");
			amountStr = amountStr.replaceAll("\t", "");
			amountStr = amountStr.replaceAll("\r", "");
			amountStr = amountStr.replaceAll("\n", "");
			amountStr = amountStr.replaceAll("EUR", "");
			amountStr = amountStr.replaceAll("USD", "");
			amountStr = amountStr.replaceAll("€", "");

			amountStr = StrUtils.prepareForParsing(amountStr);

			// now we have:
			//      1                1
			//      2,5              2.5
			//      2.50€            2.50
			//   1,002.50€        1002.50
			//   1.002,50 EUR     1002.50
			//   1.002.50USD     1.002.50
			// so all commas should be gone, all currency texts should be gone
			// however, if there were several dots, then there still are several

			// we now want to ensure that there are exactly two digits behind the dot,
			// and then drop all dots!
			if (!amountStr.contains(".")) {
				amountStr = amountStr += ".00";
			}

			// we are now sure that there is a dot somewhere in there, but the offset
			// from the end might be wrong...
			int offset = amountStr.length() - amountStr.lastIndexOf(".");
			// shorten, so 2.500 to 2.50
			if (offset > 3) {
				amountStr = amountStr.substring(0, amountStr.length() - (offset - 3));
			}
			// elongate, so 2.5 to 2.50
			if (offset == 1) {
				amountStr += "00";
			}
			if (offset == 2) {
				amountStr += "0";
			}

			// now we have:
			//      1                1.00
			//      2,5              2.50
			//      2.50€            2.50
			//   1,002.50€        1002.50
			//   1.002,50 EUR     1002.50
			//   1.002.50USD     1.002.50
			// so drop all the dots...
			amountStr = amountStr.replaceAll("\\.", "");

			// aaaand finally interpret as integer :D
			return Integer.valueOf(amountStr);

		} catch (NumberFormatException e) {
			// return null and let the caller decide how to handle that
		}

		return null;
	}

	public static Integer parseMoneyInWholeDigits(String amountStr) {

		Integer result = parseMoney(amountStr);

		if (result != null) {
			result = result / 100;
		}

		return result;
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
