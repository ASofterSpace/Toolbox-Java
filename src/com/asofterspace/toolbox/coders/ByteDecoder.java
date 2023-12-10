/**
 * Unlicensed code created by A Softer Space, 2023
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.coders;

import com.asofterspace.toolbox.utils.StrUtils;

import java.util.List;


/**
 * A class that can decode byte arrays from their string representations;
 * so input such as:
 * "[9, 12, -45, 144]"
 * or:
 * 9;12;-45;144
 * gets decoded to byte[]
 *
 * @author Moya (a softer space, 2017)
 */
public class ByteDecoder {

	public static byte[] decode(String inputStr) {

		inputStr = StrUtils.replaceAll(inputStr, " ", "");
		inputStr = StrUtils.replaceAll(inputStr, "\t", "");
		inputStr = StrUtils.replaceAll(inputStr, "\r", "");
		inputStr = StrUtils.replaceAll(inputStr, "\n", "");
		inputStr = StrUtils.replaceAll(inputStr, "[", "");
		inputStr = StrUtils.replaceAll(inputStr, "]", "");

		inputStr = StrUtils.replaceAll(inputStr, ";", ",");

		List<String> byteList = StrUtils.split(inputStr, ",");
		byte[] byteContent = new byte[byteList.size()];
		for (int i = 0; i < byteList.size(); i++) {
			byteContent[i] = StrUtils.strToByte(byteList.get(i));
		}
		return byteContent;
	}

}
