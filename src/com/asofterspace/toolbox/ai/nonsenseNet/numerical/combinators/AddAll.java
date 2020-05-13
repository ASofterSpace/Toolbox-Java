/**
 * Unlicensed code created by A Softer Space, 2020
 * www.asofterspace.com/licenses/unlicense.txt
 */
package com.asofterspace.toolbox.ai.nonsenseNet.numerical.combinators;

import com.asofterspace.toolbox.ai.nonsenseNet.numerical.Combinator;
import com.asofterspace.toolbox.ai.nonsenseNet.numerical.Node;

import java.util.List;


public class AddAll extends Combinator {

	@Override
	public int gatherInputFrom(List<Node> inputs) {

		if (inputs.size() < 1) {
			return 0;
		}

		int result = 0;
		for (Node curInput : inputs) {
			if (curInput != null) {
				result += curInput.getValue();
			}
		}
		return result;
	}

}
