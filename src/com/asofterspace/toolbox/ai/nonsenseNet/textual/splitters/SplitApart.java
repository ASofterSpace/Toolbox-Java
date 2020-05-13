/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.ai.nonsenseNet.textual.splitters;

import com.asofterspace.toolbox.ai.nonsenseNet.textual.Splitter;


public class SplitApart extends Splitter {

	@Override
	public String[] distribute(String value) {

		return value.split("");
	}

}
