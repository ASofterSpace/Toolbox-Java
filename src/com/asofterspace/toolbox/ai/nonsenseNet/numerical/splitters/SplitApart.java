/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.ai.nonsenseNet.numerical.splitters;

import com.asofterspace.toolbox.ai.nonsenseNet.numerical.Splitter;


public class SplitApart extends Splitter {

	@Override
	public int[] distribute(int value) {

		// TODO :: instead actually split the result, e.g. by outputting
		// fixed size chunks that add up to the value

		int[] result = new int[1];

		result[0] = value;

		return result;
	}

}
