/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.ai.nonsenseNet.numerical.combinators;

import com.asofterspace.toolbox.ai.nonsenseNet.numerical.Combinator;
import com.asofterspace.toolbox.ai.nonsenseNet.numerical.Node;

import java.util.List;


public class TakeNth extends Combinator {

	private int numberToTake = 0;


	public TakeNth(int which) {
		this.numberToTake = which;
	}

	@Override
	public int gatherInputFrom(List<Node> inputs) {

		if (inputs.size() < 1) {
			return 0;
		}

		if (numberToTake >= inputs.size()) {
			return 0;
		}

		return inputs.get(numberToTake).getValue();
	}

}
