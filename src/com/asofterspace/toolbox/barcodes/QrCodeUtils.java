/**
 * Unlicensed code created by A Softer Space, 2019
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.barcodes;

import com.asofterspace.toolbox.utils.BitUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;


/**
 * All kinds of lookup tables and functions
 * that are needed for QR code handling
 *
 * @author Moya (a softer space), 2019
 */
public class QrCodeUtils {

	// convert between numbers and alphas (exponents of 2 in the GF256 according to the QR standard)
	private static int[] NUM_TO_ALPHA = null;
	private static int[] ALPHA_TO_NUM = null;


	private static void init() {

		if (NUM_TO_ALPHA != null) {
			return;
		}

		NUM_TO_ALPHA = new int[256];
		ALPHA_TO_NUM = new int[256];

		int prev = 1;
		NUM_TO_ALPHA[prev] = 0;
		ALPHA_TO_NUM[0] = prev;

		for (int i = 1; i < 256; i++) {
			prev = prev * 2;

			if (prev > 255) {
				prev = prev ^ 285;
			}

			NUM_TO_ALPHA[prev] = i;
			ALPHA_TO_NUM[i] = prev;
		}

		// we have a^0 = 1 AND a^255 = 1, but it is advantageous to insist on 1 = a^0, not 255 = a^0
		NUM_TO_ALPHA[1] = 0;
	}

	/**
	 * add two alphas, so a^alphaOne + a^alphaTwo, in the Galois field 256
	 * public for testing purposes
	 */
	public static int addAlpha(int alphaOne, int alphaTwo) {

		init();

		// we XOR the numerical representations of the alphas...
		int result = ALPHA_TO_NUM[alphaOne] ^ ALPHA_TO_NUM[alphaTwo];

		// ... and convert back to alphas
		return NUM_TO_ALPHA[result];
	}

	/**
	 * multiply two alphas, so a^alphaOne * a^alphaTwo, in the Galois field 256
	 */
	public static int multiplyAlpha(int alphaOne, int alphaTwo) {

		init();

		int alphaSum = alphaOne + alphaTwo;

		return (alphaSum % 256) + (alphaSum / 256);
	}

	/**
	 * gets the generator polynomial, expressed in alphas
	 * public for testing
	 */
	public static int[] getGeneratorPolynomialAlphas(int ecAmount) {

		int length = ecAmount - 1;

		int[] result = new int[length + 2];

		// we multiply (a^0 x + a^i) for increasing i from 0 to ecAmount
		// so e.g. for i = 2 we have (a^0 x + a^0) (a^0 x + a^1) (a^0 x + a^2)

		// therefore, we use recursion, ending at (a^0 x + a^0)...

		if (ecAmount < 2) {
			result[0] = 0;
			result[1] = 0;
			return result;
		}

		// ... and just generating the very last step here, in which we now have e.g.
		//   (a^k x^2 + a^m x + a^n) (a^0 x + a^ecAmount)
		// = a^k a^0 x^3 + (a^k a^ecAmount + a^m a^0) x^2 + (a^m a^ecAmount + a^n a^0) x + a^n a^ecAmount
		// = a^k x^3 + (a^k a^ecAmount + a^m) x^2 + (a^m a^ecAmount + a^n) x + a^n a^ecAmount
		int[] smallerPolynomial = getGeneratorPolynomialAlphas(ecAmount - 1);

		result[0] = smallerPolynomial[0];
		for (int i = 1; i < length + 1; i++) {
			result[i] = addAlpha(multiplyAlpha(smallerPolynomial[i - 1], length), smallerPolynomial[i]);
		}
		result[length + 1] = multiplyAlpha(smallerPolynomial[length], length);

		return result;
	}

	public static int[] polynomialToNum(int[] polynomial) {

		init();

		for (int i = 0; i < polynomial.length; i++) {
			polynomial[i] = ALPHA_TO_NUM[polynomial[i]];
		}

		return polynomial;
	}

	public static int[] polynomialToAlpha(int[] polynomial) {

		init();

		for (int i = 0; i < polynomial.length; i++) {
			polynomial[i] = NUM_TO_ALPHA[polynomial[i]];
		}

		return polynomial;
	}

	public static int[] getErrorCorrectionCodewords(int[] messagePolynomial, int version, QrCodeQualityLevel edcLevel) {

		init();

		int ecAmount = getEcCodewordsPerBlock(version, edcLevel);

		int[] result = new int[ecAmount];

		// we get the generator polynomial in alpha notation
		int[] generatorPolynomial = getGeneratorPolynomialAlphas(ecAmount);

		// we transfer the generator polynomial to numerical notation
		generatorPolynomial = polynomialToNum(generatorPolynomial);

		// multiply message polynomial by x^n, which basically just means making the array n wider,
		// where n is the ecAmount
		int[] messagePolyWider = new int[messagePolynomial.length + ecAmount];
		System.arraycopy(messagePolynomial, 0, messagePolyWider, 0, messagePolynomial.length);

		// and some space to work within...
		int[] curGenPolyWider = new int[messagePolyWider.length];

		// multiply generator poly by lead term of message poly
		for (int curpos = 0; curpos < messagePolynomial.length; curpos++) {
			int leadTerm = messagePolyWider[curpos];
			leadTerm = NUM_TO_ALPHA[leadTerm];
			for (int j = 0; j < messagePolyWider.length; j++) {
				int i = j - curpos;
				if ((i > 0) && (i < generatorPolynomial.length) && (generatorPolynomial[i] > 0)) {
					curGenPolyWider[j] = ALPHA_TO_NUM[multiplyAlpha(leadTerm, NUM_TO_ALPHA[generatorPolynomial[i]])];
				} else {
					curGenPolyWider[j] = 0;
				}
				// XOR generatorPolyWider with the message polynomial
				messagePolyWider[j] = curGenPolyWider[j] ^ messagePolyWider[j];
			}
		}

		// assign resulting coefficients as result
		System.arraycopy(messagePolyWider, messagePolyWider.length - ecAmount, result, 0, ecAmount);

		return result;
	}

	public static int getVersionBasedOnDataStreamLength(boolean[] datastream, QrCodeQualityLevel edcLevel) {

		int length = datastream.length;

		int version = 1;

		while (getDataLength(version, edcLevel) < length) {
			version++;

			if (version > 40) {
				return 40;
			}
		}

		return version;
	}

	public static int widthToVersion(int width) {

		// version 1 is 21x21, version 2 is 25x25, version 3 is 29x29, ...
		return (width - 17) / 4;
	}

	public static int versionToWidth(int version) {

		// version 1 is 21x21, version 2 is 25x25, version 3 is 29x29, ...
		return 17 + (4 * version);
	}

	/**
	 * gets the length (in bits) of the data field - of course the data can be truncated
	 * already earlier by a 0 nibble (followed by padding), but this is the maximum amount
	 * of data that will be in a QR code of this error correction level and version
	 */
	public static int getDataLength(int version, QrCodeQualityLevel edcLevel) {

		// TODO :: add the values for further versions!

		switch (edcLevel) {
			case LOW_QUALITY:
				switch (version) {
					case 1: return 8*19;
					case 2: return 8*34;
					case 3: return 8*55;
					case 4: return 8*80;
					case 5: return 8*108;
					case 6: return 8*136;
				}
				break;

			case MEDIUM_QUALITY:
				switch (version) {
					case 1: return 8*16;
					case 2: return 8*28;
					case 3: return 8*44;
					case 4: return 8*64;
					case 5: return 8*86;
					case 6: return 8*108;
				}
				break;

			case QUALITY:
				switch (version) {
					case 1: return 8*13;
					case 2: return 8*22;
					case 3: return 8*34;
					case 4: return 8*48;
					case 5: return 8*62;
					case 6: return 8*76;
				}
				break;

			case HIGH_QUALITY:
				switch (version) {
					case 1: return 8*9;
					case 2: return 8*16;
					case 3: return 8*26;
					case 4: return 8*36;
					case 5: return 8*46;
					case 6: return 8*60;
				}
				break;
		}

		return 0;
	}

	public static int getEcCodewordsPerBlock(int version, QrCodeQualityLevel edcLevel) {

		// TODO :: add the EC codewords per block for further versions!

		// TODO :: add a function to get the amount of blocks, as needed in higher versions!
		// (which then need to be interleaved etc., oh yea! - so maybe make an extra getInterleave function or somesuch)

		switch (edcLevel) {
			case LOW_QUALITY:
				switch (version) {
					case 1: return 7;
					case 2: return 10;
					case 3: return 15;
					case 4: return 20;
					case 5: return 26;
					case 6: return 18; // 2 blocks!
				}
				break;

			case MEDIUM_QUALITY:
				switch (version) {
					case 1: return 10;
					case 2: return 16;
					case 3: return 26;
					case 4: return 18; // 2 blocks!
					case 5: return 24; // 2 blocks!
					case 6: return 16; // 4 blocks!
				}
				break;

			case QUALITY:
				switch (version) {
					case 1: return 13;
					case 2: return 22;
					case 3: return 18; // 2 blocks!
					case 4: return 26; // 2 blocks!
					case 5: return 18; // 2 blocks twice!
					case 6: return 24; // 4 blocks!
				}
				break;

			case HIGH_QUALITY:
				switch (version) {
					case 1: return 17;
					case 2: return 28;
					case 3: return 22; // 2 blocks!
					case 4: return 16; // 4 blocks!
					case 5: return 22; // 2 blocks twice!
					case 6: return 28; // 4 blocks!
				}
				break;
		}

		return 0;
	}

	public static boolean[] encodeData(String data, int version) {

		boolean[] result;
		int cur = 0;

		byte[] byteData = data.getBytes(StandardCharsets.UTF_8);
		byte[] byteDataAscii = data.getBytes(StandardCharsets.US_ASCII);

		int blockLen;
		if (version > 9) {
			blockLen = 16;
		} else {
			blockLen = 8;
		}

		// we are just using ASCII...
		if (Arrays.equals(byteData, byteDataAscii)) {

			// 4 bit mode indicator +
			// 8 or 16 bit length indicator +
			// 8 bits per string data +
			// 4 bit end of data marker -> we always add this for simplicity, although we could leave it out under certain circumstances
			result = new boolean[4 + blockLen + (8 * byteData.length) + 4];

		} else {

			// 8 bit eci indicator +
			// 4 bit mode indicator +
			// 8 or 16 bit length indicator +
			// 8 bits per string data +
			// 4 bit end of data marker -> we always add this for simplicity, although we could leave it out under certain circumstances
			result = new boolean[8 + 4 + blockLen + (8 * byteData.length) + 4];

			// encode in UTF8
			int eciNumber = 26;

			boolean[] eciNumberArr = BitUtils.intToBits(eciNumber);
			result[0] = eciNumberArr[24];
			result[1] = eciNumberArr[25];
			result[2] = eciNumberArr[26];
			result[3] = eciNumberArr[27];
			result[4] = eciNumberArr[28];
			result[5] = eciNumberArr[29];
			result[6] = eciNumberArr[30];
			result[7] = eciNumberArr[31];
			cur += 8;
		}

		boolean[] stringModeArr = BitUtils.intToBits(4);
		result[cur+0] = stringModeArr[28];
		result[cur+1] = stringModeArr[29];
		result[cur+2] = stringModeArr[30];
		result[cur+3] = stringModeArr[31];
		cur += 4;

		boolean[] strDataLengthArr = BitUtils.intToBits(byteData.length);
		if (version > 9) {
			result[cur+0] = strDataLengthArr[16];
			result[cur+1] = strDataLengthArr[17];
			result[cur+2] = strDataLengthArr[18];
			result[cur+3] = strDataLengthArr[19];
			result[cur+4] = strDataLengthArr[20];
			result[cur+5] = strDataLengthArr[21];
			result[cur+6] = strDataLengthArr[22];
			result[cur+7] = strDataLengthArr[23];
			result[cur+8] = strDataLengthArr[24];
			result[cur+9] = strDataLengthArr[25];
			result[cur+10] = strDataLengthArr[26];
			result[cur+11] = strDataLengthArr[27];
			result[cur+12] = strDataLengthArr[28];
			result[cur+13] = strDataLengthArr[29];
			result[cur+14] = strDataLengthArr[30];
			result[cur+15] = strDataLengthArr[31];
			cur += 16;
		} else {
			result[cur+0] = strDataLengthArr[24];
			result[cur+1] = strDataLengthArr[25];
			result[cur+2] = strDataLengthArr[26];
			result[cur+3] = strDataLengthArr[27];
			result[cur+4] = strDataLengthArr[28];
			result[cur+5] = strDataLengthArr[29];
			result[cur+6] = strDataLengthArr[30];
			result[cur+7] = strDataLengthArr[31];
			cur += 8;
		}

		for (byte curbyte : byteData) {
			boolean[] curbits = BitUtils.intToBits(curbyte);
			result[cur+0] = curbits[24];
			result[cur+1] = curbits[25];
			result[cur+2] = curbits[26];
			result[cur+3] = curbits[27];
			result[cur+4] = curbits[28];
			result[cur+5] = curbits[29];
			result[cur+6] = curbits[30];
			result[cur+7] = curbits[31];
			cur += 8;
		}

		return result;
	}

	public static String decodeData(boolean[] datastream, int version, QrCodeQualityLevel edcLevel) {

		StringBuilder result = new StringBuilder();

		// make our life a bit simpler by not having to write such a long name
		boolean[] ds = datastream;

		// now read the encoded data blocks...
		int cur = 0;

		// by default (if no ECI is given) use ASCII...
		int eciNumber = 27;

		int dataLength = getDataLength(version, edcLevel);

		while (cur < dataLength) {
			// ... with each data block starting with a 4 bit mode indicator
			byte modeIndicator = BitUtils.bitsToNibble(ds[cur], ds[cur+1], ds[cur+2], ds[cur+3]);
			cur += 4;

			switch (modeIndicator) {

				// end of message
				case 0:
					return result.toString();

				// numeric
				case 1:
					// TODO
					break;

				// alphanumeric
				case 2:
					// TODO
					break;

				// ascii bytes (unless the eci number was fiddled with)
				case 4:

					// the length of the field length depends on the version, of course .-.
					int blockLength;

					if (version > 9) {
						blockLength = BitUtils.bitsToInt(ds, cur, 16);
						cur += 16;
					} else {
						blockLength = BitUtils.bitsToInt(ds, cur, 8);
						cur += 8;
					}

					switch (eciNumber) {
						case 1:
						case 3:
							// ISO8859-1
							result.append(new String(BitUtils.bitsToByteArr(ds, cur, 8*blockLength), StandardCharsets.ISO_8859_1));
							cur += 8*blockLength;
							break;
						case 25:
							// UTF-16BE
							result.append(new String(BitUtils.bitsToByteArr(ds, cur, 16*blockLength), StandardCharsets.UTF_16BE));
							cur += 16*blockLength;
							break;
						case 26:
							// UTF-8
							result.append(new String(BitUtils.bitsToByteArr(ds, cur, 8*blockLength), StandardCharsets.UTF_8));
							cur += 8*blockLength;
							break;
						case 27:
						case 170:
							// ASCII
							result.append(new String(BitUtils.bitsToByteArr(ds, cur, 8*blockLength), StandardCharsets.US_ASCII));
							cur += 8*blockLength;
							break;
						// TODO :: add others
						default:
							// as default, if we got nothing useful, just add as is and hope and pray...
							for (int n = 0; n < blockLength; n++) {
								result.append((char) BitUtils.bitsToInt(ds, cur, 8));
								cur += 8;
							}
					}

					break;

				// ECI - extended channel interpretation
				case 7:

					if (ds[cur] == false) {
						eciNumber = BitUtils.bitsToInt(ds, cur, 8);
						cur += 8;
					} else {
						if (ds[cur+1] == false) {
							eciNumber = BitUtils.bitsToInt(ds, cur, 16);
							cur += 16;
						} else {
							eciNumber = BitUtils.bitsToInt(ds, cur, 24);
							cur += 24;
						}
					}

					break;

				// kanji
				case 8:
					// TODO
					break;
			}
		}

		return result.toString();
	}

}
