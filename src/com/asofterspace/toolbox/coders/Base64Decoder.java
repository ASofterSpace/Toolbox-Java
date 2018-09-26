package com.asofterspace.toolbox.coders;

/**
 * A class that can decode text from Base64
 *
 * @author Moya (a softer space, 2017)
 */
public class Base64Decoder {

	public final static char[] CHAR_TO_HEX_DIGIT = new char[] {
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * Decodes a base64 encoded text back into regular text (and as there are a million base64
     * standards out there: we are trying to achieve compatibility with both the Original Base64 for
     * PEM and the current Base64 for MIME)
     * @param base64Text  A base64 encoded text
     * @return The text that has been encoded by the input
     */
    public static String decodeFromBase64(String base64Text) {
	
		return decodeFromBase64(base64Text, Base64Encoder.NUM_TO_BASE64_CHAR);
	}
	
    /**
     * Decodes a base64 encoded text back into regular text (and as there are a million base64
     * standards out there: we are trying to achieve compatibility with both the Original Base64 for
     * PEM and the current Base64 for MIME)
     * @param base64Text  A base64 encoded text
	 * @param base64Chars  The base64 characters to be used
     * @return The text that has been encoded by the input
     */
    public static String decodeFromBase64(String base64Text, char[] base64Chars) {
	
        char[] numbers = new char[base64Text.length()];
        int charPos = 0;

        // for each character in the input...
        for (char thisChar : base64Text.toCharArray()) {
            // ... check if it is one of the known characters, and if so, add it to the numbers
            // array (and ignore it otherwise, just like the specs ask us to do)
            for (int i = 0; i < base64Chars.length; i++) {
                if (base64Chars[i] == thisChar) {
                    numbers[charPos] = (char) i;
                    charPos++;
                    break;
                }
            }
        }
		
        StringBuilder result = new StringBuilder();

        int offset = 0;
        int buffer = 0;

        // the numbers are arranged in such a way:
        // numbers: 00 00 00 11 11 11 22 22 22 33 33 33 | base64 numbers 0, 1, 2 and 3
        // result:  aa aa aa aa bb bb bb bb cc cc cc cc | characters a, b and c in ASCII

        for (int numPos = 0; numPos < charPos; numPos++) {
            switch (offset) {
                case 0:
                    buffer = numbers[numPos];
                    offset = 2;
                    break;

                case 2:
                    char thisChar = numbers[numPos];
                    int shiftedBuffer = buffer << 2;
                    shiftedBuffer = shiftedBuffer & 0xFC; // 11111100b;
                    result.append((char) (shiftedBuffer | (thisChar >>> 4)));
                    buffer = thisChar;
                    offset = 4;
                    break;

                case 4:
                    thisChar = numbers[numPos];
                    shiftedBuffer = buffer << 4;
                    shiftedBuffer = shiftedBuffer & 0xF0; // 11110000b;
                    result.append((char) (shiftedBuffer | (thisChar >>> 2)));
                    buffer = thisChar;
                    offset = 6;
                    break;

                case 6:
                    thisChar = numbers[numPos];
                    shiftedBuffer = buffer << 6;
                    shiftedBuffer = shiftedBuffer & 0xC0; // 11000000b;
                    result.append((char) (shiftedBuffer | thisChar));
                    offset = 0;
                    break;
            }
        }

        // add the last character
        switch (offset) {
            case 2:
                int shiftedBuffer = buffer << 2;
                shiftedBuffer = shiftedBuffer & 0xFC; // 11111100b;
                result.append((char) shiftedBuffer);
                break;

            case 4:
                shiftedBuffer = buffer << 4;
                shiftedBuffer = shiftedBuffer & 0xF0; // 11110000b;
                result.append((char) shiftedBuffer);
                break;

            case 6:
                shiftedBuffer = buffer << 6;
                shiftedBuffer = shiftedBuffer & 0xC0; // 11000000b;
                result.append((char) shiftedBuffer);
                break;
        }
		
		if (base64Text.endsWith("=")) {
			result.setLength(result.length() - 1);
		}

        return result.toString();
    }

    /**
     * Decodes a base64 encoded text into a hex string representation of it (and as there are a million base64
     * standards out there: we are trying to achieve compatibility with both the Original Base64 for
     * PEM and the current Base64 for MIME)
     * @param base64Text  A base64 encoded text
     * @return The text that has been encoded by the input
     */
    public static String decodeFromBase64ToHexStr(String base64Text) {
	
		return decodeFromBase64ToHexStr(base64Text, Base64Encoder.NUM_TO_BASE64_CHAR);
	}
	
    /**
     * Decodes a base64 encoded text into a hex string representation of it (and as there are a million base64
     * standards out there: we are trying to achieve compatibility with both the Original Base64 for
     * PEM and the current Base64 for MIME)
     * @param base64Text  A base64 encoded text
	 * @param base64Chars  The base64 characters to be used
     * @return The text that has been encoded by the input
     */
    public static String decodeFromBase64ToHexStr(String base64Text, char[] base64Chars) {
	
		return decodeFromBase64ToHexStr(base64Text, base64Chars, CHAR_TO_HEX_DIGIT);
	}

    /**
     * Decodes a base64 encoded text into a hex string representation of it (and as there are a million base64
     * standards out there: we are trying to achieve compatibility with both the Original Base64 for
     * PEM and the current Base64 for MIME)
     * @param base64Text  A base64 encoded text
	 * @param base64Chars  The base64 characters to be used
	 * @param hexChars  The hex characters to be used
     * @return The text that has been encoded by the input
     */
    public static String decodeFromBase64ToHexStr(String base64Text, char[] base64Chars, char[] hexChars) {
	
        char[] numbers = new char[base64Text.length()];
        int charPos = 0;

        // for each character in the input...
        for (char thisChar : base64Text.toCharArray()) {
            // ... check if it is one of the known characters, and if so, add it to the numbers
            // array (and ignore it otherwise, just like the specs ask us to do)
            for (int i = 0; i < base64Chars.length; i++) {
                if (base64Chars[i] == thisChar) {
                    numbers[charPos] = (char) i;
                    charPos++;
                    break;
                }
            }
        }
		
		// TODO :: until here, this is exactly the same as in the other decoder function - maybe put the part above into its own function?
		// (but then again, an array AND an int would need to be returned, making everything harder... args!)
		
        StringBuilder result = new StringBuilder();

        int offset = 0;
        int buffer = 0;

        // the numbers are arranged in such a way:
        // numbers: 00 00 00 11 11 11 22 22 22 ... | base64 numbers 0, 1
        // result:  aa aa bb bb cc cc dd dd ee ... | hex characters a, b and c

        for (int numPos = 0; numPos < charPos; numPos++) {
            switch (offset) {
                case 0:
                    char thisChar = numbers[numPos];
                    result.append(CHAR_TO_HEX_DIGIT[(char) (thisChar >>> 2)]);
                    buffer = thisChar & 0x03; // 00000011b;
                    offset = 2;
                    break;

                case 2:
                    thisChar = numbers[numPos];
                    int shiftedBuffer = buffer << 2;
                    result.append(CHAR_TO_HEX_DIGIT[(char) (shiftedBuffer | (thisChar >>> 4))]);
					thisChar = (char) (thisChar & 0x0F); // 00001111b;
                    result.append(CHAR_TO_HEX_DIGIT[thisChar]);
                    offset = 0;
                    break;
            }
        }

        // add the last character
        switch (offset) {
            case 2:
                int shiftedBuffer = buffer << 2;
                shiftedBuffer = shiftedBuffer & 0xFC; // 11111100b;
                result.append((char) shiftedBuffer);
                break;
        }
		
		if (base64Text.endsWith("=")) {
			result.setLength(result.length() - 1);
		}

        return result.toString();
    }

}