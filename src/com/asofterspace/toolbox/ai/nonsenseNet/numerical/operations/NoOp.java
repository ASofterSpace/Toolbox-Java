/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.ai.nonsenseNet.numerical.combinators;

import com.asofterspace.toolbox.ai.nonsenseNet.numerical.Operation;


public class NoOp extends Operation {

	@Override
	public int applyTo(int input) {
		return input;
	}
}
