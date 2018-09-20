package com.asofterspace.toolbox.coders;

/**
 * A class that can encode numbers into Roman numerals
 *
 * @author Moya (a softer space, 2017)
 */
public class RomanNumeralEncoder {

    /**
     * Encodes numbers into Roman numerals
     *
     * @param numbers  A string full of numbers (in digits, not letters ^^)
     * @return A string containing the Roman numerals corresponding to the input
     */
    public static String encodeNumbersIntoRomanNumerals(String numbers) {

        StringBuilder result = new StringBuilder();

        try {
            Integer input = Integer.valueOf(numbers);

            if (input == 0) {
                return "Sorry - Romans did not yet have the number 0.";
            } else if (input < 0) {
                return "Sorry - Romans did not yet have negative numbers.";
            }

            int ones = input % 10;
            input = input / 10;
            int tens = input % 10;
            input = input / 10;
            int hundreds = input % 10;
            input = input / 10;
            int thousands = input % 10;
            input = input / 10;
            int tenthousands = input % 10;
            input = input / 10;
            int hundredthousands = input % 10;
            input = input / 10;
            int millions = input % 10;
            input = input / 10;
            int tenmillions = input % 10;
            input = input / 10;

            // there really, really, really is nothing useful above |M| (just imagine the stroke
            // above it for now), so let's simply fill it up with M...
            for (int i = 0; i < input; i++) {
                result.append("|M\u0305|");
            }
            result.append(intToRoman(tenmillions, "|C\u0305|", "|D\u0305|", "|M\u0305|"));
            result.append(intToRoman(millions, "|X\u0305|", "|L\u0305|", "|C\u0305|"));
            result.append(intToRoman(hundredthousands, "|I\u0305|", "|V\u0305|", "|X\u0305|"));
            result.append(intToRoman(tenthousands, "X\u0305", "L\u0305", "|I\u0305|"));
            result.append(intToRoman(thousands, "M", "V\u0305", "X\u0305"));
            result.append(intToRoman(hundreds, "C", "D", "M"));
            result.append(intToRoman(tens, "X", "L", "C"));
            result.append(intToRoman(ones, "I", "V", "X"));

            // if we added more than 3 M as hundred millions (so if we are at four million or above),
            // display a message about this being QUITE high...
            if (input > 3) {
                result.append("\nThis is getting ridiculous... Romans only really went up to " +
                    "MMM (3000) consistently, but even when using the funky |M\u0305| numbers, " +
                    "going above |M\u0305M\u0305M\u0305| (300000000) is madness!");
            }

            String resultStr = result.toString();

            // we want to get rid of adjacent pipes, as they add nothing new to the equation...
            resultStr = resultStr.replace("||", "");

            return resultStr;

        } catch (NumberFormatException e) {

            return "N/A";
        }
    }

    private static String intToRoman(int amount, String oneStr, String fiveStr, String tenStr) {

        switch (amount) {
            case 1:
                return oneStr;
            case 2:
                return oneStr+oneStr;
            case 3:
                return oneStr+oneStr+oneStr;
            case 4:
                return oneStr+fiveStr;
            case 5:
                return fiveStr;
            case 6:
                return fiveStr+oneStr;
            case 7:
                return fiveStr+oneStr+oneStr;
            case 8:
                return fiveStr+oneStr+oneStr+oneStr;
            case 9:
                return oneStr+tenStr;
        }

        return "";
    }
}
