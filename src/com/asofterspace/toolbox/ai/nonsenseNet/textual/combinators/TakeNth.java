/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.ai.nonsenseNet.textual.combinators;

import com.asofterspace.toolbox.ai.nonsenseNet.textual.Combinator;
import com.asofterspace.toolbox.ai.nonsenseNet.textual.Node;

import java.util.List;


public class TakeNth extends Combinator {

	private int numberToTake = 0;


	public TakeNth(int which) {
		this.numberToTake = which;
	}

	@Override
	public String gatherInputFrom(List<Node> inputs) {

		if (inputs.size() < 1) {
			return "";
		}

		if (numberToTake >= inputs.size()) {
			return "";
		}

		return inputs.get(numberToTake).getValue();
	}

}
