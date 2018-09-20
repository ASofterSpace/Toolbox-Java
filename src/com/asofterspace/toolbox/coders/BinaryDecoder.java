package com.asofterspace.toolbox.coders;

/**
 * A class that can dencode numbers from their binary representations
 *
 * @author Moya (a softer space, 2017)
 */
public class BinaryDecoder {

    /**
     * Decodes a (whole) number from a string containing itself represented in binary, ignoring all
     * characters in the input string except 1 and 0, and returning its result as a string
     * @param binaryStr  A string containing a representation of the (whole) number in binary
     * @return The integer which was encoded in binary in the string, represented within a string
     */
    public static String decodeFromBinaryIntoStr(String binaryStr) {
        return "" + decodeFromBinary(binaryStr);
    }

    /**
     * Decodes a (whole) number from a string containing itself represented in binary, ignoring all
     * characters in the input string except 1 and 0
     * @param binaryStr  A string containing a representation of the (whole) number in binary
     * @return The integer which was encoded in binary in the string
     */
    public static int decodeFromBinary(String binaryStr) {

        int result = 0;
        int currentTwoPower = 1;

        for (int i = binaryStr.length() - 1; i >= 0; i--) {

            // ignore every character in the input that is not 1 or 0
            switch (binaryStr.charAt(i)) {
                case '1':
                    result += currentTwoPower;
                    // we are so cool that we actually want to fall through to the next staatement;
                    // therefore, NO BREAK HERE
                case '0':
                    currentTwoPower *= 2;
            }
        }

        return result;
    }

}
