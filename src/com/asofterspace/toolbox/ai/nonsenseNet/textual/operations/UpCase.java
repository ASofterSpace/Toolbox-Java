/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.ai.nonsenseNet.textual.combinators;

import com.asofterspace.toolbox.ai.nonsenseNet.textual.Operation;


public class UpCase extends Operation {

	@Override
	public String applyTo(String input) {

		return input.toUpperCase();
	}

}
