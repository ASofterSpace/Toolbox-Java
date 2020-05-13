/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.ai.nonsenseNet.textual;

import com.asofterspace.toolbox.ai.nonsenseNet.textual.Splitter;


public class ReturnToAll extends Splitter {

	@Override
	public String[] distribute(String value) {

		String[] result = new String[1];

		result[0] = value;

		return result;
	}

}
