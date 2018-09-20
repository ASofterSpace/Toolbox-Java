package com.asofterspace.toolbox.coders;

import java.math.BigInteger;

/**
 * A class that can encode numbers into their binary representations
 *
 * @author Moya (a softer space, 2017)
 */
public class BinaryEncoder {

    /**
     * Takes a number (written inside a string) and returns the number again as a string, this time
     * containing the number represented in binary
     * @param number  The number to be encoded in binary
     * @return A string representation of the number in binary
     */
    public static String encodeIntoBinaryString(String number) {
        return encodeIntoBinaryString(number, null);
    }

    /**
     * Takes a number (written inside a string) and returns the number again as a string, this time
     * containing the number represented in binary
     * @param number  The number to be encoded in binary
     * @param digits  The amount of digits with which the number is supposed to be given; null and 0
     *                both have a special meaning (as the least possible amount will be used)
     * @return A string representation of the number in binary
     */
    public static String encodeIntoBinaryString(String number, Integer digits) {

        try {

            long actualNumber = Long.valueOf(number);
            return encodeIntoBinaryString(actualNumber, digits);

        } catch (NumberFormatException e) {

            return "N/A";
        }
    }

    /**
     * Takes a number and returns the number again as a string containing the number represented in
     * binary
     * @param number  The number to be encoded in binary
     * @return A string representation of the number in binary
     */
    public static String encodeIntoBinaryString(long number) {
        return encodeIntoBinaryString(number, null);
    }

    /**
     * Takes a number and returns the number again as a string containing the number represented in
     * binary
     * @param number  The number to be encoded in binary
     * @param digits  The amount of digits with which the number is supposed to be given; null and 0
     *                both have a special meaning (as the least possible amount will be used)
     * @return A string representation of the number in binary
     */
    public static String encodeIntoBinaryString(long number, Integer digits) {

        if (number < 0) {
            return "Sorry, but representing numbers smaller than zero in binary is not uniquely " +
                    "possible (very simply speaking, you can agree on an amount of digits that " +
                    "a binary number should have and then append a 1 in the front to mean minus, " +
                    "but in this app we do not specify any particular amount of digits.)";
        }

        StringBuilder result = new StringBuilder();

        Long curly = 1L;

        if ((digits == null) || (digits == 0)) {
            while (curly < number) {
                curly *= 2;
            }
        } else {
            for (int i = 0; i < digits; i++) {
                curly *= 2;
            }
        }

        while (curly > 0) {
            result.append(number >= curly ? 1 : 0);

            if (number >= curly) {
                number = number - curly;
            }

            curly /= 2;
        }

        String resultStr = result.toString();

        if ((digits == null) || (digits == 0)) {
            while (resultStr.startsWith("0")) {
                resultStr = resultStr.substring(1);
            }
            if ("".equals(resultStr)) {
                resultStr = "0";
            }
        }

        return resultStr;
    }
}
